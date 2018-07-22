package jayjay.de.piusapp;


import java.util.concurrent.atomic.AtomicInteger;

//Atomic Integer es gibt aber bestimmt bessere Möglichkeiten, wird also wahrscheinlich noch geändert
//TODO ändern
public class NotificationID {
    private final static AtomicInteger c = new AtomicInteger(0);
    public static int getID() {
        return c.incrementAndGet();
    }
}