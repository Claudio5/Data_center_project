package ch.epfl.esl.blankphonewearapp;

/**
 * Created by Claudio on 05.12.2017.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import ch.epfl.esl.blankphonewearapp.XYplotSeriesList;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.esl.commons.DataLayerCommons;

import static java.lang.Integer.max;


public class SecondActivity extends Activity implements
        CapabilityApi.CapabilityListener,
        MessageApi.MessageListener,
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Tag for Logcat
    private static final String TAG = "SecondActivity";

    // Members used for the Wear API
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    // Fake data generator to send to the Wear device
    private ScheduledExecutorService mGeneratorExecutor;
    private ScheduledFuture<?> mDataItemGeneratorFuture;

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 12345;

    private String url;
    private Handler handler = new Handler();

    // This will be called every minute
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            Log.e(TAG,"Every 60 seconds");
            /*
            new GetRacks(){
                @Override
                protected void onPostExecute(String[] result) {
                    if(result!=null) {
                        Float[] valF = new Float[result.length];
                        Number[] val = new Number[result.length];
                        for (int i = 0; i < result.length; i++) {
                            int value = Integer.parseInt(result[i]);
                            val[i] = value;
                            valF[i] = (float) value;
                        }
                        plotUpdate(val);
                        setPowerAvgTxtView(valF);
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"URL is obsolete, please start again", Toast.LENGTH_LONG).show();
                    }

                }
            }.execute(url);
            */
            String [] urls = decode_list(url);
            //for(int i=0;i<urls.length;i++){
            //    new GetJSON_Val().execute(urls[i]+Integer.toString(i));
            //}
            //plotUpdate();
            new GetJSON_val(){
                @Override
                protected void onPostExecute(String[] result) {

                    //updatePlot(result);

                    if(result!=null) {
                        Float[] valF = new Float[string2nbr(result[0]).length];
                        //Log.e(TAG,"result length = "+result.length);
                        for (int i = 0; i < java.lang.Math.min(result.length,5); i++) {


                            Number[] val = string2nbr(result[i]);
                            series_update(val, i);
                            Log.e(TAG,"series i "+ series[i]);
                            for (int j = 0; j < val.length; j++) {
                                int value = val[j].intValue();


                                //val[j] = value;
                                if (i == 0)
                                    valF[j] = (float) value;
                            }
                        }
                        plotUpdate();
                        setPowerAvgTxtView(valF);
                        if(result.length>5) {

                            Toast.makeText(getApplicationContext(), "Only the first 5 are plotted", Toast.LENGTH_LONG).show();
                            //Number[] val = new Number[result.length];

                            //series=result;
                            //series_update(val,id);

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

        Bundle bundle = getIntent().getExtras();
        url = bundle.getString("url");
        String [] urls = decode_list(url);
        Log.e(TAG,"the url : "+url);


        //for(int i=0;i<urls.length;i++){
        //    new GetJSON_Val().execute(urls[i]+Integer.toString(i));
        //}
        //plotUpdate();
        new GetJSON_val(){
            @Override
            protected void onPostExecute(String[] result) {

                //updatePlot(result);

                if(result!=null) {
                    Float[] valF = new Float[string2nbr(result[0]).length];
                    //Log.e(TAG,"result length = "+result.length);
                    for (int i = 0; i < java.lang.Math.min(result.length,5); i++) {


                        Number[] val = string2nbr(result[i]);
                        series_update(val, i);
                        Log.e(TAG,"series i "+ series[i]);
                        for (int j = 0; j < val.length; j++) {
                            int value = val[j].intValue();


                            //val[j] = value;
                            if (i == 0)
                                valF[j] = (float) value;
                        }
                    }
                    plotUpdate();
                    setPowerAvgTxtView(valF);
                    if(result.length>5) {

                        Toast.makeText(getApplicationContext(), "Only the first 5 are plotted", Toast.LENGTH_LONG).show();
                        //Number[] val = new Number[result.length];

                        //series=result;
                        //series_update(val,id);

                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),"False URL, please try other options", Toast.LENGTH_LONG).show();
                    kill_activity();
                }


            }
        }.execute(url);


        /*
        new GetRacks(){
            @Override
            protected void onPostExecute(String[] result) {
                if(result!=null) {
                    Float[] valF = new Float[result.length];
                    Number[] val = new Number[result.length];
                    for (int i = 0; i < result.length; i++) {
                        int value = Integer.parseInt(result[i]);
                        val[i] = value;
                        valF[i] = (float) value;
                    }
                    plotUpdate(val);
                    setPowerAvgTxtView(valF);
                }
                else {
                    Toast.makeText(getApplicationContext(),"False URL, please try other options", Toast.LENGTH_LONG).show();
                    kill_activity();
                }

            }
        }.execute(urls[0]);
        */


        FloatingActionButton call2 = (FloatingActionButton) findViewById(R.id.call_fltbtn2);
        call2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences settings = getSharedPreferences("id",0);
                String phone = settings.getString("phone", "");

                //String phone = edit.getText().toString();
                //Log.e(TAG,"phone number"+phone);
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

        //Number[] series1Numbers = {1, 4, 2, 8, 4};

        //plotUpdate(series1Numbers);

        // Update the xyPlot every minute
        handler.postDelayed(runnable, 60000);

    }

    /*
    private class GetJSON_Val extends AsyncTask<String, Void, String[]> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }


        @Override
        protected String[] doInBackground(String... url) {
            urlHandler sh = new urlHandler();
            String [] urls = decode_list(url[0]);

            String [] powerArray= new String[urls.length];
            for(int i=0;i<urls.length;i++){
                String jsonStr = sh.getjsonstring(urls[i]);

                //Log.e(TAG, "Response from url: " + jsonStr);

                if(jsonStr!=null) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonStr);

                        //for (int i = 0; i < jsonObject.length(); i++) {



                        Iterator<String> iterator = jsonObject.keys();
                        String name = iterator.next();

                        JSONArray racks = jsonObject.getJSONArray(name);
                        String power="";

                        for (int j = 0; j < racks.length(); j++) {
                            power=power+racks.getString(j)+";";
                            //powerArray[j]=racks.getString(j);
                            //Log.e(TAG, "power: " + racks.getString(j));
                        }

                        //String racks = jsonObject.getString("racks");





                        powerArray[i]=power;
                        //return powerArray;
                    }
                    catch (final JSONException e) {
                        Log.e(TAG, "Json parsing error: " + e.getMessage());



                    }



                }
                else {
                    Log.e(TAG, "No response");
                }

            }

            return powerArray;


            //return null;
        }

        @Override
        protected void onPostExecute(String[] result) {

            //updatePlot(result);

            if(result!=null) {
                Float[] valF = new Float[string2nbr(result[0]).length];
                //Log.e(TAG,"result length = "+result.length);
                for (int i = 0; i < java.lang.Math.min(result.length,5); i++) {


                    Number[] val = string2nbr(result[i]);
                    series_update(val, i);
                    Log.e(TAG,"series i "+ series[i]);
                    for (int j = 0; j < val.length; j++) {
                        int value = val[j].intValue();


                        //val[j] = value;
                        if (i == 0)
                            valF[j] = (float) value;
                    }
                }
                plotUpdate();
                setPowerAvgTxtView(valF);
                if(result.length>5) {

                    Toast.makeText(getApplicationContext(), "Only the first 5 are plotted", Toast.LENGTH_LONG).show();
                    //Number[] val = new Number[result.length];

                    //series=result;
                    //series_update(val,id);

                }
            }
            else {
                Toast.makeText(getApplicationContext(),"False URL, please try other options", Toast.LENGTH_LONG).show();
                kill_activity();
            }


        }

    }
    */


    private void kill_activity(){
        finish();
    }

    private String [] decode_list(String list) {

        String [] urls = list.split("#end#");
        return urls;
    }



    // Calculates the average power of the 5 last values
    private void setPowerAvgTxtView(Float[] pwr){
        TextView pwrAvgView = (TextView)findViewById(R.id.pwrAvgNmbview);
        float sum=0;
        float avg=0;
        for (int i=0;i<pwr.length;i++){
            sum += pwr[i];
        }
        avg=sum/pwr.length;
        pwrAvgView.setText(Float.toString(avg));
    }

    private Number[] generateData(){
        Number[] series1Numbers = new Number[5];

        for (int i=0;i<5;i++){
            int randomNum = ThreadLocalRandom.current().nextInt(1, 9 + 1);
            series1Numbers[i]=randomNum;
        }
        return series1Numbers;
    }


    private String[] series = {"0;0;0;0;0;","0;0;0;0;0;","0;0;0;0;0;","0;0;0;0;0;","0;0;0;0;0;"};

    private void series_update(Number[] nbr,int id){
        String series_id="";
        for(int i=0;i<nbr.length;i++){
            series_id=series_id+Integer.toString(nbr[i].intValue())+";";
        }
        series[id]=series_id;
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

    /* Enter an array of Number and it updates the plot accordingly the values entered */
    public void plotUpdate() {
        XYPlot plot = (XYPlot) findViewById(R.id.plotxy);
        plot.clear();


        //series_update(series_new,id_series);
        final String[] xlabels = generateHourMinute();

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
        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.RED, Color.RED, Color.TRANSPARENT, null);

        LineAndPointFormatter series2Format = new LineAndPointFormatter(Color.GREEN, Color.GREEN, Color.TRANSPARENT, null);

        LineAndPointFormatter series3Format = new LineAndPointFormatter(Color.BLUE, Color.BLUE, Color.TRANSPARENT, null);

        LineAndPointFormatter series4Format = new LineAndPointFormatter(Color.YELLOW, Color.YELLOW, Color.TRANSPARENT, null);

        LineAndPointFormatter series5Format = new LineAndPointFormatter(Color.WHITE, Color.WHITE, Color.TRANSPARENT, null);

        // add an "dash" effect to the series2 line:
        series2Format.getLinePaint().setPathEffect(new DashPathEffect(new float[] {

                // always use DP when specifying pixel sizes, to keep things consistent across devices:
                PixelUtils.dpToPix(20),
                PixelUtils.dpToPix(15)}, 0));

        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        //series1Format.setInterpolationParams(
        //        new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        //series2Format.setInterpolationParams(
        //        new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:



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

    }

    @Override
    public void onPause() {

        super.onPause();
        // User is leaving the app, stop the fake data generator
        mDataItemGeneratorFuture.cancel(true /* mayInterruptIfRunning */);
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
            if(Math.random() > .5) {
                createAndSendBitmap();
                return;
            }

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

