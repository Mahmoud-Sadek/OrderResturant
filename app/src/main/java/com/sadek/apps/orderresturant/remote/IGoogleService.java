package com.sadek.apps.orderresturant.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by Mahmoud Sadek on 11/11/2018.
 */

public interface IGoogleService {
    @GET
    Call<String> getAdressName(@Url String url);

    @GET
    Call<String> getLocationFromAddress(@Url String url);

    @GET("maps/api/directions/json")
    Call<String> getDirections(@Query("origin") String origin, @Query("destination") String destination, @Query("key")String key);
}
