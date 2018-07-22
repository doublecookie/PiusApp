package jayjay.de.piusapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
            //TODO: das hier hat noch nie gut funktiioniert, deshalb wechsel zur Speicherung des Planes in Json
            String dokumentString = "error";
            if (htmlWrapper.success) {
                dokumentString = processData(Jsoup.parse(htmlWrapper.downloadData));
            }
            else {
                returnWrapper.success = false;
                returnWrapper.errorMessage = htmlWrapper.errorMessage;
            }

            if (!(dokumentString.equals("error") || dokumentString.equals(""))) {
                dokumentString = "°" + aktuelleZeit + "\n" + dokumentString;
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

    //ALte Methode TODO: Umschreiben zu Json
    String processData(Document doc){

        try {
            StringBuilder dokumentBuilder = new StringBuilder();

            Element tickerElement = doc.select("div>p").first(); //tickertext
            for (Element element : tickerElement.select("br")) {
                element.replaceWith(new TextNode("§", doc.baseUri()));
            }
            String tickerText = tickerElement.text().replace('§', '\n');


            //ersetze <br>
            for (Element element : doc.select("br")) {
                element.replaceWith(new TextNode("\n", doc.baseUri()));
            }
            //ersetze <s>
            for (Element element : doc.select("s")) {
                String StringErsetzerS = "^" + element.text() + "^";
                TextNode textNodeErsetzerS = new TextNode(StringErsetzerS, doc.baseUri());
                element.replaceWith(textNodeErsetzerS);
            }

            Elements VDaten = doc.select("table"); //Tages Tabellen

            //Alle Größen Herausfinden... #$@/ Java

            int anzahlTage = VDaten.size(); //Anzahl Tage

            String[] Datum = new String[anzahlTage];//Liste der Datums(._.) der Tabellen

            String Aktualisierung = ""; //Letzte Aktualisierung (keine Liste)

            String[] Betroffen = new String[anzahlTage];//Liste der Betroffenen Klassen

            int[] anzahlKlassen = new int[anzahlTage];


            int tagCounter = 0;

            for (Element tagDaten : VDaten) {
                Datum[tagCounter] = tagDaten.previousElementSibling().previousElementSibling().previousElementSibling().previousElementSibling().text();

                Betroffen[tagCounter] = tagDaten.previousElementSibling().text();

                Aktualisierung = tagDaten.previousElementSibling().previousElementSibling().previousElementSibling().text();

                anzahlKlassen[tagCounter] = tagDaten.select("th.links").size();

                tagCounter++;
            }

            for(String tickerline: tickerText.split("\\n")){
                dokumentBuilder.append("€"+tickerline);
                dokumentBuilder.append("\n");
            }
            dokumentBuilder.append("#");
            dokumentBuilder.append(Aktualisierung);
            dokumentBuilder.append("\n~");
            dokumentBuilder.append(anzahlTage);

            int i = 0;
            for (Element tagDaten : VDaten) {
                dokumentBuilder.append("\n_");
                dokumentBuilder.append("\n#");
                dokumentBuilder.append(Datum[i]);
                dokumentBuilder.append("\n#");
                dokumentBuilder.append(Betroffen[i]);
                dokumentBuilder.append("\n~");
                dokumentBuilder.append(anzahlKlassen[i]);

                for (Element klasse : tagDaten.select("th.links")) {
                    dokumentBuilder.append("\n*");
                    dokumentBuilder.append(klasse.text());
                    Element trElement = klasse.parent().nextElementSibling().nextElementSibling();
                    int anzahlVertretung = 0;
                    while (true) {
                        try {
                            if (trElement.child(0).hasClass("vertretung")) {
                                trElement = trElement.nextElementSibling();
                                anzahlVertretung++;
                            } else {
                                break;
                            }
                        } catch (Exception e) {
                            break;
                        }
                    }
                    dokumentBuilder.append("\n~");
                    dokumentBuilder.append(anzahlVertretung);
                    trElement = klasse.parent().nextElementSibling().nextElementSibling();
                    while (true) {
                        try {
                            if (trElement.child(0).hasClass("vertretung")||trElement.child(0).hasClass("vertretung neu")) {
                                dokumentBuilder.append("\n-");
                                for (Element tdVertretung : trElement.children()) {
                                    dokumentBuilder.append("\n$");
                                    if (tdVertretung.hasClass("vertretung neu")) {
                                        dokumentBuilder.append("%");
                                    }
                                    dokumentBuilder.append(tdVertretung.text());
                                }
                                trElement = trElement.nextElementSibling();
                            }
                            else {
                                try{
                                    while(trElement.child(1).hasClass("eva")) {
                                        dokumentBuilder.append("\n?");
                                        dokumentBuilder.append(trElement.child(2).text());
                                        trElement = trElement.nextElementSibling();
                                    }

                                }
                                catch(Exception e){
                                    break;
                                }
                            }

                        } catch (Exception e) {
                            Log.e("Asynktask: ", e.toString());
                            break;
                        }
                    }
                }

                i++;
            }
            dokumentBuilder.append("\n!");
            Log.v("Asynktask", "Daten verarbeitet");
            return dokumentBuilder.toString();
        }
        catch(Exception e){
            Log.e("AsyncTask:processData",e.toString());
            return "error";
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
        //Methode des übergebenen Interfaces wird aufgerufen und somit Wrapper an Activity oder Service zurückgegeben
        asyncTaskCompleteListener.onComplete(wrapper);
        super.onPostExecute(wrapper);
    }
}
