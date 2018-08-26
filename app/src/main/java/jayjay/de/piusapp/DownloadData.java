package jayjay.de.piusapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

//AsyncTask zum herunterladen von Daten
public class DownloadData extends AsyncTask<Void ,Void ,DownloadWrapper> {

    //Attribute
    private Context mContext;
    private AsyncTaskCompleteListener asyncTaskCompleteListener;
    private boolean mOnlyCheckConnection;

    HttpURLConnection httpURLConnection = null;
    BufferedReader bufferedReader = null;

    //Shared Preferences Objecte um auf Einstellungen und Login zuzugreifen
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    //erster Konstruktor
    DownloadData(Context context, AsyncTaskCompleteListener listener){
        //Speichern der ÜbergabeParameter in Attributen
        mContext = context;
        asyncTaskCompleteListener = listener;
        mOnlyCheckConnection = false; //keine Übergabe im Konstruktor --> Default: false

        //SharedPreference Objecte initialisieren
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = preferences.edit();
    }

    //zweiter Konstruktor, falls man nur checken möchte ob Login funktioniert
    DownloadData(Context context, AsyncTaskCompleteListener listener, boolean onlyCheckConnection){
        //Speichern der ÜbergabeParameter in Attributen
        mContext = context;
        asyncTaskCompleteListener = listener;
        mOnlyCheckConnection = onlyCheckConnection;

        //SharedPreference Objecte initialisieren
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = preferences.edit();
    }

    //als erstes ausgeführte Methode
    @Override
    protected DownloadWrapper doInBackground(Void... params) {
        Log.v("DwonloadData AsyncTask", "starte do in Background");

        //login daten aus SharedPreferences
        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);

        //Log.v("DownloadData:Password:","username:"+username+":password:"+password);

        String aktuelleZeit = System.currentTimeMillis() + "";

        String strURL = "http://pius-gymnasium.de/vertretungsplan/";

        //Object des DownloadWrappers welches ganz am Ende zurückgegeben wird
        DownloadWrapper returnWrapper = new DownloadWrapper();
        returnWrapper.downloadData = "";
        returnWrapper.success = false;

        //Object des DownloadWrappers für den Download des HTML Codes
        DownloadWrapper htmlWrapper = new DownloadWrapper();
        htmlWrapper.success = false;

        try {
            URL url = new URL(strURL);
            htmlWrapper = loadHtmlCode(username, password, url); //htmlWrapper wird gefüllt mit Methode loadHtmlCode
        } catch (Exception e) {
            Log.e("DownloadData:loadHtml", e.toString());
        }

        //Ob Html Code noch umgewandelt werden soll oder nicht
        if(mOnlyCheckConnection){
            returnWrapper.downloadData = htmlWrapper.downloadData;
            returnWrapper.success = htmlWrapper.success;
            return returnWrapper;
        }else{
            String dokumentString = "error";
            if (htmlWrapper.success) {
                dokumentString = processData(Jsoup.parse(htmlWrapper.downloadData));
            }
            else {
                returnWrapper.success = false;
                returnWrapper.errorMessage = htmlWrapper.errorMessage;
            }

            if (!(dokumentString.equals("error") || dokumentString.equals(""))) {
                returnWrapper.downloadData = dokumentString;
                returnWrapper.success = true;
            }
            return returnWrapper; //Beenden des AsyncTasks in PostExecute
        }
    }

    DownloadWrapper loadHtmlCode(final String strUserId, final String strPassword, URL url) {

        //neuer Wrapper mit Standart Werten
        DownloadWrapper wrapper = new DownloadWrapper();
        wrapper.downloadData = "";
        wrapper.success = true;
        wrapper.errorMessage = mContext.getString(R.string.vertretungs_error);

        //Handler der nach 60 Sekunden Download abbricht --> um Android Endlos Lade Bug zu verhindern
        Handler abbruchLadeHandler = new Handler(Looper.getMainLooper());
        abbruchLadeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cancel();
            }
        }, 60000);

        try {

            Log.v("DownloadData","Aufbau der Verbindung");
            // Aufbau der Verbindung
            httpURLConnection = (HttpURLConnection) url.openConnection();

            //Passwort und Benutzername Eingabe
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(strUserId, strPassword.toCharArray());
                }
            });

            //InputStream von Html Code
            InputStream inputStream = httpURLConnection.getInputStream();

            //Wenn kein Code dann fehlgeschlagen
            if (inputStream == null) {
                wrapper.success = false;
            }

            //Abspeichern des Codes in STring
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            StringBuilder downloadBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                downloadBuilder.append(line).append("\n");
                //Log.v("MainActivity", "Line: " + line);
            }
            String downloadStr = downloadBuilder.toString();
            if (downloadStr.length() == 0) { //wenn String leer, dann fehlgeschlagen
                wrapper.success = false;
            }

            //Abbruch Handler kann beendet werden( dann wird er nicht ausgeführt), da wenn Programm
            // an diesem Punkt angelangt dann kein Endlos Lade Bug
            abbruchLadeHandler.removeCallbacksAndMessages(null);
            Log.v("Asynktask", "Daten geladen");

            wrapper.downloadData = downloadStr;
        }
        catch (IOException e) {
            Log.e("DownloadData:loadhtml", "ConnectionError: " + e.toString());
                wrapper.success = false;
                wrapper.errorMessage = mContext.getString(R.string.vertretungs_error);
        }
        finally {
            //disconnecten
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (final IOException e) {
                    Log.e("DownloadData", "Error closing stream", e);
                }
            }
        }

        return wrapper;
    }

    String processData(Document doc){

        try {
            JSONObject vertretungsPlan = new JSONObject();

            //ersetze <br>
            for (Element element : doc.select("br")) {
                element.replaceWith(new TextNode("\n"));
            }
            //ersetze <s>
            for (Element element : doc.select("s")) {
                String StringErsetzerS = "^" + element.text() + "^";
                TextNode textNodeErsetzerS = new TextNode(StringErsetzerS);
                element.replaceWith(textNodeErsetzerS);
            }

            Element tickerElement = doc.select("div>p").first(); //tickertext
            //element.replaceWith(new TextNode("§", doc.baseUri()));
            //String tickerText = tickerElement.text().replace('§', '\n');
            vertretungsPlan.put("ticker", tickerElement.text());

            Elements VDaten = doc.select("table"); //Tages Tabellen
            JSONArray tage = new JSONArray();

            for (Element tagDaten : VDaten) {
                JSONObject tag = new JSONObject();

                tag.put("tag", tagDaten.previousElementSibling().previousElementSibling().previousElementSibling().previousElementSibling().text());
                tag.put("letzteAktualisierung", tagDaten.previousElementSibling().previousElementSibling().previousElementSibling().text());
                tag.put("betroffen", tagDaten.previousElementSibling().text());

                JSONArray klassen = new JSONArray();

                for(Element klassenDaten : tagDaten.select("th.links")){
                    JSONObject klasse = new JSONObject();

                    klasse.put("klasse", klassenDaten.text());

                    JSONArray vertretungen = new JSONArray();

                    Element trElement = klassenDaten.parent().nextElementSibling().nextElementSibling();
                    while (true) {
                        try {
                            if (trElement.child(0).hasClass("vertretung")||trElement.child(0).hasClass("vertretung neu")) {

                                for (Element tdVertretung : trElement.children()) {
                                    if (tdVertretung.hasClass("vertretung neu")) {
                                        tdVertretung.text("%"+tdVertretung.text());
                                    }
                                }

                                JSONObject vertretung = new JSONObject();

                                vertretung.put("stunden", trElement.child(0).text());
                                vertretung.put("art", trElement.child(1).text());
                                vertretung.put("kurs", trElement.child(2).text());
                                vertretung.put("raum", trElement.child(3).text());
                                vertretung.put("lehrerAktuell", trElement.child(4).text());
                                vertretung.put("lehrerPlan", trElement.child(5).text());
                                vertretung.put("bemerkung", trElement.child(6).text());

                                JSONArray evas = new JSONArray();

                                try{
                                    while(trElement.child(1).hasClass("eva")) {
                                        evas.put(trElement.child(2).text());
                                        trElement = trElement.nextElementSibling();
                                    }
                                }
                                catch(Exception e){
                                    break;
                                }

                                vertretung.put("eva", evas);

                                vertretungen.put(vertretung);

                                trElement = trElement.nextElementSibling();
                            }
                            else {
                                break;
                            }

                        } catch (Exception e) {
                            Log.e("Asynktask: ", e.toString());
                            break;
                        }
                    }

                    klasse.put("vertretungen", vertretungen);

                    klassen.put(klasse);
                }

                tag.put("klassen", klassen);

                tage.put(tag);
            }
            vertretungsPlan.put("tage", tage);

            return vertretungsPlan.toString();
        }
        catch(Exception e){
            Log.e("AsyncTask:processData",e.toString());
            return "error";
        }
    }

    public void writeToFile(String data,String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mContext.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            Log.v("Success","Wrote to "+filename);
        }
        catch (IOException e) {
            Log.e("Exception", "File("+filename+") write failed: " + e.toString());
        }
    }

    private void cancel(){
        Log.v("DownloadData","cancelled");
        try{
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }catch(Exception e){Log.e("cancelDownloadData",e.toString());}
    }

    @Override
    protected void onCancelled() {
        cancel();
        super.onCancelled();
    }

    //wird aufgerufen am Ende
    @Override
    protected void onPostExecute(DownloadWrapper wrapper) {
        if(!mOnlyCheckConnection) writeToFile(wrapper.downloadData, mContext.getString(R.string.vertretungs_filename));
        //Log.v("vertretungsDaten", wrapper.downloadData);
        //Methode des übergebenen Interfaces wird aufgerufen und somit Wrapper an Activity oder Service zurückgegeben
        asyncTaskCompleteListener.onComplete(wrapper);
        super.onPostExecute(wrapper);
    }
}
