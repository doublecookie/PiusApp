package jayjay.de.piusapp;

import org.json.JSONArray;

//Interface um KurseFragment und den Dialog/ die PDF&Kamera Activity zu verbinden
//Die Activity oder der Service erstellt Object von KursSelectionCompleteListener und executet dann den,
//wobei es den Listener übergibt
//Zum Schluss werden sie über das Interface zurückgegeben
public interface KursSelectionCompleteListener {
    void onComplete(JSONArray neueKurse);
}
