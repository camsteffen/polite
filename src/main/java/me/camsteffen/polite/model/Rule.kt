package me.camsteffen.polite.model

import android.content.Context
import me.camsteffen.polite.rule.scheduleSummary
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime


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
        return keywords.joinToString()
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
    val beginTime: LocalTime,
    val endTime: LocalTime,
    val daysOfWeek: Set<DayOfWeek>
) : Rule() {

    override fun getCaption(context: Context) = scheduleSummary(context, daysOfWeek, beginTime, endTime)

    fun asScheduleRuleEntity() = ScheduleRuleEntity(
        id = id,
        beginTime = beginTime,
        endTime = endTime,
        daysOfWeek = DaysOfWeekEntity(daysOfWeek)
    )
}

