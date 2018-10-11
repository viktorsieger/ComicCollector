package se.umu.visi0009.comiccollector.entities;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import se.umu.visi0009.comiccollector.fragments.MapFragment;

public class GeofenceTransitionsIntentService extends IntentService {

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Geofence geofence;
        GeofencingEvent geofencingEvent;
        Intent intentGeofenceRequestId;
        List<Geofence> triggeredGeofences;

        geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d("TEST", GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode()));
            return;
        }

        if(geofencingEvent.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {

            triggeredGeofences = geofencingEvent.getTriggeringGeofences();

            if(triggeredGeofences.size() == 1) {
                geofence = triggeredGeofences.get(0);

                intentGeofenceRequestId = new Intent(MapFragment.ACTION_GEOFENCE);
                intentGeofenceRequestId.putExtra(MapFragment.KEY_GEOFENCE_REQUEST_ID, geofence.getRequestId());
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentGeofenceRequestId);
            }
            else {
                Log.d("TEST", "Error: More than one geofence triggered");
            }
        }
        else {
            Log.d("TEST", "Error: Invalid geofence transition type");
        }
    }
}
