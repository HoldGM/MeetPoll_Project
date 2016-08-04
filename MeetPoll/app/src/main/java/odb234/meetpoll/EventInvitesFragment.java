package odb234.meetpoll;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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

        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if(mParam1 != null && mParam2 != null){
            View rootView = inflater.inflate(R.layout.fragment_event_invites, container, false);
            invitedEventsList = (ListView)rootView.findViewById(R.id.main_invite_eventsList);
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child(sp.getString("Uid", "")).child("invited-events");

            ListAdapter adapter = new FirebaseListAdapter<String>(getActivity(), String.class, R.layout.cell_view, mRef){
                @Override
                protected void populateView(View v, String model, int position) {
                    final View tempView = v;
                    final Firebase tempBase = new Firebase(getString(R.string.firebase_path) +  model);
                    tempView.setTag(tempBase.getPath().toString());
                    tempBase.addValueEventListener(new com.firebase.client.ValueEventListener() {
                        @Override
                        public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                            ((TextView) tempView.findViewById(R.id.list_event_host)).setText(dataSnapshot.child("hostName").getValue().toString());
                            ((TextView) tempView.findViewById(R.id.list_event_date)).setText(dataSnapshot.child("eventDate").getValue().toString());
                            ((TextView) tempView.findViewById(R.id.list_event_name)).setText(dataSnapshot.child("eventName").getValue().toString());
                            ((TextView) tempView.findViewById(R.id.list_time)).setText(dataSnapshot.child("eventTime").getValue().toString());
                            long voteCount = (long)dataSnapshot.child("places").child("0").child("voteCount").getValue();
                            String location = dataSnapshot.child("places").child("0").child("name").getValue().toString();
                            for(com.firebase.client.DataSnapshot child : dataSnapshot.child("places").getChildren()) {
                                if((long)child.child("voteCount").getValue() > voteCount){
                                    voteCount = (long)child.child("voteCount").getValue();
                                    location = child.child("name").getValue().toString();
                                }
                            }
                            ((TextView) tempView.findViewById(R.id.list_location)).setText(location);
                            tempView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String path = view.getTag().toString();
                                    String[] pathSplit = path.split("/");
                                    Log.d(TAG, Arrays.deepToString(pathSplit));
                                    Intent intent = new Intent(getContext(), VoteListActivity.class);
                                    intent.putExtra("uid", pathSplit[1]);
                                    intent.putExtra("path", pathSplit[3]);
                                    startActivity(intent);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
            };


//            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    ArrayList<String> tempArr = new ArrayList<String>();
//                    for(DataSnapshot child : dataSnapshot.getChildren()){
//                        Log.d(TAG, child.getValue().toString());
//                        if((new Firebase(getString(R.string.firebase_path) + child.getValue().toString())) == null) {
//                            Firebase fb = new Firebase(child.toString());
//                            fb.removeValue();
//                            continue;
//                        }else {
//                            tempArr.add(child.getValue().toString());
//                        }
//                    }
//                    ListAdapter adapter = new InviteListAdapter(getContext(), tempArr);
//                    invitedEventsList.setAdapter(adapter);
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
            invitedEventsList.setAdapter(adapter);
            return rootView;
        }
        return inflater.inflate(R.layout.fragment_event_invites, container, false);
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

    public class InviteListAdapter extends BaseAdapter{
        ArrayList<String> list;
        LayoutInflater inflater;
        public InviteListAdapter(Context context, ArrayList<String> l){
            list = l;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View currentView = view;
            if(currentView == null){
                currentView = inflater.inflate(R.layout.cell_view, viewGroup, false);
            }
            final Firebase tempBase = new Firebase(getString(R.string.firebase_path) + list.get(i));
            Log.d(TAG, tempBase.toString());
            currentView.setTag(tempBase.toString());
            final View replacementView = currentView;
            Log.d(TAG, "Replacement View Tag: " + replacementView.getTag().toString());
            tempBase.addValueEventListener(new com.firebase.client.ValueEventListener() {
                @Override
                public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                    ((TextView) replacementView.findViewById(R.id.list_event_name)).setText(dataSnapshot.child("eventName").getValue().toString());
                    ((TextView) replacementView.findViewById(R.id.list_event_date)).setText(dataSnapshot.child("eventDate").getValue().toString());
                    ((TextView) replacementView.findViewById(R.id.list_time)).setText(dataSnapshot.child("eventTime").getValue().toString());
                    ((TextView) replacementView.findViewById(R.id.list_event_host)).setText(dataSnapshot.child("hostName").getValue().toString());
                    long votes = (long) dataSnapshot.child("places").child("0").child("voteCount").getValue();
                    String location = dataSnapshot.child("places").child("0").child("name").getValue().toString();

                    for (com.firebase.client.DataSnapshot child : dataSnapshot.child("places").getChildren()) {
                        if ((long) child.child("voteCount").getValue() > votes) {
                            votes = (long) child.child("voteCount").getValue();
                            location = child.child("name").getValue().toString();
                        }
                    }
                    ((TextView) replacementView.findViewById(R.id.list_location)).setText(location);

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
            Button detailBtn = (Button)replacementView.findViewById(R.id.edit_event);
            detailBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String path = view.getTag().toString();
                    Log.d(TAG, path);
                    Intent intent = new Intent(getContext(), EventDetailsActivity.class);
                    intent.putExtra("fullPath", path);
                }
            });
            return replacementView;
        }
    }
}
