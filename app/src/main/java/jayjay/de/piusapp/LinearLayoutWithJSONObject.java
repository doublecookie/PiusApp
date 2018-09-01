package jayjay.de.piusapp;

import android.content.Context;
import android.widget.LinearLayout;

import org.json.JSONObject;

class LinearLayoutWithJSONObject extends LinearLayout {

    private JSONObject jsonObject;

    public LinearLayoutWithJSONObject(Context context, JSONObject mJSONObject) {
        super(context);
        jsonObject = mJSONObject;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }
}
