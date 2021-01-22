package com.example.eindproject.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ClientRetrofit {
    private static Retrofit retroFitClient = null;

    public static Retrofit getClient(){

        if (retroFitClient == null){
            retroFitClient = new Retrofit.Builder()
                    .baseUrl("http://192.168.0.13:5000")
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retroFitClient;
    }
}