package com.example.foodallergy;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.foodallergy.config.ApplicationConfig;
import com.example.foodallergy.model.AllergyRequest;
import com.example.foodallergy.model.LoginRequest;
import com.example.foodallergy.utils.Helper;
import com.example.foodallergy.utils.VolleySingleton;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateAllergyActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private String[] substrings;
    private String allergies;
    private RequestQueue requestQueue;
    private FloatingActionButton submitFab;
    private EditText inputAllergy;
    private Helper helper;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_allergy);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Allergy list");
        actionBar.setDisplayHomeAsUpEnabled(true);

        requestQueue = VolleySingleton.getmInstance(this).getRequestQueue();
        linearLayout = findViewById(R.id.relativeLayout);
        inputAllergy = findViewById(R.id.inputAllergyText);
        submitFab = findViewById(R.id.submitfab);
        helper = new Helper(UpdateAllergyActivity.this);

        SharedPreferences preferences = getSharedPreferences(ApplicationConfig.APP_PREFERENCE_NAME, MODE_PRIVATE);
        allergies = preferences.getString("allergies", "");
        token = preferences.getString("token", "");

        // Split the comma-separated string of allergies into an array of substrings
        substrings = allergies.split(",");


        if (!substrings[0].trim().isEmpty()) {
            for (int i = 0; i < substrings.length; i++) {
                // Create a new TextView
                TextView textView = new TextView(this);

                //Set background with border radius
                textView.setBackgroundResource(R.drawable.background_done_button);

                // Set text and background color for the TextView
                textView.setText(substrings[i].trim());
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        inputAllergy.setText(allergies);
                    }
                });

                // Set left margin for items after the first one
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                if (i > 0) {
                    params.setMargins(20, 0, 0, 0); // left, top, right, bottom
                }
                /*else {
                    params.setMargins(0, 0, 0, 0); // left, top, right, bottom
                }*/
                textView.setLayoutParams(params);
                textView.setPadding(35, 20, 35, 20);

                // Add the TextView to your layout
                linearLayout.addView(textView);
            }
        }

        submitFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });;
    }

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

    public void submit() {
        if (inputAllergy.getText().toString().isEmpty()) {
            Toast.makeText(UpdateAllergyActivity.this, "New allergies to be added is expected", Toast.LENGTH_SHORT).show();
        }
        else {
            String[] allergyInput = inputAllergy.getText().toString().split(",");

            String elementsNotInAllergyInput = Arrays.stream(allergyInput)
                    .filter(element -> !Arrays.asList(substrings).contains(element.trim().toLowerCase()))
                    .collect(Collectors.joining(",")).replaceAll("\\s*,\\s*", ",").toLowerCase();

            String preparedAllergyString;
            if (allergies.isEmpty()) {
                preparedAllergyString = elementsNotInAllergyInput;
            } else {
                preparedAllergyString = allergies.concat(",").concat(elementsNotInAllergyInput);
            }

            Log.d("INPUT DIFFERENCE", "Input difference: " + preparedAllergyString);
            //Initializing progress  indicator
            ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Updating allergy list...");
            mDialog.show();

            //Building request
            AllergyRequest request = new AllergyRequest();
            request.setAllergies(preparedAllergyString);

            //Converting request to json string
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = null;
            try {
                jsonStr = Obj.writeValueAsString(request);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            //Making API call
            String url = ApplicationConfig.BASE_URL.concat("update_allergies");
            JSONObject jsonBody = null;
            try {
                jsonBody = new JSONObject(jsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            mDialog.dismiss();
                            try {
                                if (response.getString("code").equals("SUCCESS")) {
                                    SharedPreferences preferences = getSharedPreferences(ApplicationConfig.APP_PREFERENCE_NAME, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("allergies", preparedAllergyString);
                                    //startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    Toast.makeText(UpdateAllergyActivity.this, "Allergy list updated", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
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
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };

            this.requestQueue.add(jsonObjectRequest);
        }

    }
}