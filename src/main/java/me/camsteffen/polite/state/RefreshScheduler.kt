package me.camsteffen.polite.state

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import me.camsteffen.polite.AppBroadcastReceiver
import me.camsteffen.polite.AppTimingConfig
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
    private val timingConfig: AppTimingConfig
) {
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

    fun cancelScheduledRefresh() {
        alarmManager.cancel(AppBroadcastReceiver.pendingRefreshIntent(context))
    }
}
