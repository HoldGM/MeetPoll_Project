package odb234.meetpoll;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.type.ArrayType;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.zip.Inflater;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VoteCountFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VoteCountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VoteCountFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    ListView list;
    static ArrayList<LocationListing> voteList;

    public VoteCountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VoteCountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VoteCountFragment newInstance(String param1, String param2) {
        VoteCountFragment fragment = new VoteCountFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(mParam1 != null && mParam2 != null){
            View rootView = inflater.inflate(R.layout.fragment_vote_count, container, false);
            list = (ListView)rootView.findViewById(R.id.detail_vote_count_list);
            voteList = new ArrayList<LocationListing>();
            Query ref = FirebaseDatabase.getInstance().getReference().child(mParam1).child("events").child(mParam2).child("places").orderByChild("voteCount");

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChildren()){
                        Toast.makeText(getActivity(), "Sorry, this event has been cancelled", Toast.LENGTH_LONG).show();
                    }
                    voteList = new ArrayList<LocationListing>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        voteList.add(0, new LocationListing(child.child("name").getValue().toString(), child.child("address").getValue().toString(), (int) ((long) child.child("voteCount").getValue())));
                    }
                    ListAdapter adapter = new reverseAdapter(getContext(), voteList);
                    list.setAdapter(adapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            
            return rootView;

        }
        return inflater.inflate(R.layout.fragment_vote_count, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onVoteDetailFragmentInteraction(uri);
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
        void onVoteDetailFragmentInteraction(Uri uri);
    }

    public class reverseAdapter extends BaseAdapter{

        ArrayList<LocationListing> list;
        LayoutInflater inflater;
        public reverseAdapter(Context context, ArrayList<LocationListing> l){
            list = l;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            return list.size();
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
        public View getView(int i, View view, ViewGroup viewGroup) {
            View currentView = view;
            if(currentView == null){
                currentView = inflater.inflate(R.layout.vote_count_list_cell, viewGroup, false);
            }

            ((TextView)currentView.findViewById(R.id.detail_vote_count)).setText(list.get(i).getVoteCount() + "");
            ((TextView)currentView.findViewById(R.id.detail_vote_name)).setText(list.get(i).getName());
            ((TextView)currentView.findViewById(R.id.detail_vote_address)).setText(list.get(i).getAddress());
            return currentView;
        }
    }
}
