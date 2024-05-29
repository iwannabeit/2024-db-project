package com.example.db_wifi;

import com.google.gson.annotations.SerializedName;

public class NaverMapData {
    @SerializedName("id")
    private int id;

    @SerializedName("region")
    private String region;

    @SerializedName("city")
    private String city;

    @SerializedName("address")
    private String address;

    @SerializedName("place")
    private String place;

    @SerializedName("x")
    private double x;
    @SerializedName("y")
    private double y;
    @SerializedName("side")
    private String side;


    public int getId() {
        return id;
    }

    public String getRegion() {
        return region;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getPlace(){
        return place;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public String getSide() { return side;}

}
