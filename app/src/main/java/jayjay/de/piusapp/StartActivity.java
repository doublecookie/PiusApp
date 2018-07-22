package jayjay.de.piusapp;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StartActivity extends AppCompatActivity {

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    LinearLayout loginLinear;
    Button nextButton;
    ProgressBar loading;
    TextView startIntroduction;
    ImageView appLogo;
    WatchfulEditText usernameEdit;
    WatchfulEditText passwordEdit;

    DownloadData lastAsyncTask;
    Activity thisActivity;

    boolean backAlreadyPressed = false;
    boolean testingInProgress = false;
    boolean userOfOlderVersion = false;
    int state = 0;

    boolean informationStateUser = false;
    boolean informationStatePassw = false;

    final int RESULT_EXIT = 101;
    final int RESULT_REFRESH = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        thisActivity = this;

        loginLinear = findViewById(R.id.login_linear);
        nextButton = findViewById(R.id.next_button);
        loading = findViewById(R.id.start_loading);
        startIntroduction = findViewById(R.id.start_introduction);
        appLogo = findViewById(R.id.app_logo);
        usernameEdit = findViewById(R.id.username_edit_text);
        passwordEdit = findViewById(R.id.password_edit_text);

        findViewById(R.id.start_info_button_username).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(informationStateUser) findViewById(R.id.start_info_username).setVisibility(View.GONE);
                else{
                    findViewById(R.id.start_info_username).setVisibility(View.VISIBLE);
                    if(informationStatePassw){
                        findViewById(R.id.start_info_password).setVisibility(View.GONE);
                        informationStatePassw = false;
                    }
                }
                informationStateUser = !informationStateUser;
            }
        });

        findViewById(R.id.start_info_button_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(informationStatePassw) findViewById(R.id.start_info_password).setVisibility(View.GONE);
                else{
                    findViewById(R.id.start_info_password).setVisibility(View.VISIBLE);
                    if(informationStateUser){
                        findViewById(R.id.start_info_username).setVisibility(View.GONE);
                        informationStateUser = false;
                    }
                }
                informationStatePassw = !informationStatePassw;
            }
        });

        if((userOfOlderVersion = readFromFile("login").length()>2)){
            startIntroduction.setText(getString(R.string.start_introduction_comeback));
            usernameEdit.setText(readFromFile("login").split("\n")[0]);
            passwordEdit.setText(readFromFile("login").split("\n")[1]);

            //TODO: read settings
        }

        loginLinear.setVisibility(View.GONE);
        loading.setVisibility(View.INVISIBLE);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
    }

    @Override
    public void onBackPressed() {
        Log.v("Start Activity", "back pressed");
        if (backAlreadyPressed) {
            setResult(RESULT_EXIT);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) finishAndRemoveTask();
            else this.finishAffinity();
        } else {
            backAlreadyPressed = true;
        }
    }

    static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private AsyncTaskCompleteListener asyncTaskCompleteListener = new AsyncTaskCompleteListener() {
        @Override
        public void onComplete(DownloadWrapper data) {
            System.out.println("data.success = " + data.success);
            loading.setVisibility(View.GONE);
            if(data.success && state == 1){
                hideKeyboard(thisActivity);
                nextButton.setTextColor(getResources().getColor(R.color.colorAccent));
                loginLinear.animate().alpha(0f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        appLogo.animate().translationY(-192f).alpha(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(500);
                    }
                }, 200);
                //TODO: animate Kurse Object to fly in
                state++;
            }else if(!data.success){
                //TODO: show user that it is the wrong password
            }
        }
    };

    public void next(View view){
        switch(state){
            case 0:
                loginLinear.setVisibility(View.VISIBLE);
                loginLinear.setAlpha(0f);

                appLogo.animate().translationY(-192f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(500);
                startIntroduction.animate().alpha(0f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200);
                loginLinear.animate().translationY(-128f).alpha(1f).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator());

                nextButton.setTextColor(getResources().getColor(R.color.gray));

                usernameEdit.clearFocus();

                passwordEdit.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void afterTextChanged(Editable editable) {
                        checkForValidateCredentials();
                    }
                });
                usernameEdit.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void afterTextChanged(Editable editable) {
                        checkForValidateCredentials();
                    }
                });

                usernameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if(b){
                            onSoftKeyboardChanged(true);
                            backAlreadyPressed = false;
                        }
                        else if(!b && !passwordEdit.hasFocus()) onSoftKeyboardChanged(false);
                    }
                });
                passwordEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if(b){
                            onSoftKeyboardChanged(true);
                            backAlreadyPressed = false;
                        }
                        else if(!b && !usernameEdit.hasFocus()) onSoftKeyboardChanged(false);
                    }
                });

                usernameEdit.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                        if(keyCode == KeyEvent.KEYCODE_BACK) onSoftKeyboardChanged(false);
                        return false;
                    }
                });
                passwordEdit.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                        if(keyCode == KeyEvent.KEYCODE_BACK) onSoftKeyboardChanged(false);
                        return false;
                    }
                });

                if(userOfOlderVersion) new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkForValidateCredentials();
                    }
                }, 1000);

                state++;
                break;

            case 1:
                //TODO: Zeige dass weiter button nicht aktiv ist
                break;

            case 2:
                if(false /*keine Kurse*/){
                    //TODO: Warnmeldung
                }else{
                    editor.putBoolean("firstRunComplete", true);
                    editor.commit();
                    setResult(RESULT_REFRESH);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) finishAndRemoveTask();
                    else this.finishAffinity();
                }
                break;
        }
    }

    private void onSoftKeyboardChanged(boolean open){
        if(open){
            appLogo.animate().alpha(0f).translationY(-256f).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator());
            loginLinear.animate().translationY(-768f).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator());
            loading.animate().translationY(-640f).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator());
        }else{
            loginLinear.animate().translationY(-128f).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator());
            loading.animate().translationY(0f).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    appLogo.animate().alpha(1f).translationY(-192f).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator());
                }
            }, 300);
        }
    }

    private void checkForValidateCredentials(){
        String username = usernameEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        if(lastAsyncTask != null) lastAsyncTask.cancel(true);
        if(username.length() >= 4 && password.length() >= 4){
            editor.putString("username", username);
            editor.putString("password", password);
            editor.apply();
            lastAsyncTask = new DownloadData(getApplicationContext(), asyncTaskCompleteListener, true);
            lastAsyncTask.execute();
            loading.setVisibility(View.VISIBLE);
            testingInProgress = true;
        }
    }

    private void starteBackgroundTasks(){

        //schedule the job only once.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) scheduleJob();

        else{
            //TODO: starte AlarmManager
        }

        //create the notification channel only once.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel();

        editor.putBoolean("firstRunComplete", true);
    }

    @RequiresApi(21)
    private void scheduleJob(){
        JobScheduler jobScheduler = (JobScheduler)getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);

        ComponentName componentName = new ComponentName(this, UpdateJobStartService.class);

        JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(1, componentName).setMinimumLatency(600000).setOverrideDeadline(900000)/*.setPeriodic(1200000).setPersisted(true).setBackoffCriteria(300000, JobInfo.BACKOFF_POLICY_EXPONENTIAL)*/;
        JobInfo jobInfo = jobInfoBuilder.build();
        jobScheduler.schedule(jobInfo);
        Log.v("JobScheduler", "scheduled");
    }

    @RequiresApi(26)
    private void createNotificationChannel(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Create the NotificationChannel
        String channel_id = getString(R.string.notification_channel_id);
        CharSequence name = getString(R.string.notification_channel_name);
        String description = getString(R.string.notification_channel_description);

        NotificationChannel mChannel = new NotificationChannel(channel_id, name, NotificationManager.IMPORTANCE_HIGH);
        mChannel.setDescription(description);
        //mChannel.enableLights(true);
        //mChannel.setLightColor(R.color.colorPius);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{0,500});

        // Register the channel with the system; you can't change the importance
        // or other notifications behaviors after this
        notificationManager.createNotificationChannel(mChannel);
    }

    public String readFromFile(String filename) {

        String ret = "";

        try {
            InputStream inputStream = getApplicationContext().openFileInput(filename+".txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString).append("\n");
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("read file activity", "File("+filename+".txt) not found: " + e.toString());
        } catch (IOException e) {
            Log.e("read file activity", "Can not read file("+filename+".txt): " + e.toString());
        }
        return ret;
    }
}
