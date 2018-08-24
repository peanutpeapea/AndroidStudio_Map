package com.layout.boss.afinal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapsActivity";
    private static final int locationPermissionCodeGranted = 1234;
    private Boolean locationPermissionsGranted = false;
    private static final float DEFAULT_ZOOM = 15f;
    private int VEHICLE_MOTORBIKE = 1;
    private int VEHICLE_CAR = 0;
    private static Context context;

    private int[] weatherConditions;
    public String[] parkingName;

    private Marker markers[];

    ParkingLot[] parkingLots;
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
    FloatingActionButton allParkingInfo, aboutUs, moreWidgets, dismissWidgets;

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
        LatLng[] nearestParkingLots = CalculateDistance(destination);
        ParkingLot[] neededParkingLot = new ParkingLot[3];
        for (int i=0; i<3; i++){
            for (int j=0; j<21; j++){
                if (nearestParkingLots[i].longitude == parkingLots[j].getLongtitude()
                        && nearestParkingLots[i].latitude==parkingLots[j].getLatitude())
                    neededParkingLot[i] = parkingLots[j];
            }
        }
        mMap.clear();
        Marker[] neededMarkers = putMarker(neededParkingLot);
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
        parkingLots = CreateParkingLot();

        //SEARCH ACTION BUTTON;
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
        moreWidgets = findViewById(R.id.fab);
        moreWidgets.setImageBitmap(textAsBitmap("+",20, Color.WHITE));

        allParkingInfo = findViewById(R.id.button_allInfo);
        allParkingInfo.setImageResource(R.drawable.ic_parkinglot);

        aboutUs = findViewById(R.id.aboutUs);
        aboutUs.setImageResource(R.drawable.ic_aboutus);

        dismissWidgets = findViewById(R.id.fab_dismiss);
        dismissWidgets.setImageBitmap(textAsBitmap("X", 20, Color.WHITE));
        moreWidgets.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
            showMoreWidget();
            }
        });
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
        dismissWidgets.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setDismissWidgets();
            }
        });
        }
    public static Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.0f); // round
        int height = (int) (baseline + paint.descent() + 0.0f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }
    public void showMoreWidget(){
        allParkingInfo.show();
        aboutUs.show();
        dismissWidgets.show();
        moreWidgets.hide();
    }
    public void setDismissWidgets(){
        allParkingInfo.hide();
        aboutUs.hide();
        dismissWidgets.hide();
        moreWidgets.show();
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
    }
    private void initAllParkingInfo() {
        mMap.clear();
        markers = putMarker(parkingLots);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.12);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, padding);
        mMap.animateCamera(cameraUpdate);
        mMap.setOnMarkerClickListener(this);
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
    private LatLng[] CalculateDistance(LatLng destination){
        float[] distance = new float[21];
        LatLng[] result = new LatLng[3];
        float[] results = new float[10];
        LatLng[] latLng = new LatLng[21];
        ListOfLatLng[] listOfLatLng = CreateList();
        for(int i=0;i<listOfLatLng.length;++i){
            latLng[i] = new LatLng(listOfLatLng[i].getLat(), listOfLatLng[i].getLng());
        }
        double end_latitude, end_longitude;
        for(int i=0; i<distance.length;++i){
            end_latitude = latLng[i].latitude;
            end_longitude = latLng[i].longitude;
            Location.distanceBetween(destination.latitude, destination.longitude, end_latitude, end_longitude, results);
            distance[i]=results[0];
            Log.d("Distance", String.valueOf(distance[i]));
        }
        float[] finalDistance =  distance;
        Arrays.sort(finalDistance);
        for (int i=0; i<3; i++){
            for (int j=0; j<21; j++){
                if (finalDistance[i]==distance[j])
                    result[i]=latLng[j];
            }

        }
        return result;
    }
    private ListOfLatLng[] CreateList(){
        ListOfLatLng[] listOfLatLngs = new ListOfLatLng[21];
        listOfLatLngs[0] = new ListOfLatLng(10.769299, 106.6976081);
        listOfLatLngs[1] = new ListOfLatLng(10.7710947, 106.7038785);
        listOfLatLngs[2] = new ListOfLatLng(10.7710946, 106.695691);
        listOfLatLngs[3] = new ListOfLatLng(10.772948, 106.7022655);
        listOfLatLngs[4] = new ListOfLatLng(10.7770484, 106.7011244);
        listOfLatLngs[5] = new ListOfLatLng(10.7771705, 106.6867483);
        listOfLatLngs[6] = new ListOfLatLng(10.7812634, 106.6939001);
        listOfLatLngs[7] = new ListOfLatLng(10.7768643, 106.702604);
        listOfLatLngs[8] = new ListOfLatLng(10.7774164, 106.699404);
        listOfLatLngs[9] = new ListOfLatLng(10.7774163, 106.6928379);
        listOfLatLngs[10] = new ListOfLatLng(10.7795121, 106.6909072);
        listOfLatLngs[11] = new ListOfLatLng(10.781123, 106.6995238);
        listOfLatLngs[12] = new ListOfLatLng(10.7799927, 106.6969962);
        listOfLatLngs[13] = new ListOfLatLng(10.7798579, 106.6977188);
        listOfLatLngs[14] = new ListOfLatLng(10.7827846, 106.6986138);
        listOfLatLngs[15] = new ListOfLatLng(10.7745714, 106.701146);
        listOfLatLngs[16] = new ListOfLatLng(10.772147, 106.7047488);
        listOfLatLngs[17] = new ListOfLatLng(10.774121, 106.7027408);
        listOfLatLngs[18] = new ListOfLatLng(10.7764498, 106.6904771);
        listOfLatLngs[19] = new ListOfLatLng(10.7732471, 106.6907314);
        listOfLatLngs[20] = new ListOfLatLng(10.77305, 106.6911546);
        return listOfLatLngs;
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
    private Marker[] putMarker(ParkingLot[] parkingLots) {
        Marker[] markers = new Marker[parkingLots.length];
        for (int i = 0; i < parkingLots.length; ++i) {
            markers[i] = mMap.addMarker(new MarkerOptions()
                    .title(parkingLots[i].getName())
                    .position(new LatLng(parkingLots[i].getLatitude(), parkingLots[i].getLongtitude())));
            markers[i].setTag(parkingLots[i]);
        }
        return markers;
    }
    //PARKING STUFF
    private ParkingLot[] CreateParkingLot() {
        ParkingLot[] parkingLots = new ParkingLot[21];
        parkingLots[0] = new ParkingLot(10.769299, 106.6976081, "Bãi Giữ Xe Bảo Tàng Mỹ Thuật", R.drawable.baotangmythuat, "100", "50", "07:00 - 22:00");
        parkingLots[1] = new ParkingLot(10.7710947, 106.7038785, "Bãi Giữ Xe Đường Hàm Nghi", R.drawable.hamnghi, "200", "0", "07:00 - 21:00");
        parkingLots[2] = new ParkingLot(10.7710946, 106.695691, "Bãi Giữ Xe Ô Tô 24/24 - Nam Kỳ Khởi Nghĩa", R.drawable.namkykhoinghia, "0", "100", "24:00 - 24:00");
        parkingLots[3] = new ParkingLot(10.772948, 106.7022655, "Bãi Giữ Xe Bitexco", R.drawable.bitexco, "1000", "50", "08:00 - 22:00");
        parkingLots[4] = new ParkingLot(10.7770484, 106.7011244, "Bãi Xe Ô Tô Nhà Hát Lớn Thành Phố", R.drawable.nhahatthanhpho, "0", "50", "08:00 - 23:00");
        parkingLots[5] = new ParkingLot(10.7771705, 106.6867483, "Bãi Giữ Xe Dinh Độc Lập ", R.drawable.dinhdoclap, "1000", "0", "08:00 - 18:00");
        parkingLots[6] = new ParkingLot(10.7812634, 106.6939001, "Bãi Giữ Xe Bệnh Viện Nhi Đồng 2 - Nguyễn Du", R.drawable.benhviennhidong, "2000", "0", "24:00 - 24:00");
        parkingLots[7] = new ParkingLot(10.7768643, 106.702604, "Bãi Giữ Xe Nhà Hát Thành Phố ", R.drawable.nhahatthanhphogiuxe, "200", "0", "08:00 - 22:00");
        parkingLots[8] = new ParkingLot(10.7774164, 106.699404, "Bãi Giữ Xe Vincom Center", R.drawable.vincomcenter, "2000", "0", "08:00 - 23:00");
        parkingLots[9] = new ParkingLot(10.7774163, 106.6928379, "BBãi Giữ Xe Diamond Plaza", R.drawable.diamondplaza, "1000", "0", "09:30 - 22:00");
        parkingLots[10] = new ParkingLot(10.7795121, 106.6909072, "Bãi Giữ Xe Nhà Văn Hóa Thanh Niên", R.drawable.nhavanhoathanhnien, "1000", "0", "08:00 - 22:00");
        parkingLots[11] = new ParkingLot(10.781123, 106.6995238, "Bãi Giữ Xe Đường Sách - Hai Bà Trưng", R.drawable.duongsach, "200", "0", " 08:00 - 22:00");
        parkingLots[12] = new ParkingLot(10.7799927, 106.6969962, "Bãi Giữ Xe Đường Sách Nguyễn Văn Bình", R.drawable.duongsachnvb, "200", "0", "08:00 - 22:00");
        parkingLots[13] = new ParkingLot(10.7798579, 106.6977188, "Bãi Giữ Xe Bưu Điện Thành Phố ", R.drawable.buudienthanhpho, "60", "0", "07:00 - 21:30");
        parkingLots[14] = new ParkingLot(10.7827846, 106.6986138, "Bãi Giữ Xe Đường Lê Duẩn", R.drawable.leduan, "2000", "0", "07:00 - 23:00");
        parkingLots[15] = new ParkingLot(10.7745714, 106.701146, "Bãi Giữ Xe Chung Cư 42 Nguyễn Huệ", R.drawable.chungcunguyenhue, "60", "0", "08:00 - 22:00");
        parkingLots[16] = new ParkingLot(10.772147, 106.7047488, "Bãi Giữ Xe Phố Nguyễn Huệ - Hải Triều", R.drawable.haitrieu, "200", "0", "08:00 - 22:00");
        parkingLots[17] = new ParkingLot(10.774121, 106.7027408, "Bãi Giữ Xe Phố Nguyễn Huệ - Tôn Thất Thiệp", R.drawable.tonthatthiep, "2000", "0", "24:00 - 24:00");
        parkingLots[18] = new ParkingLot(10.7764498, 106.6904771, "Bãi Giữ Xe Cung Văn Hóa Lao Động", R.drawable.cungvanhoalaodong, "1000", "0", "07:00 - 22:00");
        parkingLots[19] = new ParkingLot(10.7732471, 106.6907314, "Bãi Giữ Xe Galaxy Nguyễn Du ", R.drawable.galaxynguyendu, "1000", "0", "08:00 - 23:00");
        parkingLots[20] = new ParkingLot(10.77305, 106.6911546, "Bãi Xe Công Viên Tao Đàn - Khuôn Viên Tao Đàn", R.drawable.khuonvientaodan, "500", "0", "06:00 - 21:00");
        return parkingLots;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        ParkingLot parkingLot = (ParkingLot) marker.getTag();
        assert parkingLot != null;
        LatLng yournewposition = new LatLng(parkingLot.getLatitude(), parkingLot.getLongtitude());

        CameraPosition cameraPosition = CameraPosition.builder().target(yournewposition).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        final Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        intent.putExtra("parkingLotTag", parkingLot);
        marker.showInfoWindow();
        recreate();
        startActivity(intent);
        return true;
    }

    public class ListOfLatLng implements Parcelable {
        private double lat, lng;

        public ListOfLatLng(double lat, double lng){
            this.lat = lat;
            this.lng = lng;
        }

        protected ListOfLatLng(Parcel in) {
            lat = in.readDouble();
            lng = in.readDouble();
        }

        public final Creator<ListOfLatLng> CREATOR = new Creator<ListOfLatLng>() {
            @Override
            public ListOfLatLng createFromParcel(Parcel in) {
                return new ListOfLatLng(in);
            }

            @Override
            public ListOfLatLng[] newArray(int size) {
                return new ListOfLatLng[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeDouble(lat);
            dest.writeDouble(lng);
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }
    }


}

