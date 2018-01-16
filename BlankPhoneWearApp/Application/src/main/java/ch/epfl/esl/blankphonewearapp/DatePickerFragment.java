package ch.epfl.esl.blankphonewearapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by Claudio on 13.01.2018.
 */

/*public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener, DialogInterface.OnCancelListener {



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dp=new DatePickerDialog(getActivity(), this, year, month, day);
        dp.getDatePicker().setMaxDate(System.currentTimeMillis());


        //return new DatePickerDialog(getActivity(), this, year, month, day);
        return dp;

    }


    public void onDateSet(DatePicker view, int year, int month, int day) {
        //cancelled=false;
        TimePickerFragment dialog = new TimePickerFragment();
        dialog.show(getFragmentManager(),"Time");
        String date = "";
        String monthStr = "";
        String dayStr="";
        if(month>=9) {
            monthStr=Integer.toString(month+1);
        }
        else {
            monthStr="0"+Integer.toString(month+1);
        }

        if(day>=10) {
            dayStr=Integer.toString(day);
        }
        else {
            dayStr="0"+Integer.toString(day);
        }
        date = Integer.toString(year)+monthStr+dayStr;

        ((SecondActivity) getActivity()).setDate(date);



    }

    public void onCancel(DialogInterface dialog) {

        ((SecondActivity) getActivity()).setDate("");
        Intent intent = ((SecondActivity) getActivity()).getIntent();
        ((SecondActivity) getActivity()).finish();
        startActivity(intent);


    }

}*/