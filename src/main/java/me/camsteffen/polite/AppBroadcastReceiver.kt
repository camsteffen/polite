package me.camsteffen.polite

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.android.DaggerBroadcastReceiver
import me.camsteffen.polite.state.PoliteStateManager
import me.camsteffen.polite.util.finishAsync
import javax.inject.Inject

class AppBroadcastReceiver : DaggerBroadcastReceiver() {

    companion object {
        private const val ACTION_CANCEL = "cancel"
        private const val ACTION_REFRESH = "refresh"

        fun refreshIntent(context: Context): Intent {
            return Intent(context, AppBroadcastReceiver::class.java)
                .setAction(ACTION_REFRESH)
        }

        fun pendingCancelIntent(context: Context): PendingIntent {
            val intent = Intent(context, AppBroadcastReceiver::class.java)
                .setAction(ACTION_CANCEL)
            return PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        fun pendingRefreshIntent(context: Context): PendingIntent {
            val intent = refreshIntent(context)
            return PendingIntent.getBroadcast(context, 0, intent, 0)
        }
    }

    @Inject lateinit var stateManager: PoliteStateManager

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED,
            ACTION_REFRESH -> finishAsync(stateManager::refresh)
            ACTION_CANCEL -> finishAsync(stateManager::cancel)
        }
    }
}
