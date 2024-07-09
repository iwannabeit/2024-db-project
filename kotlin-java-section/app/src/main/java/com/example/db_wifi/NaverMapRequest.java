package com.example.db_wifi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NaverMapRequest {
    public static String BASE_URL = "http://192.168.0.7/2024-db-project/php-section/";
//    public static String BASE_URL = "http://192.168.0.52/2024-db-project/php-section/";


    private static Retrofit retrofit;
    public static Retrofit getClient(){

        if(retrofit == null){
            retrofit = new Retrofit.Builder() // retrofit 객체 생성
                    .baseUrl(BASE_URL) // BASE_URL로 통신
                    .addConverterFactory(GsonConverterFactory.create()) // gson-converter로 데이터 parsing
                    .build();
        }
        return retrofit;
    }
}