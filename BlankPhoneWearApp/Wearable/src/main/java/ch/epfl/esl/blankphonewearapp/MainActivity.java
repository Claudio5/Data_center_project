package ch.epfl.esl.blankphonewearapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

import ch.epfl.esl.commons.DataLayerCommons;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    public static final String NOTIFICATION_RECEIVED = "NOTIFICATION_RECEIVED";
    private String notif="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"ONCREATTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTE");
        setContentView(R.layout.main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*FloatingActionButton callButton = findViewById(R.id.callButton);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:123456789"));
                startActivity(callIntent);
            }
        });*/
        //LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
        //        new IntentFilter(NOTIFICATION_RECEIVED));



        if(getIntent().getExtras()!=null) {
            TextView warnView = findViewById(R.id.warningView);
            warnView.setText(getIntent().getExtras().getString("warning"));
        }

        Button buttonClear = (Button) findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                TextView warnView = findViewById(R.id.warningView);
                warnView.setText("Nothing");
            }
        });




    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(NOTIFICATION_RECEIVED));

    }

    @Override
    protected void onPause() {
        // Unregister since the activity is about to be suspended
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }



    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e(TAG, "Got message!");
            notif = intent.getStringExtra(NOTIFICATION_RECEIVED);
            TextView warnView = findViewById(R.id.warningView);
            warnView.setText(notif);

        }
    };

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TextView warnView = findViewById(R.id.warningView);
        outState.putString("savText", warnView.getText().toString() );
        Log.e(TAG,"onSAVEEEEEEEEE "+warnView.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        TextView warnView = findViewById(R.id.warningView);
        warnView.setText(savedInstanceState.getString("savText"));
        Log.e(TAG,"ONRESTORRRRRRRRRRRRRRRRRRRRRRE "+savedInstanceState.getString("savText"));

    }*/


}