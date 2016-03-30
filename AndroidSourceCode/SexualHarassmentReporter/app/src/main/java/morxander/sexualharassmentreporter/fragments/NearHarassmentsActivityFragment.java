package morxander.sexualharassmentreporter.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import morxander.sexualharassmentreporter.R;
import morxander.sexualharassmentreporter.adapters.HarassmentAdapter;
import morxander.sexualharassmentreporter.items.Harassment;
import morxander.sexualharassmentreporter.loaders.HarassmentLoader;
import morxander.sexualharassmentreporter.loaders.NearHarassmentsLoader;
import morxander.sexualharassmentreporter.providers.MainProvider;
import morxander.sexualharassmentreporter.utilities.ViewsUtility;


public class NearHarassmentsActivityFragment extends Fragment implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LoaderManager.LoaderCallbacks<List<Harassment>> {

    private TextView txt_title,txt_status;
    private GoogleMap m_map;
    private MapFragment map;
    public static double latitude, longitude;
    private GoogleApiClient google_api_client;
    private LocationRequest location_request;
    private boolean location_enabled = false;
    private boolean fetched_data=false;
    private AsyncHttpClient client;
    private int user_id;
    private String api_token;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_near_harassments, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        txt_title = (TextView)rootView.findViewById(R.id.txt_title);
        txt_status = (TextView)rootView.findViewById(R.id.txt_status);
        ViewsUtility.changeTypeFace(getActivity(),txt_title);
        ViewsUtility.changeTypeFace(getActivity(),txt_status);
        // Get User Data
        Uri user_uri = Uri.parse(MainProvider.USER_URL);
        Cursor cursor = getActivity().getContentResolver().query(user_uri, null, null, null, null);
        cursor.moveToFirst();
        int id_position = cursor.getColumnIndex("user_id");
        int token_position = cursor.getColumnIndex("api_token");
        user_id = cursor.getInt(id_position);
        api_token = cursor.getString(token_position);
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location_enabled = true;
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle("Enable Location");
            alert.setMessage("Please enable location");
            alert.setInverseBackgroundForced(true);
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            alert.show();
        }
        google_api_client = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        map = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
        map.getMap().setMyLocationEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        m_map = googleMap;
        map.getMap().setMyLocationEnabled(true);
        m_map.getUiSettings().setMyLocationButtonEnabled(false);
        LatLng cairo_latlo = new LatLng(30.0500, 31.2333);
        CameraPosition position = CameraPosition.builder().target(cairo_latlo).zoom(13).build();
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(position));
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        if(!fetched_data){
            getLoaderManager().initLoader(1, null, NearHarassmentsActivityFragment.this).forceLoad();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (location_enabled) {
            google_api_client.connect();
        }
    }

    @Override
    public void onStop() {
        if (google_api_client != null) {
            if (google_api_client.isConnected()) {
                google_api_client.disconnect();
            }
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (google_api_client != null) {
            if (!google_api_client.isConnected()) {
                google_api_client.connect();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        location_request = LocationRequest.create();
        location_request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        location_request.setInterval(1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(google_api_client, location_request, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public Loader<List<Harassment>> onCreateLoader(int i, Bundle bundle) {
        return new NearHarassmentsLoader(getActivity().getBaseContext());
    }

    @Override
    public void onLoadFinished(Loader<List<Harassment>> loader, List<Harassment> harassmentList) {
        txt_status.setText("Data has been fetched");
        for(Harassment harassment : harassmentList){
            LatLng latLng = new LatLng(harassment.getLat(),harassment.getLon());
            m_map.addMarker(new MarkerOptions().position(latLng));
            if(harassmentList.indexOf(harassment) == 0){
                m_map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                m_map.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
        }
        fetched_data = true;
    }

    @Override
    public void onLoaderReset(Loader<List<Harassment>> loader) {

    }
}
