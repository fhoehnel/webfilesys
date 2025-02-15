package de.webfilesys.util;

import de.webfilesys.GeoTag;
import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.CameraExifData;

public class GPSUtil {

    public static GeoTag getGPSCoordinates(String filePath) {
        GeoTag geoTag = MetaInfManager.getInstance().getGeoTag(filePath);
        if (geoTag == null) {
            CameraExifData exifData = new CameraExifData(filePath);
            float latitude = exifData.getGpsLatitude();
            float longitude = exifData.getGpsLongitude();
            if (latitude < 0 || longitude < 0) {
                return null;
            }
            if ("S".equals(exifData.getGpsLatitudeRef())) {
                latitude = -latitude;
            }
            if ("W".equals(exifData.getGpsLongitudeRef())) {
                longitude = -longitude;
            }
            geoTag = new GeoTag();
            geoTag.setLatitude(latitude);
            geoTag.setLongitude(longitude);
        }
        return geoTag;
    }

    /**
     * Berechnet die Entfernung zwischen zwei Koordinaten in Metern.
     *
     * @param ax Breite der ersten Koordinate in Dezimalgrad
     * @param ay Laenge der ersten Koordinate in Dezimalgrad
     * @param bx Breite der zweiten Koordinate in Dezimalgrad
     * @param by Laenge der zweiten Koordinate in Dezimalgrad
     * @return Distanz in Metern
     */
    public static double calculateDistance(double ax, double ay, double bx, double by) {

        if ((ax == bx) && (ay == by)) {
            return 0.0f;
        }

        double x = 1.0f / 298.257223563f;  // Abplattung der Erde

        double a = 6378137.0f / 1000.0f;  // Aequatorradius der Erde in km

        double f = (ax + bx) / 2.0f;

        double g = (ax - bx) / 2.0f;

        double l = (ay - by) / 2.0f;

        // auf Bogenmass bringen

        f = (Math.PI / 180.0f) * f;

        g = (Math.PI / 180.0f) * g;

        l = (Math.PI / 180.0f) * l;

        double s = Math.pow(Math.sin(g), 2) * Math.pow(Math.cos(l), 2) + Math.pow(Math.cos(f), 2) * Math.pow(Math.sin(l), 2);

        double c = Math.pow(Math.cos(g), 2) * Math.pow(Math.cos(l), 2) + Math.pow(Math.sin(f), 2) * Math.pow(Math.sin(l), 2);

        double w = Math.atan(Math.sqrt(s / c));

        double d = 2.0f * w * a;

        double r = Math.sqrt(s * c) / w;

        double h1 = (3.0f * r - 1.0f) / (2.0f * c);

        double h2 = (3.0f * r + 1.0f) / (2.0 * s);

        return(1000.0f * d * (1.0f + x * h1 * Math.pow(Math.sin(f), 2) * Math.pow(Math.cos(g), 2) - x * h2 * Math.pow(Math.cos(f), 2) * Math.pow(Math.sin(g), 2)));
    }
}
