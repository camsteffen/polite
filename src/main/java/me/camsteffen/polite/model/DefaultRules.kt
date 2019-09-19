package me.camsteffen.polite.model

import android.content.Context
import me.camsteffen.polite.R
import me.camsteffen.polite.util.TimeOfDay
import org.threeten.bp.DayOfWeek
import java.util.EnumSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultRules
@Inject constructor(val context: Context) {

    fun calendar() = CalendarRule(
        id = Rule.NEW_ID,
        name = context.getString(R.string.rule_default_name),
        enabled = RuleDefaults.enabled,
        vibrate = RuleDefaults.vibrate,
        calendarIds = emptySet(),
        matchBy = CalendarEventMatchBy.ALL,
        inverseMatch = false,
        keywords = emptySet()
    )

    fun schedule() = ScheduleRule(
        id = Rule.NEW_ID,
        name = context.getString(R.string.rule_default_name),
        enabled = RuleDefaults.enabled,
        vibrate = RuleDefaults.vibrate,
        begin = TimeOfDay(12, 0),
        end = TimeOfDay(13, 0),
        days = EnumSet.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
    )
}

private object RuleDefaults {
    const val enabled = true
    const val vibrate = false
}
