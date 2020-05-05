package com.example.assignment03;

/* Address item that hold the address and coordinates for recycler view */
public class AddressItem {
    private String address; //address name string
    private double latitude; //address latitude coordinate
    private double longitude; //address longitude coordinate

    public AddressItem(String ads, double lat, double lng) {
        address = ads;
        latitude = lat;
        longitude = lng;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
