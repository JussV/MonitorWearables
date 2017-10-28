package smartlife.monitorwearables.service.volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Joana on 9/28/2017.
 */

public interface VolleyCallback {
    void onSuccess(JSONObject result) throws JSONException;
    void onFailure();
}
