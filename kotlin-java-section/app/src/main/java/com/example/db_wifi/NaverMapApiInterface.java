package com.example.db_wifi;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface NaverMapApiInterface {
    @GET("demo.php")
    Call<NaverMapItem> getMapData();
}