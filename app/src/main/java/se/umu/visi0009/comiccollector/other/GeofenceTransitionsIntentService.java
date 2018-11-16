package se.umu.visi0009.comiccollector.other;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import se.umu.visi0009.comiccollector.activities.MainActivity;
import se.umu.visi0009.comiccollector.fragments.MapFragment;

public class GeofenceTransitionsIntentService extends IntentService {

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Geofence geofence;
        GeofencingEvent geofencingEvent;
        Intent intentNewCard;
        Intent intentReplaceGeofence;
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

                intentReplaceGeofence = new Intent(MapFragment.ACTION_GEOFENCE);
                intentReplaceGeofence.putExtra(MapFragment.KEY_GEOFENCE_REQUEST_ID, geofence.getRequestId());
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentReplaceGeofence);
            }
            else {
                Log.d("TEST", "Error: More than one geofence triggered");
                return;
            }
        }
        else {
            Log.d("TEST", "Error: Invalid geofence transition type");
            return;
        }

        intentNewCard = new Intent(MainActivity.ACTION_GEOFENCE_2);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentNewCard);
    }
}
