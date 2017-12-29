package ch.epfl.esl.blankphonewearapp;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Claudio on 28.12.2017.
 */

public class MyService extends Service {

    private static final String TAG = "MyService";

    Alarm alarm = new Alarm();
    // First method called
    public void onCreate() {
        super.onCreate();
    }

    // Second method called
    // Here we pass the extras of our intent
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        alarm.setAlarm(this,intent);
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        alarm.setAlarm(this,intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
