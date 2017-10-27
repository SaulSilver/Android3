package a2dv606.com.dv606hh222ixassignment3.RoadMap4;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.commons.io.FileUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import a2dv606.com.dv606hh222ixassignment3.R;

import static java.net.HttpURLConnection.HTTP_OK;

public class RoadMap extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private Polyline route;
    private ArrayList<Marker> routeMarkers = new ArrayList<>();
    private final String[] routes = {"http://cs.lnu.se/android/VaxjoToStockholm.kml", "http://cs.lnu.se/android/VaxjoToCopenhagen.kml",
            "http://cs.lnu.se/android/VaxjoToOdessa.kml"};
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road_map4);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        file = getExternalFilesDir(null);
        new RouteRetriever().execute(routes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.road_map_4_menu, menu);
        System.out.println("Options menu created");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        File newRoute;
        switch (item.getItemId()) {
            case R.id.clear_road_map:
                if (route != null)
                    route.remove();
                if (!routeMarkers.isEmpty())
                    refreshRouteMarkers();
                break;
            case R.id.odessa_road_map:
                newRoute = new File(file, "VaxjoToOdessa.kml");
                showRoute(newRoute, "Odessa");
                break;
            case R.id.stockholm_road_map:
                newRoute = new File(file, "VaxjoToStockholm.kml");
                showRoute(newRoute, "Stockholm");
                break;
            case R.id.copenhagen_road_map:
                newRoute = new File(file, "VaxjoToCopenhagen.kml");
                showRoute(newRoute, "Copenhagen");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRoute(File routeFile, String routeName) {
        ArrayList<String> coordinates = readCoordinates(routeFile);
        ArrayList<LatLng> latLngCoordinates = getLatLng(coordinates.get(0));
        if (route != null)
            route.remove();
        if (!routeMarkers.isEmpty())
            refreshRouteMarkers();  
        route = map.addPolyline(new PolylineOptions().addAll(latLngCoordinates));
        LatLng start = placeMarker(coordinates.get(1), "Växjö");
        LatLng end = placeMarker(coordinates.get(2), routeName);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(start);
        builder.include(end);

        for (LatLng latLng : route.getPoints())
            builder.include(latLng);

        LatLngBounds bounds = builder.build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 600, 600, 5);
        map.moveCamera(cu);
    }

    private void refreshRouteMarkers() {
        for(Iterator<Marker> iter = routeMarkers.iterator(); iter.hasNext();) {
            Marker m = iter.next();
            m.remove();
            iter.remove();
        }
    }

    private LatLng placeMarker(String s, String routeName) {
        String[] coordinatesArray = s.split(",");
        LatLng coordinates = new LatLng(Double.valueOf(coordinatesArray[1]), Double.valueOf(coordinatesArray[0]));
        routeMarkers.add(map.addMarker(new MarkerOptions().title(routeName).position(coordinates)));
        return coordinates;
    }

    private ArrayList<LatLng> getLatLng(String coordinates) {
        ArrayList<LatLng> latLngCoordinates = new ArrayList<>();

        String[] coordinatesArray = coordinates.split("\\s+");
        for (String coordinate : coordinatesArray) {
            String[] item = coordinate.split(",");

            LatLng latLng = new LatLng(Double.valueOf(item[1]), Double.valueOf(item[0]));
            latLngCoordinates.add(latLng);
        }
        return latLngCoordinates;
    }

    /**
     * Getting all the coordinates tags in the .kml file
     */
    private ArrayList<String> readCoordinates(File routeFile) {
        ArrayList<String> checkpointsList = new ArrayList<>();
        try {
            InputStream inputStream = new FileInputStream(routeFile);

            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(inputStream, null);

            while (xmlPullParser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xmlPullParser.getEventType() == XmlPullParser.START_TAG) {
                    if (xmlPullParser.getName().equals("coordinates"))
                            checkpointsList.add(xmlPullParser.nextText());
                }
                xmlPullParser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return checkpointsList;
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
    }


    //----------------- File Retriever AsyncTask class------------
    private class RouteRetriever extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                getRoutesFiles(strings);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }


    /**
     * Get the files from the urls for the routes and put them into the external file
     */
    private void getRoutesFiles(String[] urls) throws IOException {
        for (String stringUrl : urls) {
            URL url = new URL(stringUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HTTP_OK) {
                String routeFileName = stringUrl.substring(stringUrl.lastIndexOf("/") + 1, stringUrl.length());
                FileUtils.copyURLToFile(url, new File(file, routeFileName));
            }
        }
    }
}
