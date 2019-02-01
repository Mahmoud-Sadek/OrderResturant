package com.sadek.apps.orderresturant.remote;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by Mahmoud Sadek on 11/29/2018.
 */

public class GoogleRetrofitClient  {
    public static Retrofit retrofitScalar = null;

    public static Retrofit getGoogleClient(String baseUrl_device_position) {
        if (retrofitScalar == null) {
            retrofitScalar = new Retrofit.Builder()
                    .baseUrl(baseUrl_device_position)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofitScalar;
    }

}
