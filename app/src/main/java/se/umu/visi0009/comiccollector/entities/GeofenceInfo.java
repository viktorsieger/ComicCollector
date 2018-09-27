package se.umu.visi0009.comiccollector.entities;

import com.google.android.gms.location.Geofence;

import java.io.Serializable;

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

    public String getRequestId() {
        return requestId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getRadius() {
        return radius;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public int getTransitionTypes() {
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
