package ch.epfl.esl.blankphonewearapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by Claudio on 28.12.2017.
 */

 /* Class that gets the actual data of the racks and then update the UI
 * Need to override onPostExecute method*/
public class GetRacks extends AsyncTask<String, Void, String[]> {


    private Context mContext;
    private static final String TAG = "GetRacks";

    public GetRacks(Context context){
        super();
        mContext = context;
    }

    public GetRacks(){
        super();
    }

    public Context getContext(){
        return mContext;
    }

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


}

