package morxander.sexualharassmentreporter.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
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

import cz.msebera.android.httpclient.Header;
import morxander.sexualharassmentreporter.R;
import morxander.sexualharassmentreporter.adapters.HarassmentAdapter;
import morxander.sexualharassmentreporter.db.UserModel;
import morxander.sexualharassmentreporter.items.Harassment;
import morxander.sexualharassmentreporter.utilities.ViewsUtility;


public class NearHarassmentsActivityFragment extends Fragment implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private TextView txt_title,txt_status;
    private GoogleMap m_map;
    private MapFragment map;
    private double latitude, longitude;
    private GoogleApiClient google_api_client;
    private LocationRequest location_request;
    private boolean location_enabled = false;
    private boolean fetched_data=false;
    private AsyncHttpClient client;
    private UserModel userModel;

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
        userModel = UserModel.getCurrentUser();
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
            getHarassments();
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

    // This function will get the harassments list and fill it into the listview
    private void getHarassments() {
        client = new AsyncHttpClient();
        txt_status.setText("Getting data ....");
        String url = getString(R.string.api_base_url) + "nearby_harassments";
        url = url + "?" + getString(R.string.api_user_id) + "=" + userModel.getUser_id();
        url = url + "&" + getString(R.string.api_token) + "=" + userModel.getApi_token();
        url = url + "&" + "lat" + "=" + latitude;
        url = url + "&" + "lon" + "=" + longitude;
        client.get(getActivity(), url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    txt_status.setText("Showing the data");
                    String str = new String(responseBody, "UTF-8");
                    JSONObject json = new JSONObject(str);
                    if (json.getBoolean(getString(R.string.api_response_status))) {
                        JSONArray requests_list = json.getJSONArray("harassments_list");
                        if (requests_list.length() != 0) {
                            for (int i = 0; i < requests_list.length(); i++) {
                                JSONObject harassmentObject = requests_list.getJSONObject(i);
                                Harassment harassment = new Harassment();
                                harassment.setId(harassmentObject.getInt(getString(R.string.api_response_id)));
                                harassment.setTitle(harassmentObject.getString("title"));
                                harassment.setBody(harassmentObject.getString("body"));
                                harassment.setLat(harassmentObject.getDouble("lat"));
                                harassment.setLon(harassmentObject.getDouble("lon"));
                                LatLng latLng = new LatLng(harassment.getLat(),harassment.getLon());
                                m_map.addMarker(new MarkerOptions().position(latLng));
                                if(i == requests_list.length() - 1){
                                    m_map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                    m_map.animateCamera(CameraUpdateFactory.zoomTo(15));
                                }
                            }
                            fetched_data = true;
                        }
                    } else {
                        // Wrong token
                        Toast.makeText(getActivity(), R.string.wrong_token, Toast.LENGTH_LONG).show();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.v("Error",String.valueOf(statusCode));
            }
        });
    }
}
