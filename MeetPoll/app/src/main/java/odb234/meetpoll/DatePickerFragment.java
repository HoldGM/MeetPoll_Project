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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view,int year, int month, int day){
        mBtn = (Button) rootView.findViewById(R.id.event_date);
//        Log.d(tag,mBtn.getText().toString());
        mBtn.setText(month + "/" + day + "/" + year);
    }
    public static DatePickerFragment newInstance(View v) {
        rootView = v;
        return new DatePickerFragment();
    }
}
