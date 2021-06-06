package com.weather.outsidesky.retrofit;

import com.weather.outsidesky.datamodel.WeatherModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("weather")
    Call<WeatherModel> getWeather(@Query("q") String city, @Query("appid") String appid);

}
