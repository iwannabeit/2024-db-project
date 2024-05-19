package com.example.db_wifi.network;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NaverMapRequest {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain -> {
                return chain.proceed(chain.request().newBuilder()
                        .addHeader("X-NCP-APIGW-API-KEY-ID", "f5wddcflyd")
                        .addHeader("X-NCP-APIGW-API-KEY", "1isX4VdL0ZA5GRCTfcVFYNFoucNx46Vcz5FGdm2m")
                        .build());
            }).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
