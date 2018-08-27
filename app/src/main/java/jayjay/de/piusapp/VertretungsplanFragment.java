package jayjay.de.piusapp;


import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 *
 */
public class VertretungsplanFragment extends Fragment implements MainActivity.refreshInterface{


    public VertretungsplanFragment() {
        // Required empty public constructor
    }

    LayoutInflater layoutInflater;

    SwipeRefreshLayout mSwipeRefreshLayout;
    TableLayout vertretungsTable;
    View tableFadeView;

    TextView tickerHeader;
    TextView tickerTextView;
    TextView fehlerView;

    int piusDarkColor;
    int piusColor;
    int weiss;
    int rot;
    int grau;
    int evaGelb;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layoutInflater = inflater;
        return inflater.inflate(R.layout.fragment_vertretungsplan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout = getActivity().findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,R.color.colorPrimary,R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                aktualisiere();
            }
        });

        vertretungsTable = getActivity().findViewById(R.id.vertretungs_table);
        tableFadeView = getActivity().findViewById(R.id.tableFadeView);

        tickerHeader = getActivity().findViewById(R.id.ticker_header);
        tickerTextView = getActivity().findViewById(R.id.ticker_text);
        fehlerView = getActivity().findViewById(R.id.fehler_view);

        piusDarkColor = ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark);
        piusColor = ContextCompat.getColor(getActivity(), R.color.colorPrimary);
        weiss = ContextCompat.getColor(getActivity(), android.R.color.white);
        rot = ContextCompat.getColor(getActivity(), android.R.color.holo_red_dark);
        grau = Color.rgb(113, 122, 133);
        evaGelb = Color.rgb(255,204,0);

        load();
    }

    private AsyncTaskCompleteListener asyncTaskCompleteListener = new AsyncTaskCompleteListener() {
        @Override
        public void onComplete(DownloadWrapper data) {

            if(data.success) {
                fehlerView.setVisibility(View.GONE);
                erstelleLayout(data.downloadData);
            }else{
                fehlerView.setText(data.errorMessage);
                fehlerView.setVisibility(View.VISIBLE);
                //TODO optimize error output
            }

        }
    };

    void load(){
        erstelleLayout(readFromFile(getString(R.string.vertretungs_filename)));
    }

    @Override
    public void aktualisiereDurchMainActivity() {
        aktualisiere();
    }

    public void aktualisiere(){
        try{mSwipeRefreshLayout.setRefreshing(true);}catch(Exception e){Log.e("SwipeRefresh",e.toString());}

        new DownloadData(getActivity(), asyncTaskCompleteListener).execute();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try{mSwipeRefreshLayout.setRefreshing(false);}catch(Exception e){Log.e("SwipeRefresh",e.toString());}
            }
        }, 1000);
    }

    void erstelleLayout(final String data){

        tableFadeView.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try{

                    vertretungsTable.removeAllViews();

                    JSONObject vertretungsplan = new JSONObject(data);

                    String[] klassenBeschriftung = getActivity().getResources().getStringArray(R.array.vertretungsplan_klassen_beschriftung);

                    String tickerText = vertretungsplan.getString("ticker");
                    if(!tickerText.equals("") && tickerText != null) {
                        tickerHeader.setVisibility(View.VISIBLE);
                        tickerTextView.setVisibility(View.VISIBLE);
                        tickerTextView.setText(tickerText);
                    }

                    JSONArray tage = vertretungsplan.getJSONArray("tage");

                    for (int i = 0; i < tage.length(); i++) {

                        JSONObject tag = tage.getJSONObject(i);

                        String tagText = tag.getString("tag");
                        String letzteAktualisierung = tag.getString("letzteAktualisierung");
                        String betroffen = tag.getString("betroffen");

                        TableRow tagTextRow = new TableRow(vertretungsTable.getContext());
                        TableLayout.LayoutParams tagTextRowParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        tagTextRowParams.setMargins(0,96,0,0);
                        tagTextRow.setLayoutParams(tagTextRowParams);

                        TextView tagTextTextView = new TextView(tagTextRow.getContext());
                        TableRow.LayoutParams tagTextTextViewParams= new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                        tagTextTextViewParams.span = 7;
                        tagTextTextView.setText(tagText);
                        tagTextTextView.setTextColor(piusDarkColor);
                        tagTextTextView.setTextSize(16f);
                        tagTextTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                        tagTextTextView.setLayoutParams(tagTextTextViewParams);

                        tagTextRow.addView(tagTextTextView);

                        TableRow letzteAktualisierungRow = new TableRow(vertretungsTable.getContext());
                        TableLayout.LayoutParams letzteAktualisierungRowParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        letzteAktualisierungRowParams.setMargins(0,32,0,0);
                        letzteAktualisierungRow.setLayoutParams(letzteAktualisierungRowParams);

                        TextView letzteAktualisierungTextView = new TextView(letzteAktualisierungRow.getContext());
                        TableRow.LayoutParams letzteAktualisierungTextViewParams= new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                        letzteAktualisierungTextViewParams.span = 7;
                        letzteAktualisierungTextView.setText(letzteAktualisierung);
                        letzteAktualisierungTextView.setTextColor(grau);
                        letzteAktualisierungTextView.setTextSize(12f);
                        letzteAktualisierungTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                        letzteAktualisierungTextView.setLayoutParams(letzteAktualisierungTextViewParams);

                        letzteAktualisierungRow.addView(letzteAktualisierungTextView);

                        TableRow betroffenRow = new TableRow(vertretungsTable.getContext());
                        TableLayout.LayoutParams betroffenRowParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        betroffenRowParams.setMargins(0,32,0,0);
                        betroffenRow.setLayoutParams(betroffenRowParams);

                        TextView betroffenTextView = new TextView(betroffenRow.getContext());
                        TableRow.LayoutParams betroffenTextViewParams= new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                        betroffenTextViewParams.span = 7;
                        betroffenTextView.setText(betroffen);
                        betroffenTextView.setTextColor(piusDarkColor);
                        betroffenTextView.setTextSize(13f);
                        betroffenTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                        betroffenTextView.setLayoutParams(betroffenTextViewParams);

                        betroffenRow.addView(betroffenTextView);

                        vertretungsTable.addView(tagTextRow);
                        vertretungsTable.addView(letzteAktualisierungRow);
                        vertretungsTable.addView(betroffenRow);

                        JSONArray klassen = tag.getJSONArray("klassen");

                        for (int j = 0; j < klassen.length(); j++) {

                            JSONObject klasse = klassen.getJSONObject(j);

                            String klassenText = klasse.getString("klasse");

                            TableRow klassenRow = new TableRow(vertretungsTable.getContext());
                            TableLayout.LayoutParams klassenRowParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            klassenRowParams.setMargins(0,64,0,5);
                            klassenRow.setBackgroundColor(piusDarkColor);
                            klassenRow.setLayoutParams(klassenRowParams);

                            TextView klassenTextView = new TextView(klassenRow.getContext());
                            klassenTextView.setText(klassenText);
                            klassenTextView.setTextColor(weiss);
                            klassenTextView.setGravity(Gravity.LEFT);
                            klassenTextView.setPadding(16,10,0,10);
                            klassenTextView.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                            klassenRow.addView(klassenTextView);

                            TableRow klassenBeschriftungRow = new TableRow(vertretungsTable.getContext());
                            TableLayout.LayoutParams klassenBeschriftungRowParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            klassenBeschriftungRow.setBackgroundColor(piusDarkColor);
                            klassenBeschriftungRow.setLayoutParams(klassenBeschriftungRowParams);

                            for (int k = 0; k < klassenBeschriftung.length; k++) {

                                String beschriftung = klassenBeschriftung[k];

                                TextView beschriftungsTextView = new TextView(klassenBeschriftungRow.getContext());
                                beschriftungsTextView.setText(beschriftung);
                                beschriftungsTextView.setTextColor(weiss);
                                beschriftungsTextView.setTextSize(10f);
                                beschriftungsTextView.setSingleLine(true);
                                beschriftungsTextView.setMinEms(beschriftung.length());
                                beschriftungsTextView.setPadding(8,4,8,4);
                                beschriftungsTextView.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                                klassenBeschriftungRow.addView(beschriftungsTextView);

                                if(k != klassenBeschriftung.length-1) {
                                    layoutInflater.inflate(R.layout.divider, klassenBeschriftungRow);
                                }

                            }

                            vertretungsTable.addView(klassenRow);
                            vertretungsTable.addView(klassenBeschriftungRow);

                            JSONArray vertretungen = klasse.getJSONArray("vertretungen");

                            for (int k = 0; k < vertretungen.length(); k++) {

                                JSONObject vertretung = vertretungen.getJSONObject(k);

                                String[] vertretungsTexte = new String[7];

                                vertretungsTexte[0] = vertretung.getString("stunden");
                                vertretungsTexte[1] = vertretung.getString("art");
                                vertretungsTexte[2] = vertretung.getString("kurs");
                                vertretungsTexte[3] = vertretung.getString("raum");
                                vertretungsTexte[4] = vertretung.getString("lehrerAktuell");
                                vertretungsTexte[5] = vertretung.getString("lehrerPlan");
                                vertretungsTexte[6] = vertretung.getString("bemerkung");

                                TableRow vertretungsRow = new TableRow(vertretungsTable.getContext());
                                TableLayout.LayoutParams vertretungsRowParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                vertretungsRowParams.setMargins(0,5,0,0);
                                vertretungsRow.setBackgroundColor(piusColor);
                                vertretungsRow.setLayoutParams(vertretungsRowParams);

                                for (int l = 0; l < vertretungsTexte.length; l++) {
                                    String vertretungsText = vertretungsTexte[l];

                                    TextView vertretungsTextView = new TextView(vertretungsRow.getContext());

                                    if (vertretungsText.length()>1 && vertretungsText.substring(0, 1).equals("%")) {
                                        vertretungsTextView.setBackgroundColor(rot);
                                        vertretungsText = vertretungsText.substring(1);
                                    }
                                    if(vertretungsText.equals("Vertretung")) vertretungsText = "Vtr.";
                                    else if(vertretungsText.equals("Mitbetreuung")) vertretungsText = "Mitbetr.";
                                    if (vertretungsText.contains("^")) {
                                        int start = vertretungsText.indexOf("^");
                                        int end = vertretungsText.indexOf("^", start + 1);
                                        vertretungsText = vertretungsText.replace("^", "");
                                        SpannableStringBuilder spanString = new SpannableStringBuilder(vertretungsText);
                                        spanString.setSpan(new StrikethroughSpan(), start, end - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                        vertretungsTextView.setText(spanString);
                                    } else {
                                        vertretungsTextView.setText(vertretungsText);
                                    }

                                    vertretungsTextView.setPadding(10, 0, 0, 0);
                                    if(l == 0){
                                        vertretungsTextView.setSingleLine(true);
                                    }else if(l == vertretungsTexte.length-1){
                                        vertretungsTextView.setMaxWidth(vertretungsTable.getWidth() / 4);
                                    }

                                    vertretungsTextView.setTextColor(weiss);
                                    vertretungsTextView.setTextSize(10f);
                                    vertretungsTextView.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

                                    vertretungsRow.addView(vertretungsTextView);

                                    if(l != vertretungsTexte.length-1) {
                                        View divider = new View(vertretungsRow.getContext());
                                        divider.setLayoutParams(new TableRow.LayoutParams(3, ViewGroup.LayoutParams.MATCH_PARENT));
                                        divider.setBackgroundColor(weiss);

                                        vertretungsRow.addView(divider);
                                    }
                                }

                                vertretungsTable.addView(vertretungsRow);

                                JSONArray evas = vertretung.getJSONArray("eva");

                                for (int l = 0; l < evas.length(); l++) {

                                    //TODO maybe optimize eva

                                    String evaText = evas.getString(l);

                                    float density = vertretungsTable.getContext().getResources().getDisplayMetrics().density;

                                    TableLayout.LayoutParams rowEvaParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    TableRow rowEva = new TableRow(vertretungsTable.getContext());
                                    rowEva.setBackgroundColor(evaGelb);
                                    rowEva.setLayoutParams(rowEvaParams);

                                    TableRow.LayoutParams evaLinearParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT,1f);
                                    evaLinearParams.span= 13;
                                    ConstraintLayout constraintEva = new ConstraintLayout(rowEva.getContext());
                                    constraintEva.setPadding(15,15,15,15);
                                    constraintEva.setId(View.generateViewId());
                                    //linearEva.setBackgroundColor(Color.rgb(255,0,0));
                                    constraintEva.setLayoutParams(evaLinearParams);
                                    ConstraintSet constraintSet = new ConstraintSet();
                                    constraintSet.clone(constraintEva);

                                    ConstraintLayout.LayoutParams evaBeschrParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                                    TextView evaBeschrTextView = new TextView(constraintEva.getContext());
                                    evaBeschrTextView.setTextColor(piusDarkColor);
                                    evaBeschrTextView.setTextSize(10);
                                    evaBeschrTextView.setSingleLine(true);
                                    evaBeschrTextView.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                                    evaBeschrTextView.setText(getString(R.string.eva));
                                    evaBeschrTextView.setId(TextView.generateViewId());
                                    constraintEva.addView(evaBeschrTextView,0,evaBeschrParams);

                                    constraintSet.connect(evaBeschrTextView.getId(),ConstraintSet.LEFT,ConstraintSet.PARENT_ID,ConstraintSet.LEFT);
                                    constraintSet.connect(evaBeschrTextView.getId(),ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP);
                                    constraintSet.constrainHeight(evaBeschrTextView.getId(),ConstraintSet.WRAP_CONTENT);
                                    constraintSet.constrainWidth(evaBeschrTextView.getId(),ConstraintSet.WRAP_CONTENT);
                                    constraintSet.applyTo(constraintEva);

                                    ConstraintLayout.LayoutParams evaParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                                    evaParams.setMargins(0,0,0,0);
                                    final TextView evaTextView = new TextView(constraintEva.getContext());
                                    evaTextView.setPadding(20,0,(int)(40*density),0);
                                    evaTextView.setTextColor(piusDarkColor);
                                    evaTextView.setTextSize(10);
                                    evaTextView.setId(TextView.generateViewId());
                                    evaTextView.setText(evaText);
                                    constraintEva.addView(evaTextView,1,evaParams);

                                    constraintSet.connect(evaTextView.getId(),ConstraintSet.LEFT,evaBeschrTextView.getId(),ConstraintSet.RIGHT);
                                    constraintSet.connect(evaTextView.getId(),ConstraintSet.RIGHT,vertretungsTable.getId(),ConstraintSet.RIGHT);
                                    constraintSet.connect(evaTextView.getId(),ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP);
                                    constraintSet.connect(evaTextView.getId(),ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM);
                                    constraintSet.constrainHeight(evaTextView.getId(),ConstraintSet.WRAP_CONTENT);
                                    constraintSet.constrainWidth(evaTextView.getId(),ConstraintSet.MATCH_CONSTRAINT);

                                    constraintSet.applyTo(constraintEva);

                                    vertretungsTable.addView(rowEva);

                                }

                            }
                        }

                    }
                }
                catch(Exception e){
                    Log.e("erstelleLayoutFehler", e.toString());
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tableFadeView.setVisibility(View.GONE);
                    }
                }, 100);
            }
        }, 500);
    }

    public String readFromFile(String filename) {

        String ret = "";

        try {
            InputStream inputStream = getActivity().openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString).append("\n");
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("read file activity", "File("+filename+") not found: " + e.toString());
        } catch (IOException e) {
            Log.e("read file activity", "Can not read file("+filename+"): " + e.toString());
        }
        return ret;
    }
}
