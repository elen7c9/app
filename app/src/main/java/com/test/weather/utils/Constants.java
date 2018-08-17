package com.test.weather.utils;

public class Constants {

    /**
     * SharedPreference names
     */
    public static final String APP_SETTINGS_NAME = "config";
    public static final String PREF_WEATHER_NAME = "weather_pref";
    public static final String PREF_FORECAST_NAME = "weather_forecast";

    /**
     * Preferences constants
     */
    public static final String APP_SETTINGS_LATITUDE = "latitude";
    public static final String APP_SETTINGS_LONGITUDE = "longitude";
    public static final String APP_SETTINGS_CITY = "city";
    public static final String APP_SETTINGS_COUNTRY_CODE = "country_code";
    public static final String APP_SETTINGS_GEO_COUNTRY_NAME = "geo_country_name";
    public static final String APP_SETTINGS_GEO_DISTRICT_OF_CITY = "geo_district_name";
    public static final String APP_SETTINGS_GEO_CITY = "geo_city_name";
    public static final String LAST_UPDATE_TIME_IN_MS = "last_update";
    
    public static final String KEY_PREF_TEMPERATURE = "temperature_pref_key";




    public static final String WEATHER_DATA_TEMPERATURE = "temperature";
    public static final String WEATHER_DATA_DESCRIPTION = "description";
    public static final String WEATHER_DATA_PRESSURE = "pressure";
    public static final String WEATHER_DATA_HUMIDITY = "humidity";
    public static final String WEATHER_DATA_WIND_SPEED = "wind_speed";
    public static final String WEATHER_DATA_ICON = "icon";

    /**
     * Widget action constants
     */
    public static final String ACTION_FORCED_APPWIDGET_UPDATE =
            "com.test.weather.action.FORCED_APPWIDGET_UPDATE";


    /**
     * URIs constants
     */

    public static final String WEATHER_ENDPOINT = "http://api.openweathermap.org/data/2.5/weather";
    public static final String WEATHER_FORECAST_ENDPOINT = "http://api.openweathermap.org/data/2.5/forecast/daily";


    public static final int PARSE_RESULT_SUCCESS = 0;
    public static final int TASK_RESULT_ERROR = -1;
    public static final int PARSE_RESULT_ERROR = -2;
}
