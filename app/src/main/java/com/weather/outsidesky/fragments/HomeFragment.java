package com.weather.outsidesky.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.weather.outsidesky.R;
import com.weather.outsidesky.databinding.FragmentHomeBinding;
import com.weather.outsidesky.datamodel.WeatherModel;
import com.weather.outsidesky.utils.MyMVVM;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class HomeFragment extends Fragment implements View.OnClickListener {

    FragmentHomeBinding homeBinding;

    private View view;

    private String city = "jammu";
    private String apiKey = "79c5ee2c24e8da505b11b5ef529dbf57";

    private EditText searchET;

    private MyMVVM myMVVM;

    private ImageView searchImg;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private Location mLastKnownLocation;

    private double latitude, longitude;

    private LocationCallback locationCallback;

    private String completeAddress, city_name;

    private LocationManager locationManager;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myMVVM = ViewModelProviders.of(HomeFragment.this).get(MyMVVM.class);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        homeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        view = homeBinding.getRoot();

        findIds();

        getDeviceLocation();

        showWeather(city);

        return homeBinding.getRoot();
    }

    private void findIds() {

        searchImg = view.findViewById(R.id.searchImg);

        searchET = view.findViewById(R.id.searchET);

        searchImg.setOnClickListener(this);

    }

    private void showWeather(String cityN) {
        myMVVM.weatherLive(getActivity(), city, apiKey).observe(getActivity(), new Observer<WeatherModel>() {
            @Override
            public void onChanged(WeatherModel weatherModel) {
                homeBinding.setTemprature(weatherModel);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tempTxt :{
                showWeather(city);
                break;
            }
            case R.id.searchImg:{
                city = searchET.getText().toString().trim();
                showWeather(city);
            }
        }
    }


    //Get Location as lat and long

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getDeviceLocation() {
        checkPermission();
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                latitude = mLastKnownLocation.getLatitude();
                                longitude = mLastKnownLocation.getLongitude();
                                Toast.makeText(getActivity(), "Location fetched"+latitude+longitude, Toast.LENGTH_SHORT).show();

//                                getAddress(latitude,longitude);

                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        mLastKnownLocation = locationResult.getLastLocation();
                                        latitude = mLastKnownLocation.getLatitude();
                                        longitude = mLastKnownLocation.getLongitude();

                                        Toast.makeText(getActivity(), "Last Location fetched", Toast.LENGTH_SHORT).show();

//                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };

                                checkPermission();
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }

                            getAddress(latitude, longitude);

                        } else {
                            Toast.makeText(getActivity(), "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Get Complete Address with Lat and Long

    public String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {

            System.out.println("get address");
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                System.out.println("size====" + addresses.size());
                Address address = addresses.get(0);

                city_name = address.getLocality();

//                location_txt.setText(city_name);

                searchET.setText(city_name);
                Toast.makeText(getActivity(), city_name+"this is city", Toast.LENGTH_LONG).show();

                for (int i = 0; i <= addresses.get(0).getMaxAddressLineIndex(); i++) {
                    if (i == addresses.get(0).getMaxAddressLineIndex()) {
                        result.append(addresses.get(0).getAddressLine(i));
                    } else {
                        result.append(addresses.get(0).getAddressLine(i) + ",");
                    }
                }
                System.out.println("ad==" + address);
                System.out.println("result---" + result.toString());
                completeAddress = result.toString();
                Log.i("my", completeAddress);
//                addressz.setText(result.toString()); // Here is you AutoCompleteTextView where you want to set your string address (You can remove it if you not need it)
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }





    //Check current status for location fetch service

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void statusCheck() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }


    //create Permissioins request dialog

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    //Check permissions

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            statusCheck();
            return;
        } else {
            statusCheck();
        }
    }
}