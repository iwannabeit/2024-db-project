package com.example.db_wifi.network;

import retrofit2.Call;
import retrofit2.http.GET;

public interface NaverMapApiInterface {
    @GET("/v1/driving")
    Call<NaverMapItem> getMapData();
}