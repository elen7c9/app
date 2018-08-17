package com.test.weather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.test.weather.service.CurrentWeatherService;
import com.test.weather.utils.Utils;

public class BaseActivity extends AppCompatActivity {

    static final int PICK_CITY = 1;

    private DrawerLayout drawerLayout;

    private Toolbar toolbar;
    private TextView headerCity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        getToolbar();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavDrawer();
    }

    private void setupNavDrawer() {
        drawerLayout = (DrawerLayout) findViewById(com.test.weather.R.id.drawer_layout);

        if (drawerLayout == null) {
            return;
        }


        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        configureNavView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    private void configureNavView() {
        NavigationView navigationView = (NavigationView) findViewById(com.test.weather.R.id.navigation_view);

        View headerLayout = navigationView.getHeaderView(0);
        headerCity = (TextView) headerLayout.findViewById(com.test.weather.R.id.nav_header_city);
        headerCity.setText(Utils.getCityAndCountry(this));
    }


    protected Toolbar getToolbar() {
        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(com.test.weather.R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
            }
        }

        return toolbar;
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDraw();
        } else {
            super.onBackPressed();
        }
    }

    protected boolean isNavDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    protected void closeNavDraw() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @NonNull
    protected ProgressDialog getProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.isIndeterminate();
        dialog.setMessage(getString(com.test.weather.R.string.load_progress));
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_CITY:
                ConnectionDetector connectionDetector = new ConnectionDetector(this);
                if (resultCode == RESULT_OK) {
                    headerCity.setText(Utils.getCityAndCountry(this));

                    if (connectionDetector.isNetworkAvailableAndConnected()) {
                        startService(new Intent(this, CurrentWeatherService.class));
                    }
                }
                break;
        }
    }
}
