package se.umu.visi0009.comiccollector.other;

import com.google.android.gms.location.Geofence;

import java.io.Serializable;

/**
 * Class that holds information about a Geofence.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class GeofenceInfo implements Serializable {

    private final String requestId;
    private final double latitude;
    private final double longitude;
    private final float radius;
    private final long durationMillis;
    private final int transitionTypes;

    public GeofenceInfo(String requestId, double latitude, double longitude, float radius, long durationMillis, int transitionTypes) {
        this.requestId = requestId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.durationMillis = durationMillis;
        this.transitionTypes = transitionTypes;
    }

    private String getRequestId() {
        return requestId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    private float getRadius() {
        return radius;
    }

    private long getDurationMillis() {
        return durationMillis;
    }

    private int getTransitionTypes() {
        return transitionTypes;
    }

    public Geofence getGeofence() {
        return new Geofence.Builder()
                .setRequestId(getRequestId())
                .setCircularRegion(getLatitude(), getLongitude(), getRadius())
                .setExpirationDuration(getDurationMillis())
                .setTransitionTypes(getTransitionTypes())
                .build();
    }
}
