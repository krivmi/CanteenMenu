package com.example.hiofweeklymenu;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logger {
    private static final String LOG_TAG = "MyWidgetProvider";
    private static final String LOG_FILE_NAME = "widget_log.txt";

    public static void log(Context context, String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String logMessage = timestamp + " - " + message + "\n";

        // Log to Logcat for real-time debugging
        Log.d(LOG_TAG, message);

        // Write to file
        writeToFile(context, logMessage);
    }

    public static void logError(Context context, String message, Exception e) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String logMessage = timestamp + " - ERROR: " + message + " - " + e.toString() + "\n";

        // Log to Logcat
        Log.e(LOG_TAG, message, e);

        // Write to file
        writeToFile(context, logMessage);
    }

    private static void writeToFile(Context context, String logMessage) {
        File logFile = new File(context.getFilesDir(), LOG_FILE_NAME);
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.append(logMessage);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to write log file", e);
        }
    }
}

