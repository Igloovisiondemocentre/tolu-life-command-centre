package com.tolu.cc;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

public class TaskWidgetProvider extends AppWidgetProvider {
    static final String ACTION_COMPLETE = "com.tolu.cc.COMPLETE_TASK";
    static final String ACTION_NEXT = "com.tolu.cc.NEXT_TASK";
    static final String EXTRA_ID = "task_id";
    static final String EXTRA_TITLE = "task_title";

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) manager.updateAppWidget(id, build(context, id));
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_COMPLETE.equals(intent.getAction())) {
            String id = intent.getStringExtra(EXTRA_ID);
            String title = intent.getStringExtra(EXTRA_TITLE);
            if (id != null) {
                WidgetStore.complete(context, id, title == null ? id : title);
                Toast.makeText(context, "Completed — review it in Tolu's CC next time", Toast.LENGTH_LONG).show();
                refreshAll(context);
            }
        } else if (ACTION_NEXT.equals(intent.getAction())) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, TaskWidgetProvider.class));
            for (int widgetId : ids) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_root);
                views.showNext(R.id.task_flipper);
                manager.partiallyUpdateAppWidget(widgetId, views);
            }
        }
    }

    static void refreshAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, TaskWidgetProvider.class));
        for (int id : ids) manager.updateAppWidget(id, build(context, id));
    }

    private static RemoteViews build(Context context, int widgetId) {
        RemoteViews root = new RemoteViews(context.getPackageName(), R.layout.widget_root);
        root.removeAllViews(R.id.task_flipper);
        JSONArray tasks = WidgetStore.tasks(context);
        if (tasks.length() == 0) {
            root.addView(R.id.task_flipper, new RemoteViews(context.getPackageName(), R.layout.widget_empty));
        } else {
            int total = Math.min(tasks.length(), 24);
            for (int i = 0; i < total; i++) {
                JSONObject task = tasks.optJSONObject(i);
                if (task == null) continue;
                String id = task.optString("id", "TASK");
                String title = task.optString("title", "Current mission");
                String priority = task.optString("priority", "Medium");
                RemoteViews card = new RemoteViews(context.getPackageName(), R.layout.widget_task);
                card.setTextViewText(R.id.task_priority, priority.toUpperCase() + " // ACTIVE SIGNAL");
                card.setTextViewText(R.id.task_index, String.format(java.util.Locale.UK, "%02d / %02d", i + 1, total));
                card.setTextViewText(R.id.task_title, title);
                card.setTextViewText(R.id.task_next, task.optString("next", "Open the app for the next action"));
                card.setTextViewText(R.id.task_due, "DUE // " + task.optString("due", "NO DATE").toUpperCase());
                int color = priorityColor(priority);
                card.setTextColor(R.id.task_priority, color);
                card.setInt(R.id.task_card, "setBackgroundResource", priorityBackground(priority));
                Intent complete = new Intent(context, TaskWidgetProvider.class).setAction(ACTION_COMPLETE)
                    .putExtra(EXTRA_ID, id).putExtra(EXTRA_TITLE, title);
                int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
                card.setOnClickPendingIntent(R.id.complete_task, PendingIntent.getBroadcast(context, id.hashCode(), complete, flags));
                Intent openTask = new Intent(context, MainActivity.class).putExtra(EXTRA_ID, id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                card.setOnClickPendingIntent(R.id.task_title, PendingIntent.getActivity(context, 50000 + id.hashCode(), openTask, flags));
                root.addView(R.id.task_flipper, card);
            }
        }
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        Intent open = new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        root.setOnClickPendingIntent(R.id.open_app, PendingIntent.getActivity(context, 90001, open, flags));
        Intent next = new Intent(context, TaskWidgetProvider.class).setAction(ACTION_NEXT);
        root.setOnClickPendingIntent(R.id.next_task, PendingIntent.getBroadcast(context, 90002 + widgetId, next, flags));
        root.setInt(R.id.task_flipper, "setFlipInterval", 60000);
        root.setBoolean(R.id.task_flipper, "setAutoStart", true);
        return root;
    }

    private static int priorityColor(String priority) {
        if ("Critical".equalsIgnoreCase(priority)) return Color.rgb(255, 91, 69);
        if ("High".equalsIgnoreCase(priority)) return Color.rgb(255, 179, 31);
        if ("Low".equalsIgnoreCase(priority)) return Color.rgb(82, 247, 255);
        return Color.rgb(167, 123, 255);
    }

    private static int priorityBackground(String priority) {
        if ("Critical".equalsIgnoreCase(priority)) return R.drawable.widget_card_critical;
        if ("High".equalsIgnoreCase(priority)) return R.drawable.widget_card_high;
        if ("Low".equalsIgnoreCase(priority)) return R.drawable.widget_card_low;
        return R.drawable.widget_card_medium;
    }
}
