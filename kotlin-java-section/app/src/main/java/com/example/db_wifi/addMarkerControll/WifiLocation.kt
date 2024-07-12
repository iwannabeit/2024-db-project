package com.example.db_wifi.addMarkerControll

import java.io.Serializable

data class WifiLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val password: String
) : Serializable