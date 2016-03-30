package morxander.sexualharassmentreporter.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import morxander.sexualharassmentreporter.R;
import morxander.sexualharassmentreporter.items.City;
import morxander.sexualharassmentreporter.providers.MainProvider;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CityLoader extends AsyncTaskLoader<List<City>> {

    public CityLoader(Context context) {
        super(context);
    }

    @Override
    public List<City> loadInBackground() {
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
            String url = getContext().getString(R.string.api_base_url) + getContext().getString(R.string.api_city_list);
            url = url + "?" + getContext().getString(R.string.api_user_id) + "=" + user_id;
            url = url + "&" + getContext().getString(R.string.api_token) + "=" + api_token;
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            ArrayList<City> cityArrayList = new ArrayList<City>();
            Response response = client.newCall(request).execute();
            JSONObject json = new JSONObject(response.body().string());
            if (json.getBoolean(getContext().getString(R.string.api_response_status))) {
                JSONArray requests_list = json.getJSONArray(getContext().getString(R.string.api_response_city_list));
                if (requests_list.length() != 0) {
                    for (int i = 0; i < requests_list.length(); i++) {
                        JSONObject cityObject = requests_list.getJSONObject(i);
                        City city = new City();
                        city.setId(cityObject.getInt(getContext().getString(R.string.api_response_id)));
                        city.setName(cityObject.getString(getContext().getString(R.string.api_response_city_name)));
                        cityArrayList.add(city);
                    }
                }
            }
            return cityArrayList;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


}
