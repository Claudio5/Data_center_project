package ch.epfl.esl.blankphonewearapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import ch.epfl.esl.commons.DataLayerCommons;

import static ch.epfl.esl.blankphonewearapp.MainActivity.NOTIFICATION_RECEIVED;

public class DataLayerListenerService extends WearableListenerService {

    // Tag for Logcat
    private static final String TAG = "DataLayerService";

    // Member for the Wear API handle
    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        // Start the Wear API connection
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.v(TAG, "onDataChanged: " + dataEvents);
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.v(TAG, "DataItem Changed: " + event.getDataItem().toString() + "\n"
                        + DataMapItem.fromDataItem(event.getDataItem()).getDataMap());

                String path = event.getDataItem().getUri().getPath();
                switch (path) {
                    case DataLayerCommons.NOTIF_PATH:
                        Log.v(TAG, "Data Changed for NOTIF_PATH: " + event.getDataItem().toString());
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        String notif = dataMapItem.getDataMap().getString(DataLayerCommons.NOTIF_KEY);
                        Intent intent = new Intent(NOTIFICATION_RECEIVED);
                        intent.putExtra(NOTIFICATION_RECEIVED,notif);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                        break;
                    case DataLayerCommons.COUNT_PATH:
                        Log.v(TAG, "Data Changed for COUNT_PATH: " + event.getDataItem() + "\n"
                                + "Count data = " + DataMapItem.fromDataItem(event.getDataItem())
                                .getDataMap().getInt(DataLayerCommons.COUNT_KEY));
                        break;
                    default:
                        Log.v(TAG, "Data Changed for unrecognized path: " + path);
                        break;
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.v(TAG, "DataItem Deleted: " + event.getDataItem().toString());
            }

            // For demo, send a message back to the node that created the data item
            Uri uri = event.getDataItem().getUri();
            String path = uri.getPath();
            if (path.equals(DataLayerCommons.COUNT_PATH)) {
                String nodeId = uri.getHost();
                byte[] payload = uri.toString().getBytes();
                Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId,
                        DataLayerCommons.DATA_ITEM_RECEIVED_PATH, payload);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // A message has been received from the Wear API
        Log.v(TAG, "onMessageReceived: " + messageEvent);

        // Check to see if the message is to start an activity
        if (messageEvent.getPath().equals(DataLayerCommons.START_ACTIVITY_PATH)) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
    }

}