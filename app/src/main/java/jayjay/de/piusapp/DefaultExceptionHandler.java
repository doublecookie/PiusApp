package jayjay.de.piusapp;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.util.Arrays;

public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

    Activity activity;

    public DefaultExceptionHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable ex) {
        String consoleOutput;

        Log.e("ERROR", "caught by DefaultExceptionHandler:");
        Log.d("ERROR","---------" + ex.getMessage());
        consoleOutput = "---------" + ex.getMessage();
        Log.d("ERROR","--------" + ex.getCause());
        consoleOutput += "--------" + ex.getCause();
        Log.d("ERROR","--------" + Arrays.toString(ex.getStackTrace()));
        consoleOutput += "--------" + Arrays.toString(ex.getStackTrace());

        Intent intent = new Intent(activity, CrashedActivity.class);
        intent.putExtra("consoleOutput",consoleOutput);

        activity.startActivity(intent);
        activity.finish();

        System.exit(0);
    }

}