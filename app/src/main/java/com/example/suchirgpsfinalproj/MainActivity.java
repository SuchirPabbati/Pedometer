package com.example.suchirgpsfinalproj;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    TextView Lat;
    TextView Long;
    TextView addressText,distanceText,timeText;
    ArrayList<Double> times = new ArrayList<Double>();
    Double LatNum;
    Double LongNum;
    Double recentTime=0.0;
    Geocoder mGeocoder;
    final int MY_PERMISSION_FINE_LOCATION=1;
    LocationManager locationManager;
    Location prevLoc = null;
    boolean check = true;
    List<Address> addressList = new ArrayList<>();
    double distance;
    private long timeStart = SystemClock.elapsedRealtime();
    Double longestTime = 0.0;
    Double shortestTime =0.0;
    Double elapStart;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Lat = findViewById(R.id.latText);
        Long = findViewById(R.id.lonText);
        addressText = findViewById(R.id.textView2);
        timeText = findViewById(R.id.textTime);
        distanceText = findViewById(R.id.distanceText);
        if(savedInstanceState!=null){
            distance = savedInstanceState.getDouble("distance");
            longestTime = savedInstanceState.getDouble("timeLong");
            recentTime = savedInstanceState.getDouble("recentTime");
            //shortestTime = savedInstanceState.getDouble("shortestTime");

            //saving the saveInstance back to original variables

        }
        distanceText.setText("Distance: "+distance+" m");


        mGeocoder = new Geocoder(this, Locale.US);



        Lat.setText("Latitude: ");
        Long.setText("Longitude: ");

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //if perms are granted ask for location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,8,this);
            //checks if perms are granted if not req perms

        } else if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_FINE_LOCATION);

        }


    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        LatNum = location.getLatitude();
        LongNum = location.getLongitude();
        Lat.setText("Latitude: " + LatNum);
        Long.setText("Longitude: " + LongNum);
        try {
            //create arraylist of addresses
            addressList = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            addressText.setText(addressList.get(0).getAddressLine(0));
        }
        catch(IOException e){
            Log.d("TAGA","Address fail");
        }

        if (prevLoc == null)
            prevLoc = location;
        //check if there was a prevLocation
        //waits 15 seconds before changing distance
        else if(SystemClock.elapsedRealtime()>15000){
            distance += location.distanceTo(prevLoc);
            distanceText.setText("Distance Traveled: " + distance+" meters");
            prevLoc = location;
        }
        //gets the time activity started minus the time of location change
        elapStart = (SystemClock.elapsedRealtime()-timeStart)/1000.0;
        if(check) {
            elapStart = 0.0;
            check = false;
        }
        //adds the time to an arraylist of previous times
        times.add(elapStart);
        for(int i = 0; i < times.size(); i++){
            if(i!=0) {
                Double temp = times.get(i) - times.get(i - 1);
                recentTime = temp;
                if (longestTime < temp) {
                    longestTime = temp;
                }
            }
        }

        //for loop to check for the longest time in the arraylist

        timeText.setText("Longest Time:"+ roundAvoid(longestTime,3)+" s\nMost recent time: "+roundAvoid(recentTime,3)+" s");


    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //saving variables
        outState.putDouble("distance", distance);
        outState.putDouble("timeLong",longestTime);
        outState.putDouble("recentTime",recentTime);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                //checks perms and reqs locations
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try{
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,8,this);
                    }
                    catch (SecurityException eo){

                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //remove listener
        locationManager.removeUpdates(MainActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
         if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_FINE_LOCATION);
             return;
         }

    }
    public static double roundAvoid(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
}
