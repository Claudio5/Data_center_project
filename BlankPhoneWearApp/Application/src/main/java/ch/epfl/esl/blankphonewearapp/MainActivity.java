package ch.epfl.esl.blankphonewearapp;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.epfl.esl.commons.DataLayerCommons;
import ch.epfl.esl.blankphonewearapp.rackModel;
import ch.epfl.esl.blankphonewearapp.serverItem;

public class MainActivity extends Activity implements
        CapabilityApi.CapabilityListener,
        MessageApi.MessageListener,
        DataApi.DataListener,
        ConnectionCallbacks,
        OnConnectionFailedListener {

    // Tag for Logcat
    private static final String TAG = "MainActivity";

    //Service
    //private MyService service = new MyService();

    // Members used for the Wear API
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    // Fake data generator to send to the Wear device
    private ScheduledExecutorService mGeneratorExecutor;
    private ScheduledFuture<?> mDataItemGeneratorFuture;

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1234;
    private String ip = "128.179.198.237";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        setContentView(R.layout.main_activity);

        // Initialize the fake data generator
        mGeneratorExecutor = new ScheduledThreadPoolExecutor(1);

        // Start the Wear API connection
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

       // buildSpinner();
       // buildScroll();
        new GetJSON_Param().execute("http://"+ip+":5002");
        //Log.e(TAG,"nbracks "+nbRack);

        SharedPreferences settings = getSharedPreferences("id",0);
        String phone_nb = settings.getString("phone", "");
        EditText text_phone = (EditText) findViewById(R.id.hotline_nb);
        text_phone.setText("Phone : "+phone_nb);



        FloatingActionButton launchActivityTwoButton = (FloatingActionButton) findViewById(R.id.send_fltbtn);
        launchActivityTwoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
/*
                Spinner spinnerDC=(Spinner) findViewById(R.id.spinnerDC);
                String textDC = spinnerDC.getSelectedItem().toString();

                Spinner spinnerR=(Spinner) findViewById(R.id.spinnerR);
                String textR = spinnerR.getSelectedItem().toString();

                Spinner spinnerS=(Spinner) findViewById(R.id.spinnerS);
                String textS = spinnerS.getSelectedItem().toString();

                Spinner spinnerCP=(Spinner) findViewById(R.id.spinnerCP);
                String textCP = spinnerCP.getSelectedItem().toString();

                String url= urlCreate(textDC,textR,textS,textCP);
*/
                //Log.e(TAG,"url : "+url);
                String list = listsSelectedServer();
                //Log.e(TAG,list);
                //String url="";
                Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
                intent.putExtra("url",list);
                startActivity(intent);

            }
        });

        FloatingActionButton launchWEB = (FloatingActionButton) findViewById(R.id.add_button);
        launchWEB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                listsSelectedServer();
                new GetRacks().execute("http:/"+ip+":5002/rack01/s01/power/last5min");
            }
        });

        FloatingActionButton save_phone = (FloatingActionButton) findViewById(R.id.save_nb);
        save_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String url = "http://10.0.2.2:5002/racks";
                //String url = "http://0.0.0.0:5002/racks";


                //Context context = getApplicationContext();
                EditText text = (EditText) findViewById(R.id.hotline_nb);
                String phone_number = text.getText().toString();
                SharedPreferences sharedPref = getSharedPreferences("id",0);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("phone", phone_number);
                editor.commit();


            }

        });

        FloatingActionButton call = (FloatingActionButton) findViewById(R.id.call_fltbtn);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences settings = getSharedPreferences("id",0);
                String phone = settings.getString("phone", "");
                EditText edit = (EditText) findViewById(R.id.hotline_nb);
                if(phone==null){
                    //Log.e(TAG,"number null");
                    phone=edit.getText().toString();
                }
                //String phone = edit.getText().toString();

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String phone_nb = "tel:" + phone;
                callIntent.setData(Uri.parse(phone_nb));
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);
                    return;
                }
                startActivity(callIntent);

            }

        });



        //Intent servInt = new Intent(this,MyService.class);
        Intent servInt = new Intent(this,MyService.class);
        servInt.putExtra("url","http://"+ip+":5002/rack01/s01/cpu01");
        startService(servInt);

    }

    private String listsSelectedServer(){
        String url="";
        for (int i=0; i< nbRack;i++)
            for (int j=0;j<nbServer[i];j++){
                if(rackDataList.get(i).getAllItemsInSection().get(j).getSelect()) {
                    Log.v(TAG, "Rack: "+ i +" Server: "+j);
                    url=url+urlCreate("",Integer.toString(i+1),Integer.toString(j+1),"Power")+"#end#";
                }


            }

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

    private void updatePlot(String[] array) {

        Log.e(TAG,array[0]);

        Number[] val = new Number[array.length];
        for(int i=0;i<array.length;i++) {
            int value = Integer.parseInt(array[i]);
            val[i]=value;
        }
        Number[] series2Numbers = {5, 2, 10, 5, 20};

    }

    private LinearLayout myLayout;
    private LinearLayout myLayout2;

    // this is an array that holds the IDs of the drawables ...
    private int[] images ;

    private View serverCell;
    private View rackCell;
    private int nbServer[]={3,1,5,15};
    private int nbRack=4;
    private int nbCPU;
    private TextView text;

    ArrayList<rackModel> rackDataList;

    private class GetJSON_Param extends AsyncTask<String, Void, String[]> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }


        @Override
        protected String[] doInBackground(String... url) {
            urlHandler sh = new urlHandler();

            String jsonStr = sh.getjsonstring(url[0]+"/racks");

            //Log.e(TAG, "Response from url: " + jsonStr);

            if(jsonStr!=null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);

                    //for (int i = 0; i < jsonObject.length(); i++) {

                    JSONArray racks = jsonObject.getJSONArray("racks");
                    String [] nb_servers= new String[racks.length()];
                    for (int j = 0; j < racks.length(); j++) {
                        String url_srv = url[0]+"/rack0"+Integer.toString(j+1)+"/servers";
                        urlHandler servers_h = new urlHandler();
                        String jsonStr_srv = servers_h.getjsonstring(url_srv);

                        if(jsonStr_srv!=null){
                            try{
                                JSONObject jsonObject_srv = new JSONObject(jsonStr_srv);
                                JSONArray servers = jsonObject_srv.getJSONArray("servers");

                                nb_servers[j]= Integer.toString(servers.length());
                            }
                            catch(final JSONException e) {
                                Log.e(TAG, "Json parsing error2: " + e.getMessage());
                            }
                        }

                        //Log.e(TAG, "power: " + racks.getString(j));
                    }
                    //String racks = jsonObject.getString("racks");



                    //}


                    return nb_servers;
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

            //updatePlot(result);

            nbRack=result.length;
            Log.e(TAG,"this is what I get : "+nbRack);
            for(int i=0;i<nbRack;i++){
                nbServer[i]=Integer.parseInt(result[i]);
            }

            buildServerScroll();

        }

    }

    private void buildServerScroll(){

        serverDataFill();

        RecyclerView my_recycler_view = (RecyclerView) findViewById(R.id.my_recycler_view);
        my_recycler_view.setHasFixedSize(true);

        rackModelAdapter adapter = new rackModelAdapter(this, rackDataList);

        my_recycler_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        my_recycler_view.setAdapter(adapter);
    }

    private void serverDataFill(){
        rackDataList = new ArrayList<rackModel>();

        for (int i = 0; i < nbRack; i++) {

            rackModel data = new rackModel();

            data.setHeaderTitle("Rack " + i);

            ArrayList<serverItem> singleItem = new ArrayList<serverItem>();
            for (int j = 0; j < nbServer[i]; j++) {
                singleItem.add(new serverItem("Server "+j,nbCPU,"Rack "+(i+1) ));
            }

            data.setServers(singleItem);

            rackDataList.add(data);
        }
    }


    // Build the main spinner with the names for the data centers
   /* private void buildSpinner(){

        //String dataCenters[]= {"test1","test2"};

        //Spinner spinner = findViewById(R.id.spinnerDC);

        // Create an ArrayAdapter using the string array and a default spinner layout with strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.dataCenter_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        Spinner spinnerR = findViewById(R.id.spinnerR);

        // Create an ArrayAdapter using the string array and a default spinner layout with strings.xml
        ArrayAdapter<CharSequence> adapterR = ArrayAdapter.createFromResource(this,
                R.array.Racks_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        //adapterR.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerR.setAdapter(adapterR);

        Spinner spinnerS = findViewById(R.id.spinnerS);

        // Create an ArrayAdapter using the string array and a default spinner layout with strings.xml
        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.Servers_array, android.R.layout.simple_spinner_item);

        // Optional idea
        // ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, dataCenters);

        // Specify the layout to use when the list of choices appears
        //adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerS.setAdapter(adapterS);

        Spinner spinnerCP = findViewById(R.id.spinnerCP);

        // Create an ArrayAdapter using the string array and a default spinner layout with strings.xml
        ArrayAdapter<CharSequence> adapterCP = ArrayAdapter.createFromResource(this,
                R.array.CP_array, android.R.layout.simple_spinner_item);

        // Optional idea
        // ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, dataCenters);

        // Specify the layout to use when the list of choices appears
        //adapterCP.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerCP.setAdapter(adapterCP);
    }
*/
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
        SharedPreferences settings = getSharedPreferences("id",0);
        String phone_nb = settings.getString("phone", "");
        EditText text_phone = (EditText) findViewById(R.id.hotline_nb);
        text_phone.setText("Phone : "+phone_nb);
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
                .setResultCallback(new ResultCallback<DataItemResult>() {
                    @Override
                    public void onResult(@NonNull DataItemResult dataItemResult) {
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
                    new ResultCallback<SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull SendMessageResult sendMessageResult) {
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
                    .setResultCallback(new ResultCallback<DataItemResult>() {
                        @Override
                        public void onResult(@NonNull DataItemResult dataItemResult) {
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