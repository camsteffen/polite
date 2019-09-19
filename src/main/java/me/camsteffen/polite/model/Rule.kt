package me.camsteffen.polite.model

import android.content.Context
import me.camsteffen.polite.rule.scheduleSummary
import me.camsteffen.polite.util.TimeOfDay
import org.threeten.bp.DayOfWeek


sealed class Rule {

    companion object {
        // Room generates a unique ID when the inserted value is 0
        const val NEW_ID = 0L
    }

    abstract val id: Long
    abstract val name: String
    abstract val enabled: Boolean
    abstract val vibrate: Boolean

    open fun getCaption(context: Context): String = ""

    fun asRuleEntity() = RuleEntity(id, name, enabled, vibrate)
}

data class CalendarRule(
    override val id: Long,
    override val name: String,
    override val enabled: Boolean,
    override val vibrate: Boolean,
    val matchBy: CalendarEventMatchBy,
    val inverseMatch: Boolean,
    val calendarIds: Set<Long>,
    val keywords: Set<String>
) : Rule() {
    override fun getCaption(context: Context): String {
        if (keywords.isEmpty())
            return ""
        val it = keywords.iterator()
        val builder = StringBuilder(it.next())
        for (word in it) {
            builder.append(", $word")
        }
        return builder.toString()
    }

    fun asCalendarRuleEntity() = CalendarRuleEntity(id, matchBy.asEntity(), inverseMatch)

    fun calendarRuleCalendars(): List<CalendarRuleCalendar> =
        calendarIds.map { CalendarRuleCalendar(id, it) }

    fun calendarRuleKeywords(): List<CalendarRuleKeyword> =
        keywords.map { CalendarRuleKeyword(id, it) }
}

data class ScheduleRule(
    override val id: Long,
    override val name: String,
    override val enabled: Boolean,
    override val vibrate: Boolean,
    val begin: TimeOfDay,
    val end: TimeOfDay,
    val days: Set<DayOfWeek>
) : Rule() {

    override fun getCaption(context: Context) = scheduleSummary(context, days, begin, end)

    fun asScheduleRuleEntity() = ScheduleRuleEntity(
        id = id,
        beginTime = begin.toLocalTime(),
        endTime = end.toLocalTime(),
        daysOfWeek = DaysOfWeekEntity(days)
    )
}

