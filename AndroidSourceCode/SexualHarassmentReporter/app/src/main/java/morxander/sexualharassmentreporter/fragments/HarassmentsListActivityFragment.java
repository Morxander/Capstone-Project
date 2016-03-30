package morxander.sexualharassmentreporter.fragments;

import android.app.LoaderManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
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
import java.util.List;

import cz.msebera.android.httpclient.Header;
import morxander.sexualharassmentreporter.R;
import morxander.sexualharassmentreporter.adapters.HarassmentAdapter;
import morxander.sexualharassmentreporter.items.Harassment;
import morxander.sexualharassmentreporter.loaders.HarassmentLoader;
import morxander.sexualharassmentreporter.providers.MainProvider;
import morxander.sexualharassmentreporter.utilities.ViewsUtility;

public class HarassmentsListActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Harassment>>{

    private ListView listview_harassment;
    public ArrayList<Harassment> harassment_array_list;
    private SwipeRefreshLayout swipe_refresh_layout;
    private LinearLayout emptyLayout;
    private ImageView img_timeline_empty;
    private AsyncHttpClient client;
    private int user_id;
    private String api_token;
    private TextView emptyText;
    public static int city_id;
    private HarassmentAdapter adp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_harassments_list, container, false);
        initViews(rootView);
        return rootView;
    }

    private void initViews(final View rootView) {
        listview_harassment = (ListView) rootView.findViewById(R.id.listview_harassment);
        harassment_array_list = new ArrayList<Harassment>();
        swipe_refresh_layout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        swipe_refresh_layout.setVisibility(View.VISIBLE);
        emptyLayout = (LinearLayout) rootView.findViewById(R.id.empty);
        emptyText = (TextView) rootView.findViewById(R.id.empty_text);
        img_timeline_empty = (ImageView) rootView.findViewById(R.id.list_empty);
        // Get User Data
        Uri user_uri = Uri.parse(MainProvider.USER_URL);
        Cursor cursor = getActivity().getContentResolver().query(user_uri, null, null, null, null);
        cursor.moveToFirst();
        int id_position = cursor.getColumnIndex("user_id");
        int token_position = cursor.getColumnIndex("api_token");
        user_id = cursor.getInt(id_position);
        api_token = cursor.getString(token_position);
        // Set the font
        ViewsUtility.changeTypeFace(getActivity(), emptyText);
        // Refreshing
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                emptyLayout.setVisibility(View.INVISIBLE);
                harassment_array_list.clear();
                getHarassments(rootView);
            }
        });
        // Workaround to show the loading
        // Read more : http://stackoverflow.com/a/26860930/917222
        swipe_refresh_layout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        swipe_refresh_layout.setRefreshing(true);
        if(getActivity().getIntent().getExtras() != null){
            city_id = getActivity().getIntent().getExtras().getInt("city_id");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("city_id", city_id);
            editor.commit();
        }else{
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            city_id = sharedPref.getInt("city_id", 1);
        }
        getLoaderManager().initLoader(1, null, HarassmentsListActivityFragment.this).forceLoad();
    }

    // This function will get the harassments list and fill it into the listview
    private void getHarassments(final View rootView) {
        swipe_refresh_layout.setRefreshing(true);

        client = new AsyncHttpClient();
        String url = getString(R.string.api_base_url) + getString(R.string.api_harassment_list);
        url = url + "?" + getString(R.string.api_user_id) + "=" + user_id;
        url = url + "&" + getString(R.string.api_token) + "=" + api_token;
        url = url + "&" + "city_id" + "=" + city_id;
        client.get(getActivity(), url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                swipe_refresh_layout.setRefreshing(false);
                try {
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
                                harassment.setTime(harassmentObject.getString("created_at"));
                                harassment.setCity_id(harassmentObject.getInt("city_id"));
                                harassment_array_list.add(harassment);
                            }
                        } else {
                            emptyLayout.setVisibility(View.VISIBLE);
                            img_timeline_empty.setImageResource(R.drawable.safe_zone);
                            img_timeline_empty.setTag(R.drawable.safe_zone);
                            emptyText.setText(R.string.safe_zone);
                        }
                        adp = new HarassmentAdapter(rootView.getContext(), HarassmentsListActivityFragment.this, harassment_array_list);
                        listview_harassment.setAdapter(adp);
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
                Log.v("Error",String.valueOf(statusCode));
            }
        });
    }

    @Override
    public Loader<List<Harassment>> onCreateLoader(int i, Bundle bundle) {
        return new HarassmentLoader(getActivity().getBaseContext());
    }

    @Override
    public void onLoadFinished(Loader<List<Harassment>> loader, List<Harassment> harassmentList) {
        Log.v("Loader", "List Size " + harassmentList.size());
        if(harassmentList.size() > 0){
            swipe_refresh_layout.setRefreshing(false);
            harassment_array_list.addAll(harassmentList);
            adp = new HarassmentAdapter(getActivity(), HarassmentsListActivityFragment.this, harassment_array_list);
            listview_harassment.setAdapter(adp);
            adp.notifyDataSetChanged();
        }else{
            swipe_refresh_layout.setRefreshing(false);
            emptyLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Harassment>> loader) {

    }
}
