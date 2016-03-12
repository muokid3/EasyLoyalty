package com.easycoach.easyloyalty.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.PublicKey;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by muoki on 3/10/2016.
 */
public class EasyDBHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "easyDb";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "prices";
    private static final String UID = "_id";
    private static final String USER_ID = "u_id";
    private static final String DESTINATION_FROM = "destination_from";
    private static final String DESTINATION_TO ="destination_to";
    private static final String PARCEL_CHARGE = "parcel_charge";
    private static final String TRAVEL_CHARGE = "travel_charge";
    private Context context;

    private static String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ""+DESTINATION_FROM+" VARCHAR(255)," +
            ""+USER_ID+" INTEGER(5)," +
            ""+DESTINATION_TO+" VARCHAR(255)," +
            ""+PARCEL_CHARGE+" INTEGER(10)," +
            ""+TRAVEL_CHARGE+" INTEGER(10));";

    public EasyDBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME+"");
        onCreate(db);
    }

    public void insertPrice (HashMap<String, String> queryValues)
    {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(USER_ID, queryValues.get("u_id"));
        values.put(DESTINATION_TO, queryValues.get("destination_to"));
        values.put(DESTINATION_FROM, queryValues.get("destination_from"));
        values.put(PARCEL_CHARGE, queryValues.get("parcel_charge"));
        values.put(TRAVEL_CHARGE, queryValues.get("travel_charge"));

        String [] columns = {USER_ID};
        String where = USER_ID+"= ?";
        String [] selectionArgs = {queryValues.get("u_id")};

        Cursor cursor = db.query(TABLE_NAME, columns, where, selectionArgs, null, null, null);
        int rowCount = cursor.getCount();

        if (rowCount == 0)
        {
            //insert
            db.insert(TABLE_NAME, null, values);
            db.close();
        }
        else
        {
            //update
            db.update(TABLE_NAME, values, where, selectionArgs);
            db.close();
        }

    }

    public String getRouteTravelAmount(String from, String to)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        String [] columns = {TRAVEL_CHARGE};
        String [] selectionArgs = {from, to};

        Cursor cursor = db.query(TABLE_NAME, columns, DESTINATION_FROM + " = ? AND "+ DESTINATION_TO + " = ?",
                selectionArgs,null, null, null);
        StringBuffer stringBuffer = new StringBuffer();

        int count = cursor.getCount();

        if (count > 0)
        {
            while (cursor.moveToNext())
            {
                int index = cursor.getColumnIndex(TRAVEL_CHARGE);
                int charge = cursor.getInt(index);
                stringBuffer.append(charge+"");
            }

            return stringBuffer.toString();
        }
        else
        {
            return 0+"";

        }



    }

    public String getRouteParcelAmount(String from, String to)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        String [] columns = {PARCEL_CHARGE};

        String [] selectionArgs = {from, to};

        Cursor cursor = db.query(TABLE_NAME, columns, DESTINATION_FROM + " = ? AND "+ DESTINATION_TO + " = ?",
                selectionArgs,null, null, null);
        StringBuffer stringBuffer = new StringBuffer();

        int count = cursor.getCount();

        if (count > 0)
        {
            while (cursor.moveToNext())
            {
                int index = cursor.getColumnIndex(PARCEL_CHARGE);
                int charge = cursor.getInt(index);
                stringBuffer.append(charge+"");
            }

            return stringBuffer.toString();
        }
        else
        {
            return 0+"";
        }



    }


}
