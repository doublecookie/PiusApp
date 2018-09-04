package jayjay.de.piusapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * TODO Kurse
 * TODO einfache Auswahl
 * TODO ggf. Lehrer
 */
public class KurseFragment extends Fragment {


    public KurseFragment() {
        // Required empty public constructor
    }

    TextView kurseHeader;
    TextView keineKurseInfo;
    Button addKurse;
    LinearLayout kurseList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_kurse, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        kurseHeader = getActivity().findViewById(R.id.kurse_header);
        keineKurseInfo = getActivity().findViewById(R.id.keine_kurse_info);
        addKurse = getActivity().findViewById(R.id.add_kurse);
        kurseList = getActivity().findViewById(R.id.kurse_list);
        kurseList.setAlpha(1f);




        addKurse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEmptyAddDialog();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        speichereKurseListe();
    }

    @Override
    public void onResume() {
        super.onResume();

        String kurse = readFromFile(getString(R.string.kurse_filename));

        if(kurse.length() == 0 || kurse.equals("noFile")){
            keineKurseInfo.setVisibility(View.VISIBLE);
            keineKurseInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openEmptyAddDialog();
                }
            });
        }
        else if(!kurse.equals("error")){
            keineKurseInfo.setVisibility(View.GONE);
            erstelleKurseList(kurse);
    }
    }

    void openEmptyAddDialog(){
        openAddDialog(null);
    }

    void openAddDialog(JSONObject kurs){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();


        View dialogView = inflater.inflate(R.layout.dialog, null, false);

        try {
            if (kurs != null) {

                if (kurs.getBoolean("hasKlasse")){
                    builder.setTitle(getString(R.string.dialog_edit_title_klasse));
                }
                else if(kurs.getBoolean("hasKurs")){
                    builder.setTitle(getString(R.string.dialog_edit_title_kurs));
                }
                else{
                    builder.setTitle(getString(R.string.dialog_edit_title_stufe));
                }
            } else {

                builder.setTitle(getString(R.string.dialog_add_title_stufe));
            }
        }
        catch(Exception e){
            Log.e("openAddDialog", e.toString());
        }

        builder.setView(dialogView);

        builder.setPositiveButton(getString(R.string.add_kurs), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton(getString(R.string.kurse_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
    }

    void speichereKurseListe(){
        JSONArray kurse = new JSONArray();
        for (int i = 0; i < kurseList.getChildCount(); i++) {
            kurse.put(((LinearLayoutWithJSONObject) kurseList.getChildAt(i)).getJsonObject());
        }
        writeToFile(kurse.toString(), getString(R.string.kurse_filename));
    }

    void erstelleKurseList(final String data){

        kurseList.animate().alpha(0f).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                try{
                    kurseList.removeAllViews();

                    JSONArray kurse = new JSONArray(data);

                    for (int i = 0; i < kurse.length(); i++) {

                        JSONObject kurs = kurse.getJSONObject(i);

                        String kursText;
                        if(!kurs.getBoolean("hasKurs")){
                            if(kurs.getBoolean("hasKlasse")) kursText = kurs.getString("stufe") + kurs.getString("klasse");
                            else kursText = kurs.getString("stufe");
                        }
                        else{
                            kursText = kurs.getString("stufe") + " " + kurs.getString("kurs");
                        }

                        final LinearLayoutWithJSONObject linearLayout = new LinearLayoutWithJSONObject(kurseList.getContext(), kurs);
                        linearLayout.setBackgroundColor(Color.TRANSPARENT);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        linearLayout.setLayoutParams(layoutParams);
                        linearLayout.setPadding(10, 5, 10, 5);

                        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        textViewParams.gravity = Gravity.CENTER_VERTICAL;
                        final TextView textV = new TextView(linearLayout.getContext());
                        textV.setText(kursText);
                        textV.setTextSize(18);
                        textV.setGravity(Gravity.CENTER_VERTICAL);
                        textV.setLayoutParams(textViewParams);

                        LinearLayout.LayoutParams editButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        editButtonParams.gravity = Gravity.RIGHT;
                        final ImageButton edit = new ImageButton(linearLayout.getContext());
                        edit.setAdjustViewBounds(true);
                        edit.setImageResource(R.drawable.edit);
                        edit.setBackgroundColor(Color.TRANSPARENT);
                        edit.setLayoutParams(editButtonParams);

                        LinearLayout.LayoutParams removeButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        removeButtonParams.gravity = Gravity.RIGHT;
                        final ImageButton remove = new ImageButton(linearLayout.getContext());
                        remove.setAdjustViewBounds(true);
                        remove.setImageResource(R.drawable.delete);
                        remove.setBackgroundColor(Color.TRANSPARENT);
                        remove.setLayoutParams(removeButtonParams);

                        edit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                kurseList.removeView(linearLayout);
                                openAddDialog(linearLayout.getJsonObject());
                            }
                        });

                        remove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                kurseList.removeView(linearLayout);
                            }
                        });

                        linearLayout.addView(textV);
                        linearLayout.addView(edit);
                        linearLayout.addView(remove);

                        kurseList.addView(linearLayout);

                    }
                }
                catch (Exception e){
                    Log.e("erstelleKurseList", e.toString());
                }

                kurseList.animate().alpha(1f).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator());

                for (int i = 0; i < kurseList.getChildCount(); i++) {
                    View child = kurseList.getChildAt(i);
                    child.setAlpha(0f);
                    child.setTranslationY(100f);
                }
                for (int i = 0; i < kurseList.getChildCount(); i++) {
                    final View child = kurseList.getChildAt(i);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            child.animate().alpha(1f).translationY(0f).setDuration(1000).setInterpolator(new AccelerateDecelerateInterpolator());
                        }
                    }, i * 500);
                }
            }
        }, 500);
    }

    void writeToFile(String data,String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getActivity().openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            Log.v("Success","Wrote to "+filename);
        }
        catch (IOException e) {
            Log.e("Exception", "File("+filename+") write failed: " + e.toString());
        }
    }

    String readFromFile(String filename) {

        String ret = "";

        try {
            InputStream inputStream = getActivity().openFileInput(filename);

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
