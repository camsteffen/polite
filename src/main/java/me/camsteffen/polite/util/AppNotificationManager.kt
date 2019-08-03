package me.camsteffen.polite.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import me.camsteffen.polite.AppBroadcastReceiver
import me.camsteffen.polite.MainActivity
import me.camsteffen.polite.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotificationManager
@Inject constructor(
    private val context: Context,
    private val notificationManager: NotificationManager
) {
    val isNotificationPolicyAccessGranted
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                notificationManager.isNotificationPolicyAccessGranted

    fun notifyPoliteActive(text: String) {
        val notification = NotificationCompat.Builder(context)
            .setColor(ContextCompat.getColor(context, R.color.primary))
            .setSmallIcon(R.mipmap.notification_icon)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentTitle(context.resources.getString(R.string.polite_active))
            .setContentText(text)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    0
                )
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_cancel_black_24dp,
                    context.resources.getString(android.R.string.cancel),
                    AppBroadcastReceiver.pendingCancelIntent(context)
                )
                    .build()
            )
            .build()
        notificationManager.notify(NotificationIds.ACTIVE, notification)
    }

    fun cancelPoliteActive() {
        notificationManager.cancel(NotificationIds.ACTIVE)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun notifyNotificationPolicyAccessRequired() {
        val notification = NotificationCompat.Builder(context)
            .setColor(ContextCompat.getColor(context, R.color.primary))
            .setSmallIcon(R.mipmap.notification_icon)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(context.resources.getString(R.string.notification_policy_access_required))
            .setContentText(context.resources.getString(R.string.notification_policy_access_explain))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS),
                    0
                )
            )
            .build()
        notificationManager.notify(NotificationIds.NOTIFICATION_POLICY_ACCESS, notification)
    }

    fun cancelNotificationPolicyAccessRequired() {
        notificationManager.cancel(NotificationIds.NOTIFICATION_POLICY_ACCESS)
    }

    fun notifyCalendarPermissionRequired() {
        val notification = NotificationCompat.Builder(context)
            .setColor(ContextCompat.getColor(context, R.color.primary))
            .setSmallIcon(R.mipmap.notification_icon)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(context.resources.getString(R.string.calendar_permission_required))
            .setContentText(context.resources.getString(R.string.calendar_permission_explain))
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    0
                )
            )
            .build()
        notificationManager.notify(NotificationIds.CALENDAR_PERMISSION, notification)
    }

    fun cancelCalendarPermissionRequired() {
        notificationManager.cancel(NotificationIds.CALENDAR_PERMISSION)
    }
}

object NotificationIds {
    const val CALENDAR_PERMISSION = 0
    const val ACTIVE = 1
    const val NOTIFICATION_POLICY_ACCESS = 2
}
