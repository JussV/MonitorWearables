package smartlife.monitorwearables.service.volley;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import smartlife.monitorwearables.Constants;
import smartlife.monitorwearables.activities.SignUpActivity;
import smartlife.monitorwearables.activities.SignUpCompletedActivity;
import smartlife.monitorwearables.db.HRMonitorLocalDBOperations;
import smartlife.monitorwearables.entities.User;
import smartlife.monitorwearables.impl.GBDevice;


public class VolleyOperations {

    public static final int STATUS_CODE_404 = 404;

    public static void getLastSyncDateByDeviceType(String uniquesPhoneId, int deviceTypeKey, final Context context, final VolleyCallback callback){

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, Constants.URL.concat(Constants.HEART_RATE_API).concat(Constants.LATEST_SYNC_DATE).concat("?upid=" + uniquesPhoneId + "&device=" + deviceTypeKey), null, new Response.Listener<JSONObject>() {

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

    public static void addDeviceToRemoteDb(JSONObject device, Context context){
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, Constants.URL.concat(Constants.DEVICE_API), device, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        int i = 0;
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        VolleySingleton.getInstance(context).addToRequestQueue(jsObjRequest);
    }

    public static void storeDeviceToRemoteDB(final GBDevice device, final Context context){
        JsonArrayRequest jsArrRequest = new JsonArrayRequest
                (Request.Method.GET, Constants.URL.concat(Constants.DEVICE_API), null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        // if gbDevice's key is not in remote db add it
                        List<Integer> remoteDeviceKeys = new ArrayList<Integer>();
                        for (int i = 0; i < response.length() ; i++) {
                            try {
                                JSONObject mJsonObject = (JSONObject)response.get(i);
                                remoteDeviceKeys.add((Integer)mJsonObject.get(Constants.DEVICE_KEY));
                            } catch (JSONException ex){
                                ex.printStackTrace();
                            }
                        }
                        if(remoteDeviceKeys.size() == 0 || !remoteDeviceKeys.contains(device.getType().getKey())){
                            JSONObject deviceToStore = new JSONObject();
                            try {
                                deviceToStore.put(Constants.DEVICE_KEY, device.getType().getKey());
                                deviceToStore.put(Constants.DEVICE_NAME, device.getName());
                                VolleyOperations.addDeviceToRemoteDb(deviceToStore, context);
                            } catch (JSONException ex){
                                ex.printStackTrace();
                            }

                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        VolleySingleton.getInstance(context).addToRequestQueue(jsArrRequest);
    }

    public static void createUser(final JSONObject userObj, final Context context){
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, Constants.URL.concat(Constants.USER_API), userObj, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //user has been successfully added to remote db
                        //create user in local db
                        try {
                            final User user = new User(userObj.getString("name"), userObj.getString("password"), userObj.getString("email"), userObj.getString("uniquePhoneId"));
                            long newRowId = HRMonitorLocalDBOperations.insertUser(context, user);
                            if(newRowId > 0){
                                Intent signUpCompletedIntent = new Intent(context, SignUpCompletedActivity.class);
                                signUpCompletedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(signUpCompletedIntent);
                            }
                        } catch (org.json.JSONException ex){
                            ex.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = getErrorMessage(error);
                        if(errorMsg != null){
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                        if(!error.networkResponse.notModified){
                            Toast.makeText(context, "User was not created. Probably the email address already exists in the cloud app.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        int socketTimeout = 30000; // 30 seconds.
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        VolleySingleton.getInstance(context).addToRequestQueue(jsObjRequest);
    }
}
