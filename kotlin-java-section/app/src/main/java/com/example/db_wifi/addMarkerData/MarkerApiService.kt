package com.example.db_wifi

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MarkerApiService {
    @POST("addMarker.php")
    fun addMarker(@Body markerData: MarkerData): Call<Void>
}
