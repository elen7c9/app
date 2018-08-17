package com.test.weather.adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.test.weather.entity.DailyWeather;

import com.test.weather.R;

import java.util.List;

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastViewHolder> {

    private Context context;
    private List<DailyWeather> weatherForecasts;
    private FragmentManager fragmentManager;



    public WeatherForecastAdapter(Context context, List<DailyWeather> weather, FragmentManager fragmentManager) {
        this.context = context;
        this.weatherForecasts = weather;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public WeatherForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.forecast_item, parent, false);
        return new WeatherForecastViewHolder(v, context, fragmentManager);
    }

    @Override
    public void onBindViewHolder(WeatherForecastViewHolder holder, int position) {
        DailyWeather weather = weatherForecasts.get(position);
        holder.bindWeather(weather);
    }

    @Override
    public int getItemCount() {
        return (weatherForecasts != null ? weatherForecasts.size() : 0);
    }
}

