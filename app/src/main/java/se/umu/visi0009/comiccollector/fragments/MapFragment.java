package se.umu.visi0009.comiccollector.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import se.umu.visi0009.comiccollector.R;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private static final int FAILURE_ZOOM = 4;
    private static final LatLng FAILURE_LOCATION = new LatLng(62.3875, 16.325556);
    private static final int DEFAULT_ZOOM = 17;
    private static final String KEY_LOCATION_PERMISSION_ALREADY_REQUESTED = "mLocationPermissionAlreadyRequested";
    private static final String KEY_LOCATION_PERMISSION_GRANTED = "mLocationPermissionGranted";
    private static final String KEY_LOCATION_SETTINGS_ENABLED = "mLocationSettingEnabled";

    private Context mContext;
    private Activity mActivity;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mGoogleMap;
    private Location mLastKnownLocation;
    private LocationRequest mLocationRequest;
    private boolean mLocationPermissionGranted = false;
    private boolean mLocationPermissionAlreadyRequested = false;
    private boolean mLocationSettingEnabled = false;
    private boolean mIsZoomedIn = false;
    private boolean mRequestingLocationUpdates;

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
        this.mActivity = getActivity();
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
        if(mLocationPermissionAlreadyRequested) {
            return;
        }

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            locationPermissionGrantedRoutine();
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage("Location permission is needed to pinpoint your position. Your position is used to set the gaming environment.").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mLocationPermissionAlreadyRequested = true;
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                }).show();
            } else {
                mLocationPermissionAlreadyRequested = true;
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
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
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
            }, null);
        }
        catch (SecurityException se) {
            Log.e("Exception %s", se.getMessage());
        }
    }

    private void onLocationChanged(Location location) {
        Toast.makeText(mContext, "(" + location.getLatitude() + ", " + location.getLongitude() + ")", Toast.LENGTH_SHORT).show();

        if(!mIsZoomedIn) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
            mIsZoomedIn = true;
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
        outState.putBoolean(KEY_LOCATION_PERMISSION_ALREADY_REQUESTED, mLocationPermissionAlreadyRequested);
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

        if(savedInstanceState.containsKey(KEY_LOCATION_PERMISSION_ALREADY_REQUESTED)) {
            mLocationPermissionAlreadyRequested = savedInstanceState.getBoolean(KEY_LOCATION_PERMISSION_ALREADY_REQUESTED);
        }
    }
}
