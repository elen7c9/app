package com.test.weather;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.test.weather.entity.CitySearch;
import com.test.weather.entity.Weather;
import com.test.weather.service.CurrentWeatherService;
import com.test.weather.utils.AppPreference;
import com.test.weather.utils.Constants;
import com.test.weather.utils.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.test.weather.utils.AppPreference.saveLastUpdateTimeMillis;

public class MainActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "MainActivity";

    private static final long LOCATION_TIMEOUT_IN_MS = 30000L;

    private TextView iconWeather;
    private TextView tempView;
    private TextView deskView;
    private TextView humidityView;
    private TextView windSpeedView;
    private TextView pressureView;
    private TextView lastUpdateView;

    private AppBarLayout appBarLayout;
    private TextView iconWindView;
    private TextView iconHumidityView;
    private TextView iconPressureView;


    private ConnectionDetector connectionDetector;
    private Boolean isNetworkAvailable;
    private ProgressDialog mProgressDialog;
    private LocationManager locationManager;
    private SwipeRefreshLayout mSwipeRefresh;
    private Menu mToolbarMenu;
    private BroadcastReceiver mWeatherUpdateReceiver;

    private String mSpeedScale;
    private String mIconWind;
    private String mIconHumidity;
    private String mIconPressure;

    private String mPercentSign;
    private String mPressureMeasurement;

    private SharedPreferences mPrefWeather;
    private SharedPreferences mSharedPreferences;

    public static Weather mWeather;
    public static CitySearch mCitySearch;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.test.weather.R.layout.activity_main);

        mWeather = new Weather();
        mCitySearch = new CitySearch();

        weatherConditionsIcons();
        initializeTextView();
        initializeWeatherReceiver();

        connectionDetector = new ConnectionDetector(MainActivity.this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mPrefWeather = getSharedPreferences(Constants.PREF_WEATHER_NAME, Context.MODE_PRIVATE);
        mSharedPreferences = getSharedPreferences(Constants.APP_SETTINGS_NAME,
                Context.MODE_PRIVATE);
        setTitle(Utils.getCityAndCountry(this));

        /**
         * Configure SwipeRefreshLayout
         */
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(com.test.weather.R.id.main_swipe_refresh);
        int top_to_padding = 150;
        mSwipeRefresh.setProgressViewOffset(false, 0, top_to_padding);
        mSwipeRefresh.setColorSchemeResources(com.test.weather.R.color.swipe_red, com.test.weather.R.color.swipe_green,
                com.test.weather.R.color.swipe_blue);
        mSwipeRefresh.setOnRefreshListener(swipeRefreshListener);


    }

    private void updateCurrentWeather() {
        AppPreference.saveWeather(MainActivity.this, mWeather);
        mSharedPreferences = getSharedPreferences(Constants.APP_SETTINGS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor configEditor = mSharedPreferences.edit();

        mSpeedScale = Utils.getSpeedScale(MainActivity.this);
        String temperature = String.format(Locale.getDefault(), "%.0f",
                mWeather.temperature.getTemp());
        String pressure = String.format(Locale.getDefault(), "%.1f",
                mWeather.currentCondition.getPressure());
        String wind = String.format(Locale.getDefault(), "%.1f", mWeather.wind.getSpeed());

        String lastUpdate = Utils.setLastUpdateTime(MainActivity.this,
                saveLastUpdateTimeMillis(MainActivity.this));


        iconWeather.setText(
                Utils.getStrIcon(MainActivity.this, mWeather.currentWeather.getIdIcon()));
        tempView.setText(getString(com.test.weather.R.string.temperature_with_degree, temperature));

            deskView.setText(" ");
        humidityView.setText(getString(com.test.weather.R.string.humidity_label,
                String.valueOf(mWeather.currentCondition.getHumidity()),
                mPercentSign));
        pressureView.setText(getString(com.test.weather.R.string.pressure_label, pressure,
                mPressureMeasurement));
        windSpeedView.setText(getString(com.test.weather.R.string.wind_label, wind, mSpeedScale));

        lastUpdateView.setText(getString(com.test.weather.R.string.last_update_label, lastUpdate));


        configEditor.putString(Constants.APP_SETTINGS_CITY, mWeather.location.getCityName());
        configEditor.putString(Constants.APP_SETTINGS_COUNTRY_CODE,
                mWeather.location.getCountryCode());
        configEditor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        preLoadWeather();
        appBarLayout.addOnOffsetChangedListener(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mWeatherUpdateReceiver,
                new IntentFilter(
                        CurrentWeatherService.ACTION_WEATHER_UPDATE_RESULT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        appBarLayout.removeOnOffsetChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mWeatherUpdateReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mToolbarMenu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(com.test.weather.R.menu.activity_main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.test.weather.R.id.main_menu_refresh:
                startService(new Intent(this, CurrentWeatherService.class));
                break;
            case com.test.weather.R.id.main_menu_detect_location:
                requestLocation();
                break;
            case com.test.weather.R.id.main_menu_search_city:
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivityForResult(intent, PICK_CITY);
                return true;

            case com.test.weather.R.id.nav_menu_weather_forecast:
                Intent intent1Daily = new Intent(MainActivity.this, DetailWeatherActivity.class);
                startActivity(intent1Daily);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mProgressDialog.cancel();
            String latitude = String.format("%1$.2f", location.getLatitude());
            String longitude = String.format("%1$.2f", location.getLongitude());

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(mLocationListener);
            }

            connectionDetector = new ConnectionDetector(MainActivity.this);
            isNetworkAvailable = connectionDetector.isNetworkAvailableAndConnected();

            mSharedPreferences = getSharedPreferences(Constants.APP_SETTINGS_NAME,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Constants.APP_SETTINGS_LATITUDE, latitude);
            editor.putString(Constants.APP_SETTINGS_LONGITUDE, longitude);
            getAndWriteAddressFromGeocoder(latitude, longitude, editor);
            editor.apply();

            if (isNetworkAvailable) {
                startService(new Intent(MainActivity.this, CurrentWeatherService.class));
                sendBroadcast(new Intent(Constants.ACTION_FORCED_APPWIDGET_UPDATE));
            } else {
                Toast.makeText(MainActivity.this, com.test.weather.R.string.connection_not_found, Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void getAndWriteAddressFromGeocoder(String latitude, String longitude, SharedPreferences.Editor editor) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            String latitudeEn = latitude.replace(",", ".");
            String longitudeEn = longitude.replace(",", ".");
            List<Address> addresses = geocoder.getFromLocation(Double.parseDouble(latitudeEn), Double.parseDouble(longitudeEn), 1);
            if ((addresses != null) && (addresses.size() > 0)) {
                editor.putString(Constants.APP_SETTINGS_GEO_CITY, addresses.get(0).getLocality());
                editor.putString(Constants.APP_SETTINGS_GEO_COUNTRY_NAME, addresses.get(0).getCountryName());
            }
        } catch (IOException | NumberFormatException ex) {
            Log.e(TAG, "Unable to get address from latitude and longitude", ex);
        }
    }

    private SwipeRefreshLayout.OnRefreshListener swipeRefreshListener =
            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    isNetworkAvailable = connectionDetector.isNetworkAvailableAndConnected();
                    if (isNetworkAvailable) {
                        startService(new Intent(MainActivity.this, CurrentWeatherService.class));
                    } else {
                        Toast.makeText(MainActivity.this,
                                com.test.weather.R.string.connection_not_found,
                                Toast.LENGTH_SHORT).show();
                        mSwipeRefresh.setRefreshing(false);
                    }
                }
            };

    private void preLoadWeather() {
        mSpeedScale = Utils.getSpeedScale(this);
        String lastUpdate = Utils.setLastUpdateTime(this,
                AppPreference.getLastUpdateTimeMillis(this));

        String iconId = mPrefWeather.getString(Constants.WEATHER_DATA_ICON, "01d");
        float temperaturePref = mPrefWeather.getFloat(Constants.WEATHER_DATA_TEMPERATURE, 0);
        String description = mPrefWeather.getString(Constants.WEATHER_DATA_DESCRIPTION,
                "clear sky");
        int humidity = mPrefWeather.getInt(Constants.WEATHER_DATA_HUMIDITY, 0);
        float pressurePref = mPrefWeather.getFloat(Constants.WEATHER_DATA_PRESSURE, 0);
        float windPref = mPrefWeather.getFloat(Constants.WEATHER_DATA_WIND_SPEED, 0);

        String temperature = String.format(Locale.getDefault(), "%.0f", temperaturePref);
        String pressure = String.format(Locale.getDefault(), "%.1f", pressurePref);
        String wind = String.format(Locale.getDefault(), "%.1f", windPref);

        iconWeather.setText(Utils.getStrIcon(this, iconId));
        tempView.setText(getString(com.test.weather.R.string.temperature_with_degree, temperature));
        deskView.setText(description);
        lastUpdateView.setText(getString(com.test.weather.R.string.last_update_label, lastUpdate));
        humidityView.setText(getString(com.test.weather.R.string.humidity_label,
                String.valueOf(humidity),
                mPercentSign));
        pressureView.setText(getString(com.test.weather.R.string.pressure_label,
                pressure,
                mPressureMeasurement));
        windSpeedView.setText(getString(com.test.weather.R.string.wind_label, wind, mSpeedScale));


        setTitle(Utils.getCityAndCountry(this));
    }

    private void initializeTextView() {
        /**
         * Create typefaces from Asset
         */
        Typeface weatherFontIcon = Typeface.createFromAsset(this.getAssets(),
                "fonts/weathericons-regular-webfont.ttf");
        Typeface robotoThin = Typeface.createFromAsset(this.getAssets(),
                "fonts/Roboto-Thin.ttf");
        Typeface robotoLight = Typeface.createFromAsset(this.getAssets(),
                "fonts/Roboto-Light.ttf");

        iconWeather = (TextView) findViewById(com.test.weather.R.id.main_weather_icon);
        tempView = (TextView) findViewById(com.test.weather.R.id.main_temperature);
        deskView = (TextView) findViewById(com.test.weather.R.id.main_description);
        pressureView = (TextView) findViewById(com.test.weather.R.id.main_pressure);
        humidityView = (TextView) findViewById(com.test.weather.R.id.main_humidity);
        windSpeedView = (TextView) findViewById(com.test.weather.R.id.main_wind_speed);
        lastUpdateView = (TextView) findViewById(com.test.weather.R.id.main_last_update);

        appBarLayout = (AppBarLayout) findViewById(com.test.weather.R.id.main_app_bar);

        iconWeather.setTypeface(weatherFontIcon);
        tempView.setTypeface(robotoThin);
        windSpeedView.setTypeface(robotoLight);
        humidityView.setTypeface(robotoLight);
        pressureView.setTypeface(robotoLight);


        /**
         * Initialize and configure weather icons
         */
        iconWindView = (TextView) findViewById(com.test.weather.R.id.main_wind_icon);
        iconWindView.setTypeface(weatherFontIcon);
        iconWindView.setText(mIconWind);
        iconHumidityView = (TextView) findViewById(com.test.weather.R.id.main_humidity_icon);
        iconHumidityView.setTypeface(weatherFontIcon);
        iconHumidityView.setText(mIconHumidity);
        iconPressureView = (TextView) findViewById(com.test.weather.R.id.main_pressure_icon);
        iconPressureView.setTypeface(weatherFontIcon);
        iconPressureView.setText(mIconPressure);
    }

    private void weatherConditionsIcons() {
        mIconWind = getString(com.test.weather.R.string.icon_wind);
        mIconHumidity = getString(com.test.weather.R.string.icon_humidity);
        mIconPressure = getString(com.test.weather.R.string.icon_barometer);
        mPercentSign = getString(com.test.weather.R.string.percent_sign);
        mPressureMeasurement = getString(com.test.weather.R.string.pressure_measurement);

    }

    private void setUpdateButtonState(boolean isUpdate) {
        if (mToolbarMenu != null) {
            MenuItem updateItem = mToolbarMenu.findItem(com.test.weather.R.id.main_menu_refresh);
            ProgressBar progressUpdate = (ProgressBar) findViewById(com.test.weather.R.id.toolbar_progress_bar);
            if (isUpdate) {
                updateItem.setVisible(false);
                progressUpdate.setVisibility(View.VISIBLE);
            } else {
                progressUpdate.setVisibility(View.GONE);
                updateItem.setVisible(true);
            }
        }
    }

    private void initializeWeatherReceiver() {
        mWeatherUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getStringExtra(CurrentWeatherService.ACTION_WEATHER_UPDATE_RESULT)) {
                    case CurrentWeatherService.ACTION_WEATHER_UPDATE_OK:
                        mSwipeRefresh.setRefreshing(false);
                        setUpdateButtonState(false);
                        updateCurrentWeather();
                        break;
                    case CurrentWeatherService.ACTION_WEATHER_UPDATE_FAIL:
                        mSwipeRefresh.setRefreshing(false);
                        setUpdateButtonState(false);
                        Toast.makeText(MainActivity.this,
                                getString(com.test.weather.R.string.toast_parse_error),
                                Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        mSwipeRefresh.setEnabled(verticalOffset == 0);
    }




    private void detectLocation() {

        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage(getString(com.test.weather.R.string.progressDialog_gps_locate));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    locationManager.removeUpdates(mLocationListener);
                } catch (SecurityException e) {
                    Log.e(TAG, "Cancellation error", e);
                }
            }
        });


            networkRequestLocation();
            gpsRequestLocation();
            mProgressDialog.show();


    }


    public void gpsRequestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Looper locationLooper = Looper.myLooper();
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListener, locationLooper);
            final Handler locationHandler = new Handler(locationLooper);
            locationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    locationManager.removeUpdates(mLocationListener);
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastLocation != null) {
                            mLocationListener.onLocationChanged(lastLocation);
                        } else {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                        }
                    }
                }
            }, LOCATION_TIMEOUT_IN_MS);
        }
    }

    public void networkRequestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Looper locationLooper = Looper.myLooper();
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, locationLooper);
            final Handler locationHandler = new Handler(locationLooper);
            locationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    locationManager.removeUpdates(mLocationListener);
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Location lastNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        Location lastGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if ((lastGpsLocation == null) && (lastNetworkLocation != null)) {
                            mLocationListener.onLocationChanged(lastNetworkLocation);
                        } else if ((lastGpsLocation != null) && (lastNetworkLocation == null)) {
                            mLocationListener.onLocationChanged(lastGpsLocation);
                        } else {
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                        }
                    }
                }
            }, LOCATION_TIMEOUT_IN_MS);
        }
    }

    private void requestLocation() {
            detectLocation();
    }

}
