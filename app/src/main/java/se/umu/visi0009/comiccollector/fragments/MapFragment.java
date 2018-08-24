package se.umu.visi0009.comiccollector.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import se.umu.visi0009.comiccollector.R;

import static android.app.Activity.RESULT_OK;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, OnCompleteListener {

    private boolean mLocationPermissionGranted;
    private Context mContext;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mGoogleMap;
    private Location mLastKnownLocation;
    private LocationRequest mLocationRequest;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private static final int DEFAULT_ZOOM = 15;
    private final LatLng mDefaultLocation = new LatLng(63.8258, 20.2630);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        checkLocationPermission();
        updateLocationUI();

        getDeviceLocation();

        createLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(mContext);
        Task<LocationSettingsResponse> taskLocationSettingsResponse = client.checkLocationSettings(builder.build());
        taskLocationSettingsResponse.addOnCompleteListener(this);

        if(mLocationPermissionGranted) {
            setupGame();
        }

    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        }
        else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    mLocationPermissionGranted = true;
                }
                else {
                    Toast.makeText(mContext, R.string.error_permission_denied, Toast.LENGTH_SHORT).show();
                }
            }
        }

        updateLocationUI();
        getDeviceLocation();
    }

    private void updateLocationUI() {

        if (mGoogleMap == null) {
            return;
        }

        try {
            if (mLocationPermissionGranted) {
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                mGoogleMap.setOnMyLocationButtonClickListener(this);
            }
            else {
                mGoogleMap.setMyLocationEnabled(false);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        } catch (SecurityException se) {
            Log.e("Exception %s", se.getMessage());
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(mContext, "Button clicked!", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void getDeviceLocation() {
        try {
            if(mLocationPermissionGranted) {
                Task<Location> taskLocation = mFusedLocationProviderClient.getLastLocation();
                taskLocation.addOnCompleteListener(this);
            }
        } catch (SecurityException se) {
            Log.e("Exception %s", se.getMessage());
        }
    }

    @Override
    public void onComplete(@NonNull Task task) {
        if(isObjectOf(task.getResult(), Location.class)) {
            if(task.isSuccessful() && ((mLastKnownLocation = (Location)task.getResult()) != null)) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
            }
            else {
                Toast.makeText(mContext, R.string.error_location, Toast.LENGTH_SHORT).show();

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }
        else if(isObjectOf(task.getResult(), LocationSettingsResponse.class)) {
            if(task.isSuccessful()) {

            }
            else {
                if(task.getException() instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException)task.getException();
                        resolvable.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sie) {
                        Log.e("Exception %s", sie.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CHECK_SETTINGS) {
            if(resultCode == RESULT_OK) {
                //DO SOMETHING
            }
        }
    }


    private boolean isObjectOf(Object object, Class classType) {
        return classType.isInstance(object);
    }

    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void setupGame() {

    }
}
