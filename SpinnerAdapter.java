package com.meri_sg.places_finder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


 // Created on 31-Aug-16.

//spinner designer
public class SpinnerAdapter extends BaseAdapter {

    Context context;
    String[] types;
    LayoutInflater inflter;

    public SpinnerAdapter(Context applicationContext,String[] types) {
        this.context = applicationContext;
        this.types = types;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return 8;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.spinner, null);
        TextView typespinn = (TextView) view.findViewById(R.id.spinnTv);
        typespinn.setText(" "+types[position]+" ");
        return view;    }
}
