package jayjay.de.piusapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class NotificationID {

    //TODO: ggf überflüssige klasse: code könnte man auch direkt so in NotificationService implementieren

    public static int getID(Context mContext) {
        //Shared Preferences für Einstellungen
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = preferences.edit();

        int id = preferences.getInt("notificationID", 0);
        editor.putInt("notificationID", id+1);
        editor.apply();
        return id;
    }
}