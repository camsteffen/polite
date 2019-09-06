package me.camsteffen.polite.state

import android.Manifest
import android.app.AlarmManager
import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import me.camsteffen.polite.AppBroadcastReceiver
import me.camsteffen.polite.db.RuleDao
import me.camsteffen.polite.model.CalendarRule
import me.camsteffen.polite.settings.AppPreferences
import me.camsteffen.polite.settings.SharedPreferencesNames
import me.camsteffen.polite.util.AppNotificationManager
import me.camsteffen.polite.util.TimeOfDay
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val TOLERANCE = TimeUnit.SECONDS.toMillis(8)
private val WINDOW_START = TimeUnit.HOURS.toMillis(4)
private val WINDOW_LENGTH = TimeUnit.HOURS.toMillis(25)
private val LOOK_AHEAD = TimeUnit.HOURS.toMillis(30)

private val EVENT_PROJECTION = arrayOf(
        CalendarContract.Instances._ID,
        CalendarContract.Instances.CALENDAR_ID,
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.DESCRIPTION,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.END)
private val EVENT_SELECTION = "${CalendarContract.Instances.ALL_DAY}=0"
private val EVENT_SORT = "${CalendarContract.Instances.BEGIN} ASC"
private const val INDEX_ID = 0
private const val INDEX_CALENDAR_ID = 1
private const val INDEX_TITLE = 2
private const val INDEX_DESCRIPTION = 3
private const val INDEX_BEGIN = 4
private const val INDEX_END = 5

class Event(
        val id: Long,
        val calendarId: Long,
        val title: String?,
        val description: String?,
        val begin: Long,
        val end: Long)

private fun eventMatchesRule(event: Event, rule: CalendarRule): Boolean {
    if (rule.calendarIds.isNotEmpty() && !rule.calendarIds.contains(event.calendarId)) {
        return false
    }
    if (rule.matchBy.all) {
        return true
    }
    var match = false
    if (rule.matchBy.title && event.title != null && event.title.isNotBlank()) {
        val title = event.title.toLowerCase()
        match = rule.keywords.any { title.contains(it) }
    }
    if (!match && rule.matchBy.description && event.description != null && event.description.isNotBlank()) {
        val desc = event.description.toLowerCase()
        match = rule.keywords.any { desc.contains(it) }
    }
    return match.xor(rule.inverseMatch)
}

@Singleton
class PoliteStateManager
@Inject constructor(
    private val context: Context,
    private val notificationManager: AppNotificationManager,
    private val preferences: AppPreferences,
    private val ruleDao: RuleDao
) {

    private var activeEventsPreferences: SharedPreferences =
        context.getSharedPreferences(SharedPreferencesNames.POLITE_MODE_EVENTS, 0)
    private var activeScheduleRulesPreferences: SharedPreferences =
        context.getSharedPreferences(SharedPreferencesNames.ACTIVE_SCHEDULE_RULES, 0)
    private var cancelledEventsPreferences: SharedPreferences =
        context.getSharedPreferences(SharedPreferencesNames.CANCELLED_EVENTS, 0)
    private var cancelledScheduleRulesPreferences: SharedPreferences =
        context.getSharedPreferences(SharedPreferencesNames.CANCELLED_SCHEDULE_RULES, 0)
    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun cancel() {
        // save cancelled schedule rules
        val cancelledScheduleRules = cancelledScheduleRulesPreferences.all.entries
                .associateBy({ it.key }, { it.value as Long }).toSortedMap()
        activeScheduleRulesPreferences.all.entries
                .associateByTo(cancelledScheduleRules, { it.key }, { it.value as Long })
        activeScheduleRulesPreferences.edit().clear().apply()
        val cancelledScheduledRulesEditor = cancelledScheduleRulesPreferences.edit()
                .clear()
        cancelledScheduleRules.forEach {
            cancelledScheduledRulesEditor.putLong(it.key, it.value)
        }
        cancelledScheduledRulesEditor.apply()

        // save cancelled events
        val cancelledEvents = cancelledEventsPreferences.all.values.associateBy { it as Long }.keys.toHashSet()
        activeEventsPreferences.all.values.mapTo(cancelledEvents, { it as Long })
        activeEventsPreferences.edit().clear().apply()
        val cancelledEventsPreferencesEditor = cancelledEventsPreferences.edit()
                .clear()
        cancelledEvents.forEachIndexed { i, id ->
            cancelledEventsPreferencesEditor.putLong(i.toString(), id)
        }
        cancelledEventsPreferencesEditor.apply()
        deactivate()
    }

    fun refresh(modifiedRuleId: Long?) {
        // check enabled setting
        if (!preferences.enable) {
            cancelledEventsPreferences.edit().clear().apply()
            cancelledScheduleRulesPreferences.edit().clear().apply()
            notificationManager.cancelNotificationPolicyAccessRequired()
            deactivate()
            return
        }

        // check notification policy access
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.notifyNotificationPolicyAccessRequired()
            cancelledEventsPreferences.edit().clear().apply()
            cancelledScheduleRulesPreferences.edit().clear().apply()
            deactivate()
            return
        } else {
            notificationManager.cancelNotificationPolicyAccessRequired()
        }

        // get activation and deactivation preferences
        val activation = TimeUnit.MINUTES.toMillis(preferences.activation.toLong())
        val deactivation = TimeUnit.MINUTES.toMillis(preferences.deactivation.toLong())

        val now = Date().time // current time
        var activate = false // whether Polite will be activated
        var reactivate = false // whether Polite is active and ringer mode will be set again
        var vibrate = true // whether vibrate mode should be used
        var notificationText = String()
        var nextRunTime = Long.MAX_VALUE

        val calendarRules = ruleDao.getEnabledCalendarRules()
        var scheduleRules = ruleDao.getEnabledScheduleRules()

        // cancelled schedule rules
        val cancelledScheduleRules = cancelledScheduleRulesPreferences.all.entries
                .associateBy({ it.key.toLong() }, { it.value as Long })
                .filter { scheduleRules.any { rule -> rule.id == it.key } && now < it.value }
                .toSortedMap()
        val cancelledScheduleRulesEditor = cancelledScheduleRulesPreferences.edit().clear()
        cancelledScheduleRules.forEach {
            cancelledScheduleRulesEditor.putLong(it.key.toString(), it.value)
        }
        cancelledScheduleRulesEditor.apply()
        scheduleRules = scheduleRules.filterNot { cancelledScheduleRules.contains(it.id) }

        // iterate schedule rules
        val activeScheduleRulesEditor = activeScheduleRulesPreferences.edit()
                .clear()
        for (rule in scheduleRules) {
            val calendar = GregorianCalendar()
            val currentDayOfWeek = LocalDate.now().dayOfWeek
            var dayAdjust = 0
            if (TimeOfDay(calendar) >= rule.end)
                ++dayAdjust
            if (rule.begin > rule.end)
                --dayAdjust
            var dayOfWeek = DayOfWeek.MONDAY
            for (i in 0..6) {
                dayOfWeek = currentDayOfWeek + dayAdjust.toLong() + i.toLong()
                if (rule.days.contains(dayOfWeek)) {
                    dayAdjust += i
                    break
                }
            }
            if (!rule.days.contains(dayOfWeek))
                continue
            calendar.add(Calendar.DAY_OF_WEEK, dayAdjust)
            calendar.set(Calendar.HOUR_OF_DAY, rule.begin.hour)
            calendar.set(Calendar.MINUTE, rule.begin.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            var begin = calendar.timeInMillis
            var end = begin + TimeUnit.MINUTES.toMillis((rule.end - rule.begin).toLong())
            if (end < begin)
                end += TimeUnit.DAYS.toMillis(1)
            begin -= activation
            end += deactivation
            if (now < begin - TOLERANCE) {
                nextRunTime = Math.min(nextRunTime, begin)
            }
            else if (now < end - TOLERANCE) {
                activate = true
                vibrate = vibrate && rule.vibrate
                notificationText = rule.name
                nextRunTime = Math.min(nextRunTime, end)
                activeScheduleRulesEditor.putLong(rule.id.toString(), end)
            }
            else {
                nextRunTime = Math.min(nextRunTime, begin + TimeUnit.DAYS.toMillis(1))
            }
        }
        activeScheduleRulesEditor.apply()

        // check calendar permission
        val hasCalendarPermission = (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
        if (hasCalendarPermission)
            notificationManager.cancelCalendarPermissionRequired()
        else if (calendarRules.isNotEmpty()) {
            notificationManager.notifyCalendarPermissionRequired()
        }

        if (calendarRules.isEmpty() || !hasCalendarPermission) {
            cancelledEventsPreferences.edit().clear().apply()
            activeEventsPreferences.edit().clear().apply()
        } else {
            val activeEvents = activeEventsPreferences.all.values.map { it as Long }.toHashSet()
            val cancelledEvents = cancelledEventsPreferences.all.values.map { it as Long }.toHashSet()
            val currentEvents = hashSetOf<Long>()
            val currentCancelledEvents = hashSetOf<Long>()

            // get current and upcoming event instances
            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, now - deactivation)
            ContentUris.appendId(builder, now + LOOK_AHEAD)
            val eventCur = context.contentResolver.query(
                    builder.build(),
                    EVENT_PROJECTION,
                    EVENT_SELECTION,
                    null,
                    EVENT_SORT)

            // iterate calendar events
            if (eventCur != null) {
                while (eventCur.moveToNext()) {
                    val event = Event(
                            id = eventCur.getLong(INDEX_ID),
                            calendarId = eventCur.getLong(INDEX_CALENDAR_ID),
                            title = eventCur.getString(INDEX_TITLE),
                            description = eventCur.getString(INDEX_DESCRIPTION),
                            begin = eventCur.getLong(INDEX_BEGIN),
                            end = eventCur.getLong(INDEX_END))
                    val eventActivation = event.begin - activation
                    val eventDeactivation = event.end + deactivation

                    if (eventDeactivation < now + TOLERANCE) {
                        continue
                    }

                    if (eventActivation >= now + TOLERANCE) {
                        nextRunTime = Math.min(eventActivation, nextRunTime)
                        break
                    }

                    val cancelled = cancelledEvents.contains(event.id)
                    val matchingRules = calendarRules.filter { eventMatchesRule(event, it) }
                    if (cancelled) {
                        if (matchingRules.isNotEmpty()) {
                            currentCancelledEvents.add(event.id)
                        }
                        continue
                    }
                    if (matchingRules.isEmpty())
                        continue

                    activate = true
                    currentEvents.add(event.id)
                    val active = activeEvents.contains(event.id)
                    for (rule in matchingRules) {
                        if (notificationText.isEmpty()) {
                            notificationText = event.title ?: ""
                        }
                        vibrate = vibrate && rule.vibrate
                        reactivate = reactivate || !active || rule.id == modifiedRuleId
                        nextRunTime = Math.min(eventDeactivation, nextRunTime)
                    }
                }
                eventCur.close()

                // save active events
                val activeEventsPreferencesEditor = activeEventsPreferences.edit()
                        .clear()
                currentEvents.mapIndexed { i, id ->
                    activeEventsPreferencesEditor.putLong(i.toString(), id)
                }
                activeEventsPreferencesEditor.apply()
            }
        }

        // save active state
        val active = preferences.politeMode
        if (activate != active) {
            preferences.politeMode = activate
        }

        // set ringer mode
        if (active && !activate) {
            deactivate()
        } else if (activate) {
            if (!active) {
                preferences.previousRingerMode = audioManager.ringerMode
            }
            if (!active || reactivate) {
                audioManager.ringerMode = if (vibrate)
                    AudioManager.RINGER_MODE_VIBRATE
                else
                    AudioManager.RINGER_MODE_SILENT
            }
        }

        // notification
        val notificationsEnabled = preferences.notifications
        if (activate && notificationsEnabled) {
            if (!active) {
                notificationManager.notifyPoliteActive(notificationText)
            }
        } else {
            notificationManager.cancelPoliteActive()
        }

        // schedule next task
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = AppBroadcastReceiver.pendingRefreshIntent(context)
        if (nextRunTime == Long.MAX_VALUE) {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, now + WINDOW_START, WINDOW_LENGTH, pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextRunTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextRunTime, pendingIntent)
        }
    }

    private fun deactivate() {
        preferences.politeMode = false
        activeEventsPreferences.edit().clear().apply()
        notificationManager.cancelPoliteActive()

        // change to previous ringer mode only if louder than current mode
        val previousRingerMode = preferences.previousRingerMode
        if ((previousRingerMode == AudioManager.RINGER_MODE_NORMAL
                || previousRingerMode == AudioManager.RINGER_MODE_VIBRATE
                && audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT)
                && notificationManager.isNotificationPolicyAccessGranted) {
            audioManager.ringerMode = previousRingerMode
        }
    }

}
