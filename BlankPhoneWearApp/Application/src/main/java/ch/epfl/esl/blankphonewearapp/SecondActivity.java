package ch.epfl.esl.blankphonewearapp;

/**
 * Created by Claudio on 05.12.2017.
 */

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.androidplot.ui.HorizontalPositioning;
import com.androidplot.ui.Size;
import com.androidplot.ui.SizeMode;
import com.androidplot.ui.VerticalPositioning;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.esl.commons.DataLayerCommons;


public class SecondActivity extends AppCompatActivity implements
        CapabilityApi.CapabilityListener,
        MessageApi.MessageListener,
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Tag for Logcat
    private static final String TAG = "SecondActivity";
    private static final int MAX_SERVER = 5;

    // Members used for the Wear API
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    // Fake data generator to send to the Wear device
    private ScheduledExecutorService mGeneratorExecutor;
    private ScheduledFuture<?> mDataItemGeneratorFuture;

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 12345;

    private String url;
    private String[] urls;
    private String ip;
    private int nbRack;
    private int nbServer[];
    private Handler handler = new Handler();
    private boolean orientationPortrait;

    private int nbServers;
    private DatabaseHandler db=null;
    private ServerDAO server;

    private SecondActivitySwipeAdapter adapterViewPager;
    private ViewPager vpPager;
    private String date="";
    private String time="";

    private XYPlot plot;


    // This will be called every minute
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            Log.e(TAG,"Every 60 seconds");

            urls = decode_list(url);

            new GetJSON_val(){
                @Override
                protected void onPostExecute(String[] result) {

                    //updatePlot(result);

                    if(result!=null) {
                        Float[][] values = new Float[MAX_SERVER][nbServers];
                        Float[] lastValue = new Float[MAX_SERVER];
                        for (int i=0;i<MAX_SERVER;i++)
                            lastValue[i]=(float)0;
                        //Log.e(TAG,"result length = "+result.length);
                        for (int i = 0; i < java.lang.Math.min(result.length,5); i++) {


                            Number[] val = string2nbr(result[i]);
                            series_update(val, i);
                            Log.e(TAG,"series i "+ series[i]);
                            for (int j = 0; j < val.length; j++) {
                                values[j][i] = val[j].floatValue();


                                if (j == val.length-1)
                                    lastValue[i] = values[j][i];

                            }
                        }
                        plotUpdate();
                        if(getScreenOrientation()== Configuration.ORIENTATION_PORTRAIT) {
                            setTextFragments(0, getPowerAvg(values));
                            setTextFragments(1, lastValue);
                        }
                        if(result.length>5) {

                            Toast.makeText(getApplicationContext(), "Only the first 5 are plotted", Toast.LENGTH_LONG).show();


                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"False URL, please try other options", Toast.LENGTH_LONG).show();
                        kill_activity();
                    }


                }
            }.execute(url);
            handler.postDelayed(this, 60000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        setContentView(R.layout.second_activity);

        // Initialize the fake data generator
        mGeneratorExecutor = new ScheduledThreadPoolExecutor(1);

        // Start the Wear API connection
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        plot = (XYPlot)findViewById(R.id.TableRow);
        Bundle bundle = getIntent().getExtras();
        url = bundle.getString("url");
        ip=bundle.getString("ip");
        nbRack=bundle.getInt("nbR");
        nbServer=bundle.getIntArray("nbS");
        server=MainActivity.server;
        db=MainActivity.db;
        if(server==null)
            Log.e(TAG,"the server is null");
        Log.e(TAG,"URL FORMAT   "+url);
        //String [] urls = decode_list(url);

        if(getScreenOrientation()==Configuration.ORIENTATION_PORTRAIT)
            orientationPortrait = true;
        else
            orientationPortrait = false;

        OrientationEventListener mOrientationEventListener = new OrientationEventListener(
                this, SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                //checking if device was rotated
                if (orientationPortrait != isPortrait(orientation)) {
                    orientationPortrait = !orientationPortrait;
                    Log.e(TAG, "Device was rotated!");
                }
            }
        };
        mOrientationEventListener.enable();

        if(getScreenOrientation()== Configuration.ORIENTATION_PORTRAIT) {
            vpPager = (ViewPager) findViewById(R.id.viewpager);
            adapterViewPager = new SecondActivitySwipeAdapter(getSupportFragmentManager());
            vpPager.setAdapter(adapterViewPager);
        }
        if(url.contains("http")) {
            Log.e(TAG,"the url : "+url);
            urls = decode_list(url);
            nbServers = java.lang.Math.min(urls.length, 5);

            new GetJSON_val() {
                @Override
                protected void onPostExecute(String[] result) {

                    //updatePlot(result);
                    if (result != null) {

                        Float[][] values = new Float[MAX_SERVER][nbServers];
                        Float[] lastValue = new Float[MAX_SERVER];
                        for (int i=0;i<MAX_SERVER;i++)
                            lastValue[i]=(float)0;
                        //Log.e(TAG,"result length = "+result.length);
                        for (int i = 0; i < java.lang.Math.min(result.length, 5); i++) {

                            Number[] val = string2nbr(result[i]);
                            series_update(val, i);

                            for (int j = 0; j < val.length; j++) {
                                values[j][i] = val[j].floatValue();

                                if (j == val.length-1)
                                    lastValue[i] = values[j][i];
                            }
                        }
                        plotUpdate();
                        if(getScreenOrientation()== Configuration.ORIENTATION_PORTRAIT) {
                            setTextFragments(0, getPowerAvg(values));
                            setTextFragments(1, lastValue);
                        }
                        if (result.length > 5) {
                            Toast.makeText(getApplicationContext(), "Only the first 5 are plotted", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "False URL, please try other options", Toast.LENGTH_LONG).show();
                        kill_activity();
                    }

                }
            }.execute(url);
        }
        else {
            Toast.makeText(getApplicationContext(), "Please choose at least one server", Toast.LENGTH_LONG).show();
            kill_activity();
        }

        ToggleButton tog = (ToggleButton) findViewById(R.id.toggleButton);
        tog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    DatePickerFragment dialog = new DatePickerFragment();


                    dialog.show(getSupportFragmentManager(),"DatePickerFragment");

                }
                else {

                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }

            }
        });

        FloatingActionButton call2 = (FloatingActionButton) findViewById(R.id.call_fltbtn2);
        call2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences settings = getSharedPreferences("id",0);
                String phone = settings.getString("phone", "");

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String phone_nb = "tel:" + phone;
                callIntent.setData(Uri.parse(phone_nb));
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(SecondActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);
                    return;
                }
                startActivity(callIntent);

            }

        });


        handler.postDelayed(runnable, 60000);

    }

    public void NoData() {

        Toast.makeText(getApplicationContext(), "No data on that date", Toast.LENGTH_LONG).show();

        Intent intent = getIntent();
        kill_activity();
        startActivity(intent);
    }

    private int [] getRacks() {
        int i=0;
        int [] racks=new int[urls.length];
        //Log.e(TAG,"hello: "+racks.length);
        //for(int i=0;i<urls.length;i++) {

        String str = url;
        Log.e(TAG,"the url :" + str);
            Pattern pattern = Pattern.compile("/rack0(.*?)/s");
            Matcher matcher = pattern.matcher(str);
            while (matcher.find()) {
                //Log.e(TAG,"this is the string we found : "+matcher.group(1));
                racks[i]=Integer.parseInt(matcher.group(1))-1;
                i++;
            }

       // }
        return racks;
    }

    private int [] getServers() {
        int i=0;
        int [] servers=new int[nbServers];
        //for(int i=0;i<urls.length;i++) {

        String str = url;
        Pattern pattern = Pattern.compile("/s0(.*?)/");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            servers[i]=Integer.parseInt(matcher.group(1))-1;
            i++;
        }

        // }
        return servers;
    }

    private String urlAllInside(){
        String url="";
        for (int i=0; i< nbRack;i++)
            for (int j=0;j<nbServer[i];j++)
                url=url+urlCreate("",Integer.toString(i+1),Integer.toString(j+1),"Power")+"#end#";

        return url;
    }

    private String urlCreate(String textDC,String textR,String textS,String textCP) {
        //String baseUrl = "http://128.179.195.18:5002/";
        String baseUrl = "http://"+ip+":5002/";
        String strR="rack0"+textR.substring(textR.length() - 1);
        String strS="s0"+textS.substring(textS.length() - 1);
        if(textCP.contains("Power")) {
            return baseUrl + strR + "/" + strS + "/power/last5min";
        }else {
            String strCP = "cpu0" + textS.substring(textS.length() - 1);
            return baseUrl + strR + "/" + strS + "/" + strCP;

        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String notif = intent.getStringExtra("notif");
            Log.e("receiver", "Got message: " + notif);
            Toast.makeText(getBaseContext(), "Got message", Toast.LENGTH_SHORT).show();
            sendNotificationWear();
        }
    };

    private void sendNotificationWear(){

        if (mGoogleApiClient.isConnected()) {

            new SecondActivity.SendMessageTask(DataLayerCommons.START_ACTIVITY_PATH).execute();
            // Stop the fake data generator
            mDataItemGeneratorFuture.cancel(true);

            // Send the notification
            PutDataMapRequest dataMap = PutDataMapRequest.create(DataLayerCommons.NOTIFICATION_PATH);
            dataMap.getDataMap().putString(DataLayerCommons.NOTIFICATION_KEY, "WORK WEAR");
            dataMap.getDataMap().putLong("time", new Date().getTime());
            PutDataRequest request = dataMap.asPutDataRequest();
            request.setUrgent();
            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                            Log.v(TAG, "Sending notification was successful: " + dataItemResult.getStatus()
                                    .isSuccess());
                        }
                    });
        }else{
            Log.e(TAG,"No connection available");
        }

    }

    private class SendMessageTask extends AsyncTask<Void, Void, Void> {
        // Asynchronous background task to send a message through the Wear API
        private final String message;

        SendMessageTask(String message) {
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... args) {
            // doInBackground is the function executed when running the AsyncTask
            Collection<String> nodes = getNodes();
            Log.v(TAG, "Sending '" + message + "' to all " + nodes.size() + " connected nodes");
            for (String node : nodes) {
                sendMessage(message, node);
            }
            return null;
        }

        private void sendMessage(final String message, String node) {
            // Convenience function to send a message through the Wear API
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node, message, new byte[0]).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Failed to send message " + message + " with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        }
    }

    private boolean isPortrait(int orientation) {
        return (orientation >= (360 - 90) && orientation <= 360) || (orientation >= 0 && orientation <= 90);
    }


    public int getScreenOrientation()
    {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if(getOrient.getWidth()==getOrient.getHeight()){
            orientation = Configuration.ORIENTATION_SQUARE;
        } else{
            if(getOrient.getWidth() < getOrient.getHeight()){
                orientation = Configuration.ORIENTATION_PORTRAIT;
            }else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }


    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener, DialogInterface.OnCancelListener {

        //private String date="";
        //private boolean cancelled=false;

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

    }

    public void setDate(String date_new) {
        date = date_new;
    }

    public String getDate() {
        return date;
    }

    public static class TimePickerFragment extends DialogFragment
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

            int new_hour = hourOfDay;
            if(minute>=30){
                new_hour = hourOfDay+1;
            }

            h= Integer.toString(new_hour);
            min = "00";
            if(new_hour==24){
                h="00";
            }else if(new_hour<10){
                h="0"+Integer.toString(new_hour);

            }

            String time=h+min;

            String [] xlabels = obtainRange(Integer.parseInt(h),Integer.parseInt(min));

            Log.e(TAG,"The time : "+time);

            ((SecondActivity) getActivity()).setTime(time);


            int [] askedRack=((SecondActivity) getActivity()).getRacks();


            int [] askedServer=((SecondActivity) getActivity()).getServers();
            //(SecondActivity) getActivity()).getDate()+time
            String[] samples = ((SecondActivity) getActivity()).getSample("201801151515",askedRack,askedServer);

            boolean no_data=false;
            for(int k=0;k<samples.length;k++) {

                if(samples[k]!=null) {
                    ((SecondActivity) getActivity()).series_update(samples[k], k);
                }
                else {
                    no_data=true;
                }

            }

            if(no_data){
                ((SecondActivity) getActivity()).NoData();
            }

            ((SecondActivity) getActivity()).plotUpdate(xlabels);

        }

        private String [] obtainRange(int init_h,int init_min){

            String [] xlabels = new String[5];
            String min="";
            String h="";
            for(int i=0;i<5;i++) {
                //int min_new=init_min+5*i;
                int min_new=init_min-i;
                int h_new=init_h;
                /*
                if(min_new>=60) {
                    min_new=min_new % 60;
                    h_new=h_new+1;
                    if(h_new+1==24){
                        h_new=0;
                    }
                }*/
                if(min_new<0) {
                    min_new=60+min_new;
                    h_new=h_new-1;
                    if(h_new==-1){
                        h_new=23;
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
                xlabels[4-i]=h+":"+min;
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

    public String [] getSample(String time,int [] askedRack, int [] askedServer) {
        return server.getSampling(db, ip, time, askedRack, askedServer);
    }

    public void setTime(String time_new) {
        time = time_new;
    }

    void kill_activity(){
        finish();
    }

    String [] decode_list(String list) {

        String [] urls = list.split("#end#");
        return urls;
    }

    private Float[] getPowerAvg(Float[][] pwr){

        Float[] avgTable = new Float[MAX_SERVER];
        for(int i=0;i<MAX_SERVER;i++){
            avgTable[i]=(float) 0;
        }

        float sum;
        for (int i=0;i<nbServers;i++){
            sum=0;
            for(int j=0;j<5;j++){
                sum+= pwr[j][i];
            }
            avgTable[i]=sum/MAX_SERVER;
        }

        return avgTable;
    }


    private void setTextFragments(int position,Float[] vals){

        switch(position){

            case 0: FirstFragmentSwipe fragment1 = (FirstFragmentSwipe) adapterViewPager.getRegisteredFragment(position);
                    fragment1.setTextViewsAvg(vals);
                    fragment1.setVisibilityTextview(nbServers);
                    fragment1.setTextViewsTxt(legendCreateBottom(urls));
                    break;

            case 1: SecondFragmentSwipe fragment2 = (SecondFragmentSwipe) adapterViewPager.getRegisteredFragment(position);
                    fragment2.setTextViewsLastPow(vals);
                    fragment2.setVisibilityTextview(nbServers);
                    fragment2.setTextViewsLastPowTxt(legendCreateBottom(urls));
                    break;

            default : break;
        }

    }


    private String[] series = {"0;0;0;0;0;","0;0;0;0;0;","0;0;0;0;0;","0;0;0;0;0;","0;0;0;0;0;"};

    public void series_update(Number[] nbr,int id){
        String series_id="";
        for(int i=0;i<nbr.length;i++){
            series_id=series_id+Integer.toString(nbr[i].intValue())+";";
        }
        series[id]=series_id;
    }

    private void series_update(String nbr,int id){


        series[id]=nbr;
    }

    private Number[] string2nbr(String series_id){
        String [] vals = series_id.split(";");

        Number [] vals_nbr = new Number[vals.length];
        for(int i=0;i<vals.length;i++){
            vals_nbr[i]=Integer.parseInt(vals[i]);
        }
        return vals_nbr;
    }

    private String[] legendCreate(String [] urls){
        String [] legend = new String[urls.length];
        for(int i=0;i<urls.length;i++){
            String rack_nb ="";
            String srv_nb ="";
            Pattern pattern = Pattern.compile("rack(.*?)/");
            Matcher matcher = pattern.matcher(urls[i]);
            while(matcher.find()){
                rack_nb=matcher.group(1);
            }
            Pattern pattern2 = Pattern.compile("/s(.*?)/");
            Matcher matcher2 = pattern2.matcher(urls[i]);
            while(matcher2.find()){
                srv_nb=matcher2.group(1);
            }

            legend[i]="Rack"+rack_nb+"Srv"+srv_nb;

        }
        return legend;
    }

    private String[] legendCreateBottom(String [] urls){
        String [] legend = new String[MAX_SERVER];

        for(int i=0;i<nbServers;i++){
            String rack_nb ="";
            String srv_nb ="";
            Pattern pattern = Pattern.compile("rack(.*?)/");
            Matcher matcher = pattern.matcher(urls[i]);
            while(matcher.find()){
                rack_nb=matcher.group(1);
            }
            Pattern pattern2 = Pattern.compile("/s(.*?)/");
            Matcher matcher2 = pattern2.matcher(urls[i]);
            while(matcher2.find()){
                srv_nb=matcher2.group(1);
            }

            legend[i]="Rack "+rack_nb+" Server "+srv_nb;

        }

        return legend;
    }

    /* Enter an array of Number and it updates the plot accordingly the values entered */
    public void plotUpdate() {


        plot.clear();

        //series_update(series_new,id_series);
        final String[] xlabels = generateHourMinute();

        //Fix the divisions of the X-Axis
        plot.setDomainStep(StepMode.SUBDIVIDE, 5);
        // Fix the range of the Y-Axis
        plot.setRangeBoundaries(0,20,BoundaryMode.FIXED);
        plot.setRangeStep(StepMode.INCREMENT_BY_VAL,2);

        plot.setDomainLabel("Time");

        Log.e(TAG,Arrays.toString(xlabels));

        // create a couple arrays of y-values to plot:
        Number[] series1Numbers = string2nbr(series[0]);
        Number[] series2Numbers = string2nbr(series[1]);
        Number[] series3Numbers = string2nbr(series[2]);
        Number[] series4Numbers = string2nbr(series[3]);
        Number[] series5Numbers = string2nbr(series[4]);
        
        String [] urls = decode_list(url);

        String [] legend = legendCreate(urls);

        String legend1=legend[0];
        String legend2="";
        String legend3="";
        String legend4="";
        String legend5="";
        if(urls.length>=2)
            legend2=legend[1];
        if(urls.length>=3)
            legend3=legend[2];
        if(urls.length>=4)
            legend4=legend[3];
        if(urls.length>=5)
            legend5=legend[4];

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, legend1);

        XYSeries series2 = new SimpleXYSeries(
                Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, legend2);

        XYSeries series3 = new SimpleXYSeries(
                Arrays.asList(series3Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, legend3);

        XYSeries series4 = new SimpleXYSeries(
                Arrays.asList(series4Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, legend4);

        XYSeries series5 = new SimpleXYSeries(
                Arrays.asList(series5Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, legend5);

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(0xFF9B59B6, 0xFF9B59B6, Color.TRANSPARENT, null);

        LineAndPointFormatter series2Format = new LineAndPointFormatter(0xFFE67E22, 0xFFE67E22, Color.TRANSPARENT, null);

        LineAndPointFormatter series3Format = new LineAndPointFormatter(0xFF3498DB, 0xFF3498DB, Color.TRANSPARENT, null);

        LineAndPointFormatter series4Format = new LineAndPointFormatter(0xFF34495E, 0xFF34495E, Color.TRANSPARENT, null);

        LineAndPointFormatter series5Format = new LineAndPointFormatter(0xFFE74C3C, 0xFFE74C3C, Color.TRANSPARENT, null);

        series1Format.getLinePaint().setStrokeWidth(10);
        series2Format.getLinePaint().setStrokeWidth(10);
        series3Format.getLinePaint().setStrokeWidth(10);
        series4Format.getLinePaint().setStrokeWidth(10);
        series5Format.getLinePaint().setStrokeWidth(10);

        // add an "dash" effect to the series2 line:
        /*series2Format.getLinePaint().setPathEffect(new DashPathEffect(new float[] {

                // always use DP when specifying pixel sizes, to keep things consistent across devices:
                PixelUtils.dpToPix(20),
                PixelUtils.dpToPix(15)}, 0));*/

        plot.addSeries(series1, series1Format);
        if(urls.length>=2)
            plot.addSeries(series2, series2Format);
        if(urls.length>=3)
            plot.addSeries(series3, series3Format);
        if(urls.length>=4)
            plot.addSeries(series4, series4Format);
        if(urls.length>=5)
            plot.addSeries(series5, series5Format);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(Float.parseFloat(obj.toString()));
                return toAppendTo.append(xlabels[i]);
            }
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });

        plot.redraw();
    }


    private String[] generateHourMinute(){
        Calendar rightNow = Calendar.getInstance();
        int currentHour = rightNow.get(Calendar.HOUR_OF_DAY);
        int currentMinutes = rightNow.get(Calendar.MINUTE);
        String[] labels = new String[5];
        for(int i=0;i<5;i++){
            labels[4-i] = Integer.toString(currentHour) + ":" + Integer.toString(currentMinutes);
            if(currentMinutes<10){
                labels[4-i] = Integer.toString(currentHour) + ":" + "0"+Integer.toString(currentMinutes);
            }
            if(currentHour<10){
                labels[4-i] = "0"+Integer.toString(currentHour) + ":" +Integer.toString(currentMinutes);
            }
            if(currentHour<10 && currentMinutes<10){
                labels[4-i] = "0"+Integer.toString(currentHour) + ":" +"0"+Integer.toString(currentMinutes);
            }

            currentMinutes--;
            if(currentMinutes < 0){
                currentHour--;
                currentMinutes = 59;
                if(currentHour < 0){
                    currentHour = 23;
                }
            }
        }
        return labels;
    }

    public void plotUpdate(String [] x) {
        XYPlot plot = (XYPlot) findViewById(R.id.TableRow);
        plot.clear();

        final String [] xlabels=x;

        //series_update(series_new,id_series);
        //final String[] xlabels = generateHourMinute();

        //Fix the divisions of the X-Axis
        plot.setDomainStep(StepMode.SUBDIVIDE, 5);
        // Fix the range of the Y-Axis
        plot.setRangeBoundaries(0,20,BoundaryMode.FIXED);
        plot.setRangeStep(StepMode.INCREMENT_BY_VAL,2);



        Log.e(TAG,Arrays.toString(xlabels));

        // create a couple arrays of y-values to plot:
        Number[] series1Numbers = string2nbr(series[0]);
        Number[] series2Numbers = string2nbr(series[1]);
        Number[] series3Numbers = string2nbr(series[2]);
        Number[] series4Numbers = string2nbr(series[3]);
        Number[] series5Numbers = string2nbr(series[4]);

        String [] urls = decode_list(url);

        String [] legend = legendCreate(urls);

        String legend1=legend[0];
        String legend2="";
        String legend3="";
        String legend4="";
        String legend5="";
        if(urls.length>=2)
            legend2=legend[1];
        if(urls.length>=3)
            legend3=legend[2];
        if(urls.length>=4)
            legend4=legend[3];
        if(urls.length>=5)
            legend5=legend[4];



        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, legend1);

        XYSeries series2 = new SimpleXYSeries(
                Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, legend2);

        XYSeries series3 = new SimpleXYSeries(
                Arrays.asList(series3Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, legend3);

        XYSeries series4 = new SimpleXYSeries(
                Arrays.asList(series4Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, legend4);

        XYSeries series5 = new SimpleXYSeries(
                Arrays.asList(series5Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, legend5);

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(0xFF9B59B6, 0xFF9B59B6, Color.TRANSPARENT, null);

        LineAndPointFormatter series2Format = new LineAndPointFormatter(0xFFE67E22, 0xFFE67E22, Color.TRANSPARENT, null);

        LineAndPointFormatter series3Format = new LineAndPointFormatter(0xFF3498DB, 0xFF3498DB, Color.TRANSPARENT, null);

        LineAndPointFormatter series4Format = new LineAndPointFormatter(0xFF34495E, 0xFF34495E, Color.TRANSPARENT, null);

        LineAndPointFormatter series5Format = new LineAndPointFormatter(0xFFE74C3C, 0xFFE74C3C, Color.TRANSPARENT, null);

        series1Format.getLinePaint().setStrokeWidth(10);
        series2Format.getLinePaint().setStrokeWidth(10);
        series3Format.getLinePaint().setStrokeWidth(10);
        series4Format.getLinePaint().setStrokeWidth(10);
        series5Format.getLinePaint().setStrokeWidth(10);


        plot.addSeries(series1, series1Format);
        if(urls.length>=2)
            plot.addSeries(series2, series2Format);
        if(urls.length>=3)
            plot.addSeries(series3, series3Format);
        if(urls.length>=4)
            plot.addSeries(series4, series4Format);
        if(urls.length>=5)
            plot.addSeries(series5, series5Format);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(Float.parseFloat(obj.toString()));
                return toAppendTo.append(xlabels[i]);
            }
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });

        plot.redraw();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Whenever we resume the app, restart the fake data generator
        mDataItemGeneratorFuture = mGeneratorExecutor.scheduleWithFixedDelay(
                new DataItemGenerator(), 1, 3, TimeUnit.SECONDS);
        // If we are connected to the Wear API, force open the app
        if(mGoogleApiClient.isConnected()) {
            new SendMessageTask(DataLayerCommons.START_ACTIVITY_PATH).execute();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));


    }

    @Override
    public void onPause() {

        super.onPause();
        // User is leaving the app, stop the fake data generator
        mDataItemGeneratorFuture.cancel(true /* mayInterruptIfRunning */);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onStop() {
        // App is stopped, close the wear API connection
        if (!mResolvingError && (mGoogleApiClient != null) && (mGoogleApiClient.isConnected())) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        // Data on the Wear API channel has changed
        Log.v(TAG, "onDataChanged: " + dataEvents);

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.v(TAG, "DataItem Changed: " + event.getDataItem().toString() + "\n"
                        + DataMapItem.fromDataItem(event.getDataItem()).getDataMap());
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.v(TAG, "DataItem Deleted: " + event.getDataItem().toString());
            }
        }
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        // A message has been received from the Wear API
        Log.v(TAG, "onMessageReceived() A message from watch was received:"
                + messageEvent.getRequestId() + " " + messageEvent.getPath());
        Log.v(TAG, messageEvent.toString());
    }

    @Override
    public void onCapabilityChanged(final CapabilityInfo capabilityInfo) {
        // The Wear API has a changed ability
        Log.v(TAG, "onCapabilityChanged: " + capabilityInfo);
    }

    private int current = R.drawable.panels_swt;
    public void createAndSendBitmap() {
        // Alternate between the two given image to send through the Wear API
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), current);
        if (null != bitmap && mGoogleApiClient.isConnected()) {
            sendAsset(toAsset(bitmap));
        }
        current = (current == R.drawable.panels_swt ? R.drawable.rlc_grass : R.drawable.panels_swt);
    }

    private static Asset toAsset(Bitmap bitmap) {
        // Builds an Asset from a bitmap
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return Asset.createFromBytes(byteStream.toByteArray());
        } finally {
            if (null != byteStream) {
                try {
                    byteStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private void sendAsset(Asset asset) {
        // Sends an asset through the Wear API
        PutDataMapRequest dataMap = PutDataMapRequest.create(DataLayerCommons.IMAGE_PATH);
        dataMap.getDataMap().putAsset(DataLayerCommons.IMAGE_KEY, asset);
        dataMap.getDataMap().putLong("time", new Date().getTime());
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();

        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                        Log.v(TAG, "Sending image was successful: " + dataItemResult.getStatus()
                                .isSuccess());
                    }
                });
    }

    private Collection<String> getNodes() {
        // Lists all the nodes (devices) connected to the Wear API
        HashSet<String> results = new HashSet<>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

    private class DataItemGenerator implements Runnable {
        // Fake data generator to have things to send through the Wear API
        private int count = 0;
        @Override
        public void run() {
            // Send the image 50% of the time, otherwise send the counter's value
            /*if(Math.random() > .5) {
                createAndSendBitmap();
                return;
            }*/

            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(DataLayerCommons.COUNT_PATH);
            putDataMapRequest.getDataMap().putInt(DataLayerCommons.COUNT_KEY, count++);
            PutDataRequest request = putDataMapRequest.asPutDataRequest();
            request.setUrgent();

            Log.v(TAG, "Generating DataItem ('count'=" + count + ") " + request);
            if (!mGoogleApiClient.isConnected()) {
                return;
            }
            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                            if (!dataItemResult.getStatus().isSuccess()) {
                                Log.e(TAG, "ERROR: failed to putDataItem, status code: "
                                        + dataItemResult.getStatus().getStatusCode());
                            }
                        }
                    });
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Connection to the wear API
        Log.v(TAG, "Google API Client was connected");
        mResolvingError = false;
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.CapabilityApi.addListener(
                mGoogleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // Connection to the wear API is halted
        Log.v(TAG, "Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Connection to the wear API failed, try to restore it
        if (!mResolvingError) {
            if (result.hasResolution()) {
                try {
                    mResolvingError = true;
                    result.startResolutionForResult(this, 0);
                } catch (IntentSender.SendIntentException e) {
                    // There was an error with the resolution intent. Try again.
                    mGoogleApiClient.connect();
                }
            } else {
                Log.e(TAG, "Connection to Google API client has failed");
                mResolvingError = false;
                Wearable.DataApi.removeListener(mGoogleApiClient, this);
                Wearable.MessageApi.removeListener(mGoogleApiClient, this);
                Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);
            }
        }
    }
}

