package jayjay.de.piusapp;


import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;


/**
 * TODO: settings verschwinden lassen wenn bestimmte switches verändert werden + animationen!
 * TODO vibriere und headsup nur in android 7?
 */
public class SettingsFragment extends Fragment {

    //TODO: onBackPressed to Dashbaorad oder Vertretungsplan


    public SettingsFragment() {
        // Required empty public constructor
    }

    Switch refreshSwitch;
    Switch backgroundRefreshSwitch;
    TextView refreshTimeText;
    Spinner refreshTime;
    Switch refreshEveningSwitch;
    Switch notificationSwitch;
    Switch vibrateSwitch;
    Switch headsUpSwitch;
    Button crashButton;

    //Shared Preferences für Einstellungen
    SharedPreferences preferences;
    SharedPreferences.Editor editor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //get SharedPreferences
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = preferences.edit();

        //Initialize Views
        refreshSwitch = getActivity().findViewById(R.id.refresh_switch);
        backgroundRefreshSwitch = getActivity().findViewById(R.id.background_refresh_switch);

        refreshTimeText = getActivity().findViewById(R.id.background_refresh_time_text);
        refreshTime = getActivity().findViewById(R.id.refresh_time_spinner);
        //set refresh time options array in strings.xml to list for spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.settings_refresh_time_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        refreshTime.setAdapter(adapter);

        refreshEveningSwitch = getActivity().findViewById(R.id.refresh_evening_switch);
        notificationSwitch = getActivity().findViewById(R.id.notification_switch);
        vibrateSwitch = getActivity().findViewById(R.id.vibrate_switch);
        headsUpSwitch = getActivity().findViewById(R.id.heads_up_switch);
        crashButton = getActivity().findViewById(R.id.crash_button);

        //OnClickListener for onClick Actions on Buttons
        //LoginButton
        getActivity().findViewById(R.id.login_button_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startLogin = new Intent(getContext(), LoginActivity.class);
                startActivity(startLogin);
            }
        });

        //CrashBUtton
        boolean devOptions = false; //ob die Entwickler Einstellungen aktiviert sind
        try{
            int intDevOptions = Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED , 0);
            if(intDevOptions == 1) devOptions = true;
        }catch(Exception e){
            Log.e("checkDevOptions",e.toString());
        }

        if(devOptions) {
        }else{
            crashButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshSwitch.setChecked(preferences.getBoolean("RefreshOnAppStart", true));
        backgroundRefreshSwitch.setChecked(preferences.getBoolean("RefreshInBackground", true));
        refreshTime.setSelection(preferences.getInt("RefreshTimeSelection", 2));
        refreshEveningSwitch.setChecked(preferences.getBoolean("OverviewAt6pm", true));
        notificationSwitch.setChecked(preferences.getBoolean("Notifications", true));
        vibrateSwitch.setChecked(preferences.getBoolean("Vibrate", false));
        headsUpSwitch.setChecked(preferences.getBoolean("HeadsUp", false));

        if(backgroundRefreshSwitch.isChecked()){
            refreshTime.setVisibility(View.VISIBLE);
            refreshEveningSwitch.setVisibility(View.VISIBLE);
            refreshTimeText.setVisibility(View.VISIBLE);
        }else{
            refreshTime.setVisibility(View.GONE);
            refreshEveningSwitch.setVisibility(View.GONE);
            refreshTimeText.setVisibility(View.GONE);
        }

        if(notificationSwitch.isChecked() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            vibrateSwitch.setVisibility(View.VISIBLE);
            headsUpSwitch.setVisibility(View.VISIBLE);
        }else{
            vibrateSwitch.setVisibility(View.GONE);
            headsUpSwitch.setVisibility(View.GONE);
        }

        backgroundRefreshSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(backgroundRefreshSwitch.isChecked()){
                    refreshTime.setVisibility(View.VISIBLE);
                    refreshEveningSwitch.setVisibility(View.VISIBLE);
                    refreshTimeText.setVisibility(View.VISIBLE);
                }else{
                    refreshTime.setVisibility(View.GONE);
                    refreshEveningSwitch.setVisibility(View.GONE);
                    refreshTimeText.setVisibility(View.GONE);
                }
            }
        });

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(notificationSwitch.isChecked() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    vibrateSwitch.setVisibility(View.VISIBLE);
                    headsUpSwitch.setVisibility(View.VISIBLE);
                }else{
                    vibrateSwitch.setVisibility(View.GONE);
                    headsUpSwitch.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        editor.putBoolean("RefreshOnAppStart", refreshSwitch.isChecked());
        editor.putBoolean("RefreshInBackground", backgroundRefreshSwitch.isChecked());
        editor.putInt("RefreshTimeSelection", refreshTime.getSelectedItemPosition());
        editor.putBoolean("OverviewAt6pm", refreshEveningSwitch.isChecked());
        editor.putBoolean("Notifications", notificationSwitch.isChecked());
        editor.putBoolean("Vibrate", vibrateSwitch.isChecked());
        editor.putBoolean("HeadsUp", headsUpSwitch.isChecked());

        long intervallTime;
        switch(refreshTime.getSelectedItemPosition()){
            case 0:
                intervallTime = 1000 * 60 * 15;
                break;

            case 1:
                intervallTime = 1000 * 60 * 20;
                break;

            case 2:
                intervallTime = 1000 * 60 * 30;
                break;

            case 3:
                intervallTime = 1000 * 60 * 60;
                break;

            case 4:
                intervallTime = 1000 * 60 * 120;
                break;

            case 5:
                intervallTime = 1000 * 60 * 180;
                break;

            default:
                intervallTime = 1000 * 60 * 30;
                break;
        }

        editor.putLong("RefreshIntervallTime", intervallTime);
        editor.commit();

        updateJobOrAlarmManager();
    }

    private void updateJobOrAlarmManager(){

        //schedule the job only once.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) scheduleJob();

        else{
            //TODO: starte AlarmManager mit neuer Zeit und stoppe alten AlarmManager
            long refreshIntervallTime = preferences.getLong("RefreshIntervallTime", 1800000);
        }
    }

    @RequiresApi(21)
    private void scheduleJob(){
        //TODO: stop old job
        JobScheduler jobScheduler = (JobScheduler) getActivity().getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);

        ComponentName componentName = new ComponentName(getActivity(), UpdateJobService.class);

        JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(1, componentName).setPeriodic(preferences.getLong("RefreshIntervallTime", 1800000)).setPersisted(true).setBackoffCriteria(300000, JobInfo.BACKOFF_POLICY_EXPONENTIAL);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NOT_ROAMING);
        JobInfo jobInfo = jobInfoBuilder.build();
        jobScheduler.schedule(jobInfo);
        Log.v("JobScheduler", "scheduled");
    }
}
