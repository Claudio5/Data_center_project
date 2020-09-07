package ch.epfl.esl.datacenter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;


/**
 * Created by Claudio on 28.12.2017.
 */

public class Alarm extends BroadcastReceiver {

    private final int threshold = 12;
    private boolean alarmOn = false;
    private final static String TAG = "Alarm";
    private ArrayList<String> serverWarnings = new ArrayList<>();
    private String[] IDServer;
    private String servWarn;
    private Context cont;

    @Override
    public void onReceive(Context context, Intent intent) {
        cont = context;
        final PendingResult pendingResult = goAsync();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        // It checks every X minutes if the power is above or below a certain threshold
        // For the moment it simply displays a Toast
        String url = intent.getExtras().getString("url");
        Log.e(TAG, "Service " + url);

        Intent in = new Intent(context,MyIntentService.class);
        in.putExtra("url",url);
        in.putExtra("nbServer",intent.getExtras().getIntArray("nbServer"));
        context.startService(in);

        // End of alarm code

        wl.release();
    }


    // Set time parameters for the alarm
    public void setAlarm(Context context, Intent intent) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        i.putExtra("url", intent.getExtras().getString("url"));
        i.putExtra("nbServer",intent.getExtras().getIntArray("nbServer"));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES/15,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES/15, pi);

    }

    // Stop the alarm
    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }




}

