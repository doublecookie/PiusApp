package jayjay.de.piusapp;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//TODO erstelle Notification Creator Algorithmus

public class NotificationCreator {

    public String createNotification(Context context, String vertretungsData){

        String kursData = readFromFile(context, context.getString(R.string.kurse_filename));

        return "notification";
    }

    String readFromFile(Context context, String filename) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString).append("\n");
                }

                inputStream.close();
                ret = stringBuilder.toString();
                return ret;
            }
        }
        catch (FileNotFoundException e) {
            Log.e("read file activity", "File("+filename+") not found: " + e.toString());
            return "noFile";
        } catch (IOException e) {
            Log.e("read file activity", "Can not read file("+filename+"): " + e.toString());
        }
        return "error";
    }
}
