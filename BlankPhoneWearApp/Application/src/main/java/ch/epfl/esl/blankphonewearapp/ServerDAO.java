package ch.epfl.esl.blankphonewearapp;

import java.io.Serializable;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.sql.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by fouco on 1/10/18.
 */

public class ServerDAO implements Serializable{
    public static final String TABLE_NAME = "test";
    public static final String SAMPLING_TIME = "YYYYMMDDHHMM";

    public String column;

    protected DatabaseHandler db1;

    public void createDatabase(DatabaseHandler db, String name, int rack, int server[]) {
        final String tableName = getTableName(name);

        if (db.isTableExists(tableName, true) == false) {
            db.createNewTable(tableName);
            for (int i = 0; i < rack; i++)
                for (int j = 0; j < server[i]; j++) {
                    column = (" rack" + i + "server" + j + " ");
                    db.openWr().execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + column);
                }
        }
    }

    private String getTableName(String name) {

        String[] port = name.split(":");
        String[] table = port[0].split("\\.");
        String tableName = "start";

        for (int i = 0; i < table.length; i++)
            tableName += table[i];
        if(name.contains(":")) {
            tableName += port[1];
        }
        tableName += "end";

        //Log.e(TAG,"this is the table name : " +tableName);


        return tableName;
    }

    public void addSampling(final DatabaseHandler db, String name, String url, final int server[]) {

        final String tableName = getTableName(name);

        final ContentValues values = new ContentValues();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHmm");

        String date = sdf.format(cal.getTime());

        values.put(SAMPLING_TIME, "201801151515");
        Log.e(TAG,"the asked date :" + date);

        new GetJSON_val() {
            @Override
            protected void onPostExecute(String[] result) {
                int j = 0, rest = 0;
                String string;
                if (result != null) {
                    for (int i = 0; i < result.length; i++) {
                        if (rest >= server[j]) {
                            j++;
                            rest = 0;
                        }
                        values.put(" rack" + j + "server" + rest + " ", result[i]);
                        rest++;
                    }
                    db.openWr().insert(tableName, "", values);
                    Log.d("Server"," sampling finish");
                }
            }
        }.execute(url);
    }

    public String [] getSampling(final DatabaseHandler db, String table, String time, int rack[], int server[]) {

        String tableName = getTableName(table);
        String [] serverName= new String [1+rack.length];
        Log.e(TAG,"the rack length "+rack.length);
        String[] out = new String [rack.length];
        String[] timeRequest = new String[1];
        timeRequest[0] = time;
        serverName[0]=SAMPLING_TIME;
        for(int i=1;i<=rack.length;i++)
            serverName[i] = "rack" + rack[i-1] + "server" + server[i-1];

        Log.e(TAG,"the table name " + tableName);
        Log.e(TAG,"the time "+time);
        for(int m=0;m<serverName.length;m++) {
            Log.e(TAG,"the srv name "+serverName[m]);
        }


        if (timeExistInDb(db,tableName,time,serverName)) {
            Cursor c = db.openRd().query(tableName, serverName, SAMPLING_TIME + "=?", timeRequest, null, null, null);

            if (c != null && c.moveToFirst()) {
                for(int i=0;i<rack.length;i++) {
                    out[i] = c.getString(c.getColumnIndex(serverName[i+1]));
                    Log.d("Server", " data out: " + out[i]);
                }
            }
            c.close();
        }
        else
            Log.e("SQL","Time: "+time+" or Server: "+serverName+" does not exists");
        return out;
    }


    public static boolean timeExistInDb(final DatabaseHandler db,String tablename,String time,String [] serverName) {

        String Query = "Select * from " + tablename + " where " + SAMPLING_TIME + " =?";
        Cursor cursor = db.openRd().rawQuery(Query,new String[]{time});

        if(cursor.moveToFirst()==false)
            return false;

        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }

        cursor.close();
        return true;
    }

    public void deleteTable(DatabaseHandler db, String table){
        db.deleteTableInDatabase(getTableName(table));
    }

    public boolean deleteDatabase(DatabaseHandler db){
        return db.deleteDatabase();
    }

}
