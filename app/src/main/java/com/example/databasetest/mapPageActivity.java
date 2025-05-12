package com.example.databasetest;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

//import com.example.databasetest.Adapter.RecentsAdpater;
import com.example.databasetest.model.PlaceInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class mapPageActivity extends AppCompatActivity implements OnMapReadyCallback, OnConnectionFailedListener {

    private PlacesClient gPlacesClient;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private static final String TAG = "mapPageActivity";
    private static final String AFL = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String ACL = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final LatLng mDefaultLocation = new LatLng(22.3193, 114.1694);

    private static final float DEFAULT_ZOOM = 15f;

    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -160), new LatLng(71, 136));
    private AutoCompleteTextView mSearchText;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private ImageView mGps, mInfo;
    private ImageButton mSearchButton;

    private static final int LOCATION_PERMISSION_REQEST_code = 123;
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap gMap;
    private FusedLocationProviderClient gFusedLocationProviderClient;
    private PlaceAutoCompleteAdapter gPlaceAutoCompleteAdapter;

    private PlaceInfo mPlace;
    private Marker mMarker;
    //RecyclerView recentRecycler;
    //RecentsAdpater recentsAdpater;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map on running");
        gMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            gMap.setMyLocationEnabled(true);
            gMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Your buttons
        ImageButton btnProfile = findViewById(R.id.btn_profile);
        ImageButton btnHomePage = findViewById(R.id.btn_homePage);
        ImageButton btnLogout = findViewById(R.id.btn_logout);
        ImageButton btnChat = findViewById(R.id.btn_chat);
        ImageButton btnplan = findViewById(R.id.btn_plan);

        // Set button action listeners
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mapPageActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        btnHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace HomeActivity with your actual home page activity
                Intent intent = new Intent(mapPageActivity.this, homePage.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(mapPageActivity.this, homePage.class);
                startActivity(intent);
                finish();
            }
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mapPageActivity.this, ChatRoomHomePageActivity.class);
                startActivity(intent);
            }
        });

        btnplan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mapPageActivity.this, TripPlannerActivity.class);
                startActivity(intent);
            }
        });

        mSearchText = (AutoCompleteTextView) findViewById(R.id.txt_searchMap);
        mGps = (ImageView) findViewById(R.id.ic_gps);
        mInfo = (ImageView) findViewById(R.id.ic_info);
        mSearchButton = (ImageButton) findViewById(R.id.btn_searchMap);

//        recentRecycler = findViewById(R.id.place_recycler_view);
//        List<RecentsData> recentsDataList = new ArrayList<>();
//        recentsDataList.add(new RecentsData("Kyoto", "Japan", R.drawable.bg_japan));
//        recentsDataList.add(new RecentsData("Kyoto", "Japan", R.drawable.bg_japan));
//        setRecentRecycler(recentsDataList);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCIOLtoV4py0DED2XEFygb0HVwihCAQoDA");
        }

        gPlacesClient = Places.createClient(this);

        getLocationPermission();
    }

//    private void setRecentRecycler(List<RecentsData> recentsDataList) {
//        Log.d(TAG, "map recommentation ready");
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
//        recentRecycler.setLayoutManager(layoutManager);
//        recentsAdpater = new RecentsAdpater(this, recentsDataList);
//        recentRecycler.setAdapter(recentsAdpater);
//    }

    private void init() {
        Log.d(TAG, "init: initializing");

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCIOLtoV4py0DED2XEFygb0HVwihCAQoDA");
        }

        PlacesClient placesClient = Places.createClient(this);

        mSearchText.setOnItemClickListener(mAutocompleteClickListener);

        gPlaceAutoCompleteAdapter = new PlaceAutoCompleteAdapter(this, gPlacesClient,
                LAT_LNG_BOUNDS);

        mSearchText.setAdapter(gPlaceAutoCompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    geoLocate();
                }
                return false;
            }
        });

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                geoLocate();
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onclick: clicked gps ");
                getDeviceLocation();
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onclick: clicked place info ");
                try {
                    if (mMarker != null) {
                        if (mMarker.isInfoWindowShown()) {
                            mMarker.hideInfoWindow();
                        } else {
                            Log.d(TAG, "onclick: place info " + mPlace.toString());
                            mMarker.showInfoWindow();
                        }
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, "onclick: clicked place info " + e.getMessage());
                }
            }
        });
        HideSoftKeyboard();
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");
        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(mapPageActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException:" + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, "geolocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        gFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                final Task location = gFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                        DEFAULT_ZOOM, "My location");
                            } else {
                                Log.d(TAG, "Current location is null. Using defaults.");
                                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                gMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }

                        } else {
                            Log.d(TAG, "onComplete: current location is not found");
                            Toast.makeText(mapPageActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        gMap.clear();
        gMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(mapPageActivity.this));
        if (placeInfo != null) {
            try {
                StringBuilder snippet = new StringBuilder();
                if (placeInfo.getAddress() != null) {
                    snippet.append("Address: ").append(placeInfo.getAddress()).append("\n");
                }
                if (placeInfo.getPhoneNumber() != null) {
                    snippet.append("Phone Number: ").append(placeInfo.getPhoneNumber()).append("\n");
                }
                if (placeInfo.getWebsiteUri() != null) {
                    snippet.append("Website: ").append(placeInfo.getWebsiteUri()).append("\n");
                }
                if (placeInfo.getRating() != null) {
                    snippet.append("Price Rating: ").append(placeInfo.getRating()).append("\n");
                }
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet.toString());
                mMarker = gMap.addMarker(options);
            } catch (NullPointerException e) {
                Log.e(TAG, "moveCamera:　NullPointerException: " + e.getMessage());
            }
        } else {
            gMap.addMarker(new MarkerOptions().position(latLng));
        }
        HideSoftKeyboard();
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (!title.equals("My location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            gMap.addMarker(options);
        }
        HideSoftKeyboard();
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(mapPageActivity.this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: get Location Permission");
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                AFL) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    ACL) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQEST_code);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQEST_code);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: called");
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQEST_code: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: success");
                    mLocationPermissionGranted = true;
                    //initialize the map
                    initMap();
                }
            }
        }
    }

    private void HideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            HideSoftKeyboard();

            final AutocompletePrediction item = gPlaceAutoCompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            // Specify the fields to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI, Place.Field.RATING);

            // Construct a request object, passing the place ID and fields array.
            FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

            gPlacesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                Place place = response.getPlace();
                Log.i(TAG, "Place found: " + place.getName());

                // Update mPlace object with the new place details
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName());
                mPlace.setAddress(place.getAddress());
                mPlace.setId(place.getId());
                if (place.getLatLng() != null) {
                    mPlace.setLatlng(place.getLatLng());
                }
                if (place.getRating() != null) {
                    mPlace.setRating(place.getRating());
                }
                if (place.getPhoneNumber() != null) {
                    mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                }
                if (place.getWebsiteUri() != null) {
                    mPlace.setWebsiteUri(place.getWebsiteUri());
                }

                moveCamera(place.getLatLng(), DEFAULT_ZOOM, mPlace);

            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                }
            });
        }
    };
}