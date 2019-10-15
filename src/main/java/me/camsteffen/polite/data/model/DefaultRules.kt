package me.camsteffen.polite.data.model

import android.annotation.SuppressLint
import android.content.Context
import me.camsteffen.polite.R
import me.camsteffen.polite.data.db.entity.defaultAudioPolicy
import org.threeten.bp.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultRules
@Inject constructor(val context: Context) {

    fun calendar() = CalendarRule(
        id = Rule.NEW_ID,
        name = context.getString(R.string.rule_default_name),
        enabled = RuleDefaults.enabled,
        audioPolicy = RuleDefaults.audioPolicy,
        busyOnly = false,
        calendarIds = emptySet(),
        matchBy = CalendarEventMatchBy.ALL,
        inverseMatch = false,
        keywords = emptySet()
    )

    fun schedule() = ScheduleRule(
        id = Rule.NEW_ID,
        name = context.getString(R.string.rule_default_name),
        enabled = RuleDefaults.enabled,
        audioPolicy = RuleDefaults.audioPolicy,
        schedule = ScheduleRuleSchedule(
            12, 0,
            13, 0,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
    )
}

private object RuleDefaults {
    @SuppressLint("InlinedApi")
    val audioPolicy = defaultAudioPolicy
    const val enabled = true
}
