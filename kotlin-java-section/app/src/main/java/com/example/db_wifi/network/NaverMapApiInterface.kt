package com.example.db_wifi.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverMapApiInterface {
    @GET("search")
    fun getMapData(
        @Query("query") query: String,
        @Query("coordinate") coordinate: String,
        @Query("filter") filter: String
    ): Call<NaverMapItem>
}
