package smartlife.monitorwearables.service.jobs;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import smartlife.monitorwearables.Constants;
import smartlife.monitorwearables.db.HRMonitorContract;
import smartlife.monitorwearables.db.HRMonitorLocalDBOperations;
import smartlife.monitorwearables.entities.Device;
import smartlife.monitorwearables.entities.HeartRate;
import smartlife.monitorwearables.model.DeviceType;
import smartlife.monitorwearables.service.volley.VolleyCallback;
import smartlife.monitorwearables.service.volley.VolleyOperations;
import smartlife.monitorwearables.util.AndroidUtils;

public class DeleteDBJobService extends JobService {
    private static AsyncTask mBackgroundTask;
    private int deletedRows;
    private CountDownLatch latch;

    public DeleteDBJobService(){
         latch = new CountDownLatch(1);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(final JobParameters job) {
         mBackgroundTask = new AsyncTask() {
            @Override
            protected Integer doInBackground(Object[] objects) {
                final String uniquePhoneId = Device.getDeviceUniqueId(getApplicationContext());
                //delete records by deviceType
                int[] deviceTypeKeys = {DeviceType.MIBAND2.getKey(), DeviceType.ANDROIDWEAR_MOTO360SPORT.getKey()};

                for(final int deviceKey: deviceTypeKeys){
                    VolleyOperations.getLastSyncDateByDeviceType(uniquePhoneId, deviceKey, getApplicationContext(), new VolleyCallback(){
                        @Override
                        public void onSuccess(JSONObject result) throws JSONException {
                            if(result!=null && result.get("date") != null){
                                String lastSyncDate = (String)result.get("date");
                                // mongo date is not milisecond precise, therefore in order to escape inserting the same value twice we add 999 miliseconds to lastSyncDate
                                lastSyncDate = lastSyncDate.replace(lastSyncDate.subSequence(lastSyncDate.length()-4, lastSyncDate.length()-1), "999");
                                final Date lastSynchronizationDate = new Date(AndroidUtils.parseMongoDateToLocal(lastSyncDate));
                                // heart rate measurements exist for the specified date
                                ArrayList<HeartRate> heartRateListByLastSyncDate = HRMonitorLocalDBOperations.selectHeartRatesByDateAndDevice(getApplicationContext(),
                                        HRMonitorContract.HeartRate.COLUMN_CREATED_AT + " > ? AND " + HRMonitorContract.HeartRate.COLUMN_DEVICE_TYPE_KEY + "=?",  new String[] { String.valueOf(AndroidUtils.parseMongoDateToLocal(lastSyncDate)), String.valueOf(deviceKey) }, "createdAt ASC");
                                JSONArray mJSONArray = new JSONArray();
                                for (HeartRate heartRate : heartRateListByLastSyncDate) {
                                    JSONObject hrObj = new JSONObject();
                                    try {
                                        hrObj.put(Constants.HR_COLUMN_DATE, heartRate.getCreatedAt());
                                        hrObj.put(Constants.HR_COLUMN_VALUE, heartRate.getValue());
                                        hrObj.put(Constants.HR_COLUMN_UPID, uniquePhoneId);
                                        // TODO: deviceType should be taken from the device
                                        hrObj.put(Constants.HR_COLUMN_DEVICE, deviceKey);
                                        mJSONArray.put(hrObj);
                                    }catch (JSONException ex){
                                        ex.printStackTrace();
                                    }
                                }
                                Log.d("debug", String.valueOf(mJSONArray.length()));

                                VolleyOperations.bulkInsertHeartRates(getApplicationContext(), uniquePhoneId, mJSONArray, new VolleyCallback() {
                                    @Override
                                    public void onSuccess(JSONObject result) throws JSONException {
                                        //delete records from local db except from the last two days
                                        deletedRows = HRMonitorLocalDBOperations.removeHeartRatesExceptFromLastTwoDays(getApplicationContext());
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onFailure(){

                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(){
                            // no heart rate measurements exist for the specific uniquePhoneId
                            ArrayList<HeartRate> heartRateList = HRMonitorLocalDBOperations.selectHeartRatesByDateAndDevice(getApplicationContext(), HRMonitorContract.HeartRate.COLUMN_DEVICE_TYPE_KEY + "=?", new String[]{ String.valueOf(deviceKey)}, "createdAt ASC");
                            JSONArray mJSONArray = new JSONArray();
                            for (HeartRate heartRate : heartRateList) {
                                JSONObject hrObj = new JSONObject();
                                try {
                                    hrObj.put(Constants.HR_COLUMN_DATE, heartRate.getCreatedAt());
                                    hrObj.put(Constants.HR_COLUMN_VALUE, heartRate.getValue());
                                    hrObj.put(Constants.HR_COLUMN_UPID, uniquePhoneId);
                                    // TODO: deviceType should be taken from the device
                                    hrObj.put(Constants.HR_COLUMN_DEVICE, DeviceType.MIBAND2.getKey());
                                    mJSONArray.put(hrObj);
                                }catch (JSONException ex){
                                    ex.printStackTrace();
                                }
                            }
                            Log.d("debug", String.valueOf(mJSONArray.length()));

                            VolleyOperations.bulkInsertHeartRates(getApplicationContext(), uniquePhoneId, mJSONArray, new VolleyCallback() {
                                @Override
                                public void onSuccess(JSONObject result) throws JSONException {
                                    //delete records from local db except from the last two days
                                    deletedRows = HRMonitorLocalDBOperations.removeHeartRatesExceptFromLastTwoDays(getApplicationContext());
                                    latch.countDown();
                                }

                                @Override
                                public void onFailure(){
                                }
                            });
                        }

                    });
                    try {
                        latch.await();
                    } catch (InterruptedException ex){
                        ex.printStackTrace();
                    }

                }

                return  deletedRows;
            }

            @Override
            protected void onPostExecute(Object o) {
                /* false means, that job is done. we don't want to reschedule it*/
                jobFinished(job, true);
            }
        };

        mBackgroundTask.execute();
        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; // Answers the question: "Should this job be retried?"
    }
}
