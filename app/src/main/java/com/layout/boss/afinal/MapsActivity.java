package com.layout.boss.afinal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final int locationPermissionCodeGranted = 1234;
    private Boolean locationPermissionsGranted = false;
    private static final float DEFAULT_ZOOM = 15f;
    private int VEHICLE_MOTORBIKE = 1;
    private int VEHICLE_CAR = 0;
    private static Context context;

    private EditText searchText;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallBack;

    private Location currentLocation;
    private LatLng currentLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //UI stuff
        setContentView(R.layout.activity_maps);
        searchText = findViewById(R.id.input_search);

        //Mapping stuff
        initMap();                  //Initialize the map

        locationPermission();       //Check for permission
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);       //Set up the manager
        mLocationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    //The last location in the list is the newest
                    Location location = locationList.get(locationList.size() - 1);
                    Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                    currentLocation = location;
                }
            }
        };
        context = getApplicationContext();
        init();                     //Initialize the search bar
        initVehicle();
    }

    //Location stuff
    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = searchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, "new location");
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting current location");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            locationPermission();
            return;
        } else {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, null);
        }
    }

    //UI Stuff
    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        //Move to a new location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        //Add a marker to the current location
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
        mMap.addMarker(markerOptions);
    }


    //Initialization - On Start stuff
    private void locationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        //Request permission
        ActivityCompat.requestPermissions(this, permissions, locationPermissionCodeGranted);

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionsGranted = true;                                                                             //Set permission to granted
            Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();                             //Misc display
        } else {
            Toast.makeText(this, "Location access denied!", Toast.LENGTH_SHORT).show();                         //Misc display
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        locationPermissionsGranted = false;

        switch (requestCode) {
            case locationPermissionCodeGranted: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            locationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: failed to have permission.");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: got permission.");
                    locationPermissionsGranted = true;
                }
            }
        }
    }

    private void init() {
        Log.d(TAG, "init: initializing");
        //SEARCH ACTION BUTTON
        ImageButton search = findViewById(R.id.ic_magnify);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geoLocate();
            }
        });
        /*TODO: why is there a check for user search input in initialization? Shouldn't it initMap and geoLocate as default?*/
        //TEXTBOX
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    geoLocate();
                }
                //TODO: why false condition here?
                return false;
            }
        });
        //CURRENT LOCATION
        ImageButton navigation = findViewById(R.id.curLocation_button);
        navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveCamera(currentLatLng, DEFAULT_ZOOM, "Navigating");
                }
            });
        }
    private void initVehicle(){
        //VEHICLE
        ImageButton vehicleOptions = findViewById(R.id.vehicle_option);
        vehicleOptions.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                View dialogView = inflater.inflate(R.layout.vehicle_box, null);

                builder.setView(dialogView);

                ImageButton motorbike = findViewById(R.id.motorbike);
                ImageButton car = findViewById(R.id.car);
                //MOTORBIKE
                motorbike.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        //change icon to motorbike
                        VEHICLE_MOTORBIKE = 1;
                        VEHICLE_CAR = 0;
                        }});
                //CAR
                car.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        //change icon to car
                        VEHICLE_CAR = 1;
                        VEHICLE_MOTORBIKE = 0;
                        }});

                AlertDialog dialog = builder.create();
                dialog.show();
                }}
        );
    }
    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        } else {
            //Set up the map
            Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
            mMap = googleMap;
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            //Set the initial location to current location
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng curLocation = new LatLng(location.getLatitude(), location.getLongitude());
            location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(curLocation.latitude);
            location.setLongitude(curLocation.longitude);
            currentLocation = location;
            currentLatLng = curLocation;

            //Add a marker
            googleMap.addMarker(new MarkerOptions().position(curLocation));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(curLocation));

            //Set up location request
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(120000); // two minute interval
            mLocationRequest.setFastestInterval(120000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
    }
}

