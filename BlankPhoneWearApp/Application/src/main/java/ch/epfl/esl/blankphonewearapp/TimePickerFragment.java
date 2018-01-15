package ch.epfl.esl.blankphonewearapp;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Claudio on 13.01.2018.
 */


public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener, DialogInterface.OnCancelListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);


        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        String min="";
        String h="";

        if(hourOfDay<10){
            h="0"+Integer.toString(hourOfDay);
        }
        else{
            h=Integer.toString(hourOfDay);
        }

        if(minute>=8 && minute<23) {
            min=Integer.toString(15);
        }
        else if(minute>=23 && minute<38){
            min=Integer.toString(30);
        }
        else if(minute>=38 && minute<53){
            min=Integer.toString(45);
        }
        else if(minute>=53 && minute <=59){
            min="0"+Integer.toString(0);
            if(hourOfDay+1==24){
                h="00";
            }
            else {
                if(hourOfDay+1<10){
                    h="0"+Integer.toString(hourOfDay+1);
                }
                else{
                    h=Integer.toString(hourOfDay+1);
                }

            }

        }
        else {
            min="0"+Integer.toString(0);
        }

        String time=h+min;

        String [] xlabels = obtainRange(Integer.parseInt(h),Integer.parseInt(min));

        //Log.e(TAG,"The time : "+time);

        ((SecondActivity) getActivity()).setTime(time);

        Number [] nbr = {1,1,1,1,1};
        ((SecondActivity) getActivity()).series_update(nbr,0);
        ((SecondActivity) getActivity()).plotUpdate(xlabels);

    }

    private String [] obtainRange(int init_h,int init_min){

        String [] xlabels = new String[5];
        String min="";
        String h="";
        for(int i=0;i<5;i++) {
            int min_new=init_min+5*i;
            int h_new=init_h;
            if(min_new>=60) {
                min_new=min_new % 60;
                h_new=h_new+1;
                if(h_new+1==24){
                    h_new=0;
                }
            }
            if(min_new<10){
                min="0"+Integer.toString(min_new);
            }
            else {
                min=Integer.toString(min_new);
            }
            if(h_new<10) {
                h="0"+Integer.toString(h_new);
            }
            else {
                h = Integer.toString(h_new);
            }
            xlabels[i]=h+":"+min;
        }

        return xlabels;
    }

    public void onCancel(DialogInterface dialog) {

        ((SecondActivity) getActivity()).setTime("");
        Intent intent = ((SecondActivity) getActivity()).getIntent();
        ((SecondActivity) getActivity()).finish();
        startActivity(intent);


    }
}

