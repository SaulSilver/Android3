package a2dv606.com.dv606hh222ixassignment3.WeatherWidget1.Assignment1Classes;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import a2dv606.com.dv606hh222ixassignment3.R;

/**
 * This activity downloads weather data and constructs a WeatherReport,
 * a data structure containing weather data for a number of periods ahead.
 */

public class WeatherActivity extends AppCompatActivity {

    public static String TAG = "dv606.weather";
    private ArrayList<WeatherForecast> forecasts = new ArrayList<>();
    private WeatherReport report = null;
    public static String[] citiesUrls = {"http://www.yr.no/sted/Sverige/Kronoberg/V%E4xj%F6/forecast.xml", "http://www.yr.no/place/Japan/Gifu/Osaka/forecast.xml"
            , "http://www.yr.no/place/Egypt/Cairo/Cairo/forecast.xml"};     //Cities weather forecast urls in XML format

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the layout
        setContentView(R.layout.weather_fullscreen);

        Intent intent = getIntent();
        String cityName = intent.getStringExtra("city");

        TextView cityNameTv = (TextView) findViewById(R.id.city_name_tv_fullscreen);
        cityNameTv.setText(cityName);

        boolean isNetworkAvailable = isNetworkConnected();
        if(isNetworkAvailable) {
            try {
                //check for different cities
                URL cityURL = null;
                switch (cityName) {
                    case "Växjö, Sweden":
                        cityURL = new URL(citiesUrls[0]);
                        break;
                    case "Osaka, Japan":
                        cityURL = new URL(citiesUrls[1]);
                        break;
                    case "Cairo, Egypt":
                        cityURL = new URL(citiesUrls[2]);
                        break;
                }
                new WeatherRetriever().execute(cityURL);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else Toast.makeText(getApplicationContext(), "There is no internet connection", Toast.LENGTH_LONG).show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return ((networkInfo != null) && (networkInfo.isConnectedOrConnecting()));
    }

    private class WeatherRetriever extends AsyncTask<URL, Void, WeatherReport> {
        protected WeatherReport doInBackground(URL... urls) {
            try {
                return WeatherHandler.getWeatherReport(urls[0]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected void onProgressUpdate(Void... progress) {}

        protected void onPostExecute(WeatherReport result) {
            report = result;

            for (WeatherForecast forecast : report)
                forecasts.add(forecast);

            ListAdapter adapter = new WeatherForecastAdapter(getBaseContext());

            ListView lv = (ListView) findViewById(R.id.list_view);
            lv.setAdapter(adapter);
        }
    }


    private class WeatherForecastAdapter extends ArrayAdapter<WeatherForecast> {

        public WeatherForecastAdapter(Context context) {
            super(context, R.layout.weather_row, forecasts);
        }

        //Called when updating the ListView
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row;
            //Create new row view Object
            if(convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.weather_row, parent, false);
            } else row = convertView; 	//reuse old row view to save time

            //Add new data to row object
            WeatherForecast forecast = forecasts.get(position);

            ImageView weatherIcon = (ImageView) row.findViewById(R.id.weather_icon);
            TextView weatherCode = (TextView) row.findViewById(R.id.weather_code);

            int forecastWeatherCode = forecast.getWeatherCode();
            if(forecastWeatherCode == 1) {
                weatherIcon.setImageResource(R.drawable.clear);
                weatherCode.setText("Clear");
            } else if(forecastWeatherCode == 2 || forecastWeatherCode == 3 || forecastWeatherCode == 4) {
                weatherIcon.setImageResource(R.drawable.cloudy);
                weatherCode.setText("Cloudy");
            } else if (forecastWeatherCode == 40 || forecastWeatherCode == 5 || forecastWeatherCode == 41 || forecastWeatherCode == 24
                    || forecastWeatherCode == 9) {
                weatherIcon.setImageResource(R.drawable.rain);
                weatherCode.setText("Rain");
            } else {
                weatherIcon.setImageResource(R.drawable.snow);
                weatherCode.setText("Snow");
            }

            TextView weatherDescription = (TextView) row.findViewById(R.id.weather_description);
            weatherDescription.setText("Date: "+ forecast.getStartYYMMDD() + "\nFrom:" + forecast.getStartHHMM() + ", To: "+forecast.getEndHHMM()
                    +", Period: "+ forecast.getPeriodCode() +
                    "\nWind: "+ forecast.getWindDirection() + ", Speed: " + forecast.getWindSpeed() + "m/s"+
                    "\nTemperature: " + forecast.getTemperature() + ", Rain: " + forecast.getRain() + "mm/h");
            return row;
        }
    }
}