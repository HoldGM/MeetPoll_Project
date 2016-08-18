package odb234.meetpoll;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntegerRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventInvitesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventInvitesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventInvitesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    ListView invitedEventsList;

    SharedPreferences sp;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ArrayList<Event> eventList;
    private ArrayList<String> pathList;

    DatabaseReference mRef;

    private static final String TAG = "Invited Events Fragment";

    public EventInvitesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventInvitesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventInvitesFragment newInstance(String param1, String param2) {
        EventInvitesFragment fragment = new EventInvitesFragment();
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

        eventList = new ArrayList<>();
        pathList = new ArrayList<>();
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        mRef = FirebaseDatabase.getInstance().getReference().child(sp.getString("Uid", "")).child("invited-events");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    DatabaseReference temp = FirebaseDatabase.getInstance().getReference().child(child.getValue(String.class));
                    pathList.add(child.getValue(String.class));
                    temp.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "onCreate: " + dataSnapshot.child("eventName").getValue(String.class));
                            eventList.add(dataSnapshot.getValue(Event.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                Log.d(TAG, "Path list length: " + pathList.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        for(int i = 0; i < eventList.size(); i++){
            Log.d(TAG, eventList.get(i).toString());
        }
        if(mParam1 != null && mParam2 != null){
            View rootView = inflater.inflate(R.layout.fragment_event_invites, container, false);
            invitedEventsList = (ListView)rootView.findViewById(R.id.main_invite_eventsList);
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(final DataSnapshot child : dataSnapshot.getChildren()){
                        DatabaseReference temp = FirebaseDatabase.getInstance().getReference().child(child.getValue().toString());
                        temp.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                eventList.add(dataSnapshot.getValue(Event.class));
                                Event event = dataSnapshot.getValue(Event.class);
                                eventList.add(event);
                                ListAdapter adapter = new FirebaseListAdapter<String>(getActivity(), String.class, R.layout.cell_view, mRef){
                                    @Override
                                    protected void populateView(View v, String model, int position) {
                                        final View tempView = v;
                                        final Firebase tempBase = new Firebase(getString(R.string.firebase_path) +  model);
                                        tempView.setTag(tempBase.getPath().toString());
                                        tempBase.addValueEventListener(new com.firebase.client.ValueEventListener() {
                                            @Override
                                            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChildren()) {

                                                    ((TextView) tempView.findViewById(R.id.list_event_host)).setText(dataSnapshot.child("hostName").getValue().toString());
                                                    String dateTime = dataSnapshot.child("eventDateTime").getValue(String.class);
                                                    String[] split = dateTime.split("\\s+");
                                                    String date = split[0].substring(4, 6) + "/" + split[0].substring(6) + "/" + split[0].substring(0,4);
                                                    int hour = Integer.parseInt(split[1].substring(0,2));
                                                    String amPm = (hour >= 12) ? "PM" : "AM";
                                                    hour = hour % 12;
                                                    hour = (hour == 0) ? 12 : hour;
                                                    ((TextView) tempView.findViewById(R.id.list_event_date)).setText(date);
                                                    ((TextView) tempView.findViewById(R.id.list_time)).setText(hour + ":" + split[1].substring(2) + " " + amPm);
                                                    ((TextView) tempView.findViewById(R.id.list_event_name)).setText(dataSnapshot.child("eventName").getValue().toString());
                                                    String str = dataSnapshot.child("locationType").getValue().toString();
                                                    switch (str) {
                                                        case "restaurant":
                                                            ((ImageView) tempView.findViewById(R.id.host_image)).setImageResource(R.drawable.burger);
                                                            break;
                                                        case "entertainment":
                                                            ((ImageView) tempView.findViewById(R.id.host_image)).setImageResource(R.drawable.masks);
                                                            break;
                                                        case "cultural":
                                                            ((ImageView) tempView.findViewById(R.id.host_image)).setImageResource(R.drawable.painting);
                                                            break;
                                                        case "religious":
                                                            ((ImageView) tempView.findViewById(R.id.host_image)).setImageResource(R.drawable.religion);
                                                            break;
                                                        case "outdoors":
                                                            ((ImageView) tempView.findViewById(R.id.host_image)).setImageResource(R.drawable.outdoor);
                                                            break;
                                                        default:
                                                            ((ImageView) tempView.findViewById(R.id.host_image)).setImageResource(R.drawable.ic_person_black_36dp);
                                                    }
                                                    long voteCount = (long) dataSnapshot.child("places").child("0").child("voteCount").getValue();
                                                    String location = dataSnapshot.child("places").child("0").child("name").getValue().toString();
                                                    for (com.firebase.client.DataSnapshot child : dataSnapshot.child("places").getChildren()) {
                                                        if ((long) child.child("voteCount").getValue() > voteCount) {
                                                            voteCount = (long) child.child("voteCount").getValue();
                                                            location = child.child("name").getValue().toString();
                                                        }
                                                    }
                                                    Button detailBtn = (Button) tempView.findViewById(R.id.edit_event);
                                                    detailBtn.setTag(tempView.getTag());
                                                    detailBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            String path = view.getTag().toString();
                                                            String[] pathSplit = path.split("/");
                                                            Intent intent = new Intent(getContext(), EventDetailsActivity.class);
                                                            intent.putExtra("uid", pathSplit[1]);
                                                            intent.putExtra("path", pathSplit[3]);
                                                            startActivity(intent);
                                                        }
                                                    });
                                                    ((TextView) tempView.findViewById(R.id.list_location)).setText(location);
                                                    tempView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            String path = view.getTag().toString();
                                                            String[] pathSplit = path.split("/");
                                                            Intent intent = new Intent(getContext(), VoteListActivity.class);
                                                            intent.putExtra("uid", pathSplit[1]);
                                                            intent.putExtra("path", pathSplit[3]);
                                                            startActivity(intent);
                                                        }
                                                    });
                                                }

                                            }

                                            @Override
                                            public void onCancelled(FirebaseError firebaseError) {

                                            }
                                        });

                                    }
                                };
                                invitedEventsList.setAdapter(adapter);

                            }



                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            return rootView;
        }
        return inflater.inflate(R.layout.fragment_event_invites, container, false);
    }

    public void populateList(DataSnapshot snapshot){
        eventList = new ArrayList<>();
        for(final DataSnapshot child : snapshot.getChildren()){
            DatabaseReference temp = FirebaseDatabase.getInstance().getReference().child(child.getValue().toString());
            temp.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    eventList.add(dataSnapshot.getValue(Event.class));
                    Log.d(TAG, dataSnapshot.child("eventName").getValue(String.class));
                    Log.d(TAG, Arrays.deepToString(eventList.toArray()));
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String string) {
        if (mListener != null) {
            mListener.onEventInvitesFragmentInteraction(string);
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
        void onEventInvitesFragmentInteraction(String string);
    }

    class EventListAdapter extends BaseAdapter{
        ArrayList<Event> events;
        LayoutInflater inflater;
        public EventListAdapter(Context context, ArrayList<Event> e){
            events = e;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            events = sortList(events);
        }

        private ArrayList<Event> sortList(ArrayList<Event> e){
            ArrayList<Event> left = new ArrayList<>();
            ArrayList<Event> right = new ArrayList<>();
            int center;
            if(e.size() == 1){
                return e;
            }else{
                center = e.size()/2;
                //left half
                left = new ArrayList<>(e.subList(0, center));
                //right half;
                right = new ArrayList<>(e.subList(center, e.size()));
                left = sortList(left);
                right = sortList(right);
                merge(left, right, e);
            }
            return e;
        }

        private void merge(ArrayList<Event> left, ArrayList<Event> right, ArrayList<Event> whole){
            int leftIndex = 0;
            int rightIndex = 0;
            int wholeIndex = 0;
            while(leftIndex < left.size() && rightIndex < right.size()){
                if(left.get(leftIndex).getEventDateTime().compareTo(right.get(rightIndex).getEventDateTime()) < 0 ){
                    whole.set(wholeIndex, left.get(leftIndex));
                    leftIndex++;
                }else{
                    whole.set(wholeIndex, right.get(rightIndex));
                    rightIndex++;
                }
                wholeIndex++;
            }
            if(leftIndex >= left.size()){
                for(;rightIndex < right.size(); rightIndex++){
                    whole.set(wholeIndex, right.get(rightIndex));
                    wholeIndex++;
                }
            }else{
                for(;leftIndex < left.size(); leftIndex++){
                    whole.set(wholeIndex, left.get(leftIndex));
                    wholeIndex++;
                }
            }
        }
        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public Object getItem(int i) {
            return events.get(i);
        }

        @Override
        public int getCount() {
            return events.size();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View currentView = view;
            if(currentView == null){
                currentView = inflater.inflate(R.layout.cell_view, viewGroup);
            }


            return null;
        }
    }

}
