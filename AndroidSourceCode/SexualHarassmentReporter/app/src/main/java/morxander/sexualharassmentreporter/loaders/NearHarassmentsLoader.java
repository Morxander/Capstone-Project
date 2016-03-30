package morxander.sexualharassmentreporter.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import morxander.sexualharassmentreporter.R;
import morxander.sexualharassmentreporter.fragments.NearHarassmentsActivityFragment;
import morxander.sexualharassmentreporter.items.Harassment;
import morxander.sexualharassmentreporter.providers.MainProvider;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by morxander on 3/30/16.
 */
public class NearHarassmentsLoader extends AsyncTaskLoader<List<Harassment>> {

    public NearHarassmentsLoader(Context context) {
        super(context);
    }

    @Override
    public List<Harassment> loadInBackground() {
        try {
            // Get User Data
            Uri user_uri = Uri.parse(MainProvider.USER_URL);
            Cursor cursor = getContext().getContentResolver().query(user_uri, null, null, null, null);
            cursor.moveToFirst();
            int id_position = cursor.getColumnIndex("user_id");
            int token_position = cursor.getColumnIndex("api_token");
            int user_id = cursor.getInt(id_position);
            String api_token = cursor.getString(token_position);
            OkHttpClient client = new OkHttpClient();
            String url = getContext().getString(R.string.api_base_url) + "nearby_harassments";
            url = url + "?" + getContext().getString(R.string.api_user_id) + "=" + user_id;
            url = url + "&" + getContext().getString(R.string.api_token) + "=" + api_token;
            url = url + "&" + "lat" + "=" + NearHarassmentsActivityFragment.latitude;
            url = url + "&" + "lon" + "=" + NearHarassmentsActivityFragment.longitude;
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            ArrayList<Harassment> harassmentArrayList = new ArrayList<Harassment>();
            Response response = client.newCall(request).execute();
            JSONObject json = new JSONObject(response.body().string());
            Log.v("Loader", "response url " + url);
            Log.v("Loader", "response code " + response.code());
            if (json.getBoolean(getContext().getString(R.string.api_response_status))) {
                JSONArray requests_list = json.getJSONArray("harassments_list");
                Log.v("Loader", "response requests_list size " + requests_list.length());
                if (requests_list.length() != 0) {
                    for (int i = 0; i < requests_list.length(); i++) {
                        JSONObject harassmentObject = requests_list.getJSONObject(i);
                        Harassment harassment = new Harassment();
                        harassment.setId(harassmentObject.getInt(getContext().getString(R.string.api_response_id)));
                        harassment.setTitle(harassmentObject.getString("title"));
                        harassment.setBody(harassmentObject.getString("body"));
                        harassment.setLat(harassmentObject.getDouble("lat"));
                        harassment.setLon(harassmentObject.getDouble("lon"));
                        LatLng latLng = new LatLng(harassment.getLat(),harassment.getLon());
                        Log.v("Loader", "response title " + harassmentObject.getString("title"));
                        harassmentArrayList.add(harassment);
                    }
                }
            }
            return harassmentArrayList;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
