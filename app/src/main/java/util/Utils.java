package util;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Utils {
    public static boolean findDistance(LatLng latLng, LatLng latLng2) {
        float[] fArr = new float[1];
        Location.distanceBetween(latLng.latitude, latLng.longitude, latLng2.latitude, latLng2.longitude, fArr);
        if (fArr[0] < 500.0f) {
            return true;
        }
        return false;
    }
}
