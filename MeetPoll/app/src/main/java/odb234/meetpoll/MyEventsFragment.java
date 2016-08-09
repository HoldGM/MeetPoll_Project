package odb234.meetpoll;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyEventsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyEventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyEventsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private final static String TAG = "My Events Fragment";

    ListView eventsList;
    ImageView newUser;
    DatabaseReference mRoot;
    int last;
    SharedPreferences sp;
    public MyEventsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyEventsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyEventsFragment newInstance(String param1, String param2) {
        MyEventsFragment fragment = new MyEventsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        mRoot = FirebaseDatabase.getInstance().getReference().child(mParam1).child(mParam2);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (mParam1 != null && mParam2 != null){
            final View rootView = inflater.inflate(R.layout.fragment_my_events, container, false);
            eventsList = (ListView)rootView.findViewById(R.id.main_my_events_list);
            newUser = (ImageView)rootView.findViewById(R.id.main_new_user_img);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.INTERNET}, 10);
            }
            mRoot.getRef().getParent().child("eventCount").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if ( (long)dataSnapshot.getValue() == 0) {
                        newUser.setVisibility(View.VISIBLE);
                    } else {
                        newUser.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            ListAdapter adapter = new FirebaseListAdapter<Event>(getActivity(), Event.class, R.layout.cell_view, mRoot) {
                @Override
                protected void populateView(View v, Event model, int position) {
                    ((TextView)v.findViewById(R.id.list_event_host)).setText(model.getHostName());
                    ((TextView)v.findViewById(R.id.list_event_date)).setText(model.getEventDate());
                    ((TextView)v.findViewById(R.id.list_event_name)).setText(model.getEventName());
                    ((TextView)v.findViewById(R.id.list_location)).setText(model.getEventLocation());
                    String str = model.getLocationType();
                    switch(str){
                        case "restaurant":
                            ((ImageView)v.findViewById(R.id.host_image)).setImageResource(R.drawable.burger);
                            break;
                        case "entertainment":
                            ((ImageView)v.findViewById(R.id.host_image)).setImageResource(R.drawable.masks);
                            break;
                        case "cultural":
                            ((ImageView)v.findViewById(R.id.host_image)).setImageResource(R.drawable.painting);
                            break;
                        case "religious":
                            ((ImageView)v.findViewById(R.id.host_image)).setImageResource(R.drawable.religion);
                            break;
                        case "outdoors":
                            ((ImageView)v.findViewById(R.id.host_image)).setImageResource(R.drawable.outdoor);
                            break;
                        default:
                            ((ImageView)v.findViewById(R.id.host_image)).setImageResource(R.drawable.ic_person_black_36dp);
                    }
                    ArrayList<LocationListing> places = model.getPlaces();
                    String topLocation = places.get(0).getName();
                    int numVotes = 0;
                    for(int i = 0; i < places.size(); i++){
                        if(places.get(i).getVoteCount() > numVotes){
                            topLocation = places.get(i).getName();
                            numVotes = places.get(i).getVoteCount();
                        }
                    }
                    ((TextView)v.findViewById(R.id.list_location)).setText(topLocation);
                    ((TextView)v.findViewById(R.id.list_event_date)).setText(model.getEventDate());
                    ((TextView)v.findViewById(R.id.list_time)).setText(model.getEventTime());
                    final Button detailButton = (Button)v.findViewById(R.id.edit_event);
                    v.setTag(model.getKey());
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            long k = (long) view.getTag();
                            String child_event = "" + k;
                            Intent intent = new Intent(getContext(), VoteListActivity.class);
                            intent.putExtra("uid", sp.getString("Uid", ""));
                            intent.putExtra("path", child_event);
                            startActivity(intent);
                        }
                    });
                    detailButton.setTag(model.getKey());
                    detailButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            long k = (long) view.getTag();
                            Intent intent = new Intent(getContext(), EventDetailsActivity.class);
                            intent.putExtra("uid", sp.getString("Uid", ""));
                            intent.putExtra("path", "" + k);
                            startActivity(intent);
                        }
                    });

                }
            };
            eventsList.setAdapter(adapter);
            return rootView;
        }
        return inflater.inflate(R.layout.fragment_my_events, container, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 10:
                if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Internet permission requrired.", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String string) {
        if (mListener != null) {
            mListener.onMyEventsFragmentInteraction(string);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onMyEventsFragmentInteraction(String string);
    }
}
