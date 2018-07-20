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

@RequiresApi(21)
public class UpdateJobService extends JobService{

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.v("UpdateJobService", "Job Started");
        createNotification("JobScheduler","start","start");
        new DownloadData(getApplicationContext(), asyncTaskCompleteListener).execute();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    private AsyncTaskCompleteListener asyncTaskCompleteListener = new AsyncTaskCompleteListener() {
        @Override
        public void onComplete(String data) {
            createNotification("test",data,data);
        }
    };

    private void createNotification(String title, String smallText, String longText){
        JobScheduler jobScheduler = (JobScheduler)getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);

        ComponentName componentName = new ComponentName(this, NotificationService.class);

        PersistableBundle extras = new PersistableBundle(3);
        extras.putString("title", title);
        extras.putString("smallText", smallText);
        extras.putString("longText", longText);

        JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(2, componentName).setOverrideDeadline(10000);
        jobInfoBuilder.setExtras(extras);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NOT_ROAMING);
        JobInfo jobInfo = jobInfoBuilder.build();
        jobScheduler.schedule(jobInfo);
        Log.v("JobScheduler", "scheduled");
    }
}
