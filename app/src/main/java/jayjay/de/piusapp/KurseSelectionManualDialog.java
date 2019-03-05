package jayjay.de.piusapp;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;

//TODO not yet working

public class KurseSelectionManualDialog extends DialogFragment {

    boolean stufePickerOngoingScroll;
    boolean klassePickerOngoingScroll;


    public Dialog onCreateDialog(Bundle savedInstanceState, JSONObject kurs) {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();


        View dialogView = inflater.inflate(R.layout.kurspicker_dialog, null, false);
        final NumberPicker stufePicker = dialogView.findViewById(R.id.stufe_picker);
        final NumberPicker klassePicker = dialogView.findViewById(R.id.klasse_picker);
        final NumberPicker kursTypePicker = dialogView.findViewById(R.id.kurs_type_picker);
        final NumberPicker kursZahlPicker = dialogView.findViewById(R.id.kurs_number_picker);

        final TextView kursExplanationText = dialogView.findViewById(R.id.kurs_explanation_text);

        final String[] valuesStufe = getResources().getStringArray(R.array.stufen);
        final String[] valuesKlasse = getResources().getStringArray(R.array.klassen);
        final String[] valuesKurseFach = getResources().getStringArray(R.array.kurse_fach);
        final String[] valuesKurseTypeEF = getResources().getStringArray(R.array.kurse_type_EF);
        final String[] valuesKurseTypeQ1 = getResources().getStringArray(R.array.kurse_type_Q1);
        final String[] valuesKurseTypeQ2 = getResources().getStringArray(R.array.kurse_type_Q2);
        final String[] valuesKurseZahl = getResources().getStringArray(R.array.kurse_zahl);

        stufePicker.setWrapSelectorWheel(true);
        stufePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        stufePicker.setMinValue(0);

        klassePicker.setWrapSelectorWheel(true);
        klassePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        klassePicker.setMinValue(0);

        kursTypePicker.setWrapSelectorWheel(true);
        kursTypePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        kursTypePicker.setMinValue(0);

        kursZahlPicker.setWrapSelectorWheel(true);
        kursZahlPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        kursZahlPicker.setMinValue(0);

        setNumberPickerValues(stufePicker, valuesStufe);

        setNumberPickerValues(kursZahlPicker, valuesKurseZahl);

        try {
            if (kurs != null) {

                stufePicker.setValue(Arrays.asList(valuesStufe).indexOf(kurs.getString("stufe")));

                if (kurs.getBoolean("hasKlasse")){
                    kursExplanationText.setVisibility(View.GONE);

                    builder.setTitle(getString(R.string.dialog_edit_title_klasse));
                    kursTypePicker.setVisibility(View.GONE);
                    kursZahlPicker.setVisibility(View.GONE);

                    setNumberPickerValues(klassePicker,valuesKlasse);

                    klassePicker.setValue(Arrays.asList(valuesKlasse).indexOf(kurs.getString("klasse")));
                }
                else if(kurs.getBoolean("hasKurs")){
                    kursExplanationText.setVisibility(View.GONE);

                    builder.setTitle(getString(R.string.dialog_edit_title_kurs));

                    setNumberPickerValues(klassePicker,valuesKurseFach);

                    klassePicker.setValue(Arrays.asList(valuesKurseFach).indexOf(kurs.getString("klasse")));

                    if(kurs.getString("stufe").equals("EF")){
                        setNumberPickerValues(kursTypePicker, valuesKurseTypeEF);
                        kursTypePicker.setValue(Arrays.asList(valuesKurseTypeEF).indexOf(kurs.getString("kursType")));
                    }
                    else if(kurs.getString("stufe").equals("Q1")){
                        setNumberPickerValues(kursTypePicker, valuesKurseTypeQ1);
                        kursTypePicker.setValue(Arrays.asList(valuesKurseTypeQ1).indexOf(kurs.getString("kursType")));
                    }
                    else if(kurs.getString("stufe").equals("Q2")){
                        setNumberPickerValues(kursTypePicker, valuesKurseTypeQ2);
                        kursTypePicker.setValue(Arrays.asList(valuesKurseTypeQ2).indexOf(kurs.getString("kursType")));
                    }

                    kursZahlPicker.setValue(Arrays.asList(valuesKurseZahl).indexOf(kurs.getString("kursZahl")));
                }
                else{
                    builder.setTitle(getString(R.string.dialog_edit_title_stufe));

                    kursExplanationText.setVisibility(View.VISIBLE);

                    setNumberPickerValues(klassePicker, valuesKurseFach);

                    klassePicker.setValue(0);

                    kursTypePicker.setVisibility(View.GONE);
                    kursZahlPicker.setVisibility(View.GONE);
                }
            } else {
                kursExplanationText.setVisibility(View.GONE);

                setNumberPickerValues(klassePicker, valuesKlasse);

                klassePicker.setValue(0);

                kursTypePicker.setVisibility(View.GONE);
                kursZahlPicker.setVisibility(View.GONE);

                builder.setTitle(getString(R.string.dialog_add_title_stufe));
            }
        }
        catch(Exception e){
            Log.e("openAddDialog", e.toString());
        }

        final NumberPicker.OnValueChangeListener klasseValueListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {

            }
        };

        final NumberPicker.OnValueChangeListener kurseFachValueListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                if(newVal == 0 && klassePickerOngoingScroll){
                    kursTypePicker.setVisibility(View.GONE);
                    kursZahlPicker.setVisibility(View.GONE);
                    kursExplanationText.setVisibility(View.VISIBLE);
                }else{
                    if(oldVal==0){
                        kursTypePicker.setVisibility(View.VISIBLE);
                        kursZahlPicker.setVisibility(View.VISIBLE);
                        kursExplanationText.setVisibility(View.GONE);
                    }
                }
            }
        };

        stufePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                int change = 0;
                int stufeEF = Arrays.asList(valuesStufe).indexOf("EF");
                int stufeQ1 = Arrays.asList(valuesStufe).indexOf("Q1");
                int stufeQ2 = Arrays.asList(valuesStufe).indexOf("Q2");
                int stufe5 = Arrays.asList(valuesStufe).indexOf("5");
                int stufe9 = Arrays.asList(valuesStufe).indexOf("9");
                int stufeIKE = Arrays.asList(valuesStufe).indexOf("IKE");
                int stufeIKD = Arrays.asList(valuesStufe).indexOf("IKD");

                if(newVal == stufeIKD || newVal == stufeIKE) change = 1;
                else if(newVal >= stufeEF && newVal <= stufeQ2) change = 2;
                else if(newVal >= stufe5 && newVal <= stufe9) change = 3;

                switch (change){
                    case 1:
                        klassePicker.setVisibility(View.GONE);
                        kursTypePicker.setVisibility(View.GONE);
                        kursZahlPicker.setVisibility(View.GONE);
                        kursExplanationText.setVisibility(View.GONE);
                        break;

                    case 2:
                        klassePicker.setVisibility(View.VISIBLE);
                        setNumberPickerValues(klassePicker, valuesKurseFach);
                        klassePicker.setOnValueChangedListener(kurseFachValueListener);

                        if(oldVal < stufeEF || oldVal > stufeQ2) klassePicker.setValue(0);

                        kursTypePicker.setValue(0);
                        kursZahlPicker.setValue(0);
                        if(klassePicker.getValue()==0) kursExplanationText.setVisibility(View.VISIBLE);
                        break;

                    case 3:
                        klassePicker.setVisibility(View.VISIBLE);
                        setNumberPickerValues(klassePicker, valuesKlasse);
                        klassePicker.setOnValueChangedListener(klasseValueListener);

                        kursTypePicker.setVisibility(View.GONE);
                        kursZahlPicker.setVisibility(View.GONE);
                        kursExplanationText.setVisibility(View.GONE);
                        break;
                }

                if(newVal == stufeEF){
                    setNumberPickerValues(kursTypePicker, valuesKurseTypeEF);
                }
                else if(newVal == stufeQ1){
                    setNumberPickerValues(kursTypePicker, valuesKurseTypeQ1);
                }
                else if(newVal == stufeQ2){
                    setNumberPickerValues(kursTypePicker, valuesKurseTypeQ2);
                }
            }
        });

        stufePickerOngoingScroll = false;
        klassePickerOngoingScroll = false;

        stufePicker.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker numberPicker, int scrollState) {
                stufePickerOngoingScroll = (scrollState==SCROLL_STATE_IDLE);
            }
        });
        klassePicker.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker numberPicker, int scrollState) {
                klassePickerOngoingScroll = (scrollState==SCROLL_STATE_IDLE);
                if(scrollState == SCROLL_STATE_IDLE) {
                    try{
                        kurseFachValueListener.onValueChange(klassePicker,klassePicker.getValue(),klassePicker.getValue());
                    }catch(Exception e){Log.e("OnScrollListenerAddKurs", e.toString());}
                }
            }
        });

        builder.setView(dialogView);

        builder.setPositiveButton(getString(R.string.add_kurs),null);

        builder.setNegativeButton(getString(R.string.cancel_kurs), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {/*adding onClickListener so it closes on Button Click*/}
        });

        final android.app.AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {

                Button positiveButton = ((android.app.AlertDialog) dialog).getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        try{
                            JSONArray kurse = new JSONArray(readFromFile(getString(R.string.kurse_filename)));
                            JSONObject neuerKurs = new JSONObject();

                            int stufeValue = stufePicker.getValue();

                            int change = 0;
                            int stufeEF = Arrays.asList(valuesStufe).indexOf("EF");
                            int stufeQ2 = Arrays.asList(valuesStufe).indexOf("Q2");
                            int stufe5 = Arrays.asList(valuesStufe).indexOf("5");
                            int stufe9 = Arrays.asList(valuesStufe).indexOf("9");
                            int stufeIKE = Arrays.asList(valuesStufe).indexOf("IKE");
                            int stufeIKD = Arrays.asList(valuesStufe).indexOf("IKD");

                            if(stufeValue == stufeIKD || stufeValue == stufeIKE) change = 1;
                            else if(stufeValue >= stufeEF && stufeValue <= stufeQ2) change = 2;
                            else if(stufeValue >= stufe5 && stufeValue <= stufe9) change = 3;

                            switch (change){
                                case 1:
                                    neuerKurs.put("hasKurs", false);
                                    neuerKurs.put("hasKlasse", false);
                                    neuerKurs.put("stufe", valuesStufe[stufePicker.getValue()]);
                                    break;

                                case 2:
                                    neuerKurs.put("hasKlasse", false);
                                    neuerKurs.put("stufe", valuesStufe[stufePicker.getValue()]);
                                    if(klassePicker.getValue() == 0) {
                                        neuerKurs.put("hasKurs", false);
                                    }
                                    else{
                                        neuerKurs.put("hasKurs", true);
                                        neuerKurs.put("klasse", valuesKurseFach[klassePicker.getValue()]);
                                        neuerKurs.put("kursType", valuesKurseTypeQ2[kursTypePicker.getValue()]);
                                        neuerKurs.put("kursZahl", valuesKurseZahl[kursZahlPicker.getValue()]);
                                    }
                                    break;

                                case 3:
                                    neuerKurs.put("hasKlasse", true);
                                    neuerKurs.put("hasKurs", false);
                                    neuerKurs.put("stufe", valuesStufe[stufePicker.getValue()]);
                                    neuerKurs.put("klasse", valuesKlasse[klassePicker.getValue()]);
                                    break;
                            }

                            boolean dopplung = false;
                            for (int j = 0; j < kurse.length(); j++) {
                                if(kurse.getJSONObject(j).toString().equals(neuerKurs.toString())) dopplung = true;
                            }
                            if(dopplung){
                                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.duplicate_kurs), Toast.LENGTH_SHORT).show();
                            }
                            else {
                                kurse.put(neuerKurs);
                                writeToFile(kurse.toString(), getString(R.string.kurse_filename));
//                                erstelleKurseList(kurse.toString()); TODO
                                dialogInterface.dismiss();
                            }
                        }
                        catch(Exception e){
                            Log.e("addKurseSpeichern", e.toString());
                        }
                    }
                });

            }
        });

        return dialog;
    }

    void setNumberPickerValues(NumberPicker numberPicker, String[] values){
        if(numberPicker.getMaxValue() < values.length){
            numberPicker.setDisplayedValues(values);
            numberPicker.setMaxValue(values.length-1);
        }
        else {
            numberPicker.setMaxValue(values.length-1);
            numberPicker.setDisplayedValues(values);
        }
    }

    void writeToFile(String data,String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getActivity().openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            Log.v("Success","Wrote to "+filename+":"+data);
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
