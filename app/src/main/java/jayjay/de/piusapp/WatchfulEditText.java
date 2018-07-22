package jayjay.de.piusapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

//Subclass von EditText mit dem Zusatz dass es den Focus verliert wenn die Tastatur heruntergeklappt wird
//brauchte ich beim Login bei der StartActivity
public class WatchfulEditText extends android.support.v7.widget.AppCompatEditText {

    public WatchfulEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            clearFocus();
        }
        return super.onKeyPreIme(keyCode,event);
    }

}