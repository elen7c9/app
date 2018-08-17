package com.test.weather;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.test.weather.adapter.WeatherForecastAdapter;
import com.test.weather.entity.DailyWeather;
import com.test.weather.utils.AppPreference;
import com.test.weather.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.test.weather.utils.Utils.getWeatherForecastUrl;

public class DetailWeatherActivity extends BaseActivity {

    private final String TAG = "DetailWeatherActivity";

    private List<DailyWeather> weatherList;
    private ConnectionDetector connectionDetector;
    private RecyclerView recyclerView;
    private static Handler handler;
    private ProgressDialog getWeatherProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.test.weather.R.layout.activity_weather_forecast);

        connectionDetector = new ConnectionDetector(this);
        weatherList = new ArrayList<>();
        getWeatherProgress = getProgressDialog();

        recyclerView = (RecyclerView) findViewById(com.test.weather.R.id.forecast_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateUI();

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case Constants.TASK_RESULT_ERROR:
                        Toast.makeText(DetailWeatherActivity.this,
                                       com.test.weather.R.string.toast_parse_error,
                                       Toast.LENGTH_SHORT).show();
                        setVisibleUpdating(false);
                        break;
                    case Constants.PARSE_RESULT_ERROR:
                        Toast.makeText(DetailWeatherActivity.this,
                                       com.test.weather.R.string.toast_parse_error,
                                       Toast.LENGTH_SHORT).show();
                        setVisibleUpdating(false);
                        break;
                    case Constants.PARSE_RESULT_SUCCESS:
                        setVisibleUpdating(false);
                        updateUI();
                        if (!weatherList.isEmpty()) {
                            AppPreference.saveWeatherForecast(DetailWeatherActivity.this,
                                                              weatherList);
                        }
                        break;
                }
            }
        };
    }

    private void updateUI() {
        ImageView android = (ImageView) findViewById(com.test.weather.R.id.android);
        if (weatherList.size() < 5) {
            recyclerView.setVisibility(View.INVISIBLE);
            android.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            android.setVisibility(View.GONE);
        }
        WeatherForecastAdapter adapter = new WeatherForecastAdapter(this,
                                                                    weatherList,
                                                                    getSupportFragmentManager());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (weatherList.isEmpty()) {
            weatherList = AppPreference.loadWeatherForecast(this);
        }
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(com.test.weather.R.menu.weather_forecast_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.test.weather.R.id.menu_forecast_refresh:
                if (connectionDetector.isNetworkAvailableAndConnected()) {
                    getWeather();
                    setVisibleUpdating(true);
                } else {
                    Toast.makeText(DetailWeatherActivity.this,
                                   com.test.weather.R.string.connection_not_found,
                                   Toast.LENGTH_SHORT).show();
                }
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setVisibleUpdating(boolean visible) {
        if (visible) {
            getWeatherProgress.show();
        } else {
            getWeatherProgress.cancel();
        }
    }

    private void getWeather() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences pref = getSharedPreferences(Constants.APP_SETTINGS_NAME, 0);
                String latitude = pref.getString(Constants.APP_SETTINGS_LATITUDE, "53.90");
                String longitude = pref.getString(Constants.APP_SETTINGS_LONGITUDE, "27.56");
                String locale = "en";
                String units = AppPreference.getTemperatureUnit(DetailWeatherActivity.this);

                String requestResult = "";
                HttpURLConnection connection = null;
                try {
                    URL url = getWeatherForecastUrl(Constants.WEATHER_FORECAST_ENDPOINT, latitude, longitude, units, locale);
                    connection = (HttpURLConnection) url.openConnection();

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                        InputStream inputStream = connection.getInputStream();

                        int bytesRead;
                        byte[] buffer = new byte[1024];
                        while ((bytesRead = inputStream.read(buffer)) > 0) {
                            byteArray.write(buffer, 0, bytesRead);
                        }
                        byteArray.close();
                        requestResult = byteArray.toString();
                        AppPreference.saveLastUpdateTimeMillis(DetailWeatherActivity.this);
                    }
                } catch (IOException e) {
                    handler.sendEmptyMessage(Constants.TASK_RESULT_ERROR);
                    Log.e(TAG, "IOException: " + requestResult);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                parseWeatherForecast(requestResult);
            }
        });
        t.start();
    }

    private void parseWeatherForecast(String data) {
        try {
            if (!weatherList.isEmpty()) {
                weatherList.clear();
            }

            JSONObject jsonObject = new JSONObject(data);
            JSONArray listArray = jsonObject.getJSONArray("list");

            int listArrayCount = listArray.length();
            for (int i = 0; i < listArrayCount; i++) {
                DailyWeather weatherForecast = new DailyWeather();
                JSONObject resultObject = listArray.getJSONObject(i);
                weatherForecast.setDateTime(resultObject.getLong("dt"));
                weatherForecast.setPressure(resultObject.getString("pressure"));
                weatherForecast.setHumidity(resultObject.getString("humidity"));
                weatherForecast.setWindSpeed(resultObject.getString("speed"));
                weatherForecast.setWindDegree(resultObject.getString("deg"));
                weatherForecast.setCloudiness(resultObject.getString("clouds"));

                JSONObject temperatureObject = resultObject.getJSONObject("temp");
                weatherForecast.setTemperatureMin(
                        Float.parseFloat(temperatureObject.getString("min")));
                weatherForecast.setTemperatureMax(
                        Float.parseFloat(temperatureObject.getString("max")));
                weatherForecast.setTemperatureMorning(
                        Float.parseFloat(temperatureObject.getString("morn")));
                weatherForecast.setTemperatureDay(
                        Float.parseFloat(temperatureObject.getString("day")));
                weatherForecast.setTemperatureEvening(
                        Float.parseFloat(temperatureObject.getString("eve")));
                weatherForecast.setTemperatureNight(
                        Float.parseFloat(temperatureObject.getString("night")));
                JSONArray weatherArray = resultObject.getJSONArray("weather");
                JSONObject weatherObject = weatherArray.getJSONObject(0);
                weatherForecast.setDescription(weatherObject.getString("description"));
                weatherForecast.setIcon(weatherObject.getString("icon"));

                weatherList.add(weatherForecast);
                handler.sendEmptyMessage(Constants.PARSE_RESULT_SUCCESS);
            }
        } catch (JSONException e) {
            handler.sendEmptyMessage(Constants.TASK_RESULT_ERROR);
            e.printStackTrace();
        }
    }
}
