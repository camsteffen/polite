package me.camsteffen.polite.model

import android.content.Context
import me.camsteffen.polite.rule.ScheduleRuleSchedule

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
    val busyOnly: Boolean,
    val matchBy: CalendarEventMatchBy,
    val inverseMatch: Boolean,
    val calendarIds: Set<Long>,
    val keywords: Set<String>
) : Rule() {
    override fun getCaption(context: Context): String {
        return keywords.joinToString()
    }

    fun asCalendarRuleEntity() = CalendarRuleEntity(
        id = id,
        busyOnly = busyOnly,
        matchBy = matchBy.asEntity(),
        inverseMatch = inverseMatch
    )

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
    val schedule: ScheduleRuleSchedule
) : Rule() {
    override fun getCaption(context: Context) = schedule.summary(context)

    fun asScheduleRuleEntity() = ScheduleRuleEntity(
        id = id,
        beginTime = schedule.beginTime,
        endTime = schedule.endTime,
        daysOfWeek = DaysOfWeekEntity(schedule.daysOfWeek)
    )
}
