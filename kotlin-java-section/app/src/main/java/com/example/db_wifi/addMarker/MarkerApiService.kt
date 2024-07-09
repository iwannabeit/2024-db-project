package com.example.db_wifi.addMarker

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MarkerApiService {
    @POST("addMarker")
    fun addMarker(@Body markerData: MarkerData): Call<Void>
}
