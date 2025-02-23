package com.example.hiofweeklymenu;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;


public class SettingsActivity extends Activity
{
    public static final String PREFS_NAME = "WidgetPrefs";
    public static final String KEY_CANTEEN_NAME = "canteen_name";
    public static final String KEY_COLOR_MODE = "color_mode";

    public static final int DARK = 0;
    public static final int LIGHT = 1;

    public static final int HALDEN = 0;
    public static final int FREDRIKSTAD = 1;

    private Button buttonHalden;
    private Button buttonFredrikstad;
    private Button buttonDark;
    private Button buttonLight;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        buttonHalden = findViewById(R.id.button_halden);
        buttonFredrikstad = findViewById(R.id.button_fredrikstad);
        buttonDark = findViewById(R.id.button_dark);
        buttonLight = findViewById(R.id.button_light);

        // Load saved preferences
        int selectedCanteen = preferences.getInt(KEY_CANTEEN_NAME, HALDEN);
        int selectedColorMode = preferences.getInt(KEY_COLOR_MODE, DARK);

        // Set initial UI state
        updateCanteenSelection(selectedCanteen);
        updateModeSelection(selectedColorMode);

        // Canteen selection
        buttonHalden.setOnClickListener(v -> selectCanteen(HALDEN));
        buttonFredrikstad.setOnClickListener(v -> selectCanteen(FREDRIKSTAD));

        // Mode selection
        buttonDark.setOnClickListener(v -> selectMode(DARK));
        buttonLight.setOnClickListener(v -> selectMode(LIGHT));
    }

    private void selectCanteen(int canteen)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_CANTEEN_NAME, canteen);
        editor.apply();

        updateCanteenSelection(canteen);
    }

    private void selectMode(int mode)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_COLOR_MODE, mode);
        editor.apply();

        updateModeSelection(mode);
    }

    private void updateCanteenSelection(int selectedCanteen)
    {
        if (selectedCanteen == HALDEN)
        {
            buttonHalden.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.lavender));
            buttonFredrikstad.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_border));
        }
        else
        {
            buttonHalden.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_border));
            buttonFredrikstad.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.lavender));
        }
    }

    private void updateModeSelection(int mode)
    {
        if (mode == DARK)
        {
            buttonDark.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.lavender));
            buttonLight.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_border));
        }
        else
        {
            buttonDark.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_border));
            buttonLight.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.lavender));
        }
    }
}
