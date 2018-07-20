package jayjay.de.piusapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    MenuItem mPreviousMenuItem;
    final android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    final int REQUEST_EXIT = 100;
    final int RESULT_EXIT = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        checkFirstRunSettingsApplied();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //bei start wird das DashboardFragment geladen
        fragmentManager.beginTransaction().replace(R.id.content, new DashboardFragment()).commit();


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mPreviousMenuItem = navigationView.getMenu().findItem(R.id.nav_dashboard);
        mPreviousMenuItem.setCheckable(true).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        item.setCheckable(true);
        item.setChecked(true);
        if (mPreviousMenuItem != null) {
            mPreviousMenuItem.setChecked(false);
        }
        mPreviousMenuItem = item;

        switch (id) {
            case R.id.nav_dashboard:
                fragmentManager.beginTransaction().replace(R.id.content, new DashboardFragment()).commit();
                break;

            case R.id.nav_vtr_plan:
                fragmentManager.beginTransaction().replace(R.id.content, new VertretungsplanFragment()).commit();
                break;

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_EXIT && resultCode == RESULT_EXIT) finish();
    }

    private void checkFirstRunSettingsApplied(){
        if(!preferences.getBoolean("firstRunComplete", false)){

            Intent startIntent = new Intent(this, StartActivity.class);
            startActivityForResult(startIntent, REQUEST_EXIT);

        }
    }

}
