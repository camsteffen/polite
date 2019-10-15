package me.camsteffen.polite.state

import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
import android.os.Build
import me.camsteffen.polite.AppBroadcastReceiver
import me.camsteffen.polite.AppTimingConfig
import me.camsteffen.polite.AppWorkManager
import me.camsteffen.polite.receiver.CalendarChangeReceiver
import me.camsteffen.polite.util.componentName
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefreshScheduler
@Inject constructor(
    private val clock: Clock,
    private val context: Context,
    private val alarmManager: AlarmManager,
    private val timingConfig: AppTimingConfig,
    private val workManager: AppWorkManager
) {
    fun cancelAll() {
        alarmManager.cancel(AppBroadcastReceiver.pendingRefreshIntent(context))
        setRefreshOnCalendarChange(false)
    }

    fun scheduleRefresh(refreshTime: Instant? = null) {
        val pendingIntent = AppBroadcastReceiver.pendingRefreshIntent(context)
        when {
            refreshTime == null -> {
                val windowStart = (clock.instant() + timingConfig.refreshWindowDelay).toEpochMilli()
                val windowEnd = timingConfig.refreshWindowLength.toMillis()
                alarmManager.setWindow(
                    AlarmManager.RTC_WAKEUP,
                    windowStart, windowEnd,
                    pendingIntent
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    refreshTime.toEpochMilli(),
                    pendingIntent
                )
            }
            else -> {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    refreshTime.toEpochMilli(),
                    pendingIntent
                )
            }
        }
    }

    fun setRefreshOnCalendarChange(refreshOnCalendarChange: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (refreshOnCalendarChange) {
                workManager.refreshOnCalendarChange()
            } else {
                workManager.cancelRefreshOnCalendarChange()
            }
        } else {
            setReceiverEnabled<CalendarChangeReceiver>(refreshOnCalendarChange)
        }
    }

    private inline fun <reified T> setReceiverEnabled(enabled: Boolean) {
        val state = if (enabled) {
            COMPONENT_ENABLED_STATE_ENABLED
        } else {
            COMPONENT_ENABLED_STATE_DISABLED
        }
        context.packageManager.setComponentEnabledSetting(
            componentName<T>(context), state,
            PackageManager.DONT_KILL_APP
        )
    }
}
