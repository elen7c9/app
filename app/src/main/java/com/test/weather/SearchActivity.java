package com.test.weather;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.test.weather.entity.CitySearch;
import com.test.weather.utils.CityParser;
import com.test.weather.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    public static final String TAG = "SearchActivity";

    private final String APP_SETTINGS_CITY = "city";
    private final String APP_SETTINGS_COUNTRY_CODE = "country_code";
    private final String APP_SETTINGS_LATITUDE = "latitude";
    private final String APP_SETTINGS_LONGITUDE = "longitude";

    private List<CitySearch> cites;
    private SearchCityAdapter searchCityAdapter;
    private SharedPreferences cityPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, com.test.weather.R.color.colorPrimaryDark));
        }
        setContentView(com.test.weather.R.layout.activity_search);

        setupActionBar();
        setupSearchView();

        String APP_SETTINGS_NAME = "config";
        cityPref = getSharedPreferences(APP_SETTINGS_NAME, 0);

        RecyclerView recyclerView = (RecyclerView) findViewById(com.test.weather.R.id.search_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

        cites = new ArrayList<>();
        searchCityAdapter = new SearchCityAdapter(cites);
        recyclerView.setAdapter(searchCityAdapter);

        loadLastFoundCity();
    }

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(com.test.weather.R.id.search_view);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchCityAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchCityAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(com.test.weather.R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private class SearchCityHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CitySearch citySearch;
        private TextView cityName;
        private TextView countryName;

        SearchCityHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            cityName = (TextView) itemView.findViewById(com.test.weather.R.id.city_name);
            countryName = (TextView) itemView.findViewById(com.test.weather.R.id.country_code);
        }

        void bindCity(CitySearch city) {
            citySearch = city;
            cityName.setText(city.getCityName());
            countryName.setText(city.getCountryCode());
        }

        @Override
        public void onClick(View v) {
            v.setBackgroundColor(Color.rgb(227, 227, 227));
            setCity(citySearch);
            sendBroadcast(new Intent(Constants.ACTION_FORCED_APPWIDGET_UPDATE));
            setResult(RESULT_OK);
            finish();
        }
    }

    private class SearchCityAdapter extends RecyclerView.Adapter<SearchCityHolder> implements
            Filterable {

        private List<CitySearch> cites;

        SearchCityAdapter(List<CitySearch> cites) {
            this.cites = cites;
        }

        @Override
        public int getItemCount() {
            if (cites != null)
                return cites.size();

            return 0;
        }

        @Override
        public void onBindViewHolder(SearchCityHolder holder, int position) {
            CitySearch city = cites.get(position);
            holder.bindCity(city);
        }

        @Override
        public SearchCityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(SearchActivity.this);
            View v = inflater.inflate(com.test.weather.R.layout.city_item, parent, false);

            return new SearchCityHolder(v);
        }

        @Override
        public Filter getFilter() {

            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    FilterResults filterResults = new FilterResults();

                    List<CitySearch> citySearchList = CityParser.getCity(charSequence.toString());
                    filterResults.values = citySearchList;
                    filterResults.count = citySearchList != null ? citySearchList.size() : 0;

                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence,
                                              FilterResults filterResults) {
                    cites.clear();
                    if (filterResults.values != null) {
                        cites.addAll((ArrayList<CitySearch>) filterResults.values);
                    }
                    notifyDataSetChanged();
                }
            };
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setCity(CitySearch city) {
        SharedPreferences.Editor editor = cityPref.edit();
        editor.putString(APP_SETTINGS_CITY, city.getCityName());
        editor.putString(APP_SETTINGS_COUNTRY_CODE, city.getCountryCode());
        editor.putString(APP_SETTINGS_LATITUDE, city.getLatitude());
        editor.putString(APP_SETTINGS_LONGITUDE, city.getLongitude());
        editor.apply();
    }

    private void loadLastFoundCity() {
        if (cites.isEmpty()) {
            String lastCity = cityPref.getString(APP_SETTINGS_CITY, "Minsk");
            String lastCountry = cityPref.getString(APP_SETTINGS_COUNTRY_CODE, "BY");
            String lastLat = cityPref.getString(APP_SETTINGS_LATITUDE, "53.90");
            String lastLon = cityPref.getString(APP_SETTINGS_LONGITUDE, "27.56");
            CitySearch city = new CitySearch(lastCity, lastCountry, lastLat, lastLon);
            cites.add(city);
        }
    }
}
