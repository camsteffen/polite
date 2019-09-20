package me.camsteffen.polite.util

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.os.AsyncTask
import org.threeten.bp.Instant

fun appInstalledTime(context: Context): Instant {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    return Instant.ofEpochMilli(packageInfo.firstInstallTime)
}

inline fun <reified T> componentName(context: Context): ComponentName {
    return ComponentName(context, T::class.java)
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
