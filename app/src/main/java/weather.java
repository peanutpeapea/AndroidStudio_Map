import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.layout.boss.afinal.MapsActivity;
import com.layout.boss.afinal.R;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

public class weather extends MapsActivity implements GoogleApiClient.OnConnectionFailedListener{
    private final static int REQUEST_PERMISSION_RESULT_CODE = 42;
    private GoogleApiClient mGoogleApiClient;
    public Weather nWeather;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mGoogleApiClient= new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .enableAutoManage(this, this)
                .build();
        mGoogleApiClient.connect();
        //impliment weather alertdialog here
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_RESULT_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                } else {
                    Log.e("Tuts+", "Location permission denied.");
                }
            }
        }
    }
    private boolean checkLocationPermission() {
        if( !hasLocationPermission() ) {
            Log.e("Tuts+", "Does not have location permission granted");
            requestLocationPermission();
            return false;
        }

        return true;
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION )
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                weather.this,
                new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                REQUEST_PERMISSION_RESULT_CODE );
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private void detectWeather() {
        if( !checkLocationPermission() ) {
            return;
        }

        Awareness.SnapshotApi.getWeather(mGoogleApiClient)
                .setResultCallback(new ResultCallback<WeatherResult>() {
                    @Override
                    public void onResult(@NonNull WeatherResult weatherResult) {
                        nWeather = weatherResult.getWeather();
                        Log.e("Tuts+", "Temp: " + nWeather.getTemperature(Weather.FAHRENHEIT));
                        Log.e("Tuts+", "Feels like: " + nWeather.getFeelsLikeTemperature(Weather.FAHRENHEIT));
                        Log.e("Tuts+", "Dew point: " + nWeather.getDewPoint(Weather.FAHRENHEIT));
                        Log.e("Tuts+", "Humidity: " + nWeather.getHumidity() );

                        if( nWeather.getConditions()[0] == Weather.CONDITION_CLOUDY ) {
                            Log.e("Tuts+", "Looks like there's some clouds out there");
                        }
                    }
                });
    }
}
