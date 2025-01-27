package com.example.hiofweeklymenu;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.animation.Animation;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.view.View;
import android.view.animation.ScaleAnimation;

public class MyWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_REFRESH = "com.example.hiofweeklymenu.ACTION_REFRESH";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Log.d("MyWidgetProvider", "onUpdate triggered for AppWidgetIds:");

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
            views.setTextViewText(R.id.text_last_update, "");

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
            //Log.d("MyWidgetProvider", "Running");
            String[] menus = fetchMenus();
            updateWidget(menus);
        }

        private String[] fetchMenus() {
            String todayMenu = "Error fetching today's menu";
            String tomorrowMenu = "Error fetching tomorrow's menu";

            try {
                Document doc = Jsoup.connect("https://www.siost.hiof.no/diners/weekly-menu").get();
                // Extract text with line breaks (similar to soup.get_text(separator="\n"))
                StringBuilder textWithLineBreaks = new StringBuilder();
                for (Element element : doc.select("*")) { // Traverse all elements
                    if (!element.ownText().isEmpty()) { // If the element has its own text
                        textWithLineBreaks.append(element.ownText()).append("\n");
                    }
                }

                // Get the resulting text
                String text = textWithLineBreaks.toString();

                int startIndex = text.indexOf("Halden");
                int endIndex = text.indexOf("Fredrikstad", startIndex);

                if (startIndex != -1 && endIndex != -1) { // the Halden menu was found
                    String extractedText = text.substring(startIndex, endIndex + "Fredrikstad".length());
                    startIndex = extractedText.indexOf("Monday");
                    endIndex = extractedText.indexOf("Fredrikstad", startIndex);

                    if (startIndex != -1 && endIndex != -1) {
                        extractedText = extractedText.substring(startIndex, endIndex);
                        String[] lines = extractedText.split("\\n");
                        //Log.d("MyWidgetProvider", "LinesLength: " + extractedText.length() +"Lines: " + extractedText);

                        Calendar calendar = Calendar.getInstance();
                        int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
                        int tomorrowIndex = (todayIndex + 1) % 7;

                        //Log.d("MyWidgetProvider", "Today: " + todayIndex + ", Tommorow: " + tomorrowIndex);

                        if (lines.length > todayIndex * 2 + 1 && todayIndex >= 0 && todayIndex < 5)
                        {
                            todayMenu = lines[todayIndex * 2 + 1];
                        } else
                        {
                            todayMenu = "Weekend";
                        }

                        if (lines.length > tomorrowIndex * 2 + 1 && tomorrowIndex >= 0 && tomorrowIndex < 5) {
                            tomorrowMenu = lines[tomorrowIndex * 2 + 1];
                        } else if (tomorrowIndex == 5 || tomorrowIndex == 6) {
                            tomorrowMenu = "Weekend";
                        } else if (tomorrowIndex == 0 && lines.length > 1) {
                            tomorrowMenu = lines[1]; // Edge case: Monday menu on Sundays
                        } else {
                            tomorrowMenu = "No menu available";
                        }
                    }
                    else
                    {
                        todayMenu = "No menu available";
                        tomorrowMenu = "No menu available";
                    }
                }
                else
                {
                    todayMenu = "No menu available";
                    tomorrowMenu = "No menu available";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Log.d("MyWidgetProvider", "Menu: " + todayMenu + " " + tomorrowMenu);
            return new String[]{todayMenu, tomorrowMenu};
        }

        private void updateWidget(String[] menus) {
            //Log.d("MyWidgetProvider", "Updating");
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            setMenu(views, R.id.text_today_menu, "Today: ", menus[0]);
            setMenu(views, R.id.text_tomorrow_menu, "Tomorrow: ", menus[1]);
            //views.setTextViewText(R.id.text_today_menu,  "Today: " + menus[0]);
            //views.setTextViewText(R.id.text_tomorrow_menu, "Tomorrow: " + menus[1]);
            views.setTextViewText(R.id.text_last_update, formatDateTime(Calendar.getInstance().getTime()));

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            manager.updateAppWidget(new android.content.ComponentName(context, MyWidgetProvider.class), views);
        }

        private String formatDateTime(Date date) {
            // (e.g., "27/01, 20:16").
            return DateFormat.format("dd/MM, HH:mm", date).toString();
        }

        private void setMenu(RemoteViews views, int id, String prefix, String menu) {
            // 1. Create a SpannableStringBuilder.
            SpannableStringBuilder builder = new SpannableStringBuilder();

            // 2. Append the "Today: " or "Tomorrow: " prefix.
            builder.append(prefix);

            // 3. Apply bold style to the "Today: " text.
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            builder.setSpan(boldSpan, 0, prefix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // 4. Append the menu text.
            builder.append(menu);

            // 5. Set the text on the TextView.
            views.setTextViewText(id, builder);
        }
    }
}