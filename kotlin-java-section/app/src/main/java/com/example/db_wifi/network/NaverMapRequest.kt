package com.example.db_wifi.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NaverMapRequest {
    private const val BASE_URL = "https://naveropenapi.apigw.ntruss.com/map-place/v1/"  // 지도 API의 Base URL

    private val client = OkHttpClient.Builder().addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("X-NCP-APIGW-API-KEY-ID", "t0031hl9ab")  // 클라이언트 ID
            .addHeader("X-NCP-APIGW-API-KEY", "bkM6a2T67diyPPdw97f7zPBaWBjVGlu8XMNKpbMy")  // 클라이언트 비밀 키
            .build()
        chain.proceed(request)
    }.build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getClient(): Retrofit {
        return retrofit
    }
}
