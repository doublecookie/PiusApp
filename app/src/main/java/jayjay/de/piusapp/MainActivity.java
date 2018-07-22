package jayjay.de.piusapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    MenuItem mPreviousMenuItem; //letztes MenuItem(in DrawerNavigation), welches dann wenn neues ausgewählt wird deselcted werden kann
    final android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager(); //FragmentManager um passendes Fragment auswählen zu können

    FloatingActionButton refreshFloatingButton; //Floating Action Button rechts unten

    //Shared Preferences für Einstellungen
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    //Integer Konstanten als Codes bei startActivityForResult()
    final int REQUEST_EXIT = 100;
    final int RESULT_EXIT = 101;
    final int RESULT_REFRESH = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //get SharedPreferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        //testen ob diese Version der App schon mal gelaufen ist, falls nicht wird StartActivity geöffnet
        checkFirstRunSettingsApplied();

        //Floating Action Button initialisieren
        refreshFloatingButton = findViewById(R.id.fab);
        refreshFloatingButton.setOnClickListener(new View.OnClickListener() { //on Click Listener damit der auch was machen kann
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        //Drawer Layout ( das Ding was man von links rausziehen kann ;) )
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close); //Knop links oben zum öffnen
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //bei start wird das DashboardFragment geladen
        fragmentManager.beginTransaction().replace(R.id.content, new DashboardFragment()).commit();

        //Liste im Drawer Layout
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this); //Dadurch wird Methode onNavigationItemSelected zur Activity hinzugefügr (siehe unten)
        mPreviousMenuItem = navigationView.getMenu().findItem(R.id.nav_dashboard); //setze dashboard MenuItem als previous Item
        mPreviousMenuItem.setCheckable(true).setChecked(true); //markiere Dashboard MenuItem
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) { //schließe Drawer wenn zurück gedrückt falls offen
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //durch listener integrierte Methode, wird aufgerufen wenn neues Item in Drawer ausgewählt wird
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId(); //item id wie in menu/activity_main_drawer.xml

        //markiere aktuelles Item
        item.setCheckable(true);
        item.setChecked(true);
        //Entmarkiere altes Item
        if (mPreviousMenuItem != null) {
            mPreviousMenuItem.setChecked(false);
        }
        mPreviousMenuItem = item; // setze neues Item in Variable, da es das nächste Mal das alte sein wird

        //switche über id --> je nach Id wird bestimmtes Fragment in das Frame Layout @+id/content (content_main.xml) geladen
        //und dann noch der Floating Action Button (un)sichtbar gemacht, je nach dem ob gebraucht oder nicht
        switch (id) {
            case R.id.nav_dashboard: //Dashboard
                fragmentManager.beginTransaction().replace(R.id.content, new DashboardFragment()).commit();
                refreshFloatingButton.setVisibility(View.VISIBLE);
                break;

            case R.id.nav_vtr_plan: //Vertretungsplan
                fragmentManager.beginTransaction().replace(R.id.content, new VertretungsplanFragment()).commit();
                refreshFloatingButton.setVisibility(View.VISIBLE);
                break;

            case R.id.nav_settings:
                fragmentManager.beginTransaction().replace(R.id.content, new SettingsFragment()).commit();
                refreshFloatingButton.setVisibility(View.GONE);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START); //schließe Drawer wenn MenuItem ausgewählt wurde
        return true;
    }

    //wird aufgerufen wenn über startActivityForResult() geöffnete Activity setResult(REESULT_CODE) aufruft
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_REFRESH) refresh();
        //Activity schließt sich wenn StartActivity geschlossen wird und StartActivity noch nicht durch war
        else if(requestCode == REQUEST_EXIT && resultCode == RESULT_EXIT) finish();
    }

    void refresh(){

    }

    private void checkFirstRunSettingsApplied(){
        //wenn SharedPreferences  "firstRunComplete" boolean noch nicht auf true gesetzt
        if(!preferences.getBoolean("firstRunComplete", false)){

            //öffne StartActivity
            Intent startIntent = new Intent(this, StartActivity.class);
            startActivityForResult(startIntent, REQUEST_EXIT);

        }
    }

}
