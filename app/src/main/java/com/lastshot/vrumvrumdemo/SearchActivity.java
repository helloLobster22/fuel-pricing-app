package com.lastshot.vrumvrumdemo;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.Point2D;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.MapError;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapPolyline;
import com.here.sdk.mapview.MapScene;
import com.here.sdk.mapview.MapScheme;
import com.here.sdk.mapview.MapView;
import com.here.sdk.routing.CalculateRouteCallback;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.RoutingError;
import com.here.sdk.routing.TruckOptions;
import com.here.sdk.routing.Waypoint;
import com.here.sdk.search.Place;
import com.here.sdk.search.SearchCallback;
import com.here.sdk.search.SearchEngine;
import com.here.sdk.search.SearchError;
import com.here.sdk.search.SearchOptions;
import com.here.sdk.search.SuggestCallback;
import com.here.sdk.search.Suggestion;
import com.here.sdk.search.TextQuery;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private PermissionsRequestor permissionsRequestor;
    private MapView mapView;
    private LocationRequest locationRequest;
    private SearchEngine searchEngine;
    private Context context;

    private RoutingEngine routingEngine;
    private List<Waypoint> waypoints = new ArrayList<>();
    private List<MapMarker> waypointMarkers = new ArrayList<>();
    private MapPolyline routePolyline;
    private Color routeColor = Color.valueOf(0xffff0000);

    private EditText startPoint;
    private EditText endPoint;
    LinearLayout suggestionsLayout;

    private int carId;
    private Car myCar;
    private TextView carSelected;
    private GeoCoordinates currentGeoCoordinates;

    private GeoCoordinates paris = new GeoCoordinates(48.864716, 2.349014);
    private GeoCoordinates agh = new GeoCoordinates(50.06704073135628, 19.91364065591431);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        context = getApplicationContext();
        startPoint = findViewById(R.id.start_search);
        endPoint = findViewById(R.id.end_search);
        suggestionsLayout = findViewById(R.id.ll_auto_suggestion);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(2000);
        getCurrentLocation();

        // Get a MapView instance from the layout.
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        mapView.setOnReadyListener(new MapView.OnReadyListener() {
            @Override
            public void onMapViewReady() {
                // This will be called each time after this activity is resumed.
                // It will not be called before the first map scene was loaded.
                // Any code that requires map data may not work as expected.
                Log.d(TAG, "HERE Rendering Engine attached.");
            }
        });

        handleAndroidPermissions();

        try {
            searchEngine = new SearchEngine();
        } catch (InstantiationErrorException e) {
            throw  new RuntimeException("oh, something went terrbly wwrong:(");
        }

        startPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (startPoint.length() > 0) {
                    searchAutoSuggest(startPoint);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                suggestionsLayout.setVisibility(View.INVISIBLE);
            }
        });

        endPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (endPoint.length() > 0) {
                    searchAutoSuggest(endPoint);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                suggestionsLayout.setVisibility(View.INVISIBLE);
            }
        });

        try {
            routingEngine = new RoutingEngine();
        } catch (InstantiationErrorException e) {

        }

        carSelected = findViewById(R.id.message);
        Intent intent = getIntent();
        carId = intent.getIntExtra("id", -1);
        DatabaseHelper databaseHelper = new DatabaseHelper(SearchActivity.this);
        myCar = databaseHelper.getOne(carId);
        carSelected.setText(myCar.getName());
    }

    /**********************/

    private void loadMapScene() {
        mapView.getMapScene().loadScene(MapScheme.NORMAL_DAY, new MapScene.LoadSceneCallback() {
            @Override
            public void onLoadScene(@Nullable MapError mapError) {
                if (mapError == null) {
                    double distanceInMeters = 1000 * 10;
                    mapView.getCamera().lookAt(agh, distanceInMeters);
                } else {
                    Log.d(TAG, "Loading map failed: mapError: " + mapError.name());
                }
            }
        });
    }

    private void searchAutoSuggest(EditText wp) {
        int maxItems = 3;
        SearchOptions searchOptions = new SearchOptions(LanguageCode.EN_US, maxItems);
        EditText wantedPoint = wp; //findViewById(R.id.start_search);
        TextQuery textQuery = new TextQuery(wantedPoint.getText().toString(), getScreenCenter());

        searchEngine.suggest(textQuery, searchOptions, new SuggestCallback() {
            @Override
            public void onSuggestCompleted(@Nullable SearchError searchError, @Nullable List<Suggestion> list) {
                ArrayList<String> arrayList = new ArrayList<>();

                for (Suggestion suggestResult: list) {
                    Place place = suggestResult.getPlace();
                    if (place != null) {
                        arrayList.add(place.getTitle());
                    } else {
                        arrayList.add(suggestResult.getTitle());
                    }
                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_expandable_list_item_1, arrayList);

                ListView listView = findViewById(R.id.auto_suggest);
                suggestionsLayout.setVisibility(View.VISIBLE);
                listView.setAdapter(arrayAdapter);
            }
        });
    }

    /* public void clearMap(View view) {
        for (MapMarker marker: waypointMarkers) {
            mapView.getMapScene().removeMapMarker(marker);
        }

        mapView.getMapScene().removeMapPolyline(routePolyline);
        waypoints.clear();
    } */

    private GeoCoordinates getScreenCenter() {
        int screenWidthInPixels = mapView.getWidth();
        int screenHeightInPixels = mapView.getHeight();
        Point2D point = new Point2D(screenWidthInPixels * .5, screenHeightInPixels * .5);
        return mapView.viewToGeoCoordinates(point);
    }

    public void addRoute(View view) {
        searchPlaces(startPoint);
        searchPlaces(endPoint);
    }

    private void searchPlaces(EditText point) {
        int maxItems = 1;
        SearchOptions searchOptions = new SearchOptions(LanguageCode.EN_US, maxItems);

        EditText searchPoint = point; //findViewById(R.id.searchText);
        TextQuery textQuery = new TextQuery(searchPoint.getText().toString(), getScreenCenter());


        searchEngine.search(textQuery, searchOptions, new SearchCallback() {
            @Override
            public void onSearchCompleted(@Nullable SearchError searchError, @Nullable List<Place> list) {
                for (Place result: list) {
                    TextView textView = new TextView(context);
                    textView.setTextColor(android.graphics.Color.parseColor("#000000"));
                    textView.setText(result.getTitle());

                    LinearLayout linearLayout = new LinearLayout(context);
                    linearLayout.setBackgroundResource((R.color.design_default_color_primary));
                    linearLayout.setPadding(10, 10, 10, 10);
                    linearLayout.addView(textView);

                    mapView.pinView(linearLayout, result.getGeoCoordinates());

                    waypoints.add(new Waypoint(result.getGeoCoordinates()));
                    Toast.makeText(context, String.valueOf(waypoints.size()), Toast.LENGTH_SHORT).show();
                    mapView.getCamera().lookAt(result.getGeoCoordinates());
                }
            }
        });
    }

    private void drawRoute(@NonNull Route route) {
        GeoPolyline routeGeoPolyline;

        try {
            routeGeoPolyline = new GeoPolyline(route.getPolyline());
        } catch (InstantiationErrorException e) {
            return;
        }

        routePolyline = new MapPolyline(routeGeoPolyline, 20, routeColor);
        mapView.getMapScene().addMapPolyline(routePolyline);
    }

    public void calculateRoute(View view) {
        //Toast.makeText(context, "lol2", Toast.LENGTH_SHORT).show();
        routingEngine.calculateRoute(waypoints, new TruckOptions(), new CalculateRouteCallback() {
            @Override
            public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List<Route> routes) {
                if (routingError == null) {
                    Route route = routes.get(0);
                    drawRoute(route);
                    showMessage(route);
                } else {
                    // error handling
                }
            }
        });
    }

    private void showMessage(Route route) {
        double cost = route.getLengthInMeters() / 100000.0 * myCar.getMileage() - myCar.getCurrentFuelLevel();
        //Toast.makeText(context, String.valueOf(route.getLengthInMeters() / 100000.0), Toast.LENGTH_SHORT).show();
        String newMessage = "Fuel needed: " + cost + "L of " + myCar.getFuelType();

        carSelected.setText(newMessage);
    }

    /**********************/

    private void getCurrentLocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(SearchActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(SearchActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {

                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(SearchActivity.this)
                                            .removeLocationUpdates(this);

                                    if (locationResult != null && locationResult.getLocations().size() > 0) {

                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();

                                        currentGeoCoordinates = new GeoCoordinates(latitude, longitude);
                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(SearchActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(SearchActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }

    /**********************/

    private void handleAndroidPermissions() {
        permissionsRequestor = new PermissionsRequestor(this);
        permissionsRequestor.request(new PermissionsRequestor.ResultListener(){

            @Override
            public void permissionsGranted() {
                loadMapScene();
            }

            @Override
            public void permissionsDenied() {
                Log.e(TAG, "Permissions denied by user.");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsRequestor.onRequestPermissionsResult(requestCode, grantResults);

        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (isGPSEnabled()) {
                    getCurrentLocation();
                } else {
                    turnOnGPS();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                getCurrentLocation();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}