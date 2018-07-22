package jayjay.de.piusapp;

//Interface um AsyncTask DownloadData und die ausführende Activity zu verbinden
//Die Activity oder der Service erstellt Object von AsyncTaskCompleteListener und executet dann den,
//wobei es den Listener übergibt
//Wenn dann die Daten heruntergeladen sind werden sie über das Interface zurückgegeben
public interface AsyncTaskCompleteListener {
    void onComplete(DownloadWrapper data);
}
