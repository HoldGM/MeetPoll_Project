package odb234.meetpoll;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import java.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

/**
 * Created by Otis on 7/11/2016.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private Button mBtn;
    private static View rootView;
    private final String tag = "DatePickerFragment";
    static int year;
    static int month;
    static int day;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (year == 0){
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view,int year, int month, int day){
        mBtn = (Button) rootView.findViewById(R.id.event_date);
        this.year = year;
        this.month = month;
        this.day = day;
        mBtn.setText(month + 1 + "/" + day + "/" + year);
    }
    public static DatePickerFragment newInstance(View v) {
        rootView = v;
        return new DatePickerFragment();
    }
}
