package com.meri_sg.places_finder;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;


// Created on 20-Jun-16.

public class MyCursorAdapter extends CursorAdapter{


    public MyCursorAdapter(Context context, Cursor c) {
        super(context, c);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.rawforlist,parent,false);
    }

    //set listView items state and graphics
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        double currentLat;
        double currentLng;
        String unit;

        //get current location from SHARED
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(context);
        currentLat=Double.valueOf(pref.getString("currentLat", "50.0822409"));
        currentLng=Double.valueOf(pref.getString("currentLng", "14.4438791"));
        unit=pref.getString("unit","Km");

        //put place name
        TextView name = (TextView) view.findViewById(R.id.nameTv);
        String Pname = cursor.getString(cursor.getColumnIndexOrThrow(PlacesContract.PlacesFromApi.NAME));
        name.setText(Pname);

        //put place address
        TextView address = (TextView) view.findViewById(R.id.addressTv);
        String placeaddress = cursor.getString(cursor.getColumnIndexOrThrow(PlacesContract.PlacesFromApi.ADDRESS));
        address.setText(placeaddress);

        //put place distance
        TextView distance = (TextView) view.findViewById(R.id.distanceTv);
        Double chosenLat = cursor.getDouble(cursor.getColumnIndexOrThrow(PlacesContract.PlacesFromApi.LAT));
        Double chosenLng = cursor.getDouble(cursor.getColumnIndexOrThrow(PlacesContract.PlacesFromApi.LNG));

        double dLat = Math.toRadians(chosenLat - currentLat);
        double dLon = Math.toRadians(chosenLng - currentLng);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(currentLat)) * Math.cos(Math.toRadians(chosenLat))* Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = 6371 * c;

        if (unit.equals("Miles")){
            dist=dist/1.61;
            unit=context.getResources().getString(R.string.mile);
        }else {
            unit=context.getResources().getString(R.string.km);
        }

        DecimalFormat df = new DecimalFormat("0.00");
        String angleFormated = df.format(dist);
        distance.setText(angleFormated + " "+unit);

        String placeid=cursor.getString(cursor.getColumnIndexOrThrow(PlacesContract.PlacesFromApi.PLACEID));
        DbHelper helper = new DbHelper(context);
        ContentValues cv = new ContentValues();
        cv.put(PlacesContract.PlacesFromApi.DISTANCE, angleFormated);
        helper.getWritableDatabase().update(PlacesContract.PlacesFromApi.TABLE_NAME, cv, PlacesContract.PlacesFromApi.PLACEID + "='"+placeid+"'" , null);

        //put favorite/place small icon
        ImageView icon = (ImageView) view.findViewById(R.id.myiconIV);
        String favorit = cursor.getString(cursor.getColumnIndexOrThrow(PlacesContract.PlacesFromApi.FAVORIT));
        if (favorit.equals("save")){
            icon.setBackgroundResource(R.drawable.favorite);
        }else {
            icon.setBackgroundResource(R.drawable.pindrop);
        }

        //put img
        ImageView imgff = (ImageView) view.findViewById(R.id.iconIMG);
        String urlDisplay = cursor.getString(cursor.getColumnIndexOrThrow(PlacesContract.PlacesFromApi.IMG));
        Picasso.with(context).load(urlDisplay).resize(170,110).into(imgff);






    }//end of bindView






}//end
