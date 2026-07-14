package com.tolu.cc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.widget.RemoteViews;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

public class TaskWidgetProvider extends AppWidgetProvider {
    static final String ACTION_COMPLETE = "com.tolu.cc.COMPLETE_TASK";
    static final String ACTION_NEXT = "com.tolu.cc.NEXT_TASK";
    static final String ACTION_ROTATE = "com.tolu.cc.ROTATE_TASK";
    static final String EXTRA_ID = "task_id";
    static final String EXTRA_TITLE = "task_title";

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        updateAll(context, manager, ids);
        scheduleRotation(context);
    }

    @Override public void onEnabled(Context context) {
        super.onEnabled(context);
        refreshAll(context);
        scheduleRotation(context);
    }

    @Override public void onDisabled(Context context) {
        super.onDisabled(context);
        cancelRotation(context);
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_COMPLETE.equals(action)) {
            String id = intent.getStringExtra(EXTRA_ID);
            String title = intent.getStringExtra(EXTRA_TITLE);
            if (id != null && id.length() > 0) {
                WidgetStore.complete(context, id, title == null ? id : title);
                WidgetStore.prefs(context).edit().putInt("cursor", 0).apply();
                Toast.makeText(context, "Completed — review it in Tolu's CC next time", Toast.LENGTH_LONG).show();
            }
            refreshAll(context);
        } else if (ACTION_NEXT.equals(action) || ACTION_ROTATE.equals(action)) {
            advance(context);
            refreshAll(context);
            scheduleRotation(context);
        }
    }

    static void refreshAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, TaskWidgetProvider.class));
        updateAll(context, manager, ids);
    }

    private static void updateAll(Context context, AppWidgetManager manager, int[] ids) {
        for (int widgetId : ids) manager.updateAppWidget(widgetId, build(context, widgetId));
    }

    private static RemoteViews build(Context context, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_safe);
        JSONArray tasks = WidgetStore.tasks(context);
        int total = tasks.length();
        int rawCursor = WidgetStore.prefs(context).getInt("cursor", 0);
        int cursor = total == 0 ? 0 : ((rawCursor % total) + total) % total;
        String id = "";
        String title = "Open Tolu's CC to load missions";
        if (total == 0) {
            views.setTextViewText(R.id.task_index, "00 / 00");
            views.setTextViewText(R.id.task_priority, "SYSTEM READY // 60 SEC ROTATION");
            views.setTextViewText(R.id.task_next, "Your live task list synchronises when the app opens.");
            views.setTextViewText(R.id.task_due, "OPEN APP TO SYNC");
            views.setTextColor(R.id.task_priority, Color.rgb(82, 247, 255));
        } else {
            JSONObject task = tasks.optJSONObject(cursor);
            if (task != null) {
                id = task.optString("id", "");
                title = task.optString("title", "Current mission");
                String priority = task.optString("priority", "Medium");
                views.setTextViewText(R.id.task_index, String.format(java.util.Locale.UK, "%02d / %02d", cursor + 1, total));
                views.setTextViewText(R.id.task_priority, priority.toUpperCase() + " // ACTIVE SIGNAL");
                views.setTextViewText(R.id.task_next, task.optString("next", "Open the app for the next action"));
                views.setTextViewText(R.id.task_due, "DUE // " + task.optString("due", "NO DATE").toUpperCase() + "   •   NEXT ROTATION 60S");
                views.setTextColor(R.id.task_priority, priorityColor(priority));
            }
        }
        views.setTextViewText(R.id.task_title, title);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        Intent open = new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        views.setOnClickPendingIntent(R.id.open_app, PendingIntent.getActivity(context, 91001 + widgetId, open, flags));
        Intent next = new Intent(context, TaskWidgetProvider.class).setAction(ACTION_NEXT);
        views.setOnClickPendingIntent(R.id.next_task, PendingIntent.getBroadcast(context, 92001 + widgetId, next, flags));
        if (total > 0) {
            Intent complete = new Intent(context, TaskWidgetProvider.class).setAction(ACTION_COMPLETE).putExtra(EXTRA_ID, id).putExtra(EXTRA_TITLE, title);
            views.setOnClickPendingIntent(R.id.complete_task, PendingIntent.getBroadcast(context, 93001 + id.hashCode(), complete, flags));
        } else {
            views.setOnClickPendingIntent(R.id.complete_task, PendingIntent.getActivity(context, 94001 + widgetId, open, flags));
        }
        return views;
    }

    private static void advance(Context context) {
        int cursor = WidgetStore.prefs(context).getInt("cursor", 0);
        WidgetStore.prefs(context).edit().putInt("cursor", cursor + 1).apply();
    }

    private static PendingIntent rotationIntent(Context context) {
        Intent rotate = new Intent(context, TaskWidgetProvider.class).setAction(ACTION_ROTATE);
        return PendingIntent.getBroadcast(context, 95001, rotate, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void scheduleRotation(Context context) {
        AlarmManager alarms = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (alarms != null) alarms.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 60000L, rotationIntent(context));
    }

    private static void cancelRotation(Context context) {
        AlarmManager alarms = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (alarms != null) alarms.cancel(rotationIntent(context));
    }

    private static int priorityColor(String priority) {
        if ("Critical".equalsIgnoreCase(priority)) return Color.rgb(255, 91, 69);
        if ("High".equalsIgnoreCase(priority)) return Color.rgb(255, 179, 31);
        if ("Low".equalsIgnoreCase(priority)) return Color.rgb(82, 247, 255);
        return Color.rgb(167, 123, 255);
    }
}
