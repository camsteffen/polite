package me.camsteffen.polite.util

import android.content.BroadcastReceiver
import android.content.Context
import android.os.AsyncTask
import org.threeten.bp.Instant

fun appInstalledTime(context: Context): Instant {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    return Instant.ofEpochMilli(packageInfo.firstInstallTime)
}

fun BroadcastReceiver.finishAsync(block: () -> Unit) {
    val pendingResult = goAsync()
    AsyncTask.execute {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}
