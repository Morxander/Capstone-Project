package morxander.sexualharassmentreporter.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import morxander.sexualharassmentreporter.R;
import morxander.sexualharassmentreporter.utilities.ViewsUtility;

public class HarassmentActivityFragment extends Fragment implements OnMapReadyCallback {

    private TextView txt_title,txt_time,txt_body;
    private Double lat,lon;
    private GoogleMap m_map;
    private MapFragment map;
    private int city_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_harassment, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        txt_title = (TextView)rootView.findViewById(R.id.txt_title);
        txt_time = (TextView)rootView.findViewById(R.id.txt_time);
        txt_body = (TextView)rootView.findViewById(R.id.txt_body);
        // Change Font
        ViewsUtility.changeTypeFace(getActivity(),txt_title);
        ViewsUtility.changeTypeFace(getActivity(),txt_time);
        ViewsUtility.changeTypeFace(getActivity(),txt_body);
        // Get the data
        city_id = getActivity().getIntent().getExtras().getInt("city_id");
        lat = getActivity().getIntent().getExtras().getDouble("lat");
        lon = getActivity().getIntent().getExtras().getDouble("lon");
        txt_title.setText(getActivity().getIntent().getExtras().getString("title"));
        txt_time.setText(getActivity().getIntent().getExtras().getString("time"));
        txt_body.setText(getActivity().getIntent().getExtras().getString("body"));
        map = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        m_map = googleMap;
        LatLng lat_lon = new LatLng(lat, lon);
        m_map.addMarker(new MarkerOptions().position(lat_lon));
        CameraPosition position = CameraPosition.builder().target(lat_lon).zoom(13).build();
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(position));
    }

}
