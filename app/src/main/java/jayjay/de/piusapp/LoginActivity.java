package jayjay.de.piusapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this)); //ALLE Fehler werden weitergeleitet und app crashed nicht

        //TODO: implement crashButton
        //TODO Login
    }
}
