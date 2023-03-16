package com.example.petwalker;

import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

public class Map extends AppCompatActivity implements LocationListener {

    private MapView map;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Inside onCreate or onCreateView method
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        // get a reference to the MapView
        map = findViewById(R.id.map);

        // set the tile source
        map.setTileSource(TileSourceFactory.MAPNIK);

        // set the initial map view
        map.getController().setZoom(15.0);

        // get a reference to the LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // request location updates if permission is granted
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            // request permission from the user
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // update the map view to the user's location
        GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        map.getController().setCenter(userLocation);
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                }
            } else {
                // permission denied
            }
        }
    }
}
