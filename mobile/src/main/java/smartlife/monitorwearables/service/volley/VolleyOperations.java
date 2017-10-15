package smartlife.monitorwearables.service.volley;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import smartlife.monitorwearables.Constants;
import smartlife.monitorwearables.entities.HeartRate;

/**
 * Created by Joana on 9/28/2017.
 */

public class VolleyOperations {

    public static final int STATUS_CODE_404 = 404;

    public static void getLastSyncDate(String uniquesPhoneId, final Context context, final VolleyCallback callback){

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, Constants.URL.concat(Constants.HEART_RATE_API).concat(Constants.LATEST_SYNC_DATE).concat("?upid=" + uniquesPhoneId), null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            callback.onSuccess(response);
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = getErrorMessage(error);
                        if(errorMsg == null)
                            callback.onFailure();
                        else {
                            Toast.makeText(context,errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        int socketTimeout = 30000; // 30 seconds.
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
     //   jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(jsObjRequest);
    }

    public static void bulkInsertHeartRates(final Context context, String uniquePhoneId, JSONArray heartRateList, final VolleyCallback callback){
        JSONObject jsObj = new JSONObject();
        try {
            jsObj.put("heartrates", heartRateList);
        } catch (JSONException ex){
            ex.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, Constants.URL.concat(Constants.HEART_RATE_API).concat(Constants.BULK), jsObj, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            callback.onSuccess(response);
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = getErrorMessage(error);
                        if(errorMsg == null)
                            callback.onFailure();
                        else {
                            Toast.makeText(context,errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        int socketTimeout = 30000; // 30 seconds.
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        //   jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(jsObjRequest);
    }

    public static String getErrorMessage(VolleyError volleyError){
            String message = null;
            if (volleyError instanceof NetworkError) {
                message = "Network error, please check the connection!";
            } else if (volleyError instanceof AuthFailureError) {
                message = "Authentication failure...Please check your connection!";
            } else if (volleyError instanceof ServerError && volleyError.networkResponse.statusCode == 413) {
                message = "HTTP Error: Request entity too large";
            } else if (volleyError instanceof NoConnectionError) {
                message = "No connection could be established!";
            } else if (volleyError instanceof TimeoutError) {
                message = "Connection TimeOut! Please check your internet connection.";
            }
            return message;
    }
}
