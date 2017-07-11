package com.example.android.satviewer.utilities;

import android.content.Context;
import android.content.res.Configuration;
import android.location.Location;
import android.util.Log;

import java.util.Date;

import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.Satellite;

/**
 * Created by spd on 7/10/17.
 */

public final class DeviceSensorUtils {

    // The enum for direction
    public enum Direction{
        UP,
        DOWN,
        RIGHT,
        LEFT,
        UP_LEFT,
        UP_RIGHT,
        DOWN_LEFT,
        DOWN_RIGHT,
        CENTER
    }

    // The solid view angle within which we accept as a permissible limit
    private final static int VIEW_ANGLE = 5;

    /**
     * Calculates direction of satellite wrt device
     * @param context Current context
     * @param deviceAzimuth Azimuth of the device
     * @param devicePitch Pitch of the device
     * @param deviceRoll Roll of the device
     * @param mSatellite The satellite
     * @param location location of the device
     * @return the direction to be shown on device
     */
    public static Direction getDirection(Context context, double deviceAzimuth, double devicePitch, double deviceRoll, Satellite mSatellite, Location location){
        int orientation = context.getResources().getConfiguration().orientation;
        Direction direction = null;

        if(orientation != Configuration.ORIENTATION_LANDSCAPE){
            return direction;
        }

        double deviceElevation = deviceRoll;
        GroundStationPosition mDevicePosition = new GroundStationPosition(location.getLatitude(),
                location.getLongitude(),
                location.getAltitude());
        Date now = new Date();

        SatPos satellitePosition = mSatellite.getPosition(mDevicePosition, now);

        double satAzimuth = satellitePosition.getAzimuth() * 180 / Math.PI;

        // Bringing the azimuth to a [-180,180]
        if(satAzimuth > 180) {
            satAzimuth -= 360;
        }

        double satElevation = satellitePosition.getElevation() * 180 / Math.PI;

        // Translating the 0 and bringing the elevation to a [-180,180]
        satElevation += 90;
        if (satElevation > 180) {
            satElevation -= 360;
        }

        // Calculating the difference between the satellite look angle
        // and device look angle
        double diffAzimuth = satAzimuth - deviceAzimuth;
        if(diffAzimuth > 180){
            diffAzimuth -= 360;
        }
        else if(diffAzimuth <-180){
            diffAzimuth += 360;
        }

        double diffElevation = satElevation - deviceElevation;
        if(diffElevation > 180){
            diffElevation -= 360;
        }
        else if(diffElevation <-180){
            diffElevation += 360;
        }

        // Finally getting the direction
        direction = Direction.CENTER;
        if(  diffElevation < -1 * VIEW_ANGLE) {
            direction = Direction.DOWN;
            if( diffAzimuth < -1 * VIEW_ANGLE){
                direction = Direction.DOWN_LEFT;
            }
            else if (diffAzimuth > VIEW_ANGLE) {
                direction = Direction.DOWN_RIGHT;
            }
        }
        else if(diffElevation > VIEW_ANGLE){
            direction = Direction.UP;
            if( diffAzimuth < -1 * VIEW_ANGLE){
                direction = Direction.UP_LEFT;
            }
            else if (diffAzimuth > VIEW_ANGLE) {
                direction = Direction.UP_RIGHT;
            }
        }
        else {
            if (diffAzimuth < -1 * VIEW_ANGLE) {
                direction = Direction.LEFT;
            } else if (diffAzimuth > VIEW_ANGLE) {
                direction = Direction.RIGHT;
            }
        }

        Log.d("TAG", diffAzimuth + " " + diffElevation);
        return direction;
    }

    /**
     * Returns the string to be displayed
     * @param direction The direction Enum
     * @return direction as a string
     */
    public static String getDirectionString(Direction direction){
        if(direction == null){
            return null;
        }

        switch (direction){
            case UP:
                return "Up";
            case DOWN:
                return "Down";
            case RIGHT:
                return "Right";
            case LEFT:
                return "Left";
            case UP_LEFT:
                return "Up-Left";
            case UP_RIGHT:
                return "Up-Right";
            case DOWN_LEFT:
                return "Down-Left";
            case DOWN_RIGHT:
                return "Down-Right";
            case CENTER:
                return "Center";
            default:
                return null;
        }
    }
}
