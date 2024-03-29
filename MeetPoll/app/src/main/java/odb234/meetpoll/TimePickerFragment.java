package odb234.meetpoll;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;


import java.util.Calendar;

/**
 * Created by rspiegel on 7/12/16.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private static View rootView;
    private Button mBtn;
    static int hour = -1;
    static int min = -1;
    private static final String TAG = "Time Picker Fragment";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        if(min == -1)
            min= c.get(Calendar.MINUTE);
        if(hour == -1)
            hour = c.get(Calendar.HOUR_OF_DAY);

        return new TimePickerDialog(getActivity(),this,hour,min, DateFormat.is24HourFormat(getActivity()));
    }
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mBtn = (Button) rootView.findViewById(R.id.event_time);
        hour = hourOfDay;
        min = minute;
        Log.d(TAG, "Hour: " + hourOfDay + ", Min: " + min);
        String t = (hourOfDay >= 12)?"PM" : "AM";
        if(hourOfDay > 12) {
            hourOfDay = hourOfDay%12;
        }
        if(hourOfDay == 0)
            hourOfDay = 12;
        ((NewEventActivity)getActivity()).timeOut = String.format("%02d%02d", hour, min);
        mBtn.setText(String.format("%d:%02d %s",hourOfDay,minute,t));
    }
    public static TimePickerFragment newInstance(View v) {
        rootView = v;
        return new TimePickerFragment();
    }
}
