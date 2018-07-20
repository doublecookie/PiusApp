package jayjay.de.piusapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class StartActivity extends AppCompatActivity {

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    boolean backAlreadyPressed = false;

    final int RESULT_EXIT = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
    }

    @Override
    public void onBackPressed() {
        if (backAlreadyPressed) {
            setResult(RESULT_EXIT);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) finishAndRemoveTask();
            else this.finishAffinity();
        } else {
            backAlreadyPressed = true;
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
}
