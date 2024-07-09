package com.example.db_wifi.addMarker
import com.google.gson.annotations.SerializedName

data class MarkerData(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("description") val description: String
)
