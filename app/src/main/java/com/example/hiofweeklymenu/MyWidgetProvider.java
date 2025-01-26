package com.example.hiofweeklymenu;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class MyWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_REFRESH = "com.example.hiofweeklymenu.ACTION_REFRESH";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("MyWidgetProvider", "onUpdate triggered for AppWidgetIds:");

        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, MyWidgetProvider.class);
            intent.setAction(ACTION_REFRESH);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setOnClickPendingIntent(R.id.button_refresh, pendingIntent);
            views.setTextViewText(R.id.text_today_menu, "Press refresh to load menu");
            views.setTextViewText(R.id.text_tomorrow_menu, "Press refresh to load menu");
            views.setTextViewText(R.id.text_week_number, "Week: " + getWeekNumber());

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private int getWeekNumber() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction())) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(new FetchMenuTask(context));
            executor.shutdown();
        }
    }

    private static class FetchMenuTask implements Runnable {
        private final Context context;

        public FetchMenuTask(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            Log.d("MyWidgetProvider", "Running");
            String[] menus = fetchMenus();
            updateWidget(menus);
        }

        private String[] fetchMenus() {
            String todayMenu = "Error fetching today's menu";
            String tomorrowMenu = "Error fetching tomorrow's menu";

            try {
                Document doc = Jsoup.connect("https://www.siost.hiof.no/diners/weekly-menu").get();
                String text = doc.text();
                int startIndex = text.indexOf("Halden");
                int endIndex = text.indexOf("Fredrikstad", startIndex);

                if (startIndex != -1 && endIndex != -1) {
                    String extractedText = text.substring(startIndex, endIndex + "Fredrikstad".length());
                    String[] lines = extractedText.split("\\n");

                    Calendar calendar = Calendar.getInstance();
                    int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
                    int tomorrowIndex = (todayIndex + 1) % 7;

                    if (lines.length > todayIndex * 2 + 1 && todayIndex >= 0 && todayIndex < 5) {
                        todayMenu = lines[todayIndex * 2 + 1];
                    } else {
                        todayMenu = "Weekend";
                    }

                    if (lines.length > tomorrowIndex * 2 + 1 && tomorrowIndex >= 0 && tomorrowIndex < 5) {
                        tomorrowMenu = lines[tomorrowIndex * 2 + 1];
                    } else if (tomorrowIndex == 5) {
                        tomorrowMenu = "Weekend";
                    } else if (tomorrowIndex == 6 && lines.length > 1) {
                        tomorrowMenu = lines[1]; // Edge case: Monday menu on Sundays
                    } else {
                        tomorrowMenu = "No menu available";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new String[]{todayMenu, tomorrowMenu};
        }

        private void updateWidget(String[] menus) {
            Log.d("MyWidgetProvider", "Updating");
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setTextViewText(R.id.text_today_menu, "Today's Menu: " + menus[0]);
            views.setTextViewText(R.id.text_tomorrow_menu, "Tomorrow's Menu: " + menus[1]);

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            manager.updateAppWidget(new android.content.ComponentName(context, MyWidgetProvider.class), views);
        }
    }
}