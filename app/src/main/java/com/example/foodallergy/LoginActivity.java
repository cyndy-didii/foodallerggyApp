package com.example.foodallergy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.foodallergy.config.ApplicationConfig;
import com.example.foodallergy.model.LoginRequest;
import com.example.foodallergy.utils.Helper;
import com.example.foodallergy.utils.VolleySingleton;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private TextView createAccount;
    private RequestQueue requestQueue;
    private EditText email;
    private EditText password;
    private ImageButton togglePasswordButton;
    private Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginBtn);
        createAccount = findViewById(R.id.createAccountLink);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        togglePasswordButton = findViewById(R.id.togglePasswordButton);

        requestQueue = VolleySingleton.getmInstance(this).getRequestQueue();
        helper = new Helper(LoginActivity.this);

        // Set click listener for the toggle password button
        togglePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.togglePasswordVisibility(password, togglePasswordButton);
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,CreateActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doLogin();
            }
        });
    }

    // Method to login and authenticate users
    public void doLogin() {
        //Validate inputs
        if(email.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
            Toast.makeText(LoginActivity.this, "Email and password fields are mandatory", Toast.LENGTH_SHORT).show();
        }
        else {
            //Initializing progress  indicator
            ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Signing in...");
            mDialog.show();

            //Building request
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail(email.getText().toString());
            loginRequest.setPassword(password.getText().toString());

            //Converting request to json string
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = null;
            try {
                jsonStr = Obj.writeValueAsString(loginRequest);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            //Making API call
            String url = ApplicationConfig.BASE_URL.concat("login");
            JSONObject jsonBody = null;
            try {
                jsonBody = new JSONObject(jsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            mDialog.dismiss();
                            try {
                                if (response.getString("code").equals("SUCCESS")) {
                                    SharedPreferences preferences = getSharedPreferences(ApplicationConfig.APP_PREFERENCE_NAME, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("token", response.getString("access_token"));
                                    JSONObject user = response.getJSONObject("user");
                                    editor.putBoolean("IS_LOGIN", true);
                                    editor.putInt("user_id", user.getInt("id"));
                                    editor.putString("full_name", user.getString("fullname"));
                                    editor.putString("email", user.getString("email"));
                                    editor.putString("allergies", user.getString("allergies"));
                                    editor.commit();
                                    email.setText("");
                                    password.setText("");
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                } else {
                                    Toast.makeText(LoginActivity.this, "Invalid username/password combination", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mDialog.dismiss();
                    helper.parseVolleyError(error);
                }
            });

            this.requestQueue.add(jsonObjectRequest);
        }
    }
}