package me.camsteffen.polite
import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.provider.CalendarContract
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import me.camsteffen.polite.rule.calendar.CalendarRule
import me.camsteffen.polite.rule.schedule.ScheduleRule
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit

private val TOLERANCE = TimeUnit.SECONDS.toMillis(8)
private val WINDOW_START = TimeUnit.HOURS.toMillis(4)
private val WINDOW_LENGTH = TimeUnit.HOURS.toMillis(25)
private val INTERVAL = TimeUnit.HOURS.toMillis(29)
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
    if (rule.calendars.isNotEmpty() && !rule.calendars.contains(event.calendarId)) {
        return false
    }
    if (rule.matchAll) {
        return true
    }
    var match = false
    if (rule.matchTitle && event.title != null && event.title.isNotBlank()) {
        val title = event.title.toLowerCase()
        match = rule.keywords.any { title.contains(it) }
    }
    if (!match && rule.matchDescription && event.description != null && event.description.isNotBlank()) {
        val desc = event.description.toLowerCase()
        match = rule.keywords.any { desc.contains(it) }
    }
    return match.xor(rule.inverseMatch)
}

class RingerReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CANCEL = "cancel"
        const val ACTION_REFRESH = "refresh"

        const val MODIFIED_RULE_ID = "modified_rule_id"
    }

    private var previousRingerMode = 0
    private var _notificationPolicyAccess: Boolean? = null
    private val notificationPolicyAccess: Boolean
        get() {
            if (_notificationPolicyAccess == null) {
                _notificationPolicyAccess = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                        || notificationManager.isNotificationPolicyAccessGranted
            }
            return _notificationPolicyAccess!!
        }
    private lateinit var preferences: SharedPreferences
    private lateinit var activeEventsPreferences: SharedPreferences
    private lateinit var activeScheduleRulesPreferences: SharedPreferences
    private lateinit var cancelledEventsPreferences: SharedPreferences
    private lateinit var cancelledScheduleRulesPreferences: SharedPreferences
    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        preferences = Polite.preferences
        activeEventsPreferences = context.getSharedPreferences(AppPreferences.POLITE_MODE_EVENTS, 0)
        activeScheduleRulesPreferences = context.getSharedPreferences(AppPreferences.ACTIVE_SCHEDULE_RULES, 0)
        cancelledEventsPreferences = context.getSharedPreferences(AppPreferences.CANCELLED_EVENTS, 0)
        cancelledScheduleRulesPreferences = context.getSharedPreferences(AppPreferences.CANCELLED_SCHEDULE_RULES, 0)
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        previousRingerMode = preferences.getInt(AppPreferences.PREVIOUS_RINGER_MODE, 0)

        when (intent.action) {
            NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED,
            Intent.ACTION_PROVIDER_CHANGED,
            Intent.ACTION_BOOT_COMPLETED -> refresh(context, -1L)
            ACTION_REFRESH -> {
                val modifiedRuleId = intent.getLongExtra(MODIFIED_RULE_ID, -1L)
                refresh(context, modifiedRuleId)
            }
            ACTION_CANCEL -> cancel()
        }
    }

    private fun cancel() {
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

    private fun refresh(context: Context, modifiedRuleId: Long) {
        // check enabled setting
        if (!preferences.getBoolean(context.getString(R.string.preference_enable), true)) {
            cancelledEventsPreferences.edit().clear().apply()
            cancelledScheduleRulesPreferences.edit().clear().apply()
            notificationManager.cancel(Polite.NOTIFY_ID_NOTIFICATION_POLICY_ACCESS)
            deactivate()
            return
        }

        // check notification policy access
        if (!notificationPolicyAccess) {
            val notification = NotificationCompat.Builder(context)
                    .setColor(ContextCompat.getColor(context, R.color.primary))
                    .setContentTitle(context.resources.getString(R.string.notification_policy_access_required))
                    .setContentText(context.resources.getString(R.string.notification_policy_access_explain))
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.mipmap.notification_icon)
                    .setContentIntent(PendingIntent.getActivity(context, 0, Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 0))
                    .build()
            notificationManager.notify(Polite.NOTIFY_ID_NOTIFICATION_POLICY_ACCESS, notification)

            cancelledEventsPreferences.edit().clear().apply()
            cancelledScheduleRulesPreferences.edit().clear().apply()
            deactivate()
            return
        } else {
            notificationManager.cancel(Polite.NOTIFY_ID_NOTIFICATION_POLICY_ACCESS)
        }

        // get activation and deactivation preferences
        val activation = TimeUnit.MILLISECONDS.convert(preferences.getInt(
                context.getString(R.string.preference_activation), 0).toLong(), TimeUnit.MINUTES)
        val deactivation = TimeUnit.MILLISECONDS.convert(preferences.getInt(
                context.getString(R.string.preference_deactivation), 0).toLong(), TimeUnit.MINUTES)

        val now = Date().time // current time
        var activate = false // whether Polite will be activated
        var reactivate = false // whether Polite is active and ringer mode will be set again
        var vibrate = true // whether vibrate mode should be used
        var notificationText = String()
        var nextRunTime = Long.MAX_VALUE

        // get enabled rules
        val db = Polite.db.readableDatabase
        val selection = "${DB.Rule.COLUMN_ENABLE}=1"
        val calendarRules = CalendarRule.queryList(db, selection)
        var scheduleRules = ScheduleRule.queryList(db, selection)

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
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            var dayAdjust = 0
            if (TimeOfDay(calendar) >= rule.end)
                ++dayAdjust
            if (rule.begin > rule.end)
                --dayAdjust
            var dayOfWeek = -1
            for (i in 0..6) {
                dayOfWeek = (currentDayOfWeek + dayAdjust + i + 7) % 7
                if (rule.days[dayOfWeek]) {
                    dayAdjust += i
                    break
                }
            }
            if (!rule.days[dayOfWeek])
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
            notificationManager.cancel(Polite.NOTIFY_ID_CALENDAR_PERMISSION)
        else if (calendarRules.isNotEmpty()) {
            val builder = NotificationCompat.Builder(context)
                    .setColor(ContextCompat.getColor(context, R.color.primary))
                    .setContentTitle(context.resources.getString(R.string.calendar_permission_required))
                    .setContentText(context.resources.getString(R.string.calendar_permission_explain))
                    .setSmallIcon(R.mipmap.notification_icon)
                    .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0))
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setVisibility(Notification.VISIBILITY_PUBLIC)
            }
            val notification = builder.build()
            notificationManager.notify(Polite.NOTIFY_ID_CALENDAR_PERMISSION, notification)
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
        val active = preferences.getBoolean(AppPreferences.POLITE_MODE, false)
        if (activate != active) {
            preferences.edit()
                    .putBoolean(AppPreferences.POLITE_MODE, activate)
                    .apply()
        }

        // set ringer mode
        if (active && !activate) {
            deactivate()
        } else if (activate) {
            if (!active) {
                previousRingerMode = audioManager.ringerMode
                preferences.edit()
                        .putInt(AppPreferences.PREVIOUS_RINGER_MODE, previousRingerMode)
                        .apply()
            }
            if (!active || reactivate) {
                audioManager.ringerMode = if (vibrate)
                    AudioManager.RINGER_MODE_VIBRATE
                else
                    AudioManager.RINGER_MODE_SILENT
            }
        }

        // notification
        val notificationsEnabled = preferences.getBoolean(context.getString(R.string.preference_notifications), true)
        if (activate && notificationsEnabled) {
            if (!active) {
                val builder = NotificationCompat.Builder(context)
                        .setOngoing(true)
                        .setColor(ContextCompat.getColor(context, R.color.primary))
                        .setContentTitle(context.resources.getString(R.string.polite_active))
                        .setContentText(notificationText)
                        .setSmallIcon(R.mipmap.notification_icon)
                        .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0))
                        .addAction(android.support.v4.app.NotificationCompat.Action.Builder(
                                R.drawable.ic_cancel_black_24dp,
                                context.resources.getString(android.R.string.cancel),
                                PendingIntent.getBroadcast(context, 0, Intent(context,
                                        RingerReceiver::class.java).setAction(ACTION_CANCEL),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                                .build())
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.setVisibility(Notification.VISIBILITY_PUBLIC)
                }
                val notification = builder.build()
                notificationManager.notify(Polite.NOTIFY_ID_ACTIVE, notification)
            }
        } else {
            notificationManager.cancel(Polite.NOTIFY_ID_ACTIVE)
        }

        // schedule next task
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(context, 0, Intent(context, RingerReceiver::class.java)
                .setAction(ACTION_REFRESH), 0)
        if (nextRunTime == Long.MAX_VALUE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setWindow(AlarmManager.RTC_WAKEUP, now + WINDOW_START, WINDOW_LENGTH, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, now + INTERVAL, pendingIntent)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextRunTime, pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextRunTime, pendingIntent)
        } else  {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextRunTime, pendingIntent)
        }
    }

    private fun deactivate() {
        preferences.edit()
                .putBoolean(AppPreferences.POLITE_MODE, false)
                .apply()
        activeEventsPreferences.edit().clear().apply()
        notificationManager.cancel(Polite.NOTIFY_ID_ACTIVE)

        // change to previous ringer mode only if louder than current mode
        if ((previousRingerMode == AudioManager.RINGER_MODE_NORMAL
                || previousRingerMode == AudioManager.RINGER_MODE_VIBRATE
                && audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT)
                && notificationPolicyAccess) {
            audioManager.ringerMode = previousRingerMode
        }
    }

}
