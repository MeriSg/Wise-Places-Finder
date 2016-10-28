package com.meri_sg.places_finder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;


//Created on 20-Jun-16.

public class DbHelper extends SQLiteOpenHelper {


    public DbHelper(Context context) {
        super(context, PlacesContract.PlacesFromApi.TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //create the places table in DB
        String command="CREATE TABLE " + PlacesContract.PlacesFromApi.TABLE_NAME +
                "(" + PlacesContract.PlacesFromApi.ID+ " INTEGER PRIMARY KEY autoincrement," + PlacesContract.PlacesFromApi.SEARCHIT + " TEXT,"
                + PlacesContract.PlacesFromApi.PLACEID + " TEXT," + PlacesContract.PlacesFromApi.FAVORIT + " TEXT," + PlacesContract.PlacesFromApi.NAME + " TEXT,"
                + PlacesContract.PlacesFromApi.WEBSITE + " TEXT," + PlacesContract.PlacesFromApi.IMG + " TEXT," + PlacesContract.PlacesFromApi.PHONE + " TEXT,"
                + PlacesContract.PlacesFromApi.ADDRESS + " TEXT," +PlacesContract.PlacesFromApi.DISTANCE+ " REAL," +PlacesContract.PlacesFromApi.LAT+ " REAL,"
                +PlacesContract.PlacesFromApi.LNG +" REAL)";
        try {
            db.execSQL(command);
        } catch (SQLiteException ex) {

        }

    } //end of onCreate

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
