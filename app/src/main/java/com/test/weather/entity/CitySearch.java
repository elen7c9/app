package com.test.weather.entity;

public class CitySearch
{
    private String cityName;
    private String latitude;
    private String longtude;
    private String countryCode;

    public CitySearch(){}
    public CitySearch(String cityName, String countryCode, String latitude, String longitude)
    {
        this.cityName = cityName;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longtude = longitude;
    }

    public String getCityName()
    {
        return cityName;
    }

    public void setCityName(String cityName)
    {
        this.cityName = cityName;
    }

    public String getLatitude()
    {
        return latitude;
    }

    public void setLatitude(String latitude)
    {
        this.latitude = latitude;
    }

    public String getLongitude()
    {
        return longtude;
    }

    public void setLongitude(String longitude)
    {
        this.longtude = longitude;
    }

    public String getCountryCode()
    {
        return countryCode;
    }

    public void setCountryCode(String countryCode)
    {
        this.countryCode = countryCode;
    }

    @Override
    public String toString()
    {
        return cityName + ", " + countryCode;
    }
}
