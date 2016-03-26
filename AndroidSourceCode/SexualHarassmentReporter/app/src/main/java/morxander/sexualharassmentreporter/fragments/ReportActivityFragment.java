package morxander.sexualharassmentreporter.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import morxander.sexualharassmentreporter.R;
import morxander.sexualharassmentreporter.db.UserModel;
import morxander.sexualharassmentreporter.items.City;
import morxander.sexualharassmentreporter.utilities.ViewsUtility;

public class ReportActivityFragment extends Fragment implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {


    private GoogleMap m_map;
    private GoogleApiClient google_api_client;
    private LocationRequest location_request;
    private double latitude, longitude;
    private EditText txt_title, txt_body;
    private TextView txt_view_title;
    private Spinner city_list;
    private ArrayList<String> cityArrayList;
    private ArrayList<Integer> cityIdsArrayList;
    private TextInputLayout layout_title, layout_body;
    private Button bt_report;
    private UserModel userModel;
    private ProgressDialog loading_dialog;
    private AsyncHttpClient client;
    private RequestParams params;
    private MapFragment map;
    private boolean location_enabled = false;
    private boolean map_ready = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_report, container, false);
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
        initView(rootView);
        setOnClick();
        return rootView;
    }

    // This function will init the views and the necessary vars and objects
    private void initView(View rootView) {
        txt_title = (EditText) rootView.findViewById(R.id.input_title);
        txt_body = (EditText) rootView.findViewById(R.id.input_body);
        txt_view_title = (TextView) rootView.findViewById(R.id.txt_title);
        bt_report = (Button) rootView.findViewById(R.id.btn_report);
        layout_title = (TextInputLayout) rootView.findViewById(R.id.input_layout_title);
        layout_body = (TextInputLayout) rootView.findViewById(R.id.input_layout_body);
        city_list = (Spinner) rootView.findViewById(R.id.cities_spinner);
        cityArrayList = new ArrayList<String>();
        cityIdsArrayList = new ArrayList<Integer>();
        loading_dialog = new ProgressDialog(getActivity());
        loading_dialog.setMessage(getString(R.string.loading));
        ViewsUtility.changeTypeFace(getActivity(), txt_title);
        ViewsUtility.changeTypeFace(getActivity(), txt_body);
        ViewsUtility.changeTypeFace(getActivity(), bt_report);
        ViewsUtility.changeTypeFace(getActivity(), txt_view_title);
        ViewsUtility.changeTypeFace(getActivity(), layout_title);
        ViewsUtility.changeTypeFace(getActivity(), layout_body);
        userModel = UserModel.getCurrentUser();
        getCities(rootView);
        google_api_client = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        map = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
    }

    // This function is to make sure that the inputs are valid
    private boolean validateInput(EditText txt, TextInputLayout layout) {
        if (txt.getText().toString().trim().isEmpty()) {
            layout.setError(getString(R.string.required));
            ViewsUtility.requestFocus(getActivity(), txt);
            return false;
        } else {
            layout.setErrorEnabled(false);
        }
        return true;
    }

    // This function is to set the onclick listeners
    private void setOnClick() {
        bt_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInput(txt_title, layout_title) && validateInput(txt_body, layout_body)) {
                    if (latitude > 0 || longitude > 0) {
                        loading_dialog.show();
                        client = new AsyncHttpClient();
                        params = new RequestParams();
                        String url = getString(R.string.api_base_url) + getString(R.string.api_report_harassments_url);
                        params.add(getString(R.string.api_token), userModel.getApi_token());
                        params.add(getString(R.string.api_user_id), String.valueOf(userModel.getUser_id()));
                        params.add("lat", String.valueOf(latitude));
                        params.add("lon", String.valueOf(longitude));
                        params.add("title", txt_title.getText().toString());
                        params.add("body", txt_body.getText().toString());
                        int city_id = cityIdsArrayList.get(city_list.getSelectedItemPosition());
                        params.add("city_id", String.valueOf(city_id));
                        client.post(getActivity(), url, params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                String str = null;
                                try {
                                    str = new String(responseBody, "UTF-8");
                                    JSONObject json = new JSONObject(str);
                                    if (json.getBoolean(getString(R.string.api_response_status))) {
                                        Toast.makeText(getActivity(), R.string.reported, Toast.LENGTH_LONG).show();
                                        txt_title.setText("");
                                        txt_body.setText("");
                                    } else {
                                        Toast.makeText(getActivity(), R.string.wrong_token, Toast.LENGTH_LONG).show();
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                loading_dialog.dismiss();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                loading_dialog.dismiss();
                                Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), R.string.location_required, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    // This function will get the cities list and fill it into the spinner
    private void getCities(final View rootView) {
        loading_dialog.show();
        client = new AsyncHttpClient();
        String url = getString(R.string.api_base_url) + getString(R.string.api_city_list);
        url = url + "?" + getString(R.string.api_user_id) + "=" + userModel.getUser_id();
        url = url + "&" + getString(R.string.api_token) + "=" + userModel.getApi_token();
        client.get(getActivity(), url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                loading_dialog.dismiss();
                try {
                    String str = new String(responseBody, "UTF-8");
                    JSONObject json = new JSONObject(str);
                    if (json.getBoolean(getString(R.string.api_response_status))) {
                        JSONArray requests_list = json.getJSONArray(getString(R.string.api_response_city_list));
                        if (requests_list.length() != 0) {
                            for (int i = 0; i < requests_list.length(); i++) {
                                JSONObject cityObject = requests_list.getJSONObject(i);
                                City city = new City();
                                city.setId(cityObject.getInt(getString(R.string.api_response_id)));
                                city.setName(cityObject.getString(getString(R.string.api_response_city_name)));
                                cityArrayList.add(city.getName());
                                cityIdsArrayList.add(city.getId());
                            }
                        }
                        String[] cities = cityArrayList.toArray(new String[cityArrayList.size()]);
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, cities);
                        city_list.setAdapter(spinnerArrayAdapter);
                        spinnerArrayAdapter.notifyDataSetChanged();
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
                loading_dialog.dismiss();
                getFragmentManager().popBackStack();
                Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_LONG).show();
            }
        });
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
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        m_map.clear();
        m_map.addMarker(new MarkerOptions().position(latLng));
        m_map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        m_map.animateCamera(CameraUpdateFactory.zoomTo(15));
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
    public void onMapReady(GoogleMap googleMap) {
        map_ready = true;
        m_map = googleMap;
        LatLng cairo_latlo = new LatLng(30.0500, 31.2333);
        CameraPosition position = CameraPosition.builder().target(cairo_latlo).zoom(13).build();
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(position));
    }
}
