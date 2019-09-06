package me.camsteffen.polite.util

import android.content.BroadcastReceiver
import android.os.AsyncTask

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
