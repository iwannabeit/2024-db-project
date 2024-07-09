package com.example.db_wifi

import java.io.Serializable

data class WifiLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double
) : Serializable
