package com.meri_sg.places_finder;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;

public class MainActivity extends AppCompatActivity implements FragmentListener, OnMapReadyCallback, LocationListener {

    protected FrameLayout contLand;
    protected SharedPreferences pref;
    private MyBatteryReceiver receiver;
    private LocationManager locationManager;
    private String locationProvider;
    protected FragmentManager manager;
    protected FragmentTransaction ft;
    protected Toolbar myToolbar;
    private CoordinatorLayout coordinatorLayout;

    protected String wSearch = "what";

    private String chosenPlacef,chosenPhone ;
    private Double chosenLat,chosenLng,currentLat,currentLng;
    private Location myCurrentLocation;
    private boolean isGpsOff=false,isFirst=true, exitnext=false ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StartAppSDK.init(this, "********", false);

        setContentView(R.layout.activity_main);

        //check what ui
        contLand = (FrameLayout) findViewById(R.id.mapCon);


        //get Preference
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        wSearch = pref.getString("wSearch", "what");

        //set Toolbar
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setLogo(R.drawable.placefindiconsm);
        myToolbar.setTitle("  "+getString(R.string.around_and_anywher));
        setSupportActionBar(myToolbar);

        //set for snackbar
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);

        //open battery reciever
        receiver = new MyBatteryReceiver();
        IntentFilter filter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
        registerReceiver(receiver, filter);

        //open gps or network if gps off
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        locationProvider = locationManager.getBestProvider(criteria, true);

        //only at the beginning if user open the app with gps off, suggest user to open gps
        if (savedInstanceState==null&& !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ){
            isGpsOff=true;
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle(R.string.no_gps);
            builder.setMessage(R.string.no_gps_explain);
            builder.setIcon(R.drawable.nogpsalert);
            builder.setPositiveButton(R.string.turn_on, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            //do nothing
            builder.setNegativeButton(R.string.continue_anyway, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            //show the alert to the user
            builder.show();
        }

        if (locationProvider != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (savedInstanceState!=null){
                isFirst = savedInstanceState.getBoolean("isFirst");
                isGpsOff = savedInstanceState.getBoolean("isGpsOff");
            }

            //if user place indoor
            if (myCurrentLocation==null&&isFirst&&!isGpsOff) {
                //check if user location has changed every 1 second
                locationManager.requestLocationUpdates(locationProvider, 1000, 1, this);
                //after 20 second of trying without result suggest go outdoor, the message appear only once per run
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        if (myCurrentLocation==null&&isFirst){
                            showSnackBar("gpsin");
                            isFirst=false;
                        }
                    }
                }, 20000);
            }
        }

        //find last location if exist
        try {
            Location last = locationManager.getLastKnownLocation(locationProvider);
            if (last == null) {
                //if last is null get last location from Shared (or default at the first time)
                currentLat=Double.valueOf(pref.getString("currentLat", "50.0822409"));
                currentLng=Double.valueOf(pref.getString("currentLng", "14.4438791"));
            } else {
                currentLat = last.getLatitude();
                currentLng = last.getLongitude();
            }
            pref= PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor savePlace=pref.edit();
            savePlace.putString("currentLat",""+currentLat);
            savePlace.putString("currentLng",""+currentLng).apply();

        } catch (Exception e) {

        }

        //when screen rotate, get the saved state
        if (savedInstanceState!=null) {
            wSearch = savedInstanceState.getString("wSearch");
            chosenPlacef = savedInstanceState.getString("chosenPlacef");
            chosenLat = savedInstanceState.getDouble("chosenLat");
            chosenLng = savedInstanceState.getDouble("chosenLng");
            chosenPhone = savedInstanceState.getString("chosenPhone");

            updateList(wSearch);
            refreshMap();
            if (contLand!=null){
                changeFragmentback();
            }
        }else {//at the first time the app is loading
            manager = getFragmentManager();
            ft = manager.beginTransaction();
            //load fragments to small screen or large portrait
            if (contLand == null ) {
                    SearchFrag searchFrag = new SearchFrag();
                    ListPlacesFrag listPlacesFrag = new ListPlacesFrag();
                    ft.add(R.id.searchCon, searchFrag, "searchFrag");
                    searchFrag.currentLat=currentLat;
                    searchFrag.currentLng=currentLng;
                    listPlacesFrag.wSearch = wSearch;
                    ft.add(R.id.listCon, listPlacesFrag, "listPlacesFrag").commit();

            //load fragments to large screen landscape, or extra large screen
            } else {
                    SearchFrag searchFrag = new SearchFrag();
                    ListPlacesFrag listPlacesFrag = new ListPlacesFrag();
                    MapFragment nmapFrag = new MapFragment();
                    ft.add(R.id.searchCon, searchFrag, "searchFrag");
                    searchFrag.currentLat=currentLat;
                    searchFrag.currentLng=currentLng;
                    listPlacesFrag.wSearch = wSearch;
                    ft.add(R.id.listCon, listPlacesFrag, "listPlacesFrag");
                    ft.add(R.id.mapCon, nmapFrag, "landmapfrag").commit();
                    nmapFrag.getMapAsync(this);
            }

        }//end of first time loading

        //change background color when switching between normal/favorites
        changeColor(wSearch);



    }//end of onCreate



    //save state for screen rotation
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("wSearch", wSearch);
        outState.putBoolean("isFirst",isFirst);
        outState.putBoolean("isGpsOff",isGpsOff);

        try{
            outState.putString("chosenPlacef", chosenPlacef);
            outState.putDouble("chosenLat", chosenLat);
            outState.putDouble("chosenLng", chosenLng);
            outState.putString("chosenPhone", chosenPhone);

        }catch (Exception e){}

        super.onSaveInstanceState(outState);
    }//end of onSaveInstanceState


    @Override
    protected void onResume() {
        super.onResume();

        //check if user location has changed every 10 seconds
        if (locationProvider != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(locationProvider, 10000, 1, this);



            //only at the beginning if user open the app with gps off, and than turn it on,
            //check if user location has changed every 1 second
             if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)&&isGpsOff&&isFirst){
                 showSnackBar("gpson");
                 locationManager.requestLocationUpdates(locationProvider, 1000, 1, this);
                 //after 20 second of trying without result suggest go outdoor, the message appear only once per run
                 final Handler handler = new Handler();
                 handler.postDelayed(new Runnable() {
                     public void run() {
                         if (myCurrentLocation==null&&isFirst){
                             showSnackBar("gpsin");
                             isFirst=false;
                         }
                     }
                 }, 20000);
            }


        }//end if locationProvider



    }//end of onResume


    //handle back button
    //from map to "last" from "last" to "past" from "past" to end, or from "favorites" to "past" and from "past" to end
    @Override
    public void onBackPressed() {
        manager = getFragmentManager();
        ft = manager.beginTransaction();
        MapFragment cmapfrag = (MapFragment) manager.findFragmentByTag("mapfrag");
        ListPlacesFrag listPlacesFrag = (ListPlacesFrag) manager.findFragmentByTag("listPlacesFrag");
        //on portrait type screen: if currently on map screen, return to list
        if (cmapfrag != null && contLand == null) {
            ft.remove(cmapfrag);
            ListPlacesFrag nlistPlacesFrag = new ListPlacesFrag();
            nlistPlacesFrag.wSearch = wSearch;
            ft.add(R.id.listCon, nlistPlacesFrag, "listPlacesFrag").commit();
        //if currently on any list screen, go to previous search
        }else if (listPlacesFrag != null){
            if (!pref.getString("wSearch", "what").equals("past")) {
                DbCommands db = new DbCommands(this);
                db.showPast();
                pref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor savestate = pref.edit();
                savestate.putString("wSearch", "past").apply();
                listPlacesFrag.wSearch = "past";
                wSearch = "past";
            //when using BACK button several times and now is on max limited Back action, exit the app
            }else if (!exitnext) {
                exitnext=true;
                showSnackBar("exit");
            }else if (exitnext) {
                StartAppAd.onBackPressed(MainActivity.this);
                finish();
            }
        }

        changeColor(wSearch);

    }//end of onBackPressed


    //show the chosen place on the map
    @Override
    public void changeFragments(String chosenPlace, Double clat, Double clng, String cPhone) {

        chosenPlacef = chosenPlace;
        chosenLat = clat;
        chosenLng = clng;
        if (cPhone==null||cPhone.equals("nophone"))
            chosenPhone = " "+getResources().getString(R.string.no_phone);
        else
            chosenPhone = cPhone;

        //if on landscape type screen, update map for chosen location
        if (contLand != null)
            refreshMap();
        //if on portrait type screen then change screen from list to map
        else {
            manager = getFragmentManager();
            ft = manager.beginTransaction();
            ListPlacesFrag listPlacesFrag = (ListPlacesFrag) manager.findFragmentByTag("listPlacesFrag");
            if (listPlacesFrag != null){
                ft.remove(listPlacesFrag);
                MapFragment mapFrag = new MapFragment();
                ft.add(R.id.listCon, mapFrag, "mapfrag").commit();
                mapFrag.getMapAsync(this);
            }
        }

    }//end changeFragments


    //for small screen and portrait
    @Override
    public void changeFragmentback() {
        manager = getFragmentManager();
        ft = manager.beginTransaction();
        MapFragment cmapfrag = (MapFragment) manager.findFragmentByTag("mapfrag");
        //if searching while on map screen, change to list screen
        if (cmapfrag != null ){
            ft.remove(cmapfrag);
            ListPlacesFrag nlistPlacesFrag = new ListPlacesFrag();
            nlistPlacesFrag.wSearch = wSearch;
            ft.add(R.id.listCon, nlistPlacesFrag, "listPlacesFrag").commit();
        }
        changeColor(wSearch);
    }//end of changeFragmentback


    //list have to know if favorites showing, for the correct menu. so update list according to user action
    @Override
    public void updateList(String what) {
        wSearch=what;
        manager = getFragmentManager();
        ListPlacesFrag listPlacesFrag = (ListPlacesFrag) manager.findFragmentByTag("listPlacesFrag");
        if (listPlacesFrag != null)
            listPlacesFrag.wSearch = what;
    }//end of updateList


    //pull to refresh handler start search around
    @Override
    public void refreshList() {
        manager = getFragmentManager();
        SearchFrag searchFrag = (SearchFrag)manager.findFragmentByTag("searchFrag");
        if (searchFrag != null && searchFrag.wButtonS.equals("around"))
            searchFrag.searchAround(); //when pull to refresh, do search around (same action like clicking on AROUND button)
        ListPlacesFrag listPlacesFrag = (ListPlacesFrag) manager.findFragmentByTag("listPlacesFrag");
        if (listPlacesFrag != null)
            listPlacesFrag.swipeContainer.setRefreshing(false); //clear the "refreshing" icon animation
    }//end of refreshList


    //when user move or choose place to see on the map, check what state the map is
    public void refreshMap() {
        manager = getFragmentManager();
        ft = manager.beginTransaction();
        MapFragment cmapfrag = (MapFragment) manager.findFragmentByTag("mapfrag");
        MapFragment lmapfrag = (MapFragment) manager.findFragmentByTag("landmapfrag");

        try {
            //when small screen, show map
            if (cmapfrag != null && contLand == null) {
                ft.remove(cmapfrag);
                MapFragment mapFrag = new MapFragment();
                ft.add(R.id.listCon, mapFrag, "mapfrag").commit();
                mapFrag.getMapAsync(this);
            //in large landscape/xlarg if not first time
            } else if (lmapfrag != null && contLand != null) {
                ft.remove(lmapfrag);
                MapFragment mapFrag = new MapFragment();
                ft.add(R.id.mapCon, mapFrag, "landmapfrag").commit();
                mapFrag.getMapAsync(this);
            //in the first time large landscape/xlarg loading
            } else if (lmapfrag == null && contLand != null) {
                MapFragment mapFrag = new MapFragment();
                ft.add(R.id.mapCon, mapFrag, "landmapfrag").commit();
                mapFrag.getMapAsync(this);
            }

        } catch (Exception e) {

        }

    }//end of refreshMap

    //on pause, stop the location updates to save energy
    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
    }//end of onPause


    //free resources when existing
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            //dispose the battery checker
            unregisterReceiver(receiver);
        } catch (Exception e) { }
    }// end of onDestroy


    //handle map interface creation
    @Override
    public void onMapReady(final GoogleMap map) {
        //if use chose to see specific location on map
        if (chosenPlacef != null && !chosenPlacef.equals("")) {
            try {
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                LatLng cposition = new LatLng(chosenLat, chosenLng);
                LatLng nowposition = new LatLng(currentLat, currentLng);

                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                map.setMyLocationEnabled(true);

                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(cposition, 15);
                map.moveCamera(update);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(cposition)
                        .title(chosenPlacef)
                        .snippet(getResources().getString(R.string.phone)+chosenPhone)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.locationplacer));
                Marker marker = map.addMarker(markerOptions);
                marker.showInfoWindow();
                MarkerOptions markerOpt = new MarkerOptions()
                        .position(nowposition)
                        .title(getResources().getString(R.string.i_m_here))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.myclocation));
                Marker mark = map.addMarker(markerOpt);
            } catch (Exception e) {}
        //if on landscape large or xLarge, show the user current location, when no specific location yet
        }else {
            try{
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                LatLng nowposition = new LatLng(currentLat, currentLng);
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(nowposition, 15);
                map.moveCamera(update);
                MarkerOptions markerOpt = new MarkerOptions()
                        .position(nowposition)
                        .title(getResources().getString(R.string.i_m_here))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.myclocation));
                Marker mark = map.addMarker(markerOpt);
            } catch (Exception e) {}
        }
    }//end of onMapReady


    //update the current location to map and search fragment, each time the location change
    @Override
    public void onLocationChanged(Location location) {
        myCurrentLocation=location;
        currentLat=location.getLatitude();
        currentLng=location.getLongitude();
        manager = getFragmentManager();
        SearchFrag searchFrag = (SearchFrag) manager.findFragmentByTag("searchFrag");
        searchFrag.currentLat=location.getLatitude();
        searchFrag.currentLng=location.getLongitude();

        pref= PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor savePlace=pref.edit();
        savePlace.putString("currentLat",""+currentLat);
        savePlace.putString("currentLng",""+currentLng).apply();

        refreshMap();

        //only at the beginning if user open the app with gps off, and than turn it on,
        //search around will start when new location update
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)&& isGpsOff){
            isGpsOff=false;
            refreshList();
        }

    }//end of onLocationChanged

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    @Override
    public void onProviderEnabled(String provider) {

    }
    @Override
    public void onProviderDisabled(String provider) {

    }


    //Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }//end of onCreateOptionsMenu


    //Handle action bar item clicks, and sends user to configuration screen
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent=new Intent(MainActivity.this,Preference.class);
            startActivity(intent);
        }else if (item.getItemId() == R.id.back){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }//end of onOptionsItemSelected


    //Snackbar to replace most of the toasts to show messages to the user
    public void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout,"", Snackbar.LENGTH_INDEFINITE);

       if (message.equals("nofavorits")){
           snackbar = Snackbar.make(coordinatorLayout,R.string.no_favorits, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });

       }else if (message.equals("nointernet")){
           snackbar = Snackbar.make(coordinatorLayout,R.string.no_internet_connection , Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });

       }else if (message.equals("nonet")){
           snackbar = Snackbar.make(coordinatorLayout,R.string.no_internet , Snackbar.LENGTH_INDEFINITE)
                   .setAction(R.string.ok, new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                       }
                   });

       }else if (message.equals("noweb")){
           snackbar = Snackbar.make(coordinatorLayout,R.string.no_website , Snackbar.LENGTH_INDEFINITE)
                   .setAction(R.string.ok, new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                       }
                   });

       }else if (message.equals("nophone")){
           snackbar = Snackbar.make(coordinatorLayout,R.string.no_phone_to_call , Snackbar.LENGTH_INDEFINITE)
                   .setAction(R.string.ok, new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                       }
                   });

       }else if (message.equals("nocall")){
           snackbar = Snackbar.make(coordinatorLayout,R.string.no_call , Snackbar.LENGTH_INDEFINITE)
                   .setAction(R.string.ok, new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                       }
                   });

       }else if (message.equals("noresults")){
            snackbar = Snackbar.make(coordinatorLayout,R.string.no_results , Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });

        }else if (message.equals("gpson")){
           snackbar = Snackbar.make(coordinatorLayout,R.string.gps_is_on , Snackbar.LENGTH_LONG)
                   .setAction(R.string.ok, new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                       }
                   });

       }else if (message.equals("gpsin")){
           snackbar = Snackbar.make(coordinatorLayout,R.string.gps_indoor , Snackbar.LENGTH_INDEFINITE)
                   .setAction(R.string.ok, new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                       }
                   });

       }else if (message.equals("exit")){
           snackbar = Snackbar.make(coordinatorLayout,R.string.back_to_exit , Snackbar.LENGTH_INDEFINITE)
                   .setAction(R.string.stay, new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                           exitnext=false;

                       }
                   });

       }else if (message.equals("wbutt")){
           snackbar = Snackbar.make(coordinatorLayout,R.string.choose_button , Snackbar.LENGTH_INDEFINITE)
                   .setAction(R.string.ok, new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                       }
                   });

       }else if (message.equals("notext")){
            snackbar = Snackbar.make(coordinatorLayout,R.string.no_text , Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });
        }

        //design the snackbar
        View snackbarLayout = snackbar.getView();
        snackbarLayout.setBackgroundColor(Color.parseColor("#ffddd1"));

        Button btn = (Button) snackbarLayout.findViewById(android.support.design.R.id.snackbar_action);
        btn.setTextColor(Color.BLACK);
        btn.setBackgroundResource(R.drawable.btnfsmall);

        TextView textView = (TextView) snackbarLayout.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.BLACK);
        textView.setMaxLines(2);

        snackbar.show();

    }//end of snackbar


    //change color according to if user currently at favorites or not
    public void changeColor(String what){
        FrameLayout contPort = (FrameLayout) findViewById(R.id.listCon);
        if (contPort !=null && what.equals("favorit")) {
            contPort.setBackgroundColor(Color.parseColor("#ffddd1"));
        }else if (contPort !=null && !what.equals("favorit")){
            contPort.setBackgroundColor(Color.parseColor("#82B1FF"));
        }
    }//end of changeColor




}//end Main












//ty, meri sg





