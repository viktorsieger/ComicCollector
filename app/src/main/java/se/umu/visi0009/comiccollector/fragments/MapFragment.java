package se.umu.visi0009.comiccollector.fragments;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Random;

import se.umu.visi0009.comiccollector.R;
import se.umu.visi0009.comiccollector.entities.GeofenceInfo;
import se.umu.visi0009.comiccollector.entities.GeofenceTransitionsIntentService;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private static final int FAILURE_ZOOM = 4;
    private static final LatLng FAILURE_LOCATION = new LatLng(62.3875, 16.325556);
    private static final int DEFAULT_ZOOM = 14;
    private static final String KEY_LOCATION_PERMISSION_GRANTED = "mLocationPermissionGranted";
    private static final String KEY_LOCATION_SETTINGS_ENABLED = "mLocationSettingEnabled";
    private static final double GEOFENCES_MAX_DISTANCE_FROM_USER = 10000;
    private static final double GEOFENCE_RADIUS = 100;
    private static final int NUMBER_OF_GEOFENCES = 20;
    private static final String FILENAME_GEOFENCE_INFOS = "currentGeofenceInfos";

    private Context mContext;
    private Activity mActivity;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mGoogleMap;
    private LocationRequest mLocationRequest;
    private boolean mLocationPermissionGranted = false;
    private boolean mLocationSettingEnabled = false;
    private boolean mIsZoomedIn = false;
    private LocationCallback mLocationCallback;
    private GeofencingClient mGeofencingClient;
    private boolean mGeofenceInfosExists;
    private boolean mAreGeofencesAdded = false;
    private PendingIntent mGeofencePendingIntent;
    private HashMap<String, GeofenceInfo> mGeofenceInfos = new HashMap<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateValuesFromBundle(savedInstanceState);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        mGeofencingClient = LocationServices.getGeofencingClient(mContext);
        mGeofencePendingIntent = null;

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        GeofenceReceiver geofenceReceiver = new GeofenceReceiver(this);
        lbm.registerReceiver(geofenceReceiver, new IntentFilter("geofenceIntentFilter"));

        Log.d("TEST", "isFileInPersistentStorage " + isFileInPersistentStorage(FILENAME_GEOFENCE_INFOS));

        if(isFileInPersistentStorage(FILENAME_GEOFENCE_INFOS)) {
            readGeofenceInfosFromPersistentStorage();
            mGeofenceInfosExists = true;
        }
        else {
            mGeofenceInfosExists = false;
        }

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult != null) {
                    onLocationChanged(locationResult.getLastLocation());
                }
                else {
                    Toast.makeText(mContext, R.string.error_location, Toast.LENGTH_SHORT).show(); // REMOVE?
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(FAILURE_LOCATION, FAILURE_ZOOM)); // REMOVE?
                }
            }
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();

        stopLocationUpdates();

        if(mAreGeofencesAdded) {
            removeGeofences();
        }

        if(mGeofenceInfosExists) {
            writeGeofenceInfosToPersistentStorage();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if(mLocationPermissionGranted) {
            if(mLocationSettingEnabled) {
                run();
            }
            else {
                checkLocationSetting();
            }
        }
        else {
            checkLocationPermission();
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            locationPermissionGrantedRoutine();
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage("Location permission is needed to pinpoint your position. Your position is used to set the gaming environment.").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                }).show();
            } else {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            switch (requestCode) {
                case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionGranted = true;
                        locationPermissionGrantedRoutine();
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        locationPermissionDeniedRoutine();
                    }
                }
            }
        }
    }

    private void locationPermissionGrantedRoutine() {
        createLocationRequest();
        checkLocationSetting();
    }

    private void checkLocationSetting() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);

        Task<LocationSettingsResponse> taskLocationSettingsResponse = LocationServices.getSettingsClient(mContext).checkLocationSettings(builder.build());
        taskLocationSettingsResponse.addOnSuccessListener(mActivity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                mLocationSettingEnabled = true;
                locationSettingEnabledRoutine();
            }
        }).addOnFailureListener(mActivity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException rae = (ResolvableApiException) e;
                        rae.startResolutionForResult(mActivity, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sie) {
                        Log.e("Exception %s", sie.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mLocationSettingEnabled = true;
                        locationSettingEnabledRoutine();
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

    private void locationSettingEnabledRoutine() {
        run();
    }

    private void run() {
        updateLocationUI();
        createLocationRequest();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
        catch (SecurityException se) {
            Log.e("Exception %s", se.getMessage());
        }
    }

    private void onLocationChanged(Location location) {

        if(!mGeofenceInfosExists) {
            for(int i = 0; i < NUMBER_OF_GEOFENCES; i++) {
                addGeofenceInfo(location);
            }

            mGeofenceInfosExists = true;
        }

        if(!mAreGeofencesAdded) {
            if(!mGeofenceInfos.isEmpty()) {
                addMarkers();
                addGeofences();
            }

            mAreGeofencesAdded = true;
        }

        if(!mIsZoomedIn) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
            mIsZoomedIn = true;
        }
    }

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
        } while (!isNewLocationFound);

        key = findUniqueKey();

        newGeofenceInfo = new GeofenceInfo(key,
                newLocation.getLatitude(),
                newLocation.getLongitude(),
                (float)GEOFENCE_RADIUS,
                Geofence.NEVER_EXPIRE,
                Geofence.GEOFENCE_TRANSITION_ENTER);

        mGeofenceInfos.put(key, newGeofenceInfo);
    }

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

    private String findUniqueKey() {

        Integer i = 1;

        while(mGeofenceInfos.containsKey(i.toString())) {
            i++;
        }

        return i.toString();
    }

    private void addMarkers() {

        Log.d("TEST", "addMarkers");

        for(GeofenceInfo geofenceInfo : mGeofenceInfos.values()) {
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(geofenceInfo.getLatitude(), geofenceInfo.getLongitude())));
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void addGeofences() {
        try {
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(mActivity, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TEST", "addGeofences - onSuccess");
                        }
                    })
                    .addOnFailureListener(mActivity, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("TEST", "addGeofences - onFailure");
                            Log.d("TEST", "" + ((ApiException) e).getStatusCode());
                        }
                    });
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        for (GeofenceInfo geofenceInfo : mGeofenceInfos.values()) {
            builder.addGeofence(geofenceInfo.getGeofence());
        }

        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {

        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);

        mGeofencePendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return mGeofencePendingIntent;
    }

    private void removeGeofences() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(mActivity, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TEST", "removeGeofences - onSuccess");
                    }
                })
                .addOnFailureListener(mActivity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TEST", "removeGeofences - onFailure");
                    }
                });
    }

    private void locationPermissionDeniedRoutine() {
        Toast.makeText(mContext, "Location permission is needed to run the game", Toast.LENGTH_SHORT).show();
        updateLocationUI();
    }

    private void locationSettingDisabledRoutine() {
        Toast.makeText(mContext, "Location setting need to be enabled to run the game", Toast.LENGTH_SHORT).show();
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mGoogleMap == null) {
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
        } catch (SecurityException se) {
            Log.e("Exception %s", se.getMessage());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(KEY_LOCATION_PERMISSION_GRANTED, mLocationPermissionGranted);
        outState.putBoolean(KEY_LOCATION_SETTINGS_ENABLED, mLocationSettingEnabled);
        super.onSaveInstanceState(outState);
    }

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

    private boolean isFileInPersistentStorage(String filenameToCheck) {
        String[] allFiles;

        allFiles = mContext.fileList();

        for(String tempFilename : allFiles) {
            if(filenameToCheck.equals(tempFilename)) {
                return true;
            }
        }

        return false;
    }

    private void readGeofenceInfosFromPersistentStorage() {
        FileInputStream fis;
        ObjectInputStream ois;

        try {
            fis = mContext.openFileInput(FILENAME_GEOFENCE_INFOS);
            ois = new ObjectInputStream(fis);
            mGeofenceInfos = (HashMap<String, GeofenceInfo>) ois.readObject();
            ois.close();
            fis.close();
        }
        catch (Exception e) {
            Log.d("TEST", e.getMessage());
        }
    }

    private void writeGeofenceInfosToPersistentStorage() {
        FileOutputStream fos;
        ObjectOutputStream oos;

        try {
            fos = mContext.openFileOutput(FILENAME_GEOFENCE_INFOS, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(mGeofenceInfos);
            oos.close();
            fos.close();
        }
        catch (Exception e) {
            Log.d("TEST", e.getMessage());
        }
    }

    // https://stackoverflow.com/questions/34384101/after-geofence-transition-how-do-i-do-something-on-main-activity
    class GeofenceReceiver extends BroadcastReceiver {

        MapFragment mMapFragment;

        public GeofenceReceiver(MapFragment mapFragment) {
            mMapFragment = mapFragment;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TEST", "" + intent.getIntExtra("keyTest", -1));

            Toast.makeText(mContext, "GeofenceReceiver - onReceive", Toast.LENGTH_SHORT).show();
        }
    }
}
