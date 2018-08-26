package jayjay.de.piusapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

public class CrashedActivity extends AppCompatActivity {

    String consoleOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crashed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        consoleOutput = intent.getExtras().getString("consoleOutput");
    }

    public void copyConsoleToClipboard(View view){
        final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData myClip = ClipData.newPlainText("text", consoleOutput);
        clipboard.setPrimaryClip(myClip);

        Toast.makeText(this,
                "Fehler-Meldung in Zwischenablage kopiert", Toast.LENGTH_SHORT)
                .show();
    }

    public void shareConsole(View view){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, consoleOutput);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.crashed_share_chooser_heading)));
    }

    public void reset(View view){
        deleteFile("kurse.txt");
        deleteFile("settings.txt");
        deleteFile("vertretungsDaten.txt");
        deleteFile("login.txt");
        deleteFile("version.txt");
        deleteFile("betapasswort.txt");
        restartApp(view);
    }

    public void restartApp(View view){
        Intent restartIntent = new Intent(this, MainActivity.class);
        startActivity(restartIntent);
        finish();
    }

}
