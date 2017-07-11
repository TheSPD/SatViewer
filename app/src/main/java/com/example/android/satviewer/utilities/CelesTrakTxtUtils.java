package com.example.android.satviewer.utilities;

import android.util.Log;

import uk.me.g4dpz.satellite.Satellite;
import uk.me.g4dpz.satellite.TLE;

/**
 * Created by spd on 7/9/17.
 */

public final class CelesTrakTxtUtils {

    /**
     * Converts a string of TLEs to Satellite object array. There's no explicit error check right now.
     * Exception given is IllegalArgumentException of 'predict4java' is thrown if an error occurs.
     * @param satelliteTypeTLEString the type of Satellite
     * @return array of TLEs
     */
    public static TLE[] getTlesFromTxt(String satelliteTypeTLEString){
        String[] lines = satelliteTypeTLEString.split(System.getProperty("line.separator"));

        // As the full form of TLE is Three Line element. The number of TLEs hence
        int numTLEs = (lines.length/3);
        TLE[] tles = new TLE[numTLEs];


        for(int i = 0; i < numTLEs; ++i){
            int stringNum = i * 3;
            String[] tleStrings = new String[3];

            // Trust 'celestrak.com' to provide the correct file.
            tleStrings[0] = lines[stringNum];
            tleStrings[1] = lines[stringNum + 1];
            tleStrings[2] = lines[stringNum + 2];

            // Create the Satellite object.
            tles[i] = SatelliteUtils.createTLE(tleStrings);
        }

        return tles;
    }
}
