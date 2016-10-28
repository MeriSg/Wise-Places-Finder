package com.meri_sg.places_finder;

/**
 * Created on 20-Jun-16.
 */
public class PlacesFromApi {

    private String placeId;
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private String search;
    private String favorit;
    private String img;
    private String phone;
    private String website;


    public PlacesFromApi(String placeId, String name, String address, Double lat, Double lng, String search, String favorit, String img, String phone,String website) {

        this.placeId = placeId;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.search = search;
        this.favorit = favorit;
        this.img = img;
        this.phone = phone;
        this.website = website;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getSearch() {
        return search;
    }

    public String getFavorit() {
        return favorit;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public String getImg() {
        return img;
    }

    public String getPhone() {
        return phone;
    }

    public String getWebsite() {
        return website;
    }
}
