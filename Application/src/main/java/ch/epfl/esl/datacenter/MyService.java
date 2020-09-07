package ch.epfl.esl.datacenter;

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
    private Context ctx;

    Alarm alarm = new Alarm();

    // First method called
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"OnCreate Service");
    }

    // Second method called
    // Here we pass the extras of our intent
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        alarm.setAlarm(this, intent);
        return START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        alarm.cancelAlarm(this);
        Log.i(TAG, "onCreate() , service stopped...");
    }


}
