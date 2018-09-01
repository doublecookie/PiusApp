package jayjay.de.piusapp;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

//der Service der immer wieder ausgeführt wird
@RequiresApi(21)//erst ab android sdk 21, wegen jobScheduler
public class UpdateJobService extends JobService{

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.v("UpdateJobService", "Job Started");
        //createNotification("JobScheduler","start","start");
        //neuer AsyncTask, mit Übergabe des unterhalb erstellten asyncTaskCompleteListener
        new DownloadData(getApplicationContext(), asyncTaskCompleteListener).execute();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    //Interface implementation
    private AsyncTaskCompleteListener asyncTaskCompleteListener = new AsyncTaskCompleteListener() {
        @Override
        public void onComplete(DownloadWrapper data) {//Methode wird am Ende des Async Tasks aufgerufen
            // TODO: createNotification("test",data,data);
        }
    };

    //Notification texte werden erstellt und dann an Notification Service übergeben wo dann Notification erstellt wird
    private void createNotification(String title, String smallText, String longText){
        //NotificationService muss auch über JobScheduler aufgerufen werden
        JobScheduler jobScheduler = (JobScheduler)getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);

        ComponentName componentName = new ComponentName(this, NotificationService.class);

        //übergabe von titel, kurzen un dlangem Text
        PersistableBundle extras = new PersistableBundle(3);
        extras.putString("title", title);
        extras.putString("smallText", smallText);
        extras.putString("longText", longText);

        //Job wird einmalig geschedult
        JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(2, componentName).setOverrideDeadline(10000);
        jobInfoBuilder.setExtras(extras);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NOT_ROAMING);
        JobInfo jobInfo = jobInfoBuilder.build();
        jobScheduler.schedule(jobInfo);
        Log.v("JobScheduler", "scheduled");
    }
}
