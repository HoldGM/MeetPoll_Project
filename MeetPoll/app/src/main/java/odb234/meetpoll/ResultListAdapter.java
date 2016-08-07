package odb234.meetpoll;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Otis on 7/24/2016.
 */
public class ResultListAdapter extends BaseAdapter {
    ArrayList<MapMarker> markers = new ArrayList<>();

    LayoutInflater inflater;
    public ResultListAdapter(Context context, ArrayList<MapMarker> list){
        markers = list;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return markers.size();
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
        ((TextView)currentView.findViewById(R.id.results_list_name)).setText(markers.get(i).getName());
        ((TextView)currentView.findViewById(R.id.result_list_address)).setText(markers.get(i).getAddress());
        ((RatingBar)currentView.findViewById(R.id.result_list_rating)).setRating(markers.get(i).getRating());
        return currentView;
    }
}
