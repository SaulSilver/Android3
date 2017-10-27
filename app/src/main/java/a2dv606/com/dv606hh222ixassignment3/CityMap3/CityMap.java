package a2dv606.com.dv606hh222ixassignment3.CityMap3;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import a2dv606.com.dv606hh222ixassignment3.R;

public class CityMap extends FragmentActivity implements OnMapReadyCallback {

    private final String[] southernCities = {"Växjö", "Malmö", "Kalmar", "Helsinborg", "Trelleborg"};
    private final String CITY_FILE_NAME = "cityMap.txt";

    private GoogleMap map;
    private List<MarkerOptions> markerOptionsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.city_map);
        mapFragment.getMapAsync(this);

        saveCitiesToFile();             //save the cities to an internal storage file
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void saveCitiesToFile() {
        try {
            FileOutputStream fos = openFileOutput(CITY_FILE_NAME, Context.MODE_PRIVATE);
            for (String southernCity : southernCities)
                fos.write((southernCity + "\n").getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) { e.printStackTrace(); }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        //Showing a dynamic marker (crosshair) in the middle of the screen
        LatLng NEWARK = new LatLng(40.714086, -74.228697);

        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.newark_nj_1922))
                .position(NEWARK, 8600f, 6500f);

        // Add an overlay to the map, retaining a handle to the GroundOverlay object.
        GroundOverlay imageOverlay = map.addGroundOverlay(newarkMap);

        //Calculate the distance to the closest city of the 5 whenever the camera moves
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                CameraPosition currPos = map.getCameraPosition();
                MarkerOptions closestCity = getClosestToCenter(markerOptionsList, currPos.target);
                float distance = distanceTo(currPos.target, closestCity.getPosition()) / 1000;

                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                DecimalFormat decimalFormat = new DecimalFormat("#.#");
                showToast(closestCity.getTitle() + ": " + decimalFormat.format(distance) + "km");
            }
        });

        //When a city of the 5 is clicked
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showToast(marker.getTitle());
                return false;
            }
        });

        String[] cities =  readCitiesFromFile();           //get the cities from the storage internal file
        //Set the locations of the 5 cities
        for (String city : cities) {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocationName(city, 1);
                for (Address address : addresses) {
                    if (address.hasLatitude() && address.hasLongitude()) {
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(new LatLng(address.getLatitude(), address.getLongitude()))
                                .title(city);
                        map.addMarker(markerOptions);
                        markerOptionsList.add(markerOptions);
                        System.out.println(markerOptions);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MarkerOptions markerOptions : markerOptionsList) {
            builder.include(markerOptions.getPosition());
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 500, 500, 5);
        map.moveCamera(cameraUpdate);
    }


    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Calculate the distance from the current location to the closest city
     */
    private MarkerOptions getClosestToCenter(List<MarkerOptions> list, LatLng center) {
        MarkerOptions shortestOption = new MarkerOptions();
        float min = Float.MAX_VALUE;
        for (MarkerOptions markerOptions : list) {
            float distance = distanceTo(markerOptions.getPosition(), center);
            if (distance < min) {
                shortestOption = markerOptions;
                min = distance;
            }
        }
        return shortestOption;
    }

    private float distanceTo(LatLng location1, LatLng location2) {
        float[] res = new float[1];
        Location.distanceBetween(location1.latitude, location1.longitude, location2.latitude, location2.longitude, res);
        return res[0];
    }

    private String[] readCitiesFromFile() {
        String[] cities = new String[5];
        int counter = -1;
        try {
            File file = new File(getFilesDir(), CITY_FILE_NAME);
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                cities[++counter] = line;
            }
            reader.close();
            inputStream.close();
        } catch (Exception e) {e.printStackTrace();}
        return cities;
    }
}
