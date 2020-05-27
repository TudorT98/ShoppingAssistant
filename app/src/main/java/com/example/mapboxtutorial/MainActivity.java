package com.example.mapboxtutorial;

import android.content.Context;
import android.location.Location;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener, MapboxMap.OnMapClickListener {

    private MapView mapView;
    private MapboxMap map;
    private Button startButton;
    private Button currentLocation;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation ;
    private Point originPosition;
    private Point destinationPosition;
    private Marker destinationMarker;
    private NavigationMapRoute navigationMapRoute;
    private ArrayList<Locations> definedLocations = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this,getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        startButton = findViewById(R.id.startButton);
        currentLocation = findViewById(R.id.LocationButton);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .origin(originPosition)
                        .destination(destinationPosition)
                        .shouldSimulateRoute(true)
                        .build();
                NavigationLauncher.startNavigation(MainActivity.this,options);
            }
        });
       currentLocation.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               enableLocation();
           }
       });
    }


    public void loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("locations.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(json);

            for(int i = 0 ; i<jsonArray.length(); i++)
            {
                JSONObject object = jsonArray.getJSONObject(i);
                Locations location = new Locations(object.getString("Name"),object.getString("Type"),object.getDouble("Latitude"),object.getDouble("Longitude"),object.getString("WebSite"));
                definedLocations.add(location);
            }


        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
     @Override
    public void onMapReady(MapboxMap mapboxMap)
    {
      map = mapboxMap;
      map.addOnMapClickListener(this);
      enableLocation();
    }

    private void enableLocation()
    {
        if (PermissionsManager.areLocationPermissionsGranted(this))
        {
            Log.d("Permisions","LocationPermisionsGranted");
            initializeLocationEngine();
            initializeLocationLayer();
        }
        else
        {
            //request permision
            Log.d("Permisions","Request permisions");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }
    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine()
    {
      locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
      locationEngine.setPriority(LocationEnginePriority.BALANCED_POWER_ACCURACY);
      locationEngine.activate();
      Location lastLocation = locationEngine.getLastLocation();
      if (lastLocation !=null)
      {
          Log.d("LocationEngine","lastLocation not null");
          originLocation =lastLocation;
          setCameraPosition(lastLocation);
      }
      else
      {
          Log.d("LocationEngine","lastLocation null");
          locationEngine.addLocationEngineListener(this);

      }
    }
    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer()
    {
        Log.d("LocationLayer","-");
        locationLayerPlugin = new LocationLayerPlugin(mapView,map,locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
    }
    private void setCameraPosition(Location location)
    {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),12.0));
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {

        if(destinationMarker != null)
        {
            map.removeMarker(destinationMarker);
        }

        //Icon MarketIcon = findViewById(R.drawable.ic_Market);

        loadJSONFromAsset(this);
        for(int i = 0 ; i < definedLocations.size(); i++) {

                map.addMarker(new MarkerOptions()
                        .position(new LatLng(definedLocations.get(i).getLatitude(), definedLocations.get(i).getLongitude()))
                        .title(definedLocations.get(i).getName()));
                        //.setIcon(MarketIcon);
        }




        destinationMarker = map.addMarker(new MarkerOptions().position(point));
        destinationPosition = Point.fromLngLat(point.getLongitude(),point.getLatitude());
        originPosition = Point.fromLngLat(originLocation.getLongitude(),originLocation.getLatitude());

        getRoute(originPosition,destinationPosition);

        startButton.setEnabled(true);
        startButton.setBackgroundResource(R.color.mapBoxGreen);
    }
    private void getRoute(Point origin,Point destination){
        NavigationRoute.builder()
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build().getRoute(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if(response.body() == null)
                {
                    Log.d("Navigation","No routes");
                    return;
                }
                else if (response.body().routes().size() == 0)
                {
                    Log.d("Navigation","No routes found");
                }
                DirectionsRoute currentRoute = response.body().routes().get(0);

                if(navigationMapRoute != null){
                    navigationMapRoute.removeRoute();
                }
                else{
                    navigationMapRoute = new NavigationMapRoute(null,mapView,map);
                }
                navigationMapRoute.addRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                    Log.d("Navigation failure", t.getMessage());
            }
        });

    }
    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location !=null)
        {
            originLocation = location;
            setCameraPosition(location);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //present toast or dialog
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted)
        {
            enableLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();
        if(locationEngine != null)
        {
            locationEngine.requestLocationUpdates();

        }
        if(locationLayerPlugin != null)
        {
            locationLayerPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(locationEngine != null){
            locationEngine.removeLocationUpdates();
        }
        if(locationLayerPlugin != null)
        {
            locationLayerPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationEngine != null)
        {
            locationEngine.deactivate();
        }
        mapView.onDestroy();
    }



}
