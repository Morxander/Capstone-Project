package morxander.sexualharassmentreporter.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import morxander.sexualharassmentreporter.R;
import morxander.sexualharassmentreporter.adapters.CityAdapter;
import morxander.sexualharassmentreporter.db.UserModel;
import morxander.sexualharassmentreporter.items.City;
import morxander.sexualharassmentreporter.utilities.ViewsUtility;


public class CityListFragment extends Fragment {

    private ListView listview_cities;
    public ArrayList<City> cityArrayList;
    private SwipeRefreshLayout swipe_refresh_layout;
    private LinearLayout emptyLayout;
    private ImageView img_timeline_empty;
    private AsyncHttpClient client;
    private UserModel userModel;
    private TextView emptyText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_city_list, container, false);
        initViews(rootView);
        getCities(rootView);
        return rootView;
    }

    // This function will init the views and the necessary vars and objects
    private void initViews(final View rootView) {
        listview_cities = (ListView) rootView.findViewById(R.id.listview_city);
        cityArrayList = new ArrayList<City>();
        swipe_refresh_layout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        swipe_refresh_layout.setVisibility(View.VISIBLE);
        emptyLayout = (LinearLayout) rootView.findViewById(R.id.empty);
        emptyText = (TextView) rootView.findViewById(R.id.empty_text);
        img_timeline_empty = (ImageView) rootView.findViewById(R.id.list_empty);
        userModel = UserModel.getCurrentUser();
        // Set the font
        ViewsUtility.changeTypeFace(getActivity(), emptyText);
        // Refreshing
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                emptyLayout.setVisibility(View.INVISIBLE);
                cityArrayList.clear();
                getCities(rootView);
            }
        });
        // Workaround to show the loading
        // Read more : http://stackoverflow.com/a/26860930/917222
        swipe_refresh_layout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        swipe_refresh_layout.setRefreshing(true);
    }

    // This function will get the cities list and fill it into the listview
    private void getCities(final View rootView) {
        swipe_refresh_layout.setRefreshing(true);
        client = new AsyncHttpClient();
        String url = getString(R.string.api_base_url) + getString(R.string.api_city_list);
        url = url + "?" + getString(R.string.api_user_id) + "=" + userModel.getUser_id();
        url = url + "&" + getString(R.string.api_token) + "=" + userModel.getApi_token();
        client.get(getActivity(), url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                swipe_refresh_layout.setRefreshing(false);
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
                                cityArrayList.add(city);
                            }
                        } else {
                            emptyLayout.setVisibility(View.VISIBLE);
                        }
                        CityAdapter adp = new CityAdapter(rootView.getContext(), CityListFragment.this, cityArrayList);
                        listview_cities.setAdapter(adp);
                        adp.notifyDataSetChanged();
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
                swipe_refresh_layout.setRefreshing(false);
                emptyLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (client != null) {
            client.cancelAllRequests(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (client != null) {
            client.cancelAllRequests(true);
        }
    }

}
