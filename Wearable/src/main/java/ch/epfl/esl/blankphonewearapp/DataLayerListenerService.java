package ch.epfl.esl.blankphonewearapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.SyncStateContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
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
import static com.google.android.gms.wearable.PutDataRequest.WEAR_URI_SCHEME;

public class DataLayerListenerService extends WearableListenerService {

    // Tag for Logcat
    private static final String TAG = "DataLayerService";
    private int notificationId = 001;
    private String notif;

    // Member for the Wear API handle
    GoogleApiClient mGoogleApiClient;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            String action = intent.getAction();
            if (DataLayerCommons.ACTION_DISMISS.equals(action)) {
                dismissNotification();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Start the Wear API connection
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    //@Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.v(TAG, "onDataChanged: " + dataEvents);
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {

                Log.e(TAG, "DataItem Changed: " + event.getDataItem().toString() + "\n"
                        + DataMapItem.fromDataItem(event.getDataItem()).getDataMap());

                String path = event.getDataItem().getUri().getPath();

                switch (path) {
                    case DataLayerCommons.NOTIFICATION_PATH:
                        Log.v(TAG, "Data Changed for NOTIF_PATH: " + event.getDataItem().toString());
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        notif = dataMapItem.getDataMap().getString(DataLayerCommons.NOTIFICATION_KEY);
                        Intent intent = new Intent(NOTIFICATION_RECEIVED);
                        intent.putExtra(NOTIFICATION_RECEIVED, notif);
                        notif = simplifyNotif(notif);
                        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                        sendNotification("Data Center Control",notif);
                        MyApp.mainActivity.setWarningTextView(notif);


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

    private String simplifyNotif(String notif){
        String numbers = notif.replaceAll("\\D+","");
        String tmp="Alert \n";
        if(!notif.equals("Warning sent by user")) {
            for (int i = 0; i < numbers.length(); i += 4) {
                tmp += "Rack" + numbers.charAt(i) + numbers.charAt(i + 1) + " " + "Server" + numbers.charAt(i + 2) + numbers.charAt(i + 3) + "\n";
            }
            notif = tmp;
        }

        return notif;
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

    private void sendNotification(String title, String content) {

        // this intent will open the activity when the user taps the "open" action on the notification
        Intent viewIntent = new Intent(this, MainActivity.class);
        viewIntent.putExtra("warning",content);
        PendingIntent pendingViewIntent = PendingIntent.getActivity(this, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // this intent will be sent when the user swipes the notification to dismiss it
        Intent dismissIntent = new Intent(DataLayerCommons.ACTION_DISMISS);
        PendingIntent pendingDeleteIntent = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(content)
                .setDeleteIntent(pendingDeleteIntent)
                .setContentIntent(pendingViewIntent);

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] vibrationPattern = {0, 500, 50, 300};
        //-1 - don't repeat
        final int indexInPatternToRepeat = -1;
        vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);

        Notification notification = builder.build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId++, notification);
    }


    private void dismissNotification() {
        new DismissNotificationCommand(this).execute();
    }


    private class DismissNotificationCommand implements GoogleApiClient.ConnectionCallbacks, ResultCallback<DataApi.DeleteDataItemsResult>, GoogleApiClient.OnConnectionFailedListener {

        private static final String TAG = "DismissNotification";

        private final GoogleApiClient mGoogleApiClient;

        public DismissNotificationCommand(Context context) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        public void execute() {
            mGoogleApiClient.connect();
        }

        @Override
        public void onConnected(Bundle bundle) {
            final Uri dataItemUri =
                    new Uri.Builder().scheme(WEAR_URI_SCHEME).path(DataLayerCommons.NOTIFICATION_PATH).build();
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Deleting Uri: " + dataItemUri.toString());
            }
            Wearable.DataApi.deleteDataItems(
                    mGoogleApiClient, dataItemUri).setResultCallback(this);
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d(TAG, "onConnectionSuspended");
        }

        @Override
        public void onResult(DataApi.DeleteDataItemsResult deleteDataItemsResult) {
            if (!deleteDataItemsResult.getStatus().isSuccess()) {
                Log.e(TAG, "dismissWearableNotification(): failed to delete DataItem");
            }
            mGoogleApiClient.disconnect();
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d(TAG, "onConnectionFailed");
        }
    }
}