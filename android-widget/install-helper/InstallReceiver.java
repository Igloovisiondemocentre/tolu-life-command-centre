package com.tolu.installhelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.widget.Toast;

public class InstallReceiver extends BroadcastReceiver {
    private final Context appContext;

    public InstallReceiver(Context context) {
        appContext = context.getApplicationContext();
    }

    @Override public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
        String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
        appContext.getSharedPreferences("tolu_installer", Context.MODE_PRIVATE).edit()
            .putInt("status", status)
            .putString("message", message == null ? "" : message)
            .commit();
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirmation = intent.getParcelableExtra(Intent.EXTRA_INTENT);
            if (confirmation != null) {
                appContext.getSharedPreferences("tolu_installer", Context.MODE_PRIVATE).edit()
                    .putString("confirmation_intent", confirmation.toUri(Intent.URI_INTENT_SCHEME))
                    .commit();
                confirmation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.startActivity(confirmation);
            }
            return;
        }
        Toast.makeText(appContext,
            status == PackageInstaller.STATUS_SUCCESS ? "Tolu's CC installed" : "Install failed: " + message,
            Toast.LENGTH_LONG).show();
        try { appContext.unregisterReceiver(this); } catch (Exception ignored) {}
    }
}
