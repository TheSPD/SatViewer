package com.example.android.satviewer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.satviewer.utilities.DeviceSensorUtils;
import com.example.android.satviewer.utilities.SatelliteUtils;
import com.google.gson.Gson;

import uk.me.g4dpz.satellite.Satellite;
import uk.me.g4dpz.satellite.SatelliteFactory;
import uk.me.g4dpz.satellite.TLE;

/**
 * Implements LocationListener as well as SensorEventListener
 * Get the location first and then get the device orientation
 * Also creates a Satellite object and GroundPosition object to get the satellite's position.
 */
public class SatelliteFinderActivity extends AppCompatActivity implements
        LocationListener, SensorEventListener{

    // Display on screen messages
    TextView mTextView;
    ProgressBar mProgressBar;

    // Device location
    Location mLocation;

    // Satellite Object
    Satellite mSatellite;

    // The location manager for handling location events
    LocationManager locationManager;

    // Sensor manager to do the same for sensor events
    SensorManager sensorManager;

    // To hold the device senors
    Sensor accelerometer;
    Sensor magnetometer;

    // Hold accelerometer and magnetic sensor attributes
    float[] mGravity;
    float[] mGeomagnetic;

    // A code for storing the permissions while asking for permissions
    final static int M_LOCATION_PERMISSION_CODE = 1234;

    /**
     * Performs initial setup
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_satellite_finder);

        // Initialize the display parameters
        mTextView = (TextView) findViewById(R.id.tv_satellite_direction);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator_location);

        // Get the selected satellite
        Intent intent = getIntent();
        String passedGSON = intent.getStringExtra(SatelliteUtils.SATELLITE);
        TLE passedTLE = new Gson().fromJson(passedGSON, TLE.class);
        mSatellite = SatelliteFactory.createSatellite(passedTLE);

        // Initialize the managers
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Initialize the sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        // Asking for permissions if not granted already and start by getting the location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // Request permissions
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, M_LOCATION_PERMISSION_CODE);

            }
            else {
                getLocation();
            }
        }
        else {
            getLocation();
        }
    }

    /**
     * Once permissions are granted/denied
     * @param requestCode The request code
     * @param permissions the permissions
     * @param grantResults the result for these permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case M_LOCATION_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLocation();
                }
                else if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    showErrorMessage();
                }
        }
    }

    /**
     * Request for location from GPS PROVIDER
     */
    private void getLocation() {
        showLoader();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
    }

    /**
     * Request for device orientation by registering sensors
     */
    private void getOrientation() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * Once the sensor responds, display appropriate message
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // Store the values from each sensor
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = sensorEvent.values;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = sensorEvent.values;

        // Make sure that we have both the sensor values
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            // Get the rotation matrix
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {

                // get orientation
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                // Convert to degrees
                double azimuth = orientation[0] * 180 / Math.PI; // orientation contains: azimuth, pitch and roll
                double pitch = orientation[1] * 180 / Math.PI;
                double roll = orientation[2] * 180 / Math.PI;

                // Get the direction to point the device so it match satellite position
                DeviceSensorUtils.Direction dir = DeviceSensorUtils.getDirection(this, azimuth, pitch, roll, mSatellite, mLocation);
                String dirString = DeviceSensorUtils.getDirectionString(dir);

                // Show the appropriate direction
                showDirection(dirString);

            }
        }
    }

    /**
     * Shows the direction
     * @param dirString direction as strng
     */
    private void showDirection(String dirString) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mTextView.setVisibility(View.VISIBLE);
        if(dirString != null) {
            mTextView.setText(dirString);
        }
        else{
            mTextView.setText(getString(R.string.correct_orientation));
        }
    }

    /**
     * Shows the error message if permission is not granted
     */
    private void showErrorMessage(){
        mProgressBar.setVisibility(View.INVISIBLE);
        mTextView.setVisibility(View.VISIBLE);
        mTextView.setText(getString(R.string.permission_missing_message));
    }

    /**
     * Show the loader
     */
    private void showLoader(){
        mTextView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Not implemented
     * @param sensor
     * @param i
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * Once location is updated, this function is called
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        // Set the location object
        mLocation = location;
        //Remove the updates as we don't need the location often
        locationManager.removeUpdates(this);

        // Now check the orientation
        getOrientation();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    /**
     * In case we do not have the GPS enabled
     * @param s
     */
    @Override
    public void onProviderDisabled(String s) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    /**
     * Making sure to shut down the listeners to save battery on screen off
     */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        sensorManager.unregisterListener(this);
    }

    /**
     * Start the process again if we resume the app
     */
    @Override
    protected void onResume() {
        super.onResume();
        getLocation();
    }
}
