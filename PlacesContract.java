package com.meri_sg.places_finder;

import android.net.Uri;


 // Created on 20-Jun-16.

//contract + DB consts
public class PlacesContract {

    public final static String AUTHORITY ="com.meri_sg.places_finder.placesfromapi";


    public static class PlacesFromApi{

        public final static String TABLE_NAME="placesfromapi";

        public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

        public static String ID = "_id";
        public static String PLACEID = "placeid";
        public static String NAME="name";
        public static String ADDRESS ="address" ;
        public static String PHONE ="phone" ;
        public static String WEBSITE ="website" ;
        public static String IMG ="img" ;
        public static String LAT="lat";
        public static String LNG="lng";
        public static String DISTANCE="distance";
        public static String SEARCHIT="searchit";
        public static String FAVORIT="favorit";


    }


}
