package ch.epfl.esl.blankphonewearapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

    public String getServerWarning(){
        return servWarn;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        cont = context;
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        // It checks every X minutes if the power is above or below a certain threshold
        // For the moment it simply displays a Toast
        String url = intent.getExtras().getString("url");
        Log.w(TAG, "Service " + url);

        new GetJSON_AllVal().execute(url);
        // End of alarm code


        wl.release();
    }

    private void checkThreshold(float avg) {
        if (avg >= threshold) {
            alarmOn = true;
        } else {
            alarmOn = false;
        }
    }

    // Set time parameters for the alarm
    public void setAlarm(Context context, Intent intent) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        i.putExtra("url", intent.getExtras().getString("url"));
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


    private String[] decode_list(String list) {

        String[] urls = list.split("#end#");
        IDServer=legendCreate(urls);
        return urls;
    }

    private class GetJSON_AllVal extends AsyncTask<String, Void, String[]> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(String... url) {
            urlHandler sh = new urlHandler();
            String[] urls = decode_list(url[0]);

            String[] powerArray = new String[urls.length];
            for (int i = 0; i < urls.length; i++) {
                String jsonStr = sh.getjsonstring(urls[i]);


                if (jsonStr != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonStr);

                        //for (int i = 0; i < jsonObject.length(); i++) {


                        Iterator<String> iterator = jsonObject.keys();
                        String name = iterator.next();

                        JSONArray racks = jsonObject.getJSONArray(name);
                        String power = "";

                        for (int j = 0; j < racks.length(); j++) {
                            power = power + racks.getString(j) + ";";
                        }

                        powerArray[i] = power;

                    } catch (final JSONException e) {
                        Log.e(TAG, "Json parsing error: " + e.getMessage());


                    }


                } else {
                    Log.e(TAG, "No response");
                }

            }

            return powerArray;
        }



        @Override
        protected void onPostExecute(String[] result) {

            //updatePlot(result);

            if(result!=null) {

                for (int i = 0; i < result.length; i++) {

                    Number[] val = string2nbr(result[i]);

                    int sum = 0;
                    int avg;
                    for (int j = 0; j < val.length; j++) {
                        int value = val[j].intValue();
                        sum += value;

                    }
                    avg = sum/val.length;
                    if(avg>threshold){
                        serverWarnings.add(IDServer[i]);
                    }

                }
                servWarn="";
                for (String s : serverWarnings) {
                    servWarn += s + "\t";
                }

                Log.w(TAG,servWarn+"Power too high");
                if (servWarn!=""){
                    sendNotification(servWarn + "Power too high","Hello");
                    //new MainActivity().sendNotificationWear();
                }

            }


        }

    }

    private void sendNotification(String text, String title){
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent resultIntent = new Intent(cont, SecondActivity.class);
        resultIntent.putExtra("url","http://10.0.2.2:5002/rack01/s01/power/last5min#end#http://10.0.2.2:5002/rack01/s02/power/last5min#end#");




        NotificationManager mNotifyMgr = (NotificationManager) cont.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(cont)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle(title)
                .setContentText(text)
                .setSound(alarmSound)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);



        TaskStackBuilder stackBuilder = TaskStackBuilder.create(cont);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(SecondActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setLights(Color.BLUE, 500, 500);
        mNotifyMgr.notify(12345, mBuilder.build());

    }


    private String[] legendCreate(String [] urls){
        String [] legend = new String[urls.length];
        for(int i=0;i<urls.length;i++) {
            String rack_nb = "";
            String srv_nb = "";
            Pattern pattern = Pattern.compile("rack(.*?)/");
            Matcher matcher = pattern.matcher(urls[i]);
            while (matcher.find()) {
                rack_nb = matcher.group(1);
            }
            Pattern pattern2 = Pattern.compile("/s(.*?)/");
            Matcher matcher2 = pattern2.matcher(urls[i]);
            while (matcher2.find()) {
                srv_nb = matcher2.group(1);
            }

            legend[i] = "Rack " + rack_nb + " Server " + srv_nb ;

        }
        return legend;
    }


    private Number[] string2nbr(String series_id){

        String [] vals = series_id.split(";");

        Number [] vals_nbr = new Number[vals.length];
        for(int i=0;i<vals.length;i++){
            vals_nbr[i]=Integer.parseInt(vals[i]);
        }
        return vals_nbr;
    }



}

