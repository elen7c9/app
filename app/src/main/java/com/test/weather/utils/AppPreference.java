package com.test.weather.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.test.weather.entity.Weather;
import com.test.weather.entity.DailyWeather;

import com.test.weather.R;

import java.util.List;

public class AppPreference {

    public static String getTemperatureUnit(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_TEMPERATURE, "metric");
    }

    public static void saveWeather(Context context, Weather weather) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_WEATHER_NAME,
                                                                     Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(Constants.WEATHER_DATA_TEMPERATURE, weather.temperature.getTemp());
        editor.putString(Constants.WEATHER_DATA_DESCRIPTION, " ");

        editor.putFloat(Constants.WEATHER_DATA_PRESSURE, weather.currentCondition.getPressure());
        editor.putInt(Constants.WEATHER_DATA_HUMIDITY, weather.currentCondition.getHumidity());
        editor.putFloat(Constants.WEATHER_DATA_WIND_SPEED, weather.wind.getSpeed());
        editor.putString(Constants.WEATHER_DATA_ICON, weather.currentWeather.getIdIcon());

        editor.apply();
    }

    public static String[] getCityAndCode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.APP_SETTINGS_NAME,
                                                                     Context.MODE_PRIVATE);
        String[] result = new String[2];
        result[0] = preferences.getString(Constants.APP_SETTINGS_CITY, "Minsk");
        result[1] = preferences.getString(Constants.APP_SETTINGS_COUNTRY_CODE, "BY");
        return result;
    }

    public static long saveLastUpdateTimeMillis(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.APP_SETTINGS_NAME,
                                                            Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        sp.edit().putLong(Constants.LAST_UPDATE_TIME_IN_MS, now).apply();
        return now;
    }

    public static long getLastUpdateTimeMillis(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.APP_SETTINGS_NAME,
                                                            Context.MODE_PRIVATE);
        return sp.getLong(Constants.LAST_UPDATE_TIME_IN_MS, 0);
    }



    public static void saveWeatherForecast(Context context, List<DailyWeather> forecastList) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_FORECAST_NAME,
                                                                     Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String weatherJson = new Gson().toJson(forecastList);
        editor.putString("daily_forecast", weatherJson);
        editor.apply();
    }

    public static List<DailyWeather> loadWeatherForecast(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_FORECAST_NAME,
                                                                     Context.MODE_PRIVATE);
        String weather = preferences.getString("daily_forecast",
                                               context.getString(R.string.default_daily_forecast));
        return new Gson().fromJson(weather,
                                   new TypeToken<List<DailyWeather>>() {
                                   }.getType());
    }
}
