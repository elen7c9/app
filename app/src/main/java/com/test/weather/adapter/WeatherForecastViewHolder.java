package com.test.weather.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.test.weather.fragment.DetailDailyBottomSheet;
import com.test.weather.entity.DailyWeather;
import com.test.weather.utils.Utils;

import com.test.weather.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherForecastViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener {


    private DailyWeather weatherForecast;
    private Context context;
    private FragmentManager fragmentManager;

    private TextView dateTime;
    private TextView icon;
    private TextView tempMin;
    private TextView tempMax;

    public WeatherForecastViewHolder(View itemView, Context context,
                                     FragmentManager fragmentManager) {
        super(itemView);
        this.context = context;
        this.fragmentManager = fragmentManager;
        itemView.setOnClickListener(this);

        dateTime = (TextView) itemView.findViewById(R.id.forecast_date_time);
        icon = (TextView) itemView.findViewById(R.id.forecast_icon);
        tempMin = (TextView) itemView.findViewById(R.id.forecast_temperature_min);
        tempMax = (TextView) itemView.findViewById(R.id.forecast_temperature_max);
    }

    void bindWeather(DailyWeather weather) {
        weatherForecast = weather;

        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/weathericons-regular-webfont.ttf");
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMMM", Locale.getDefault());
        Date date = new Date(weather.getDateTime() * 1000);
        String temperatureMin = context.getString(R.string.temperature_with_degree,
                                                   String.format(Locale.getDefault(), "%.0f",
                                                                 weather.getTemperatureMin()));
        String temperatureMax = context.getString(R.string.temperature_with_degree,
                                                   String.format(Locale.getDefault(), "%.0f",
                                                                 weather.getTemperatureMax()));

        dateTime.setText(format.format(date));
        icon.setTypeface(typeface);
        icon.setText(Utils.getStrIcon(context, weather.getIcon()));
        if (weather.getTemperatureMin() > 0) {
            temperatureMin = "+" + temperatureMin;
        }
        tempMin.setText(temperatureMin);
        if (weather.getTemperatureMax() > 0) {
            temperatureMax = "+" + temperatureMax;
        }
        tempMax.setText(temperatureMax);
    }

    @Override
    public void onClick(View view) {
        new DetailDailyBottomSheet()
                .newInstance(weatherForecast)
                .show(fragmentManager, "forecastBottomSheet");
    }
}