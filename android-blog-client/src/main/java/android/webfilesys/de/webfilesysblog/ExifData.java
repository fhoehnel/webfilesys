package android.webfilesys.de.webfilesysblog;

import android.media.ExifInterface;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

/**
 * Created by User on 17.07.2016.
 */
public class ExifData {
    private String filePath;

    private ExifInterface exifData = null;

    public ExifData(String picFilePath) {
        filePath = picFilePath;
    }

    public LatLng getGpsLocation() {
        try {
            if (exifData == null) {
                exifData = new ExifInterface(filePath);
            }

            if (exifData != null) {
                String latStr = exifData.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                String longStr = exifData.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                String latRef = exifData.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                String longRef = exifData.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

                if ((latStr != null) && (longStr != null) && (latRef != null) && (longRef != null)) {
                    Log.d("webfilesysblog", "lat: " + latStr);
                    Log.d("webfilesysblog", "long: " + longStr);
                    Log.d("webfilesysblog", "latRef: " + latRef);
                    Log.d("webfilesysblog", "longRef: " + longRef);

                    float latitudeFromExif;
                    float longitudeFromExif;

                    if (latRef.equals("N")) {
                        latitudeFromExif = convertToDegree(latStr);
                    } else{
                        latitudeFromExif = 0 - convertToDegree(latStr);
                    }

                    if (longRef.equals("E")) {
                        longitudeFromExif = convertToDegree(longStr);
                    } else{
                        longitudeFromExif = 0 - convertToDegree(longStr);
                    }

                    Log.d("webfilesysblog", "latitude: " + latitudeFromExif);
                    Log.d("webfilesysblog", "longitude: " + longitudeFromExif);
                    return new LatLng(latitudeFromExif, longitudeFromExif);
                } else {
                    Log.d("webfilesysblog", "picture contains no EXIF GPS data");
                }
            } else {
                Log.d("webfilesysblog", "picture contains no EXIF data");
            }
        } catch (IOException ioex) {
            Log.e("webfilesysblog", "failed to extract EXIF data from picture " + filePath, ioex);
        }
        return null;
    }

    private Float convertToDegree(String stringDMS){
        Float result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0/S1;

        result = new Float(FloatD + (FloatM/60) + (FloatS/3600));

        return result;
    }

}
