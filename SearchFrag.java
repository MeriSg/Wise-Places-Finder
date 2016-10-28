package com.meri_sg.places_finder;


import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * A simple {@link Fragment} subclass.
 */

//handle searches
public class SearchFrag extends Fragment {

    public String searchit;
    public EditText searchfromuser;
    public FragmentListener myActivity;
    private String radius,type,cRadius;
    private SharedPreferences pref;
    public Double currentLat, currentLng;
    private Button searchB, aroundB, favorits;
    private Spinner spinner;
    private FrameLayout spinnframe;
    private SpinnerAdapter  spinadapter;
    public String wButtonS= "around";
    boolean loop=false;

    ProgressDialog dialog;


    //attach activity to the fragment
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            myActivity = (FragmentListener) activity;
        } catch (ClassCastException e) {
            Log.d("err", "activity must implement FragmentListener");
        }
    }//end of onAttach

    public SearchFrag() {
        // Required empty public constructor
    }

    //on create occur only once when app starts and fragment attached to main!
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref=PreferenceManager.getDefaultSharedPreferences(getActivity());
        radius=pref.getString("radius","500");
        type=pref.getString("type","restaurant");

        //check if connected to internet
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        boolean isOnline = cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
        if (isOnline) {
             //start thread to download the places Around list
            DownloadPage downloadAsync = new DownloadPage("around");
            downloadAsync.execute("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + currentLat + "," + currentLng + "&radius=" + radius + "&type=" + type + "&keyword=%20&name=%20&key=AIza...");

        } else {
            myActivity.showSnackBar("nointernet");
            showPastSearch();
        }


    }//end of onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        //hold the fragment alive to prevent crash while task run during screen rotation
        //this is made a special fragment who retained across activity recreation, savedInstanceState is always null
        setRetainInstance(true);

        pref= PreferenceManager.getDefaultSharedPreferences(getActivity());
        searchfromuser = (EditText) v.findViewById(R.id.searchEt);
      //(update) Start search by clicking the end button on keyboard
        searchfromuser.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                   if (wButtonS.equals("around")){
                       searchAround();
                   }else if (wButtonS.equals("text")){
                       hideKeyboard();
                       searchit = searchfromuser.getText().toString();
                       searchit = searchit.replace(" ", "%20");
                       if (searchit != null && !searchit.equals("")) {
                           ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
                           boolean isOnline = cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
                           if (isOnline) {
                               //start thread to download
                               DownloadPage downloadAsync = new DownloadPage("text");
                               downloadAsync.execute("https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + searchit + "&key=AIza...");
                           }else {//if no internet and favorits is showing
                               myActivity.showSnackBar("nointernet");
                               showPastSearch();
                           }
                       }else {
                           myActivity.showSnackBar("notext");
                       }
                   }else if (wButtonS.equals("favorite")){
                       myActivity.showSnackBar("wbutt");
                   }


                }

                return false;
            }
        });


        //create the places types spinner
        spinner = (Spinner) v.findViewById(R.id.typeSpinner);
        spinadapter = new SpinnerAdapter(this.getActivity(),getResources().getStringArray(R.array.types_array));
        spinner.setAdapter(spinadapter);
        spinner.setSelection(pref.getInt("position",0));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                parent.getItemAtPosition(position);
                String pType="restaurant";
                if (position==0) pType="restaurant";
                else if (position==1) pType="cafe";
                else if (position==2) pType="bar";
                else if (position==3) pType="store";
                else if (position==4) pType="shopping_mall";
                else if (position==5) pType="pharmacy";
                else if (position==6) pType="gas_station";
                else if (position==7) pType="parking";

                //if user choose new type it's save in Shared and start search around
                if (position!=pref.getInt("position",0)){
                    SharedPreferences.Editor savestate=pref.edit();
                    savestate.putInt("position",position);
                    savestate.putString("type",pType).apply();
                    buttonSelect ("around");///////////////////////////////////////////////////
                    searchAround();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }


        });

        //create button to delete the whole text from search text box
        ImageButton deltextB = (ImageButton) v.findViewById(R.id.delTextBtn);
        deltextB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchfromuser.setText("");
            }
        });


        //create the Search By Text button
        searchB = (Button) v.findViewById(R.id.searchBtn);
        searchB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                searchit = searchfromuser.getText().toString();
                searchit = searchit.replace(" ", "%20");
                if (searchit != null && !searchit.equals("")) {
                    ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
                    boolean isOnline = cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
                    if (isOnline) {
                        //start thread to download
                        DownloadPage downloadAsync = new DownloadPage("text");
                        downloadAsync.execute("https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + searchit + "&key=AIza...");
                    }else {//if no internet and favorits is showing
                        myActivity.showSnackBar("nointernet");
                        showPastSearch();
                    }
                }else {
                    myActivity.showSnackBar("notext");
                }
                buttonSelect ("text");///////////////////////////////////////////////////

            }
        });

        //create the Around button
        aroundB = (Button) v.findViewById(R.id.aroundBtn);
        aroundB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this method is available for pull to refresh and for around button
                searchAround();
                buttonSelect ("around");///////////////////////////////////////////////////

            }
        });

        //create the favorites list button
        favorits = (Button) v.findViewById(R.id.favoritsBtn);
        favorits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                DbHelper helper = new DbHelper(getActivity());
                Cursor cursor = helper.getReadableDatabase().rawQuery("SELECT * FROM "+ PlacesContract.PlacesFromApi.TABLE_NAME
                        +" WHERE "+ PlacesContract.PlacesFromApi.FAVORIT+"='save'", null);
                if (cursor.getCount()!=0){
                    //prevent deleting the last search
                    if (!pref.getString("wSearch", "").equals("favorit")){
                        helper.getWritableDatabase().delete(PlacesContract.PlacesFromApi.TABLE_NAME, PlacesContract.PlacesFromApi.SEARCHIT + "='past' AND "
                                + PlacesContract.PlacesFromApi.FAVORIT + "='no'", null);
                    }
                    SharedPreferences.Editor savestate=pref.edit();
                    savestate.putString("wSearch", "favorit").apply();
                    DbCommands db = new DbCommands(getActivity());
                    db.showFavorits();
                    myActivity.updateList("favorit");
                    myActivity.changeColor("favorit");
                    myActivity.changeFragmentback();
                    buttonSelect ("favorite");///////////////////////////////////////////////////

                }else {
                    myActivity.showSnackBar("nofavorits");
                }
                helper.close();
            }
        });


        return v;


    } //end of onCreateView


    //hide keyboard when search is start
    public void hideKeyboard(){
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }//end of hideKeyboard


    //(update) Help the user know what type of search by highlighting the current button
    public void buttonSelect(String wButton){

        spinnframe= (FrameLayout) spinner.findViewById(R.id.spinnerframe);

        if (wButton.equals("text")){
            searchB.setBackgroundResource(R.drawable.buttonsnn);
            aroundB.setBackgroundResource(R.drawable.buttons);
            spinnframe.setBackgroundResource(R.drawable.buttonfww);
            favorits.setBackgroundResource(R.drawable.buttons);
            wButtonS="text";
            searchfromuser.setHint(R.string.example_text);

        }else if (wButton.equals("around")){
            searchB.setBackgroundResource(R.drawable.buttons);
            aroundB.setBackgroundResource(R.drawable.buttonsnn);
            spinnframe.setBackgroundResource(R.drawable.buttonfsp);
            favorits.setBackgroundResource(R.drawable.buttons);
            wButtonS="around";
            searchfromuser.setHint(R.string.example);

        }else if (wButton.equals("favorite")){
            searchB.setBackgroundResource(R.drawable.buttons);
            aroundB.setBackgroundResource(R.drawable.buttons);
            spinnframe.setBackgroundResource(R.drawable.buttonfww);
            favorits.setBackgroundResource(R.drawable.buttonsnn);
            wButtonS="favorite";
        }

    }



    //shows the previous search, only if search is fail and user is in favorites. else user is already see the previous search
    public void showPastSearch(){
        if (pref.getString("wSearch", "what").equals("favorit")) {
            DbCommands db = new DbCommands(getActivity());
            db.showPast();
            SharedPreferences.Editor savestate=pref.edit();
            savestate.putString("wSearch", "past").apply();
            myActivity.updateList("past");
            myActivity.changeColor("past");
        }
        myActivity.changeFragmentback();
    }//end of showPastSearch


    //search places around when user pressed Around or pull to refresh
    public void searchAround(){
        hideKeyboard();
        radius=pref.getString("radius", "500");
        type=pref.getString("type", "restaurant");
        searchit = searchfromuser.getText().toString();
        searchit = searchit.replace(" ", "%20");
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        boolean isOnline = cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
        if (isOnline) {
            DownloadPage downloadAsync = new DownloadPage("around");
            downloadAsync.execute("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+currentLat+","+currentLng+"&radius="+radius+"&type="+type+"&keyword="+searchit+"&name=%20&key=AIza...");
        //if no internet
        }else {
            myActivity.showSnackBar("nointernet");
            showPastSearch();
        }
    }//end of searchAround


    //thread for downloading places list
    class DownloadPage extends AsyncTask<String, Void, String> {
        String what;
        public DownloadPage(String what){
            this.what=what;
        }
        private String placeaddress;

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(getActivity());
            dialog.setTitle(getString(R.string.search_in_progress)+"...");
            dialog.show();
            super.onPreExecute();
        }

        //download data from Url
        protected String doInBackground(String... urls) {

            String html = "";
            try {
                String url = urls[0];
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(url);
                HttpResponse response = client.execute(request);
                InputStream in = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
                in.close();
                html = str.toString();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return html;
        }//end of doInBackground


        //handle the end of the download to show the data on screen
        protected void onPostExecute(String result) {
            try {
                dialog.dismiss();
            }catch (Exception e){
            }

            try {
                JSONObject objbig = new JSONObject(result);
                JSONArray myarray = objbig.getJSONArray("results");
                SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor savestate=pref.edit();

                //if the search return results
                if (myarray.length()>0){
                    DbHelper helper = new DbHelper(getActivity());
                    ContentValues cv=new ContentValues();
                    if(pref.getString("wSearch","").equals("favorit")){
                        //save the favorites from deleting and if it was "last" dont show them now
                        cv.put(PlacesContract.PlacesFromApi.SEARCHIT, "favorits");
                        helper.getWritableDatabase().update(PlacesContract.PlacesFromApi.TABLE_NAME,cv,PlacesContract.PlacesFromApi.FAVORIT+"='save'",null);
                    }else {

                        //delete anything that is not favorites and not "last", the last will be saved as new "past"
                        helper.getWritableDatabase().delete(PlacesContract.PlacesFromApi.TABLE_NAME, PlacesContract.PlacesFromApi.SEARCHIT+"='past' AND "
                                + PlacesContract.PlacesFromApi.FAVORIT+"='no'", null);
                        //sign "last" as "past" for later use
                        cv.put(PlacesContract.PlacesFromApi.SEARCHIT, "past");
                        helper.getWritableDatabase().update(PlacesContract.PlacesFromApi.TABLE_NAME,cv,PlacesContract.PlacesFromApi.SEARCHIT+"='last'",null);

                    }

                    //save the current state of search
                    savestate.putString("wSearch",what).apply();
                    if (loop){
                        savestate.putString("radius",cRadius).apply();
                        loop=false;
                    }


                    //fill the database from results
                    for (int i = 0; i < myarray.length(); i++) {
                        JSONObject obj = myarray.getJSONObject(i);
                        String placeId = obj.getString("place_id");
                        String placename = obj.getString("name");
                        String url=obj.getString("icon");
                        JSONObject geometry = obj.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        Double lat = location.getDouble("lat");
                        Double lng = location.getDouble("lng");
                        if (what.equals("text")){
                            placeaddress = obj.getString("formatted_address");
                        }else if (what.equals("around")){
                            placeaddress = obj.getString("vicinity");
                        }

                        try {
                            JSONArray photosarray = obj.getJSONArray("photos");
                            JSONObject firstphoto = photosarray.getJSONObject(0);
                            String photoreference = firstphoto.getString("photo_reference");
                            int width = firstphoto.getInt("width");
                            url= "https://maps.googleapis.com/maps/api/place/photo?maxwidth="+width+"&photoreference="+photoreference+"&key=AIza...";
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }

                        DbCommands db = new DbCommands(getActivity());
                        db.addPlace(new PlacesFromApi(placeId, placename, placeaddress, lat, lng,"last","no",url,"nophone","noweb"));
                        Intent intent = new Intent(getActivity(), SearcMoreService.class);
                        intent.putExtra("searchid",placeId);
                        getActivity().startService(intent);

                        helper.close();

                    } //end of for

                    //update ui according to search
                    myActivity.updateList(what);
                    myActivity.changeColor(what);
                    myActivity.changeFragmentback();

                //if no results
                }else {
                    if (!loop){
                        cRadius=radius;
                        loop=true;
                    }


                    if (radius.equals("200")){
                        savestate.putString("radius","500").apply();
                        searchAround();
                    }else if (radius.equals("500")){
                        savestate.putString("radius","1000").apply();
                        searchAround();
                    }else if (radius.equals("1000")){
                        savestate.putString("radius","2000").apply();
                        searchAround();
                    }else if (radius.equals("2000")){
                        savestate.putString("radius","3000").apply();
                        searchAround();
                    }else if (radius.equals("3000")){
                        savestate.putString("radius",cRadius).apply();
                        loop=false;
                    }else {
                        myActivity.showSnackBar("noresults");
                        showPastSearch();
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }//end of onPostExecute


    }//end of subclass DownloadPage


}//end of class SearchFrag


