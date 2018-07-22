package jayjay.de.piusapp;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

//dieser Job wurde geschedult und wird den sich wiederholdenden Job schedulen
@RequiresApi(21)//android sdk 21 wegen JobScheduler
public class UpdateJobStartService extends JobService{

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.v("UpdateJobService", "Job Started");

        JobScheduler jobScheduler = (JobScheduler)getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);

        ComponentName componentName = new ComponentName(this, UpdateJobService.class);

        JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(1, componentName)/*.setMinimumLatency(6000).setOverrideDeadline(2000)*/.setPeriodic(1200000).setPersisted(true).setBackoffCriteria(300000, JobInfo.BACKOFF_POLICY_EXPONENTIAL);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NOT_ROAMING);
        JobInfo jobInfo = jobInfoBuilder.build();
        jobScheduler.schedule(jobInfo);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
