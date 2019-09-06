package me.camsteffen.polite.model

import android.content.Context
import me.camsteffen.polite.R
import me.camsteffen.polite.rule.scheduleSummary
import me.camsteffen.polite.util.TimeOfDay
import org.threeten.bp.DayOfWeek
import java.util.EnumSet


sealed class Rule {

    companion object {
        const val NEW_RULE = 0L
    }

    abstract var id: Long
    abstract var name: String
    abstract var enabled: Boolean
    abstract var vibrate: Boolean

    open fun getCaption(context: Context): String = ""

    fun asRuleEntity() = RuleEntity(id, name, enabled, vibrate)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Rule

        if (id != other.id) return false
        if (name != other.name) return false
        if (enabled != other.enabled) return false
        if (vibrate != other.vibrate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + enabled.hashCode()
        result = 31 * result + vibrate.hashCode()
        return result
    }

}

data class CalendarRule(
    override var id: Long,
    override var name: String,
    override var enabled: Boolean,
    override var vibrate: Boolean,
    var matchBy: CalendarEventMatchBy,
    var inverseMatch: Boolean,
    val calendarIds: MutableSet<Long>,
    val keywords: MutableSet<String>
) : Rule() {

    constructor(context: Context) : this(
        id = NEW_RULE,
        name = context.getString(R.string.rule_default_name),
        enabled = true,
        vibrate = false,
        calendarIds = mutableSetOf(),
        matchBy = CalendarEventMatchBy.ALL,
        inverseMatch = false,
        keywords = mutableSetOf()
    )

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
    override var id: Long,
    override var name: String,
    override var enabled: Boolean,
    override var vibrate: Boolean,
    val begin: TimeOfDay,
    val end: TimeOfDay,
    val days: MutableSet<DayOfWeek>
) : Rule() {

    constructor(context: Context) : this(
        id = NEW_RULE,
        name = context.getString(R.string.rule_default_name),
        enabled = true,
        vibrate = false,
        begin = TimeOfDay(12, 0),
        end = TimeOfDay(13, 0),
        days = EnumSet.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
    )

    override fun getCaption(context: Context) = scheduleSummary(context, days, begin, end)
    fun asScheduleRuleEntity() = ScheduleRuleEntity(
        id = id,
        beginTime = begin.toLocalTime(),
        endTime = end.toLocalTime(),
        days = DaysOfWeekEntity(days)
    )
}

