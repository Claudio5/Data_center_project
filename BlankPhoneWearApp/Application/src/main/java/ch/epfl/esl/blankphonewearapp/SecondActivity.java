package ch.epfl.esl.blankphonewearapp;

/**
 * Created by Claudio on 05.12.2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
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
import android.util.Log;
import ch.epfl.esl.blankphonewearapp.XYplotSeriesList;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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

import ch.epfl.esl.commons.DataLayerCommons;


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

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            Number[] serie =generateData();
            Log.e(TAG,Arrays.toString(serie));
            plotUpdate(serie);
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
        String url = bundle.getString("url");

        Log.e(TAG,url);

        //new SecondActivity.GetRacks().execute(url);

        Number[] series1Numbers = {1, 4, 2, 8, 4};

        plotUpdate(series1Numbers);

        handler.postDelayed(runnable, 60000);



    }


    /* Class that gets the actual data of the racks and update the plot accordingly
     (needs to be called with GetRacks.execute(String url))*/
    private class GetRacks extends AsyncTask<String, Void, String[]> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(String... url) {
            urlHandler sh = new urlHandler();

            String jsonStr = sh.getjsonstring(url[0]);

            Log.e(TAG, "Response from url: " + jsonStr);

            if(jsonStr!=null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);

                    //for (int i = 0; i < jsonObject.length(); i++) {
                    Iterator<String> iterator = jsonObject.keys();
                    String name = iterator.next();

                    JSONArray racks = jsonObject.getJSONArray(name);
                    String [] powerArray= new String[racks.length()];
                    for (int j = 0; j < racks.length(); j++) {
                        powerArray[j]=racks.getString(j);
                        Log.e(TAG, "power: " + racks.getString(j));
                    }
                    //String racks = jsonObject.getString("racks");


                    //}

                    return powerArray;
                }
                catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());

                }
            }
            else {
                Log.e(TAG, "No response");
            }


            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {

            Number[] val = new Number[result.length];
            for(int i=0;i<result.length;i++) {
                int value = Integer.parseInt(result[i]);
                val[i]=value;
            }
            plotUpdate(val);

        }

    }

    private Number[] generateData(){
        Number[] series1Numbers = {0, 0, 0, 0, 0};

        for (int i=0;i<5;i++){
            int randomNum = ThreadLocalRandom.current().nextInt(1, 9 + 1);
            series1Numbers[i]=randomNum;
        }
        return series1Numbers;
    }

    /* Enter an array of Number and it updates the plot accordingly the values entered */
    public void plotUpdate(Number[] series1Numbers) {
        XYPlot plot = (XYPlot) findViewById(R.id.plotxy);
        plot.clear();

        Calendar rightNow = Calendar.getInstance();
        int currentHour = rightNow.get(Calendar.HOUR_OF_DAY);
        int currentMinutes = rightNow.get(Calendar.MINUTE);
        
        final String[] labels = new String[5];

        for(int i=0;i<5;i++){

            labels[4-i] = Integer.toString(currentHour) + ":" + Integer.toString(currentMinutes);
            currentMinutes--;
            if(currentMinutes < 0){
                currentHour--;
                currentMinutes = 59;
            }
        }

        //Fix the divisions of the X-Axis
        plot.setDomainStep(StepMode.SUBDIVIDE, 5);
        // Fix the range of the Y-Axis
        plot.setRangeBoundaries(0,20,BoundaryMode.FIXED);
        plot.setRangeStep(StepMode.INCREMENT_BY_VAL,2);


        Log.e(TAG,Arrays.toString(labels));

        // create a couple arrays of y-values to plot:
        Number[] series2Numbers = {5, 2, 10, 5, 20};


        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");

        XYSeries series2 = new SimpleXYSeries(
                Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.RED, Color.RED, Color.TRANSPARENT, null);

        LineAndPointFormatter series2Format = new LineAndPointFormatter(Color.GREEN, Color.GREEN, Color.TRANSPARENT, null);

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
        plot.addSeries(series2, series2Format);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                //int i = Math.round(((Number) obj).floatValue());
                int i = Math.round(Float.parseFloat(obj.toString()));
                return toAppendTo.append(labels[i]);
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

