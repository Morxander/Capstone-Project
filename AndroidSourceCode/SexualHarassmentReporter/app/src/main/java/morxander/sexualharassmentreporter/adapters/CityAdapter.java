package morxander.sexualharassmentreporter.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import morxander.sexualharassmentreporter.R;
import morxander.sexualharassmentreporter.activities.HarassmentsListActivity;
import morxander.sexualharassmentreporter.fragments.CityListFragment;
import morxander.sexualharassmentreporter.items.City;
import morxander.sexualharassmentreporter.utilities.ViewsUtility;

/**
 * Created by morxander on 3/2/16.
 */
public class CityAdapter extends BaseAdapter {
    private Context mContext;
    private CityListFragment cityListFragment;
    private LayoutInflater inflater;
    private List<City> cityList = null;
    private ArrayList<City> cityArrayList;

    public CityAdapter(Context c, CityListFragment cityListFragment, List<City> cityList) {
        mContext = c;
        this.cityListFragment = cityListFragment;
        inflater = LayoutInflater.from(c);
        this.cityList = cityList;
        cityArrayList = new ArrayList<City>();
        cityArrayList.addAll(cityList);
    }

    @Override
    public int getCount() {
        return cityArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public class ViewHolder {
        TextView firstLetter,cityName;
        LinearLayout cityRow;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.city_row, null);
            holder.firstLetter = (TextView) convertView.findViewById(R.id.city_first_char);
            holder.cityName = (TextView) convertView.findViewById(R.id.city_name);
            holder.cityRow = (LinearLayout)convertView.findViewById(R.id.city_row);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // Set the results into TextViews
        holder.firstLetter.setText(cityArrayList.get(position).getName().toUpperCase().substring(0,1));
        holder.cityName.setText(cityArrayList.get(position).getName());
        holder.cityName.setContentDescription(mContext.getString(R.string.city) + cityArrayList.get(position).getName());
        // Set the type face
        ViewsUtility.changeTypeFace(mContext,holder.firstLetter);
        ViewsUtility.changeTypeFace(mContext,holder.cityName);
        // Set The On Click
        holder.cityRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, HarassmentsListActivity.class);
                intent.putExtra("city_id",cityArrayList.get(position).getId());
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }

}
