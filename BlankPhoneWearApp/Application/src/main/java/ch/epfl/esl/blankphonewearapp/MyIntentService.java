package ch.epfl.esl.blankphonewearapp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Claudio on 15.01.2018.
 */

public class MyIntentService extends IntentService {

    private final static String TAG = "MyIntentService";
    private final int threshold = 10;
    private boolean alarmOn = false;
    private ArrayList<String> serverWarnings = new ArrayList<>();
    private String[] IDServer;
    private String servWarn;
    private int[] nbServer;
    private Context cont;
    private ArrayList<Integer> indexWarning;
    private String url;
    private String[] urls;


    public MyIntentService(){
        super("My Intent Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        cont = this;
        url = intent.getExtras().getString("url");
        nbServer = intent.getExtras().getIntArray("nbServer");

        indexWarning = new ArrayList<>();
        indexWarning.clear();
        urlHandler sh = new urlHandler();
        String[] urls = decode_list(url);
        Log.e(TAG,"onHandleIntent "+ url);
        String[] powerArray = new String[urls.length];
        Log.e(TAG,"HELLO "+Integer.toString(urls.length));
        for (int i = 0; i < urls.length; i++) {
            String jsonStr = sh.getjsonstring(urls[i]);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);

                    Iterator<String> iterator = jsonObject.keys();
                    String name = iterator.next();

                    JSONArray racks = jsonObject.getJSONArray(name);
                    String power = "";

                    for (int j = 0; j < racks.length(); j++) {
                        power = power + racks.getString(j) + ";";
                    }

                    if(powerArray[i]==null){
                        powerArray[i] = power;
                    }else{
                        Log.e(TAG,"Error getting data");
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "No response");
                powerArray=null;
                break;
            }


        }


        boolean checkNull = checkArrayNull(powerArray);

        if(!checkNull) {

            for (int i = 0; i < powerArray.length; i++) {

                Number[] val = string2nbr(powerArray[i]);
                int sum = 0;
                int avg;
                for (int j = 0; j < val.length; j++) {
                    int value = val[j].intValue();
                    sum += value;

                }
                avg = sum / val.length;
                if (avg > threshold) {
                    serverWarnings.add(IDServer[i]);
                    indexWarning.add(i);
                }
            }
            servWarn = "";
            for (String s : serverWarnings) {
                servWarn += s + "\t";
            }

            Log.w(TAG, servWarn + "Power too high");

            if (servWarn != "") {
                sendNotification(servWarn + "Power too high", "Data Center Control",reconstructWarningURL());
                //new MainActivity().sendNotificationWear();
                Log.e("sender", "Broadcasting message");
                Intent intentMain = new Intent("custom-event-name");
                //You can also include some extra data.
                intentMain.putExtra("notif", servWarn + "Power too high");
                LocalBroadcastManager.getInstance(cont).sendBroadcast(intentMain);
            }

        }else{
            Log.e(TAG, "One or more elements in the array are null");
        }

        String IP=getIPAddress(url);
        MainActivity.server.addSampling(MainActivity.db,IP,url,nbServer);
        Log.e(TAG,"Added to database");

    }

    private String reconstructWarningURL(){
        String warningUrl="";
        for(int i=0;i<indexWarning.size();i++){
            warningUrl += urls[indexWarning.get(i)] + "#end#";
        }
        Log.e(TAG,"Warning URL "+ warningUrl);
        return warningUrl;
    }

    private String[] decode_list(String list) {

        urls = list.split("#end#");
        IDServer=legendCreate(urls);
        return urls;
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

            legend[i] = "Rack " + rack_nb + " Server " + srv_nb +" " ;

        }
        return legend;
    }



    private void sendNotification(String text, String title, String warningUrl){
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent resultIntent = new Intent(cont, SecondActivity.class);
        //resultIntent.putExtra("url","http://192.168.1.169:5002/rack01/s01/power/last5min#end#http://192.168.1.169:5002/rack01/s02/power/last5min#end#");
        resultIntent.putExtra("url",warningUrl);

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

    public Number[] string2nbr(String series_id){

        String [] vals = series_id.split(";");

        Number [] vals_nbr = new Number[vals.length];
        for(int i=0;i<vals.length;i++){
            vals_nbr[i]=Integer.parseInt(vals[i]);
        }
        return vals_nbr;
    }

    private static String getIPAddress(String url){
        String[] url_split = url.split("/");
        return url_split[2];
    }

    public boolean checkArrayNull(String[] array){
        boolean checkNull=false;

        for (int i=0; i<array.length; i++) {
            if (array[i] != null) {
                Log.e(TAG, "Array not null");
                checkNull = false;
            } else {
                checkNull = true;
                Log.e(TAG,"Array null element");
                break;
            }
        }
        return checkNull;
    }

}
