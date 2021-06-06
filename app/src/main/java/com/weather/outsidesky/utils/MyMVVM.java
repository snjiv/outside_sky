package com.weather.outsidesky.utils;

import android.app.Activity;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.weather.outsidesky.datamodel.WeatherModel;
import com.weather.outsidesky.retrofit.ApiClient;
import com.weather.outsidesky.retrofit.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyMVVM extends ViewModel {

    ApiInterface apiInterface = new ApiClient().getClient().create(ApiInterface.class);

    private MutableLiveData<WeatherModel> weatherMutable;

    public LiveData<WeatherModel> weatherLive(Activity activity, String city, String apikey){
        weatherMutable = new MutableLiveData<>();

        apiInterface.getWeather(city, apikey).enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                if(response.message().equalsIgnoreCase("ok")){
                    weatherMutable.postValue(response.body());
                }
                else
                {
                    Toast.makeText(activity, response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherModel> call, Throwable t) {
                Toast.makeText(activity, t.getMessage()+" this wala toast .....", Toast.LENGTH_SHORT).show();
            }
        });

        return weatherMutable;
    }
}
