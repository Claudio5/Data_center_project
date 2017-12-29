package ch.epfl.esl.blankphonewearapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;


/**
 * Created by Claudio on 28.12.2017.
 */

public class Alarm extends BroadcastReceiver {

    private final int threshold = 4;
    private boolean alarmOn = false;
    private final static String TAG = "Alarm";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        // It checks every X minutes if the power is above or below a certain threshold
        // For the moment it simply displays a Toast
        String url = intent.getExtras().getString("url");
        Log.w(TAG,"Service " + url);

        new GetRacks(context) {
            @Override
            protected void onPostExecute(String[] result) {
                float sum = 0;

                for (int i = 0; i < result.length; i++) {
                    sum += Float.parseFloat(result[i]);
                }
                float avg = sum / result.length;
                checkThreshold(avg);

                if (alarmOn) {
                    Toast.makeText(getContext(), "Above threshold !!!!!!!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Below threshold", Toast.LENGTH_LONG).show();
                }


            }
        }.execute(url);
        // End of alarm code


        wl.release();
    }

    private void checkThreshold(float avg){
        if(avg>= threshold){
            alarmOn=true;
        }else{
            alarmOn=false;
        }
    }

    // Set time parameters for the alarm
    public void setAlarm(Context context,Intent intent)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        i.putExtra("url",intent.getExtras().getString("url"));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 2 * 10, pi); // Millisec * Second * Minute
    }

    // Stop the alarm
    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}

