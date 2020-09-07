package ch.epfl.esl.blankphonewearapp;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import static android.content.ContentValues.TAG;

/**
 * Created by musluoglucem on 09.01.18.
 */

public class GetJSON_val extends AsyncTask <String, Void, String[]> {


    private String [] decode_list(String list) {

        String [] urls = list.split("#end#");
        return urls;
    }

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
                return null;
            }

        }

        return powerArray;


        //return null;
    }
}