package com.meri_sg.places_finder;


import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListPlacesFrag extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public MyCursorAdapter adapter;
    public Cursor cursor;
    public FragmentListener myActivity;
    public String wSearch="what";
    public SwipeRefreshLayout swipeContainer;

    //attach activity to the fragment
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            myActivity=(FragmentListener) activity;
        }catch (ClassCastException e){
            Log.d("err","activity must implement FragmentListener");
        }
    }//end of onAttach

    public ListPlacesFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_list_places, container, false);

        //initiate pull to refresh container
        swipeContainer=(SwipeRefreshLayout)v.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myActivity.refreshList();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright);

        ListView lv=(ListView)v.findViewById(R.id.placesLv);
        registerForContextMenu(lv);

        adapter=new MyCursorAdapter(getActivity(),null);
        lv.setAdapter(adapter);

        //on short click, send the chosen place details to the map, using method at MainActivity
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                view.showContextMenu();



            }
        });

        //register to loader
        getLoaderManager().initLoader(1,null,this);

        return v;

    }//end of onCreateView


    //handle loader to update the list
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),PlacesContract.PlacesFromApi.CONTENT_URI,null, PlacesContract.PlacesFromApi.SEARCHIT+" = 'last'",null ,PlacesContract.PlacesFromApi.DISTANCE+" ASC");
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.setNotificationUri(getActivity().getContentResolver(),PlacesContract.PlacesFromApi.CONTENT_URI);
        adapter.swapCursor(data);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
    //end of loader


    //inflate different menu for favorite or not
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (wSearch.equals("favorit")){
            getActivity().getMenuInflater().inflate(R.menu.favoritsmenu,menu);
            super.onCreateContextMenu(menu, v, menuInfo);
        }else {
            getActivity().getMenuInflater().inflate(R.menu.listmenu,menu);
            super.onCreateContextMenu(menu, v, menuInfo);
        }
    } //end of onCreateContextMenu


    //handle when choosing item in context menu
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo inf=(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        //adding chosen place to favorites list
        if (item.getItemId()==R.id.addfavorit){
            cursor=(Cursor)adapter.getItem(inf.position);
            String placeId=cursor.getString(cursor.getColumnIndex(PlacesContract.PlacesFromApi.PLACEID));
            String placename=cursor.getString(cursor.getColumnIndex(PlacesContract.PlacesFromApi.NAME));
            String placeaddress=cursor.getString(cursor.getColumnIndex(PlacesContract.PlacesFromApi.ADDRESS));
            Double lat= cursor.getDouble(cursor.getColumnIndex(PlacesContract.PlacesFromApi.LAT));
            Double lng= cursor.getDouble(cursor.getColumnIndex(PlacesContract.PlacesFromApi.LNG));
            String url=cursor.getString(cursor.getColumnIndex(PlacesContract.PlacesFromApi.IMG));
            String placePhone= cursor.getString(cursor.getColumnIndex(PlacesContract.PlacesFromApi.PHONE));
            String placeWebsite= cursor.getString(cursor.getColumnIndex(PlacesContract.PlacesFromApi.WEBSITE));

            //add favorite only if specific placeID doesn't exist in favorites already
            DbHelper helper = new DbHelper(getActivity());
            cursor=helper.getReadableDatabase().rawQuery("SELECT "+PlacesContract.PlacesFromApi.PLACEID+","+PlacesContract.PlacesFromApi.FAVORIT
                    +" FROM "+ PlacesContract.PlacesFromApi.TABLE_NAME
                    +" WHERE "+ PlacesContract.PlacesFromApi.PLACEID+"='"+placeId+"'"
                    +" AND "+ PlacesContract.PlacesFromApi.FAVORIT+"='save'", null);
            if (cursor.getCount()==0){
                DbCommands db = new DbCommands(getActivity());
                db.addPlace(new PlacesFromApi(placeId ,placename, placeaddress, lat, lng,"favorits","save",url,placePhone,placeWebsite));
            }

        //removing chosen place from favorites list
        }else if (item.getItemId()==R.id.removefavorit){
            cursor=(Cursor)adapter.getItem(inf.position);
            String id=cursor.getString(cursor.getColumnIndex(PlacesContract.PlacesFromApi.ID));
            DbHelper helper = new DbHelper(getActivity());
            helper.getWritableDatabase().delete(PlacesContract.PlacesFromApi.TABLE_NAME,PlacesContract.PlacesFromApi.ID+"="+id+" AND "
                    + PlacesContract.PlacesFromApi.FAVORIT+"='save'",null);
            getActivity().getContentResolver().notifyChange(PlacesContract.PlacesFromApi.CONTENT_URI,null);

        //show on map
        }else if (item.getItemId()==R.id.map){

            cursor=(Cursor)adapter.getItem(inf.position);
            String chosenPlace= cursor.getString(cursor.getColumnIndex(PlacesContract.PlacesFromApi.NAME));
            Double chosenLat= cursor.getDouble(cursor.getColumnIndex(PlacesContract.PlacesFromApi.LAT));
            Double chosenLng= cursor.getDouble(cursor.getColumnIndex(PlacesContract.PlacesFromApi.LNG));
            String chosenPhone= cursor.getString(cursor.getColumnIndex(PlacesContract.PlacesFromApi.PHONE));
            Log.d("err","zzzzzzzzzzzzzzzzzzzzzzz "+chosenPlace+" "+chosenLat+" "+chosenLng);
            myActivity.changeFragments(chosenPlace ,chosenLat,chosenLng,chosenPhone);


            //send chosen place details to a friend
        }else if (item.getItemId()==R.id.send){
            cursor=(Cursor)adapter.getItem(inf.position);
            String placename=cursor.getString(cursor.getColumnIndex(PlacesContract.PlacesFromApi.NAME));
            String address=cursor.getString(cursor.getColumnIndex(PlacesContract.PlacesFromApi.ADDRESS));
            Intent sendintent=new Intent(android.content.Intent.ACTION_SEND);
            sendintent.setType("text/plain");

            sendintent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.check_this_place)+" \""+placename+"\"");
            sendintent.putExtra(Intent.EXTRA_TEXT,getString(R.string.the_place)+" "+placename+"\n\n"+getString(R.string.the_address)+"\n"+address+"\n\n");

            startActivity(Intent.createChooser(sendintent,getString(R.string.how_to_share)));

        //call the phone of the chosen place if exists
        }else if (item.getItemId()==R.id.call){
            cursor=(Cursor)adapter.getItem(inf.position);
            String placephone=cursor.getString(cursor.getColumnIndex(PlacesContract.PlacesFromApi.PHONE));

            //check if device is able to use the phone call action
            boolean isphone = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
            if (isphone ){
                if (!placephone.equals("nophone")){
                    String uri = "tel:" + placephone.trim() ;
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }else {
                    myActivity.showSnackBar("nophone");
                }
            }else {
                myActivity.showSnackBar("nocall");
            }

        //go to chosen place website if exist
        }else if (item.getItemId()==R.id.web){
            cursor=(Cursor)adapter.getItem(inf.position);
            String placeweb=cursor.getString(cursor.getColumnIndex(PlacesContract.PlacesFromApi.WEBSITE));

            //check if is currently connected to internet
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
            boolean isOnline = cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
            if (isOnline) {
                if (placeweb==null||placeweb.equals("noweb")) {
                    myActivity.showSnackBar("noweb");
                } else {
                    Intent webintent = new Intent(Intent.ACTION_VIEW, Uri.parse(placeweb));
                    startActivity(Intent.createChooser(webintent, getString(R.string.how_to_watchw)));
                }

            }else {
                myActivity.showSnackBar("nonet");
            }

        }

        return super.onContextItemSelected(item);

    }//end of onContextItemSelected




}//end of ListPlacesFrag







//ty, meri sg