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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
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
import com.google.android.gms.awareness.state.Weather;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.layout.boss.afinal.R.drawable.ic_motorbike;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final int locationPermissionCodeGranted = 1234;
    private Boolean locationPermissionsGranted = false;
    private static final float DEFAULT_ZOOM = 15f;
    private int VEHICLE_MOTORBIKE = 1;
    private int VEHICLE_CAR = 0;
    private static Context context;

    private int[] weatherConditions;
    public String[] parkingName;
    public String[] workingHour;
    public  String[] Slots;
    public Boolean[] motorbikes;
    public Boolean[] cars;

    private String[] creatorName = {"Nguyen Mach Thanh Vy", "Ta Minh Khoi", "Tran Thanh Thao"};
    private String[] creatorID = {"1651010 16CTT", "1651050 16CTT", "1651070 16CTT"};
    private String[] creatorEmail = {"bla", "bla", "bla"};

    private EditText searchText;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallBack;

    public Location currentLocation;
    private LatLng currentLatLng;
    private Weather mWeather;

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
        //initWeather();
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
            LatLng destination = new LatLng(address.getLatitude(), address.getLongitude());
            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            moveCamera(destination, DEFAULT_ZOOM, "new location");

            //Add a marker
            mMap.addMarker(new MarkerOptions().position(destination));
            openNearestParkingLots(destination);
        }
    }

    private void openNearestParkingLots(LatLng destination) {

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
    private void addAllParkingLotsMarker() {
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
        //MORE WIDGETS BUTTON
        Button moreWidget = findViewById(R.id.moreWidgets);
        moreWidget.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View dialogView = inflater.inflate(R.layout.more_widgets_button, null);

                builder.setView(dialogView);

                ImageButton weather = dialogView.findViewById(R.id.button_weather);
                ImageButton allParkingInfo = dialogView.findViewById(R.id.button_allInfo);
                ImageButton aboutUs = dialogView.findViewById(R.id.aboutUs);

                //ALL PARKING LOTS INFORMATION

                allParkingInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initAllParkingInfo();
                    }
                });

                aboutUs.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initAboutUSLayout();
                    }
                });
                //CHANGE WEATHER ICON

                /*weather.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //getWeatherInformation();
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(MapsActivity.this);
                        LayoutInflater inflater1 = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View weatherInfo = inflater1.inflate(R.layout.weather_information, null);

                        builder1.setView(weatherInfo);

                        TextView humidity = weatherInfo.findViewById(R.id.weather_humidity);
                        TextView temperature = weatherInfo.findViewById(R.id.weather_temperature);
                        humidity.setText("Nothing yet");
                        temperature.setText("Nothing yet");

                        AlertDialog alertDialogWeather = builder1.create();
                        alertDialogWeather.show();
                    }
                });*/

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }


        });
        }

    private void getWeatherInformation() {
        weatherConditions = mWeather.getConditions() ;
    }

    private void initVehicle(){
        //VEHICLE
        final ImageButton vehicleOptions = findViewById(R.id.vehicle_option);
        vehicleOptions.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //setContentView(R.layout.vehicle_box);

                AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MapsActivity.this);

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = inflater.inflate(R.layout.vehicle_box, null);

                builder.setView(dialogView);

                final ImageButton motorbike = dialogView.findViewById(R.id.motorbike);
                ImageButton car = dialogView.findViewById(R.id.car);
                //MOTORBIKE
                motorbike.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        //change icon to motorbike
                        VEHICLE_MOTORBIKE = 1;
                        VEHICLE_CAR = 0;
                        vehicleOptions.setBackgroundResource(R.drawable.ic_motorbike);
                        }});
                //CAR
                car.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        //change icon to car
                        VEHICLE_CAR = 1;
                        VEHICLE_MOTORBIKE = 0;
                        vehicleOptions.setBackgroundResource(R.drawable.ic_car);
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
        addAllParkingLotsMarker();
    }
    private void initAllParkingInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View allParkingInfo = inflater.inflate(R.layout.all_parking_info, null);
        builder.setView(allParkingInfo);

        ListView listView = allParkingInfo.findViewById(R.id.all_parking_info);
        parkingInfoAdapter adapter = new parkingInfoAdapter();
        listView.setAdapter(adapter);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void initAboutUSLayout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View aboutUs = inflater.inflate(R.layout.about_us_info, null);
        builder.setView(aboutUs);

        ListView listView = aboutUs.findViewById(R.id.aboutus_listview);
        creatorInfoAdapter adapter = new creatorInfoAdapter();
        listView.setAdapter(adapter);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(curLocation));

            //Set up location request
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(120000); // two minute interval
            mLocationRequest.setFastestInterval(120000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
    }

    //ADAPTER
    class parkingInfoAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return parkingName.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.all_parking_info, null);
            TextView parkingNames, workingHours, slotNumber, vehicle_types;
            parkingNames = convertView.findViewById(R.id.parking_lot_name);
            workingHours = convertView.findViewById(R.id.working_hour);
            slotNumber = convertView.findViewById(R.id.slot_number);
            vehicle_types = convertView.findViewById(R.id.vehicle_type);
            String vehicleTypes = "";

            parkingNames.setText(parkingName[position]);
            workingHours.setText("Works from: "+workingHour[position]);
            slotNumber.setText("Available slots: "+Slots[position]);
            if (motorbikes[position])
                vehicleTypes = vehicleTypes+"motorbikes ";
            if (cars[position])
                vehicleTypes = vehicleTypes+"cars ";
            vehicle_types.setText(vehicleTypes);
            return convertView;
        }

    }
    class creatorInfoAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return creatorName.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView avatar;
            TextView name, ID, email;
            convertView = getLayoutInflater().inflate(R.layout.aboutus_listview, null);
            name = convertView.findViewById(R.id.self_name);
            ID = convertView.findViewById(R.id.self_ID);
            email = convertView.findViewById(R.id.self_email);
            avatar = convertView.findViewById(R.id.self_image);

            avatar.setImageResource(R.drawable.ic_motorbike);
            name.setText(creatorName[position]);
            ID.setText(creatorID[position]);
            email.setText(creatorEmail[position]);
            return convertView;
        }
    }
}

