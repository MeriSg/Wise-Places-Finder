package com.meri_sg.places_finder;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class SearcMoreService extends IntentService {

    public SearcMoreService() {
        super("SearcMoreService");
    }

    //get extra data for each place from google api
    @Override
    protected void onHandleIntent(Intent intent) {

        String searchid=intent.getStringExtra("searchid");

        Log.wtf("msg",searchid );


        String html = "";
        try {
            String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid="+searchid+"&key=AIza....";
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

        Log.wtf("msg", html);


        try {
            JSONObject obj = new JSONObject(html);
            JSONObject result = obj.getJSONObject("result");
            String phonenumber = "nophone";
            try {
                phonenumber = result.getString("formatted_phone_number");
            }catch (JSONException e) {
                e.printStackTrace();
            }
            String website = "noweb";
            try {
                website = result.getString("website");
            }catch (JSONException e) {
                e.printStackTrace();
            }

            //update the extra data to DB
            DbHelper helper=new DbHelper(this);
            ContentValues cv = new ContentValues();
            cv.put(PlacesContract.PlacesFromApi.PHONE, phonenumber);
            cv.put(PlacesContract.PlacesFromApi.WEBSITE, website);
            helper.getWritableDatabase().update(PlacesContract.PlacesFromApi.TABLE_NAME, cv, PlacesContract.PlacesFromApi.PLACEID + "='"+searchid+"'", null);
            this.getContentResolver().notifyChange(PlacesContract.PlacesFromApi.CONTENT_URI, null);
            helper.close();

        } catch (JSONException e) {
            e.printStackTrace();
        }




    }//end of onHandleIntent



}//end of SearcMoreService
