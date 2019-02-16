package se.umu.visi0009.comiccollector.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import se.umu.visi0009.comiccollector.ComicCollectorApp;
import se.umu.visi0009.comiccollector.R;
import se.umu.visi0009.comiccollector.other.GeofenceInfo;
import se.umu.visi0009.comiccollector.other.GeofenceTransitionsIntentService;

/**
 * Fragment that displays a map.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    public static final String ACTION_GEOFENCE = "geofenceIntentFilter";
    public static final String KEY_GEOFENCE_REQUEST_ID = "geofenceRequestId";
    private static final String TAG = "MapFragment";

    private static final double GEOFENCES_MAX_DISTANCE_FROM_USER = 10000;
    private static final float GEOFENCE_RADIUS = 100;
    private static final int DEFAULT_ZOOM = 11;
    private static final int FAILURE_ZOOM = 4;
    private static final int NUMBER_OF_GEOFENCES = 10;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private static final LatLng FAILURE_LOCATION = new LatLng(62.3875, 16.325556); // Center of Sweden
    private static final String FILENAME_GEOFENCE_INFOS = "currentGeofenceInfos";
    private static final String KEY_LOCATION_PERMISSION_GRANTED = "mLocationPermissionGranted";
    private static final String KEY_LOCATION_SETTINGS_ENABLED = "mLocationSettingEnabled";

    private Activity mActivity;
    private boolean mAreGeofencesStarted = false;
    private boolean mGeofenceInfosExists;
    private boolean mIsZoomedIn = false;
    private boolean mLocationPermissionDialogShown = false;
    private boolean mLocationPermissionGranted = false;
    private boolean mLocationSettingDialogShown = false;
    private boolean mLocationSettingEnabled = false;
    private boolean mToolbarActionItemVisible = false;
    private Context mContext;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GeofencingClient mGeofencingClient;
    private GoogleMap mGoogleMap;
    private HashMap<String, GeofenceInfo> mGeofenceInfos = new HashMap<>();
    private LocalBroadcastManager mLocalBroadcastManager;
    private Location mLastKnownLocation = null;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private PendingIntent mGeofencePendingIntent = null;
    private Set<Marker> mMarkers = new HashSet<>();
    private Toolbar mToolbar;

    /**
     * BroadcastReceiver that receives broadcasts when the user has found a
     * card. Replaces the triggered geofence with a new one.
     */
    private BroadcastReceiver mGeofenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String requestId = intent.getStringExtra(KEY_GEOFENCE_REQUEST_ID);

            if(mGeofenceInfos.containsKey(requestId)) {
                stopGeofences();
                mGeofenceInfos.remove(requestId);
                addGeofenceInfo(mLastKnownLocation);
                startGeofences();

                Toast.makeText(mContext, R.string.card_found_string, Toast.LENGTH_SHORT).show();
            }
            else {
                Log.e(TAG, "Error: Key not in hashmap");
            }
        }
    };

    /**
     * Called when a fragment is first attached to its context. Saves a
     * reference to the context.
     *
     * @param context   The context of the fragment.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    /**
     * Called to do initial creation of a fragment. Sets up a
     * FusedLocationProviderClient and a GeofencingClient, registers the
     * broadcast receiver and initializes the LocationCallback.
     *
     * @param savedInstanceState    If the fragment is being re-created from a
     *                              previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        updateValuesFromBundle(savedInstanceState);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        mGeofencingClient = LocationServices.getGeofencingClient(mContext);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        mLocalBroadcastManager.registerReceiver(mGeofenceReceiver, new IntentFilter(ACTION_GEOFENCE));

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult != null) {
                    onLocationChanged(locationResult.getLastLocation());
                }
                else {
                    Toast.makeText(mContext, R.string.error_location_failed, Toast.LENGTH_SHORT).show();
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(FAILURE_LOCATION, FAILURE_ZOOM));
                }
            }
        };
    }

    /**
     * Called to have the fragment instantiate its user interface view. Inflates
     * the XML layout. Also sets the callback object for the GoogleMap.
     *
     * @param inflater              LayoutInflater object that can be used to
     *                              inflate any views in the fragment.
     * @param container             If non-null, this is the parent view that
     *                              the fragment's UI should be attached to. The
     *                              fragment should not add the view itself, but
     *                              this can be used to generate the
     *                              LayoutParams of the view.
     * @param savedInstanceState    If non-null, this fragment is being
     *                              re-constructed from a previous saved state
     *                              as given here.
     * @return                      Return the View for the fragment's UI.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        ((SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        return rootView;
    }

    /**
     * Called when the fragment's activity has been created and this fragment's
     * view hierarchy is instantiated. Finds references to the activity and the
     * app bar. If geofences are stored locally the are also retrieved.
     *
     * @param savedInstanceState    If the fragment is being re-created from a
     *                              previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        mToolbar = mActivity.findViewById(R.id.toolbar);

        if(((ComicCollectorApp)mActivity.getApplication()).isFileInPersistentStorage(FILENAME_GEOFENCE_INFOS)) {
            mGeofenceInfos = (HashMap<String, GeofenceInfo>)((ComicCollectorApp)mActivity.getApplication()).readObjectFromPersistentStorage(FILENAME_GEOFENCE_INFOS);
            mGeofenceInfosExists = true;
        }
        else {
            mGeofenceInfosExists = false;
        }
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * Sets the app bar's title and inflates the app bar's menu. Also determines
     * if the location permissions dialog or the location settings dialog need
     * to be displayed to the user.
     */
    @Override
    public void onResume() {

        super.onResume();

        mToolbar.setTitle(R.string.toolbar_title_map);
        mToolbar.inflateMenu(R.menu.toolbar_mapfragment);

        if(mLocationPermissionGranted) {
            if(mLocationSettingEnabled) {
                run();
            }
            else {
                if(!mLocationSettingDialogShown) {
                    mLocationSettingDialogShown = true;
                    createLocationRequest();
                    checkLocationSetting();
                }
            }
        }
        else {
            if(!mLocationPermissionDialogShown) {
                mLocationPermissionDialogShown = true;
                checkLocationPermission();
            }
        }
    }

    /**
     * Called when the Fragment is no longer resumed. Stops location updates,
     * removes geofences, stores geofences locally and clears the app bar's
     * menu.
     */
    @Override
    public void onPause() {

        super.onPause();

        stopLocationUpdates();

        if(mAreGeofencesStarted) {
            stopGeofences();
            mAreGeofencesStarted = false;
        }

        if(mGeofenceInfosExists) {
            ((ComicCollectorApp)mActivity.getApplication()).writeObjectToPersistentStorage(FILENAME_GEOFENCE_INFOS, mGeofenceInfos);
        }

        mToolbar.getMenu().clear();
        mToolbarActionItemVisible = false;
    }

    /**
     * Called when the fragment is no longer in use. Unregisters the broadcast
     * receiver for geofencing broadcasts.
     */
    @Override
    public void onDestroy() {
        mLocalBroadcastManager.unregisterReceiver(mGeofenceReceiver);
        super.onDestroy();
    }

    /**
     * Called when the map is ready to be used. Saves a reference to the
     * GoogleMap and updates the GoogleMap's UI.
     *
     * @param googleMap     The fragment's GoogleMap.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        updateLocationUI();
    }

    /**
     * Checks if location permission is granted. If the permission is denied the
     * user is prompted to grant the permission.
     */
    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            locationPermissionGrantedRoutine();
        }
        else {
            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage(R.string.alert_location_message_string)
                       .setCancelable(false)
                       .setPositiveButton(R.string.alert_location_positive_button_string, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                }).show();
            }
            else {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    /**
     * Callback for the result from requesting permissions. Handles the result.
     *
     * @param requestCode       The request code passed in
     *                          requestPermissions(android.app.Activity,
     *                          String[], int).
     * @param permissions       The requested permissions. Never null.
     * @param grantResults      The grant results for the corresponding
     *                          permissions which is either PERMISSION_GRANTED
     *                          or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if(grantResults.length > 0) {
            switch(requestCode) {
                case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionGranted = true;
                    }
                    else if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        locationPermissionDeniedRoutine();
                    }
                }
            }
        }
    }

    /**
     * Called when location permissions is granted. Creates a location request
     * and checks if the location setting is enabled.
     */
    private void locationPermissionGrantedRoutine() {
        createLocationRequest();
        checkLocationSetting();
    }

    /**
     * Checks if the location setting is enabled. If the setting is disabled the
     * user is prompted to enabled the setting.
     */
    private void checkLocationSetting() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);

        Task<LocationSettingsResponse> taskLocationSettingsResponse = LocationServices.getSettingsClient(mContext).checkLocationSettings(builder.build());
        taskLocationSettingsResponse.addOnSuccessListener(mActivity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                mLocationSettingEnabled = true;
                run();
            }
        }).addOnFailureListener(mActivity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException rae = (ResolvableApiException) e;
                        rae.startResolutionForResult(mActivity, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sie) {
                        Log.e(TAG, sie.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Callback for the result from requesting location setting. Handles the
     * result.
     *
     * @param requestCode   The integer request code originally supplied to
     *                      startResolutionForResult(), allowing you to identify
     *                      who this result came from.
     * @param resultCode    The integer result code returned by the child
     *                      activity through its setResult().
     * @param data          An Intent, which can return result data to the
     *                      caller (various data can be attached to Intent
     *                      "extras").
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch(resultCode) {
                    case Activity.RESULT_OK:
                        mLocationSettingEnabled = true;
                        break;
                    case Activity.RESULT_CANCELED:
                        locationSettingDisabledRoutine();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    /**
     * Called when location permission is granted and location updates are
     * enabled. Updates the GoogleMap's UI, creates a location request and
     * starts regular location updates.
     */
    private void run() {
        updateLocationUI();
        createLocationRequest();
        startLocationUpdates();
    }

    /**
     * Starts regular location updates.
     */
    private void startLocationUpdates() {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
        catch(SecurityException se) {
            Log.e(TAG, se.getMessage());
        }
    }

    /**
     * Stops location updates.
     */
    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * Called when the device receives a location update. Inflates the app bar's
     * menu if the menu isn't already inflated, adds geofences if there aren't
     * any and moves the map to the current location.
     *
     * @param location
     */
    private void onLocationChanged(Location location) {

        mLastKnownLocation = location;

        if(!mToolbarActionItemVisible) {
            // If action item isn't in menu
            if(mToolbar.getMenu().findItem(R.id.toolbar_map_new_geofences) == null) {
                mToolbar.inflateMenu(R.menu.toolbar_mapfragment);
            }

            mToolbar.getMenu().findItem(R.id.toolbar_map_new_geofences).setVisible(true);
            mToolbarActionItemVisible = true;
        }

        if(!mGeofenceInfosExists) {
            for(int i = 0; i < NUMBER_OF_GEOFENCES; i++) {
                addGeofenceInfo(location);
            }

            mGeofenceInfosExists = true;
        }

        if(!mAreGeofencesStarted) {
            startGeofences();
            mAreGeofencesStarted = true;
        }

        if(!mIsZoomedIn) {
            if(mGoogleMap != null) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
                mIsZoomedIn = true;
            }
        }
    }

    /**
     * Constructs and adds a geofence. The new geofence is constructed to not
     * overlap with any of the other geofences and is constructed to be
     * sufficiently far away from the input location.
     *
     * @param userLocation  The base location to construct the new geofence
     *                      around.
     */
    private void addGeofenceInfo(Location userLocation) {

        boolean isNewLocationFound;
        Location newLocation;
        Location tempLocation;
        GeofenceInfo newGeofenceInfo;
        String key;

        isNewLocationFound = false;
        tempLocation = new Location("");

        do {
            newLocation = generateLocation(userLocation, GEOFENCES_MAX_DISTANCE_FROM_USER);

            if(userLocation.distanceTo(newLocation) >= (3 * GEOFENCE_RADIUS)) {
                if(mGeofenceInfos.isEmpty()) {
                    isNewLocationFound = true;
                }
                else {
                    for(GeofenceInfo geofenceInfo : mGeofenceInfos.values()) {
                        tempLocation.setLatitude(geofenceInfo.getLatitude());
                        tempLocation.setLongitude(geofenceInfo.getLongitude());

                        if(newLocation.distanceTo(tempLocation) >= (3 * GEOFENCE_RADIUS)) {
                            isNewLocationFound = true;
                        }
                        else {
                            isNewLocationFound = false;
                            break;
                        }
                    }
                }
            }
        } while(!isNewLocationFound);

        key = findUniqueKey();

        newGeofenceInfo = new GeofenceInfo(key,
                newLocation.getLatitude(),
                newLocation.getLongitude(),
                GEOFENCE_RADIUS,
                Geofence.NEVER_EXPIRE,
                Geofence.GEOFENCE_TRANSITION_ENTER);

        mGeofenceInfos.put(key, newGeofenceInfo);
    }

    /**
     * Generates a location within a certain distance from the input location.
     *
     * @param originLocation    The base location to construct the new geofence
     *                          around.
     * @param radiusInMeters    The max distance in meters the new location will
     *                          be from the input location.
     * @return                  A randomly generated location that is within the
     *                          input radius from the origin location.
     */
    private Location generateLocation(Location originLocation, double radiusInMeters) {

        Random rand;
        double radiusInDegrees, u, v, w, t, x, y, new_x, foundLatitude, foundLongitude;
        Location foundLocation;

        rand = new Random();
        foundLocation = new Location("");

        radiusInDegrees = radiusInMeters / 111000f;

        u = rand.nextDouble();
        v = rand.nextDouble();
        w = radiusInDegrees * Math.sqrt(u);
        t = 2 * Math.PI * v;
        x = w * Math.cos(t);
        y = w * Math.sin(t);

        new_x = x / Math.cos(originLocation.getLatitude());

        foundLatitude = y + originLocation.getLatitude();
        foundLongitude = new_x + originLocation.getLongitude();

        foundLocation.setLatitude(foundLatitude);
        foundLocation.setLongitude(foundLongitude);

        return foundLocation;
    }

    /**
     * Returns an unique key that is not in the current geofences.
     *
     * @return      A string representing a unique key.
     */
    private String findUniqueKey() {

        Integer i = 1;

        while(mGeofenceInfos.containsKey(i.toString())) {
            i++;
        }

        return i.toString();
    }

    /**
     * Convenience method that start monitoring geofences and adds markers to
     * the map.
     */
    private void startGeofences() {
        if(!mGeofenceInfos.isEmpty()) {
            addGeofences();
            addMarkers();
        }
    }

    /**
     * Convenience method that stops monitoring geofences and removes markers
     * from the map.
     */
    private void stopGeofences() {
        removeGeofences();
        removeMarkers();
    }

    /**
     * Adds markers for the geofences on the map.
     */
    private void addMarkers() {

        Marker marker;

        if(mGoogleMap == null) {
            return;
        }

        for(GeofenceInfo geofenceInfo : mGeofenceInfos.values()) {
            marker = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(geofenceInfo.getLatitude(), geofenceInfo.getLongitude())));
            mMarkers.add(marker);
        }
    }

    /**
     * Removes markers from the map.
     */
    private void removeMarkers() {
        for(Marker marker : mMarkers) {
            marker.remove();
        }
    }

    /**
     * Starts monitoring geofences.
     */
    private void addGeofences() {
        try {
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnFailureListener(mActivity, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error: Could not add geofences");
                        }
                    });
        } catch(SecurityException se) {
            Log.e(TAG, se.getMessage());
        }
    }

    /**
     * Stops monitoring geofences.
     */
    private void removeGeofences() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnFailureListener(mActivity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error: Could not remove geofences");
                    }
                });
    }

    /**
     * Returns a GeofencingRequest. The request specifies what geofences to
     * monitor and how they are triggered.
     *
     * @return      A GeofencingRequest with information about geofences.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        for(GeofenceInfo geofenceInfo : mGeofenceInfos.values()) {
            builder.addGeofence(geofenceInfo.getGeofence());
        }

        return builder.build();
    }

    /**
     * Returns a PendingIntent specifying the IntentService to handle the
     * geofence transitions.
     *
     * @return      A PendingIntent specifying the IntentService to handle the
     *              geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {

        Intent intent;

        if(mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        intent = new Intent(mContext, GeofenceTransitionsIntentService.class);

        mGeofencePendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return mGeofencePendingIntent;
    }

    /**
     * Sets up the location request. Defines update intervals.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Called when location permissions is denied. Displays a toast to the user
     * and updates the GoogleMap's UI.
     */
    private void locationPermissionDeniedRoutine() {
        Toast.makeText(mContext, R.string.error_location_permission, Toast.LENGTH_SHORT).show();
        updateLocationUI();
    }

    /**
     * Called when location setting is disabled. Displays a toast to the user
     * and updates the GoogleMap's UI.
     */
    private void locationSettingDisabledRoutine() {
        Toast.makeText(mContext, R.string.error_location_setting, Toast.LENGTH_SHORT).show();
        updateLocationUI();
    }

    /**
     * Updates the GoogleMap's UI.
     */
    private void updateLocationUI() {

        if(mGoogleMap == null) {
            return;
        }

        try {
            if(mLocationPermissionGranted && mLocationSettingEnabled) {
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
            else {
                mGoogleMap.setMyLocationEnabled(false);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch(SecurityException se) {
            Log.e(TAG, se.getMessage());
        }
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it can
     * later be reconstructed in a new instance of its process is restarted.
     *
     * @param outState      Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(KEY_LOCATION_PERMISSION_GRANTED, mLocationPermissionGranted);
        outState.putBoolean(KEY_LOCATION_SETTINGS_ENABLED, mLocationSettingEnabled);
        super.onSaveInstanceState(outState);
    }

    /**
     * Called to restore state from data in bundle.
     *
     * @param savedInstanceState    Bundle containing state data.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {

        if(savedInstanceState == null) {
            return;
        }

        if(savedInstanceState.containsKey(KEY_LOCATION_PERMISSION_GRANTED)) {
            mLocationPermissionGranted = savedInstanceState.getBoolean(KEY_LOCATION_PERMISSION_GRANTED);
        }

        if(savedInstanceState.containsKey(KEY_LOCATION_SETTINGS_ENABLED)) {
            mLocationSettingEnabled = savedInstanceState.getBoolean(KEY_LOCATION_SETTINGS_ENABLED);
        }
    }

    /**
     * Handles interaction with the app bar's menu. If the 'new geofences' item
     * was selected the current geofences are removed and new ones are created.
     *
     * @param menuItem      The menu item that was selected.
     * @return              Return false to allow normal menu processing to
     *                      proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if(menuItem.getItemId() == R.id.toolbar_map_new_geofences) {

            stopGeofences();

            mGeofenceInfos.clear();
            mMarkers.clear();

            for(int i = 0; i < NUMBER_OF_GEOFENCES; i++) {
                addGeofenceInfo(mLastKnownLocation);
            }

            startGeofences();
        }

        return super.onOptionsItemSelected(menuItem);
    }
}
