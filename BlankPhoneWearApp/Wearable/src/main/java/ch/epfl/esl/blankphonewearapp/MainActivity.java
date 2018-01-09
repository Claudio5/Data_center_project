package ch.epfl.esl.blankphonewearapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    public static final String NOTIFICATION_RECEIVED = "NOTIFICATION_RECEIVED";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Register to receive messages from the service handling the Wear API connection
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named as IMAGE_DECODED
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(NOTIFICATION_RECEIVED));


        FloatingActionButton callButton = findViewById(R.id.callButton);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:123456789"));
                startActivity(callIntent);
            }
        });


    }

    @Override
    protected void onPause() {
        // Unregister since the activity is about to be suspended
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }


    public void sendNotification(String text, String title){
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager mNotifyMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle(title)
                .setContentText(text)
                .setSound(alarmSound)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        mBuilder.setLights(Color.BLUE, 500, 500);
        mNotifyMgr.notify(12345, mBuilder.build());

    }



    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the image, display it and fade out
            Log.d(TAG, "Got message!");
            String notif = intent.getStringExtra(NOTIFICATION_RECEIVED);
            TextView warnView = findViewById(R.id.warningView);
            warnView.setText(notif);
            sendNotification(notif,"HELLO");
        }
    };
}