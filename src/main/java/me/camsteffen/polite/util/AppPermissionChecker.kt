package me.camsteffen.polite.util

import android.Manifest.permission.READ_CALENDAR
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.core.content.ContextCompat.checkSelfPermission
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPermissionChecker
@Inject constructor(
    private val context: Context,
    private val notificationManager: AppNotificationManager
) {
    fun checkReadCalendarPermission(): Boolean {
        if (checkSelfPermission(context, READ_CALENDAR) != PERMISSION_GRANTED) {
            notificationManager.notifyCalendarPermissionRequired()
            return false
        }
        return true
    }

    fun checkNotificationPolicyAccess(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !notificationManager.isNotificationPolicyAccessGranted
        ) {
            notificationManager.notifyNotificationPolicyAccessRequired()
            return false
        }
        return true
    }
}
