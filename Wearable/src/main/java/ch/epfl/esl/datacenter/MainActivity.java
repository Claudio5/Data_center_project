package ch.epfl.esl.datacenter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    public static final String NOTIFICATION_RECEIVED = "NOTIFICATION_RECEIVED";
    private String notif;
    private final String MY_PREFERENCES = "PREFERENCES_TEXT";
    private TextView warnView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp.mainActivity = this;
        Log.e(TAG,"onCreate");
        setContentView(R.layout.main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        String restoredText = prefs.getString("warning", null);

        if (restoredText == null) {
           restoredText = "No alert";
        }
        Log.e(TAG, "Restored String "+ restoredText);
        TextView warnView = findViewById(R.id.warningView);
        warnView.setMovementMethod(new ScrollingMovementMethod());
        warnView.setText(restoredText);

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



        /*if(getIntent().getExtras()!=null) {
            warnView = findViewById(R.id.warningView);
            warnView.setText(getIntent().getExtras().getString("warning"));
        }*/

        Button buttonClear = (Button) findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                TextView warnView = findViewById(R.id.warningView);
                warnView.setText("No Alert");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("warning", "No Alert");
                editor.apply();

            }
        });




    }

    public void setWarningTextView(String notif){
        SharedPreferences prefs = this.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("warning", notif);
        editor.apply();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MyApp.mainActivity=null;
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