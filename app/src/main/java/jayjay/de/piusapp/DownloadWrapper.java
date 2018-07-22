package jayjay.de.piusapp;

//Object, welches beim AsyncTask DownloadData benutzt wird, um diese drei Werte übergeben zu können, wenn man nur einen Wert übergeben darf
public class DownloadWrapper {
    public String downloadData;
    public boolean success;
    public String errorMessage;
}