package jayjay.de.piusapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.v("NotificationService","executed");

        //diese attribute werden beim aufruf des services übergeben
        PersistableBundle extras = jobParameters.getExtras();
        String titel = extras.getString("title");
        String smallText = extras.getString("smallText");
        String longText = extras.getString("longText");

        //immer eine andere id so dass sich notifications nicht auslöschen
        int notificationId = NotificationID.getID();

        //methode
        displayNotification(titel, smallText, longText, notificationId);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    private void displayNotification(String title, String smallText, String longText, int notificationID){

        NotificationCompat.Builder notification;
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificaionPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Android 8 und höher

                String channel_id = getString(R.string.notification_channel_id);
                notification = new NotificationCompat.Builder(this,channel_id);
            }
            else { // Älter als Android 8

                notification = new NotificationCompat.Builder(this)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVibrate(new long[]{0,500});
            }

            notification
                    .setContentTitle(title)
                    .setContentText(smallText)
                    .setSmallIcon(R.drawable.ic_ntf_logo_white)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_ntf_logo_big))
                    //.setSound()
                    //.setLights(notificationColor, 500, 2000)
                    .setContentIntent(notificaionPendingIntent)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(longText));

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(notificationID, notification.build());

        }catch(Exception e){
            Log.v("NotificationService","Notification Build:");
            e.printStackTrace();
        }
    }
}
