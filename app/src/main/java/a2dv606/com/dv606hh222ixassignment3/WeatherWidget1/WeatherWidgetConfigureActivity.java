package a2dv606.com.dv606hh222ixassignment3.WeatherWidget1;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import a2dv606.com.dv606hh222ixassignment3.R;

/**
 * To choose which city to show its weather forecast.
 * The configuration screen for the {@link WeatherWidgetProvider WeatherWidgetProvider} AppWidget.
 */
public class WeatherWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "a2dv606.com.dv606hh222ixassignment3.Exercise1.WeatherWidgetProvider";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    RadioGroup cityRadioGroup;
    RadioButton radioCityButton;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.weather_widget_configure);
        cityRadioGroup = (RadioGroup) findViewById(R.id.radio_group_cities);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = WeatherWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            int selectedCityId = cityRadioGroup.getCheckedRadioButtonId();
            radioCityButton = (RadioButton) findViewById(selectedCityId);

            if (radioCityButton != null) {
                saveTitlePref(context, mAppWidgetId, radioCityButton.getText().toString());

                //Setup the widget properties by onUpdate() broadcast
                Intent weatherWidgetIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, context, WeatherWidgetProvider.class);
                weatherWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {mAppWidgetId});
                sendBroadcast(weatherWidgetIntent);
                setResult(RESULT_OK, weatherWidgetIntent);
                finish();
            } else  Snackbar.make(v, "You must choose a city to continue",Snackbar.LENGTH_SHORT).show();
        }
    };

    public WeatherWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

}

