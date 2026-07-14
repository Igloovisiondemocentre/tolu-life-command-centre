package com.tolu.cc;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private static final String APP_URL = "https://igloovisiondemocentre.github.io/tolu-life-command-centre/#missions";
    private static final String APP_ORIGIN = "https://igloovisiondemocentre.github.io/tolu-life-command-centre/";
    private static final int PICK_FILE = 9201;
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(7, 11, 18));
        getWindow().setNavigationBarColor(Color.rgb(7, 11, 18));
        webView = new WebView(this);
        setContentView(webView);
        webView.setBackgroundColor(Color.rgb(7, 11, 18));
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(false);
        webView.addJavascriptInterface(new NativeBridge(), "ToluNative");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                picker.addCategory(Intent.CATEGORY_OPENABLE);
                picker.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                startActivityForResult(picker, PICK_FILE);
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.startsWith(APP_ORIGIN)) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); return true; }
                catch (Exception ignored) { return false; }
            }
            @Override public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript("window.dispatchEvent(new Event('tolu-native-ready'))", null);
                requestWidgetPinOnce();
            }
        });
        webView.loadUrl(APP_URL);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (webView != null) {
            webView.loadUrl(APP_URL);
            webView.evaluateJavascript("window.dispatchEvent(new Event('tolu-native-ready'))", null);
        }
    }

    @Override public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_FILE || fileCallback == null) return;
        Uri[] result = null;
        if (resultCode == RESULT_OK && data != null && data.getData() != null) result = new Uri[]{data.getData()};
        fileCallback.onReceiveValue(result);
        fileCallback = null;
    }

    private void requestWidgetPinOnce() {
        if (android.os.Build.VERSION.SDK_INT < 26) return;
        if (WidgetStore.prefs(this).getBoolean("pin_requested_v3", false)) return;
        AppWidgetManager manager = (AppWidgetManager)getSystemService(APPWIDGET_SERVICE);
        if (manager == null) return;
        try {
            java.lang.reflect.Method supported = AppWidgetManager.class.getMethod("isRequestPinAppWidgetSupported");
            if (!((Boolean)supported.invoke(manager)).booleanValue()) return;
            ComponentName provider = new ComponentName(this, TaskWidgetProvider.class);
            Intent success = new Intent(this, TaskWidgetProvider.class).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            PendingIntent callback = PendingIntent.getBroadcast(this, 70001, success, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            java.lang.reflect.Method request = AppWidgetManager.class.getMethod("requestPinAppWidget", ComponentName.class, Bundle.class, PendingIntent.class);
            if (((Boolean)request.invoke(manager, provider, null, callback)).booleanValue()) WidgetStore.prefs(this).edit().putBoolean("pin_requested_v3", true).apply();
        } catch (Exception ignored) {}
    }

    private final class NativeBridge {
        @JavascriptInterface public void syncTasks(String tasksJson) {
            WidgetStore.saveTasks(MainActivity.this, tasksJson);
            runOnUiThread(new Runnable() { @Override public void run() { TaskWidgetProvider.refreshAll(MainActivity.this); } });
        }

        @JavascriptInterface public String consumeCompletions() {
            return WidgetStore.consumeCompletions(MainActivity.this);
        }
    }
}
