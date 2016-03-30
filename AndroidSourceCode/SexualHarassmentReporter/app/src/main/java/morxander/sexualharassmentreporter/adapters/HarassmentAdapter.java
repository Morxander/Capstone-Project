package morxander.sexualharassmentreporter.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import morxander.sexualharassmentreporter.R;
import morxander.sexualharassmentreporter.activities.HarassmentActivity;
import morxander.sexualharassmentreporter.fragments.HarassmentsListActivityFragment;
import morxander.sexualharassmentreporter.items.Harassment;
import morxander.sexualharassmentreporter.utilities.ViewsUtility;

/**
 * Created by morxander on 3/20/16.
 */
public class HarassmentAdapter extends BaseAdapter {
    private Context mContext;
    private HarassmentsListActivityFragment harassmentsListActivityFragment;
    private LayoutInflater inflater;
    private List<Harassment> harassmentList = null;
    private ArrayList<Harassment> harassmentArrayList;

    public HarassmentAdapter(Context c, HarassmentsListActivityFragment harassmentsListActivityFragment, List<Harassment> harassmentList) {
        mContext = c;
        this.harassmentsListActivityFragment = harassmentsListActivityFragment;
        inflater = LayoutInflater.from(c);
        this.harassmentList = harassmentList;
        harassmentArrayList = new ArrayList<Harassment>();
        harassmentArrayList.addAll(harassmentList);
    }

    @Override
    public int getCount() {
        return harassmentArrayList.size();
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
        TextView firstLetter, harassmetTitle;
        LinearLayout harassmentRow;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.harassment_row, null);
            holder.firstLetter = (TextView) convertView.findViewById(R.id.harassment_first_char);
            holder.harassmetTitle = (TextView) convertView.findViewById(R.id.harassment_title);
            holder.harassmentRow = (LinearLayout)convertView.findViewById(R.id.harassment_row);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // Set the results into TextViews
        holder.firstLetter.setText(harassmentArrayList.get(position).getTitle().toUpperCase().substring(0,1));
        holder.harassmetTitle.setText(harassmentArrayList.get(position).getTitle());
        holder.harassmetTitle.setContentDescription(mContext.getString(R.string.harassment_title_desc) + harassmentArrayList.get(position).getTitle());
        // Set the type face
        ViewsUtility.changeTypeFace(mContext,holder.firstLetter);
        ViewsUtility.changeTypeFace(mContext,holder.harassmetTitle);
        // Set The On Click
        holder.harassmentRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, HarassmentActivity.class);
                intent.putExtra("title",harassmentArrayList.get(position).getTitle());
                intent.putExtra("time",harassmentArrayList.get(position).getTime());
                intent.putExtra("body",harassmentArrayList.get(position).getBody());
                intent.putExtra("lat",harassmentArrayList.get(position).getLat());
                intent.putExtra("lon",harassmentArrayList.get(position).getLon());
                intent.putExtra("city_id",harassmentArrayList.get(position).getCity_id());
                harassmentsListActivityFragment.startActivityForResult(intent,1);
            }
        });
        return convertView;
    }

}
