package ch.epfl.esl.datacenter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.database.Cursor;

import java.io.Serializable;

/**
 * Created by fouco on 1/9/18.
 */

public class  DatabaseHandler extends SQLiteOpenHelper implements Serializable  {

    public static final String DATABASE_NAME = "test.db";
    public static final String TABLE_NAME = "test";
    public static final String SAMPLING_TIME = "YYYYMMDDHHMM";
    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    private static Context myContext;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);
        myContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // db.execSQL(TABLE_CREATE);
    }

    public SQLiteDatabase openWr() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db;
    }

    public SQLiteDatabase openRd() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db;
    }

    public boolean isTableExists(String tableName, boolean openDb) {
        SQLiteDatabase mDatabase = this.openWr();
        if (openDb) {
            if (mDatabase == null || !mDatabase.isOpen()) {
                mDatabase = getReadableDatabase();
            }

            if (!mDatabase.isReadOnly()) {
                mDatabase.close();
                mDatabase = getReadableDatabase();
            }
        }

        Cursor cursor = mDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TABLE_DROP);
        onCreate(db);
    }

    public void createNewTable(String tableName) {
        final String tb = tableName;

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("CREATE TABLE IF NOT EXISTS " + tb + " (" + "id INTEGER PRIMARY KEY AUTOINCREMENT," + SAMPLING_TIME + " )");
    }

    public void deleteTableInDatabase(String tableName) {
        final String tb = tableName;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + tb + ";");
    }

    public boolean deleteDatabase() {
        return myContext.deleteDatabase(DATABASE_NAME);
    }
}

