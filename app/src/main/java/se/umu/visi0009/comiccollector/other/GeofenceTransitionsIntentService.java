package se.umu.visi0009.comiccollector.other;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import se.umu.visi0009.comiccollector.ui.activities.MainActivity;
import se.umu.visi0009.comiccollector.ui.fragments.MapFragment;

/**
 * The service that handles triggered Geofences.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "GeofenceTransitions";

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    /**
     * Called when a Geofence is triggered. Sends a broadcast to the MapFragment
     * so the Geofence can be replaced. Also sends a broadcast to the
     * MainActivity that creates a new card.
     *
     * @param intent    An intent containing the geofencing event data.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Geofence geofence;
        GeofencingEvent geofencingEvent;
        Intent intentNewCard;
        Intent intentReplaceGeofence;
        List<Geofence> triggeredGeofences;

        geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.e(TAG, GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode()));
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
                Log.e(TAG, "Error: More than one geofence triggered");
                return;
            }
        }
        else {
            Log.e(TAG, "Error: Invalid geofence transition type");
            return;
        }

        intentNewCard = new Intent(MainActivity.ACTION_GEOFENCE_2);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentNewCard);
    }
}
