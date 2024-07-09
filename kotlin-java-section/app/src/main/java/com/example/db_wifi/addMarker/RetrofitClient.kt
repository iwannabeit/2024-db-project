package com.example.db_wifi.addMarker

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {


    private const val BASE_URL = "http://192.168.0.7/2024-db-project/php-section/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val markerApiService: MarkerApiService = retrofit.create(MarkerApiService::class.java)
}