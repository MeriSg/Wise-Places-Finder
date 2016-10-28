package com.meri_sg.places_finder;

import android.content.ContentValues;
import android.content.Context;


//Created on 20-Jun-16.

public class DbCommands {
    Context context;
    public DbCommands(Context context) {
        this.context = context;
    }

    //add place object to DB
    public void addPlace(PlacesFromApi placesfromapi) {

        DbHelper helper = new DbHelper(context);
        ContentValues cv = new ContentValues();

        cv.put(PlacesContract.PlacesFromApi.PLACEID, placesfromapi.getPlaceId());
        cv.put(PlacesContract.PlacesFromApi.NAME, placesfromapi.getName());
        cv.put(PlacesContract.PlacesFromApi.ADDRESS, placesfromapi.getAddress());
        cv.put(PlacesContract.PlacesFromApi.LAT, placesfromapi.getLat());
        cv.put(PlacesContract.PlacesFromApi.LNG, placesfromapi.getLng());
        cv.put(PlacesContract.PlacesFromApi.SEARCHIT, placesfromapi.getSearch());
        cv.put(PlacesContract.PlacesFromApi.FAVORIT, placesfromapi.getFavorit());
        cv.put(PlacesContract.PlacesFromApi.IMG, placesfromapi.getImg());
        cv.put(PlacesContract.PlacesFromApi.PHONE, placesfromapi.getPhone());
        cv.put(PlacesContract.PlacesFromApi.WEBSITE, placesfromapi.getWebsite());

        helper.getWritableDatabase().insert(PlacesContract.PlacesFromApi.TABLE_NAME, null, cv);

        //update provider that something changed
        context.getContentResolver().notifyChange(PlacesContract.PlacesFromApi.CONTENT_URI, null);

        helper.close();

    }//end of addPlace


    //show the favorites list
    public void showFavorits() {

        DbHelper helper = new DbHelper(context);
        ContentValues cv = new ContentValues();

        //sign all the places in DB as "past" (don't show it)
        cv.put(PlacesContract.PlacesFromApi.SEARCHIT, "past");
        helper.getWritableDatabase().update(PlacesContract.PlacesFromApi.TABLE_NAME, cv, null, null);

        //sign only the favorites places in DB as "last" (show it)
        cv.put(PlacesContract.PlacesFromApi.SEARCHIT, "last");
        helper.getWritableDatabase().update(PlacesContract.PlacesFromApi.TABLE_NAME, cv, PlacesContract.PlacesFromApi.FAVORIT + "='save'", null);
        context.getContentResolver().notifyChange(PlacesContract.PlacesFromApi.CONTENT_URI, null);

        helper.close();

    }//end of showFavorits

    //show places signed as "past" (showing the last search)
    public void showPast() {

        DbHelper helper = new DbHelper(context);
        ContentValues cv = new ContentValues();

        //if is not favorite and it's "past" then put it on "hold" for the moment. (sometimes, some of favorites items could be on "past")
        cv.put(PlacesContract.PlacesFromApi.SEARCHIT, "hold");
        helper.getWritableDatabase().update(PlacesContract.PlacesFromApi.TABLE_NAME, cv, PlacesContract.PlacesFromApi.SEARCHIT + "='past' AND "
                + PlacesContract.PlacesFromApi.FAVORIT + "='no'" , null);

        //sign all the places in DB as "past" (don't show it), unless it's signed as "hold"
        cv.put(PlacesContract.PlacesFromApi.SEARCHIT, "past");
        helper.getWritableDatabase().update(PlacesContract.PlacesFromApi.TABLE_NAME, cv, PlacesContract.PlacesFromApi.SEARCHIT + "='last'", null);

        //sign only the "hold" places in DB as "last" (show it)
        cv.put(PlacesContract.PlacesFromApi.SEARCHIT, "last");
        helper.getWritableDatabase().update(PlacesContract.PlacesFromApi.TABLE_NAME, cv, PlacesContract.PlacesFromApi.SEARCHIT + "='hold'", null);
        context.getContentResolver().notifyChange(PlacesContract.PlacesFromApi.CONTENT_URI, null);

        helper.close();
    }//end of showPast



}//end class
