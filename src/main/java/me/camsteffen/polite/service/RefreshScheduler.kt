package me.camsteffen.polite.service

import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
import android.os.Build
import me.camsteffen.polite.service.receiver.AppBroadcastReceiver
import me.camsteffen.polite.service.receiver.CalendarChangeReceiver
import me.camsteffen.polite.service.work.AppWorkManager
import me.camsteffen.polite.util.AppTimingConfig
import me.camsteffen.polite.util.componentName
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import timber.log.Timber
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
        Timber.i("Cancelling all scheduled refreshes")
        alarmManager.cancel(AppBroadcastReceiver.pendingRefreshIntent(context))
        setRefreshOnCalendarChange(false)
    }

    fun scheduleRefresh(refreshTime: Instant) {
        Timber.i("Scheduling refresh at %s", refreshTime)
        val pendingIntent = AppBroadcastReceiver.pendingRefreshIntent(context)
        when {
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

    fun scheduleRefreshInWindow() {
        val pendingIntent = AppBroadcastReceiver.pendingRefreshIntent(context)
        val windowStart = (clock.instant() + timingConfig.refreshWindowDelay).toEpochMilli()
        val windowLength = timingConfig.refreshWindowLength.toMillis()
        Timber.i("Scheduling refresh window, start=%s, length=%s",
            Instant.ofEpochMilli(windowStart), Duration.ofMillis(windowLength))
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            windowStart, windowLength,
            pendingIntent
        )
    }

    fun setRefreshOnCalendarChange(refreshOnCalendarChange: Boolean) {
        Timber.d("Setting refresh on calendar change to %b", refreshOnCalendarChange)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (refreshOnCalendarChange) {
                workManager.refreshOnCalendarChange()
            } else {
                workManager.cancelRefreshOnCalendarChange()
            }
            setReceiverEnabled<CalendarChangeReceiver>(false)
        } else {
            setReceiverEnabled<CalendarChangeReceiver>(refreshOnCalendarChange)
        }
    }

    private inline fun <reified T> setReceiverEnabled(enabled: Boolean) {
        Timber.i("Setting component[%s] enabled=%b", T::class.java.simpleName, enabled)
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
