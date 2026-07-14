package com.tolu.cc;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

final class WidgetStore {
    private static final String PREFS = "tolu_cc_widget";
    private static final String TASKS = "tasks";
    private static final String COMPLETIONS = "pending_completions";

    private WidgetStore() {}

    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static JSONArray tasks(Context context) {
        try { return new JSONArray(prefs(context).getString(TASKS, "[]")); }
        catch (Exception ignored) { return new JSONArray(); }
    }

    static void saveTasks(Context context, String json) {
        try {
            JSONArray clean = new JSONArray(json);
            prefs(context).edit().putString(TASKS, clean.toString()).apply();
        } catch (Exception ignored) {}
    }

    static void complete(Context context, String id, String title) {
        JSONArray tasks = tasks(context);
        JSONArray remaining = new JSONArray();
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task != null && !id.equals(task.optString("id"))) remaining.put(task);
        }
        JSONArray pending;
        try { pending = new JSONArray(prefs(context).getString(COMPLETIONS, "[]")); }
        catch (Exception ignored) { pending = new JSONArray(); }
        boolean exists = false;
        for (int i = 0; i < pending.length(); i++) {
            if (id.equals(pending.optJSONObject(i).optString("id"))) { exists = true; break; }
        }
        if (!exists) {
            JSONObject item = new JSONObject();
            try {
                item.put("id", id);
                item.put("title", title);
                item.put("completedAt", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.UK).format(new java.util.Date()));
                pending.put(item);
            } catch (Exception ignored) {}
        }
        prefs(context).edit().putString(TASKS, remaining.toString()).putString(COMPLETIONS, pending.toString()).apply();
    }

    static synchronized String consumeCompletions(Context context) {
        SharedPreferences p = prefs(context);
        String json = p.getString(COMPLETIONS, "[]");
        p.edit().putString(COMPLETIONS, "[]").commit();
        return json;
    }
}
