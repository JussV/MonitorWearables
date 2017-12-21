package smartlife.monitorwearables.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import smartlife.monitorwearables.R;
import smartlife.monitorwearables.entities.Device;
import smartlife.monitorwearables.service.volley.VolleyOperations;
import smartlife.monitorwearables.util.GB;
import smartlife.monitorwearables.util.ValidatorUtils;

public class SignUpActivity extends AppCompatActivity {
    private String TAG = "SingUp";
    private EditText email;
    private EditText username;
    private EditText password;
    private Button signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        email = findViewById(R.id.et_email);
        username = findViewById(R.id.et_username);
        password = findViewById(R.id.et_pass);
        signUp = findViewById(R.id.btn_signup);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailTxt = email.getText().toString();
                String passTxt = password.getText().toString();

                if(!isNetworkAvailable()){
                    GB.toast("Internet connection is required to sign up to the cloud app.",  Toast.LENGTH_LONG, GB.ERROR);
                } else {
                    if (ValidatorUtils.isEmpty(email)) {
                        email.setError("Email is required");
                    } else {
                        if (!ValidatorUtils.isValidEmail(emailTxt)) {
                            email.setError("Invalid Email");
                        }
                    }

                    if (ValidatorUtils.isEmpty(password)) {
                        password.setError("Password is required");
                    } else {
                        if (!ValidatorUtils.isValidPassword(passTxt)) {
                            password.setError("Password should be at least 8 characters long.");
                        }
                    }

                    if (ValidatorUtils.isEmpty(username)) {
                        username.setError("Username is required");
                    }

                    if(email.getError() == null && password.getError() == null && username.getError() == null){
                        JSONObject user = new JSONObject();
                        try {
                            final String uniquePhoneId = Device.getDeviceUniqueId(getApplicationContext());
                            user.put("name", username.getText().toString());
                            user.put("password", passTxt);
                            user.put("email", emailTxt);
                            user.put("uniquePhoneId", uniquePhoneId);
                            VolleyOperations.createUser(user, getApplicationContext());
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }
                }


            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
