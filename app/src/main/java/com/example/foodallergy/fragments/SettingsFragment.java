package com.example.foodallergy.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.foodallergy.R;
import com.example.foodallergy.UpdateAllergyActivity;
import com.example.foodallergy.utils.Helper;


public class SettingsFragment extends PreferenceFragmentCompat {

    Helper helper;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        getActivity().setTitle("Settings");
        helper = new Helper(getActivity());

        // Find the logout preference
        Preference logoutPreference = findPreference("logout");
        if (logoutPreference != null) {
            logoutPreference.setOnPreferenceClickListener(preference -> {
                // Perform logout operation here
                helper.displayClosingAlertBox(true);
                return true;
            });
        }

        Preference allergiesPreference = findPreference("allergies");
        if (allergiesPreference != null) {
            allergiesPreference.setOnPreferenceClickListener(preference -> {
                // Perform logout operation here
                startActivity(new Intent(getActivity(), UpdateAllergyActivity.class));
                return true;
            });
        }
    }
}