package com.meri_sg.places_finder;


//Created on 21-Jun-16.

public interface FragmentListener {

    void changeFragments(String chosenPlace,Double clat,Double clng,String cPhone);

    void changeFragmentback();

    void updateList(String what);

    void changeColor(String what);

    void showSnackBar(String message);

    void refreshList();
}
