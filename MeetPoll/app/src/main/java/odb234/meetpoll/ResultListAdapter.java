package odb234.meetpoll;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Otis on 7/24/2016.
 */
public class ResultListAdapter extends BaseAdapter {
    String[] names;
    String[] addresses;
    String[] ids;
    float[] ratings;

    LayoutInflater inflater;
    public ResultListAdapter(Context context, String[] name, String[] add, String[] id, float[] r){
        names = name;
        addresses = add;
        ids = id;
        ratings = r;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View currentView = view;
        if(currentView == null){
            currentView = inflater.inflate(R.layout.results_cell_view, viewGroup, false);
        }
        TextView textView = (TextView)currentView.findViewById(R.id.results_list_name);
        textView.setText(names[i]);
        textView = (TextView)currentView.findViewById(R.id.result_list_address);
        textView.setText(addresses[i]);
        RatingBar rating = (RatingBar)currentView.findViewById(R.id.result_list_rating);
        rating.setRating(ratings[i]);
        return currentView;
    }
}
