package com.example.foodallergy;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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
import com.example.foodallergy.model.User;
import com.example.foodallergy.utils.Helper;
import com.example.foodallergy.utils.VolleySingleton;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateActivity extends AppCompatActivity {

    private TextView signInLink;
    private Button createAccountButton;
    private RequestQueue requestQueue;
    private EditText name;
    private EditText email;
    private EditText password;
    private ImageButton togglePasswordButton;
    private Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        actionBar.setDisplayHomeAsUpEnabled(true);

        signInLink = findViewById(R.id.signInLink);
        createAccountButton = findViewById(R.id.createAccountBtn);
        name = findViewById(R.id.fullname);
        email = findViewById(R.id.createEmail);
        password = findViewById(R.id.createPassword);
        togglePasswordButton = findViewById(R.id.togglePasswordButton);

        requestQueue = VolleySingleton.getmInstance(this).getRequestQueue();
        helper = new Helper(CreateActivity.this);

        // Set click listener for the toggle password button
        togglePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.togglePasswordVisibility(password, togglePasswordButton);
            }
        });

        signInLink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(CreateActivity.this, LoginActivity.class));
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    public void createAccount() {
        //Initializing progress  indicator
        ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setMessage("Creating account...");
        mDialog.show();

        //Building request
        User user = new User();
        user.setFullname(name.getText().toString());
        user.setEmail(email.getText().toString());
        user.setPassword(password.getText().toString());

        //Converting request to json string
        ObjectMapper Obj = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = Obj.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        //Making API call
        String  url = ApplicationConfig.BASE_URL.concat("signup");
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
                            if(response.getString("code").equals("SUCCESS")) {
                                Toast.makeText(CreateActivity.this, ApplicationConfig.ACCOUNT_CREATION_SUCCESS_MSG, Toast.LENGTH_LONG).show();
                                startActivity(new Intent(CreateActivity.this, LoginActivity.class));

                            }
                            else {
                                Toast.makeText(CreateActivity.this, "An error created occurred while creating account", Toast.LENGTH_LONG).show();
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

    // Inside your activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // Handle the back arrow click
                onBackPressed(); // Or call finish() to navigate back
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}