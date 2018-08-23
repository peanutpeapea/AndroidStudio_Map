package com.layout.boss.afinal;

import android.os.Parcel;
import android.os.Parcelable;

public class ParkingLot implements Parcelable{
    private double Latitude;
    private double Longtitude;
    private String Name;
    private String BikeVol;
    private String CarVol;
    private String Time;
    private int Image;

    public ParkingLot (double Latitude, double Longtitude, String Name, int Image, String BikeVol, String CarVol, String Time){
        this.Latitude = Latitude;
        this.Longtitude = Longtitude;
        this.Name = Name;
        this.BikeVol = BikeVol;
        this.CarVol = CarVol;
        this.Time = Time;
        this.Image = Image;
    }

    protected ParkingLot(Parcel in) {
        Latitude = in.readDouble();
        Longtitude = in.readDouble();
        Name = in.readString();
        BikeVol = in.readString();
        CarVol = in.readString();
        Time = in.readString();
        Image = in.readInt();
    }

    public static final Creator<ParkingLot> CREATOR = new Creator<ParkingLot>() {
        @Override
        public ParkingLot createFromParcel(Parcel in) {
            return new ParkingLot(in);
        }

        @Override
        public ParkingLot[] newArray(int size) {
            return new ParkingLot[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(Latitude);
        dest.writeDouble(Longtitude);
        dest.writeString(Name);
        dest.writeString(BikeVol);
        dest.writeString(CarVol);
        dest.writeString(Time);
        dest.writeInt(Image);
    }

    public double getLatitude() {
        return Latitude;
    }

    public double getLongtitude() {
        return Longtitude;
    }

    public int getImage() {
        return Image;
    }

    public String getName() {
        return Name;
    }

    public void setImage(int image) {
        Image = image;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public void setLongtitude(double longtitude) {
        Longtitude = longtitude;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getBikeVol() {
        return BikeVol;
    }

    public String getCarVol() {
        return CarVol;
    }

    public void setBikeVol(String bikeVol) {
        BikeVol = bikeVol;
    }

    public void setCarVol(String carVol) {
        CarVol = carVol;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }
}
