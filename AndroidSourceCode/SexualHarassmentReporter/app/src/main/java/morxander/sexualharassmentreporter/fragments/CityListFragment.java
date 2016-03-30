package morxander.sexualharassmentreporter.fragments;


import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
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

import com.loopj.android.http.AsyncHttpClient;

import java.util.ArrayList;
import java.util.List;

import morxander.sexualharassmentreporter.R;
import morxander.sexualharassmentreporter.adapters.CityAdapter;
import morxander.sexualharassmentreporter.items.City;
import morxander.sexualharassmentreporter.loaders.CityLoader;
import morxander.sexualharassmentreporter.providers.MainProvider;
import morxander.sexualharassmentreporter.utilities.ViewsUtility;


public class CityListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<City>> {

    private ListView listview_cities;
    public ArrayList<City> cityArrayList;
    private SwipeRefreshLayout swipe_refresh_layout;
    private LinearLayout emptyLayout;
    private ImageView img_timeline_empty;
    private AsyncHttpClient client;
    private int user_id;
    private String api_token;
    private TextView emptyText;
    private CityAdapter adp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_city_list, container, false);
        initViews(rootView);
//        getCities(rootView);
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
        // Set the font
        ViewsUtility.changeTypeFace(getActivity(), emptyText);
        // Refreshing
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                emptyLayout.setVisibility(View.INVISIBLE);
                cityArrayList.clear();
                getLoaderManager().initLoader(1, null, CityListFragment.this).forceLoad();
            }
        });
        // Get User Data
        Uri user_uri = Uri.parse(MainProvider.USER_URL);
        Cursor cursor = getActivity().getContentResolver().query(user_uri, null, null, null, null);
        cursor.moveToFirst();
        int id_position = cursor.getColumnIndex("user_id");
        int token_position = cursor.getColumnIndex("api_token");
        user_id = cursor.getInt(id_position);
        api_token = cursor.getString(token_position);
        // Workaround to show the loading
        // Read more : http://stackoverflow.com/a/26860930/917222
        swipe_refresh_layout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        swipe_refresh_layout.setRefreshing(true);
        getLoaderManager().initLoader(1, null, CityListFragment.this).forceLoad();

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


    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public Loader<List<City>> onCreateLoader(int i, Bundle bundle) {
        return new CityLoader(getActivity().getBaseContext());
    }

    @Override
    public void onLoadFinished(Loader<List<City>> loader, List<City> cities) {
        if(cities.size() > 0){
            swipe_refresh_layout.setRefreshing(false);
            cityArrayList.addAll(cities);
            adp = new CityAdapter(getActivity(), CityListFragment.this, cities);
            listview_cities.setAdapter(adp);
            adp.notifyDataSetChanged();
        }else{
            swipe_refresh_layout.setRefreshing(false);
            emptyLayout.setVisibility(View.VISIBLE);
        }
    }
}
