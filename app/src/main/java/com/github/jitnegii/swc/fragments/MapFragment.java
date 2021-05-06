package com.github.jitnegii.swc.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jitnegii.swc.Activity;
import com.github.jitnegii.swc.R;
import com.github.jitnegii.swc.models.Bin;
import com.github.jitnegii.swc.models.User;
import com.github.jitnegii.swc.utils.AnimationUtils;
import com.github.jitnegii.swc.utils.AppUtils;
import com.github.jitnegii.swc.utils.FirebaseUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapsFragment";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int LOC_PERMISSION_RQST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    public boolean locPermissionGranted = false;

    private Activity activity;
    private LocationManager mLocationManager;


    private Button findBin;
    private FloatingActionButton copyFb, myLocFab;
    private EditText searchBar;
    private GoogleMap gMap;
    private Marker currentMarker, searchedLoc;
    private AnimationUtils animationUtils;
    private LatLng currentLatlong;

    private List<Bin> bins;

    private View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_map, container, false);

        findBin = view.findViewById(R.id.findBin);
        // myLoc = view.findViewById(R.id.myLoc);
        myLocFab = view.findViewById(R.id.myLocFac);
        copyFb = view.findViewById(R.id.copyFab);

        searchBar = view.findViewById(R.id.searchBar);
        searchBar.setSingleLine(true);


        activity = (Activity) getActivity();
        animationUtils = new AnimationUtils(activity);

        mLocationManager = (LocationManager) activity.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        AppUtils.hideSoftKeyboard(activity, view);

        if (isServiceOK()) {
            init();
            initMap();
        }
        return view;
    }

    private void init() {

        bins = new ArrayList<>();
        myLocFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getLocationPermission()) {
                    gMap.setMyLocationEnabled(true);
                    getDeviceLocation();

                }

            }
        });

        copyFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentLatlong != null) {
                    String location = "latlong:" + currentLatlong.latitude + ":" + currentLatlong.longitude;

                    AppUtils.copyText(location, activity);
                } else {
                    Toast.makeText(activity, "Select the marker", Toast.LENGTH_SHORT).show();
                }
            }
        });

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    AppUtils.hideSoftKeyboard(activity, view);

                    String location = searchBar.getText().toString();
                    geoLocate(location, false);
                    return true;
                }
                return false;
            }
        });

        getBinLoc();

    }


    public void myLocation() {

        if (gMap != null) {
            User user = AppUtils.getUserFromDb(activity.getApplicationContext());

            if (user != null) {
                String searchString = user.getState() + ", " + user.getCity();

                Geocoder geocoder = new Geocoder(activity);
                List<Address> list = new ArrayList<>();

                try {
                    list = geocoder.getFromLocationName(searchString, 1);
                } catch (IOException e) {
                    Log.d(TAG, "geoLocate: IOException: " + e.getMessage());
                }

                if (list.size() > 0) {
                    Address address = list.get(0);
                    Log.d(TAG, "geoLocate: found a locatioin: " + address.toString());

                    moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),
                            DEFAULT_ZOOM, address.getAddressLine(0));
                }
            }
        }
    }

    public void geoLocate(String location, boolean isLatLong) {


        Geocoder geocoder = new Geocoder(activity);
        List<Address> list = new ArrayList<>();

        if (!isLatLong) {
            try {
                list = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                Log.d(TAG, "geoLocate: IOException: " + e.getMessage());
            }

        } else {
            String[] splitLatLong = location.split(":");
            try {
                list = geocoder.getFromLocation(Double.parseDouble(splitLatLong[0]), Double.parseDouble(splitLatLong[1]), 1);
            } catch (IOException e) {
                Log.d(TAG, "geoLocate: IOException: " + e.getMessage());
            }
        }

        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: found a locatioin: " + address.toString());

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),
                    DEFAULT_ZOOM, address.getAddressLine(0));
        }

        Toast.makeText(activity, "GeoLoc", Toast.LENGTH_SHORT).show();

    }

    public void initMap() {
        Log.d(TAG, "initMap: initialize map");
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null)
            mapFragment.getMapAsync(MapFragment.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map ready");
        Toast.makeText(activity, "Map is ready", Toast.LENGTH_SHORT).show();
        gMap = googleMap;

        gMap.getUiSettings().setMyLocationButtonEnabled(false);
        gMap.getUiSettings().setMapToolbarEnabled(false);

        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {


                    if (currentMarker != null)
                        currentMarker.remove();

                    currentMarker = gMap.addMarker(new MarkerOptions().position(latLng).title("Clicked Location"));
                    copyFb.setColorFilter(ContextCompat.getColor(activity, R.color.Gray));
                    currentLatlong = null;

            }
        });

        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                currentLatlong = marker.getPosition();
                copyFb.setColorFilter(ContextCompat.getColor(activity, R.color.colorPurple_A400));

                return false;
            }
        });

    }


    private boolean isServiceOK() {
        Log.d(TAG, "Checking google service version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);

        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(activity, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(activity, "Can't make map request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean getLocationPermission() {

        Log.d(TAG, "LocPermission: called");

        String[] permission = {FINE_LOCATION, COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locPermissionGranted = true;

            } else {

                ActivityCompat.requestPermissions(activity,
                        permission,
                        LOC_PERMISSION_RQST_CODE);
                return false;
            }
        } else {
            ActivityCompat.requestPermissions(activity,
                    permission,
                    LOC_PERMISSION_RQST_CODE);
            return false;
        }

        if (locPermissionGranted && !isLocationServiceEnabled()) {
            new AlertDialog.Builder(activity)
                    .setMessage("Open location")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                    }).show();
            return false;
        }

        if (locPermissionGranted && isLocationServiceEnabled())
            return true;
        else
            return false;

    }

    private boolean isLocationServiceEnabled() {

        return mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    }

    private void getDeviceLocation() {
        Log.d(TAG, "Getting device location");

        FusedLocationProviderClient fusedLoc = LocationServices.getFusedLocationProviderClient(activity);

        try {
            if (locPermissionGranted) {
                final Task location = fusedLoc.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {
                            Log.d(TAG, "Found location");
                            Location currentLoc = (Location) task.getResult();

                            moveCamera(new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude()),
                                    DEFAULT_ZOOM, "My Location");

                        } else {
                            Log.d(TAG, "Current location null");
                            Toast.makeText(activity, "Unable to find location", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        } catch (SecurityException s) {
            Log.e(TAG, "Security exception");
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {

        if (gMap != null) {
            Log.d(TAG, "MoveCamera to lat: " + latLng.latitude + " long: " + latLng.longitude);

            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

            if (!title.equals("My Location")) {
                addMarker(latLng, title, true);
            }
        }
    }


    private void getBinLoc() {

        FirebaseUtils.getFirebaseDRef()
                .child("bins").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bins.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Bin newBin = dataSnapshot.getValue(Bin.class);

                    if (newBin != null) {
                        Log.d("Bin ", newBin.getLatitude() + " " + newBin.getLongitude() + " " + newBin.getStatus());
                        bins.add(new Bin(newBin.getLatitude(), newBin.getLongitude(), newBin.getStatus()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void addBinMarker() {

        if (gMap != null && bins != null) {
            LatLng latLng;

            for (Bin bin : bins) {

                latLng = new LatLng(Double.parseDouble(bin.getLatitude()), Double.parseDouble(bin.getLongitude()));

                addMarker(latLng, "Bin", false);
            }
        }
    }

    private void addMarker(LatLng latLng, String title, boolean rememberLoc) {
        MarkerOptions marker;
        marker = new MarkerOptions()
                .position(latLng)
                .title("Bin");

        if (rememberLoc)
            gMap.addMarker(marker);
        else
            searchedLoc = gMap.addMarker(marker);
    }


}
