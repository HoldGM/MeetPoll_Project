package odb234.meetpoll;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventDetailFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private static final String TAG = "Event fragment";

    private GoogleMap mMap;
    MapView mapView;

    ArrayList<MapMarker> markers;
    MapMarker topMarker;

    RelativeLayout callView;
    RelativeLayout directionView;
    ImageButton editDateBtn;
    ImageButton editTimeBtn;
    TextView dateText;
    TextView timeText;

    public EventDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventDetailFragment newInstance(String param1, String param2) {
        EventDetailFragment fragment = new EventDetailFragment();
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
        markers = new ArrayList<>();
        Log.d(TAG, "mParam1: " + mParam1 + ", mParam2: " + mParam2);
        if(mParam1 != null  && mParam2 != null) {
            final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            editDateBtn = (ImageButton)rootView.findViewById(R.id.event_detail_edit_date);
            editTimeBtn = (ImageButton)rootView.findViewById(R.id.event_detail_edit_time);
            dateText = (TextView)rootView.findViewById(R.id.detail_event_date);
            timeText = (TextView)rootView.findViewById(R.id.detail_event_time);



            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(mParam1).child("events").child(mParam2).child("places");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DataSnapshot topLocation = dataSnapshot.child("0");
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.child("voteCount").getValue(int.class) > topLocation.child("voteCount").getValue(int.class)) {
                            topLocation = child;
                        }
                    }
                    topMarker = new MapMarker(new LatLng((double) topLocation.child("lat").getValue(), (double) topLocation.child("lng").getValue()), topLocation.child("name").getValue().toString());
                    ((TextView) rootView.findViewById(R.id.details_location_name)).setText(topLocation.child("name").getValue().toString());
                    ((TextView) rootView.findViewById(R.id.details_location_address)).setText(topLocation.child("address").getValue().toString());
                    ((TextView) rootView.findViewById(R.id.details_location_phone)).setText(topLocation.child("phone").getValue().toString());
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (!child.child("address").toString().equals(topLocation.child("address").toString())) {
                            markers.add(new MapMarker(new LatLng((double) child.child("lat").getValue(), (double) child.child("lng").getValue()), child.child("name").getValue().toString()));
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            (ref.getParent()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()) {
                        Log.d(TAG, dataSnapshot.child("eventDate").getValue().toString());
                        Log.d(TAG, dataSnapshot.child("eventTime").getValue().toString());
                        ((TextView) rootView.findViewById(R.id.detail_event_date)).setText(dataSnapshot.child("eventDate").getValue().toString());
                        ((TextView) rootView.findViewById(R.id.detail_event_time)).setText(dataSnapshot.child("eventTime").getValue().toString());
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mapView = (MapView) rootView.findViewById(R.id.map_view);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;

                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            marker.showInfoWindow();
                            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                            Log.d(TAG, "Marker selected" + marker.getTitle().toString());
                            return true;
                        }
                    });
                    for (int i = 0; i < markers.size(); i++) {
                        mMap.addMarker(new MarkerOptions().position(markers.get(i).getLatLng()).title(markers.get(i).getName()).icon(BitmapDescriptorFactory.defaultMarker(195)));
                    }
                    mMap.addMarker(new MarkerOptions().position(topMarker.getLatLng()).title(topMarker.getName()).icon(BitmapDescriptorFactory.defaultMarker(18)));
                    LatLngBounds.Builder bounds = new LatLngBounds.Builder();
                    bounds.include(topMarker.getLatLng());
                    for (int i = 0; i < markers.size(); i++) {
                        bounds.include(markers.get(i).getLatLng());
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(topMarker.getLatLng(), 15));
                }
            });

            callView = (RelativeLayout)rootView.findViewById(R.id.detail_call_view);
            callView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("tel:"+((TextView)rootView.findViewById(R.id.details_location_phone)).getText().toString()));
                    startActivity(intent);
                }
            });

            directionView = (RelativeLayout)rootView.findViewById(R.id.detail_event_directions);
            directionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + ((TextView) rootView.findViewById(R.id.details_location_address)).getText().toString()));
                    startActivity(intent);
                }
            });
            if(!mParam1.equals(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("Uid", "")))
            {
                editDateBtn.setVisibility(View.GONE);
                editTimeBtn.setVisibility(View.GONE);
            }else {
                setButtonListeners();
            }
            return rootView;
        }
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    public void setButtonListeners(){

        editDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] date = dateText.getText().toString().split("/");
                DatePickerDialog dpd = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener(){
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        FirebaseDatabase.getInstance().getReference().child(mParam1).child("events").child(mParam2).child("eventDate").setValue((i1+1) + "/" + i2 + "/" + i);
                    }
                },Integer.parseInt(date[2]),Integer.parseInt(date[0])-1,Integer.parseInt(date[1]));
                dpd.show();
            }
        });
        editTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] time = timeText.getText().toString().split(":|\\s+");
                int hour = Integer.parseInt(time[0]);
                int min = Integer.parseInt(time[1]);
                String amPm = time[2];

                hour = (amPm.equals("AM"))? hour : hour + 12;
                if(hour == 12 && amPm.equals("AM"))
                    hour = 0;

                TimePickerDialog tpd = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        String amPm = (i >= 12) ? "PM": "AM";
                        i = (i > 12)? i % 12 : i;
                        if(i == 0)
                            i = 12;
                        FirebaseDatabase.getInstance().getReference().child(mParam1).child("events").child(mParam2).child("eventTime").setValue(i + ":" + i1 + " " + amPm);
                    }
                }, hour, min, false);
                tpd.show();
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String string) {
        if (mListener != null) {
            mListener.onEventDetailFragmentInteraction(string);
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
        void onEventDetailFragmentInteraction(String string);
    }
}
