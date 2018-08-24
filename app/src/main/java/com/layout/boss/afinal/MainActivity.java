package com.layout.boss.afinal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.layout.boss.afinal.Bank;
import com.layout.boss.afinal.Cafe;
import com.layout.boss.afinal.Cinema;
import com.layout.boss.afinal.MapsActivity;

public class MainActivity extends AppCompatActivity {
    private ParkingLot parkingLot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ánh xạ
        this.parkingLot = getIntent().getParcelableExtra("parkingLotTag");
        ImageView imageView = (ImageView) findViewById(R.id.Image);
        ImageView imageView1 = (ImageView) findViewById(R.id.Bike);
        ImageView imageView2 = (ImageView) findViewById(R.id.Car);
        TextView textView = (TextView) findViewById(R.id.Name);
        TextView textView1 = (TextView) findViewById(R.id.CarVol);
        TextView textView2 = (TextView) findViewById(R.id.BikeVol);
        TextView textView3 = (TextView) findViewById(R.id.Share);
        TextView textView4 = (TextView) findViewById(R.id.Time);
        ImageButton imageButton = (ImageButton) findViewById(R.id.restaurant);
        ImageButton imageButton1 = (ImageButton) findViewById(R.id.bank);
        ImageButton imageButton2= (ImageButton) findViewById(R.id.hospital);
        ImageButton imageButton3 = (ImageButton) findViewById(R.id.cafe);
        ImageButton imageButton4 = (ImageButton) findViewById(R.id.cinema);
        ImageButton imageButton5 = (ImageButton) findViewById(R.id.facebook);
        ImageButton imageButton6 = (ImageButton) findViewById(R.id.twitter);

        // Thay đổi giá trị
        imageView.setImageResource(this.parkingLot.getImage());
        //////////
        textView.setText(this.parkingLot.getName());
        textView1.setText(this.parkingLot.getBikeVol());
        textView2.setText(this.parkingLot.getCarVol());
        textView4.setText(this.parkingLot.getTime());
        //////////
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Restaurant restaurant = new Restaurant();
                Intent nearbyRestaurant = new Intent(MainActivity.this, Restaurant.class);
                nearbyRestaurant.putExtra("parkingLotTag", parkingLot);
                startActivity(nearbyRestaurant);
            }
        });
        imageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bank bank = new Bank();
                Intent nearbyBank = new Intent(MainActivity.this, Bank.class);
                nearbyBank.putExtra("parkingLotTag", parkingLot);
                startActivity(nearbyBank);
            }
        });
        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Hospital hospital = new Hospital();
                Intent nearbyHospital = new Intent(MainActivity.this, Hospital.class);
                nearbyHospital.putExtra("parkingLotTag", parkingLot);
                startActivity(nearbyHospital);
            }
        });
        imageButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cafe cafe = new Cafe();
                Intent nearbyCafe = new Intent(MainActivity.this, Cafe.class);
                nearbyCafe.putExtra("parkingLotTag", parkingLot);
                startActivity(nearbyCafe);
            }
        });
        imageButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cinema cinema = new Cinema();
                Intent nearbyCinema = new Intent(MainActivity.this, Cinema.class);
                nearbyCinema.putExtra("parkingLotTag", parkingLot);
                startActivity(nearbyCinema);
            }
        });
        imageButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FacebookLogin facebookLogin = new FacebookLogin();
                Intent fb_login = new Intent(MainActivity.this, FacebookLogin.class);
                startActivity(fb_login);

            }
        });
        imageButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }
}
