package ch.epfl.esl.blankphonewearapp;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
