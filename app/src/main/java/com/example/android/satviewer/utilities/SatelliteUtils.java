package com.example.android.satviewer.utilities;

import uk.me.g4dpz.satellite.Satellite;
import uk.me.g4dpz.satellite.SatelliteFactory;
import uk.me.g4dpz.satellite.TLE;

/**
 * Created by spd on 7/8/17.
 * This class uses predict4java library to create the Satellite objects. Also provides some other
 * utilities.
 */

public final class SatelliteUtils {

    /**
     * A temporary solution to hold the satellite tyeps.
     * TODO: Move this to a database
     */
    public enum SatelliteType{
        WEATHER,
        GPS_OPERATIONS,
        GEODETIC
    };

    // One for the bundle
    public static String TYPE = "SatelliteType";

    public static String SATELLITE = "Satellite";

    public static String SATELLITE_BUNDLE ="SatelliteBundle";

    /**
     * Get the full name of the satellite type. Again a temporary solution.
     * TODO: Move this to a database oriented solution
     * @param satelliteType
     * @return
     */
    public static String getFullTypeName(SatelliteType satelliteType) {
        String satelliteString;

        switch (satelliteType){
            case WEATHER:
                satelliteString = "Weather";
                break;
            case GPS_OPERATIONS:
                satelliteString = "GPS Operations";
                break;
            case GEODETIC:
                satelliteString = "Geodetic";
                break;
            default:
                satelliteString = null;
        }

        return satelliteString;
    }

    /**
     * Get the type of satellite enum from the satelliteTypeString.
     * @param satelliteTypeString
     * @return
     */
    public static SatelliteType getType(String satelliteTypeString){
        SatelliteType satelliteType;

        switch (satelliteTypeString){
            case "Weather":
                satelliteType = SatelliteType.WEATHER;
                break;
            case "GPS Operations":
                satelliteType = SatelliteType.GPS_OPERATIONS;
                break;
            case "Geodetic":
                satelliteType = SatelliteType.GEODETIC;
                break;
            default:
                satelliteType = null;
        }

        return satelliteType;
    }

    /**
     * Get the URL extension of the satellite type
     * @param satelliteType
     * @return
     */
    public static String getURLString(SatelliteType satelliteType) {
        String URLString;

        switch (satelliteType){
            case WEATHER:
                URLString = "weather.txt";
                break;
            case GPS_OPERATIONS:
                URLString = "gps-ops.txt";
                break;
            case GEODETIC:
                URLString = "geodetic.txt";
                break;
            default:
                URLString = null;
        }

        return URLString;
    }

    /**
     * Get all the satellite types. Used this to fill the screen with all types.
     * @return
     */
    public static String[] getAllSatelliteTypeStrings(){
        String [] allSatelliteStrings = new String[SatelliteType.values().length];

        int i = 0;
        for (SatelliteType satelliteType: SatelliteType.values()){
            allSatelliteStrings[i++] = getFullTypeName(satelliteType);
        }

        return allSatelliteStrings;
    }

    /**
     * Create an object of type satellite. Accepts the tle strings.
     * @param tleStrings
     * @return
     */
    public static TLE createTLE(String[] tleStrings){
        return new TLE(tleStrings);
    }
}
