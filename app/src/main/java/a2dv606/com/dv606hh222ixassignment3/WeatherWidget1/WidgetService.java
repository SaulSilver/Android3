package a2dv606.com.dv606hh222ixassignment3.WeatherWidget1;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.RemoteViews;

import java.io.IOException;
import java.net.URL;

import a2dv606.com.dv606hh222ixassignment3.WeatherWidget1.Assignment1Classes.WeatherActivity;
import a2dv606.com.dv606hh222ixassignment3.WeatherWidget1.Assignment1Classes.WeatherForecast;
import a2dv606.com.dv606hh222ixassignment3.WeatherWidget1.Assignment1Classes.WeatherHandler;
import a2dv606.com.dv606hh222ixassignment3.WeatherWidget1.Assignment1Classes.WeatherReport;
import a2dv606.com.dv606hh222ixassignment3.R;

import static a2dv606.com.dv606hh222ixassignment3.WeatherWidget1.Assignment1Classes.WeatherActivity.citiesUrls;

public class WidgetService extends Service {

    private RemoteViews remoteViews;
    private AppWidgetManager appWidgetManager;
    private int appWidgetId;
    private boolean update = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
        int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

            for (int appWidgetId : appWidgetIds) {
                if (intent.getAction() != null && intent.getAction().equals("UPDATE")) {
                    System.out.println("UPDATED");
                    update = true;
                }
                this.appWidgetId = appWidgetId;
                setUpWidget(getApplicationContext(), appWidgetManager, appWidgetIds, appWidgetId);
            }
        stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * A method to update the widget with its onclick listeners
     */
    private void setUpWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, int appWidgetId) {

        CharSequence cityName = WeatherWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        remoteViews.setTextViewText(R.id.city_name_tv_widget, cityName);

        //Setting the weather view for the weather widget
        if (!cityName.equals("EXAMPLE"))
            getWeatherUrl(cityName);

        //Intent for WeatherActivity
        Intent weatherActivityIntent = new Intent(context, WeatherActivity.class);
        weatherActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        weatherActivityIntent.putExtra("city", cityName);
        weatherActivityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        //appwidget id used to distinguish between different widgets and their activities
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, weatherActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.weather_widget_whole, pendingIntent);

        //TODO:Update the widget
        Intent updateIntent = new Intent(context, getClass());
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.putExtra("city", cityName);
        updateIntent.setAction("UPDATE");
        PendingIntent updatePendingIntent = PendingIntent.getService(context, appWidgetId, updateIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.update_btn, updatePendingIntent);
        if (update)
            getWeatherUrl(cityName);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Retrieving the weather url
     */
    private void getWeatherUrl(CharSequence cityNameChar) {
        update = false;
        String cityName = cityNameChar.toString();

        if (isNetworkConnected()) {
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
        } else  remoteViews.setTextViewText(R.id.weather_description_widget, "There is no internet, please try again when connected");
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return ((networkInfo != null) && (networkInfo.isConnectedOrConnecting()));
    }

    //Setting the weather view for the weather widget
    private void showWeather(WeatherReport result) {
        WeatherForecast forecast = result.getFirstForecast();
        int forecastWeatherCode = forecast.getWeatherCode();
        if (forecastWeatherCode == 1) {
            remoteViews.setImageViewResource(R.id.weather_icon_widget, R.drawable.clear);
            remoteViews.setTextViewText(R.id.weather_code_widget, "Clear");
        } else if (forecastWeatherCode == 2 || forecastWeatherCode == 3 || forecastWeatherCode == 4) {
            remoteViews.setImageViewResource(R.id.weather_icon_widget, R.drawable.cloudy);
            remoteViews.setTextViewText(R.id.weather_code_widget, "Cloudy");
        } else if (forecastWeatherCode == 40 || forecastWeatherCode == 5 || forecastWeatherCode == 41 || forecastWeatherCode == 24
                || forecastWeatherCode == 9) {
            remoteViews.setImageViewResource(R.id.weather_icon_widget, R.drawable.rain);
            remoteViews.setTextViewText(R.id.weather_code_widget, "Rain");
        } else {
            remoteViews.setImageViewResource(R.id.weather_icon_widget, R.drawable.snow);
            remoteViews.setTextViewText(R.id.weather_code_widget, "Snow");
        }

        remoteViews.setTextViewText(R.id.weather_description_widget, ("Date: " + forecast.getStartYYMMDD() + "\nFrom:" + forecast.getStartHHMM()
                + ", To: " + forecast.getEndHHMM() + "\nWind: " + forecast.getWindDirection() + ", Speed: "
                + forecast.getWindSpeed() + "m/s" + "\nTemperature: " + forecast.getTemperature() + ", Rain: " + forecast.getRain() + "mm/h"));

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }


    /**
     * Retrieve the weather
     */
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
            System.out.println("in onPostExecute");
            //Setting the weather view for the weather widget
            showWeather(result);
        }
    }
}

