package com.example.foodallergy.utils;

import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.android.volley.VolleyError;
import com.example.foodallergy.LoginActivity;
import com.example.foodallergy.R;
import com.example.foodallergy.model.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;

public class Helper {

    private Context mContext;

    public Helper(Context context) {
        mContext = context;
    }
    // Method to toggle password visibility
    public void togglePasswordVisibility(EditText password, ImageView togglePasswordButton) {
        int inputType = password.getInputType();
        if (inputType == 129) { // If password is currently visible, hide it
            password.setInputType(128);
            togglePasswordButton.setImageResource(R.drawable.ic_baseline_visibility_off_24);
            togglePasswordButton.setContentDescription(mContext.getString(R.string.hide_password));
        } else { // If password is currently hidden, show it
            password.setInputType(129);
            togglePasswordButton.setImageResource(R.drawable.ic_baseline_visibility_24);
            togglePasswordButton.setContentDescription(mContext.getString(R.string.show_password));
        }
        int cursorposition = password.getText().length();
        password.setSelection(cursorposition);
    }

    public void displayClosingAlertBox(boolean isLogoutAction){
        String msg = "Are you sure you want to exit?";
        if(isLogoutAction) msg = msg.replace("exit", "logout");
        new AlertDialog.Builder(mContext)
                //.setIcon(android.R.drawable.star_on)
                .setTitle("Exiting the Application")
                .setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "ON STOP: YES");
                        if (isLogoutAction == true) {
                            SharedPreferences preferences = mContext.getSharedPreferences("preferences", MODE_PRIVATE);
                            preferences.edit().clear().commit();
                            mContext.startActivity(new Intent(mContext, LoginActivity.class));
                        } else {
                            if (mContext instanceof Activity) {
                                ((Activity) mContext).finishAffinity();
                            }
                        }
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            Log.d("PARSE VOLLEY ERROR", "=== "+ responseBody);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                ApiErrorResponse response = objectMapper.readValue(responseBody, ApiErrorResponse.class);
                Toast.makeText(mContext, response.getMessage(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException errorr) {
        }
    }
}
