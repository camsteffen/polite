package me.camsteffen.polite.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation

class CalendarRuleEntry(
    @Embedded
    val ruleBase: RuleEntity,

    @Embedded(prefix = "match_")
    val matchBy: CalendarEventMatchByEntity,

    @ColumnInfo(name = "inverse_match")
    val inverseMatch: Boolean,

    @Relation(entity = CalendarRuleCalendar::class, entityColumn = "rule_id", parentColumn = "id", projection = ["calendar_id"])
    val calendarIds: Set<Long>,

    @Relation(entity = CalendarRuleKeyword::class, entityColumn = "rule_id", parentColumn = "id", projection = ["keyword"])
    val keywords: Set<String>
) {
    fun asCalendarRule(): CalendarRule {
        return CalendarRule(
                id = ruleBase.id,
                name = ruleBase.name,
                enabled = ruleBase.enabled,
                vibrate = ruleBase.vibrate,
                inverseMatch = inverseMatch,
                matchBy = matchBy.asCalendarEventMatchBy()!!,
                calendarIds = calendarIds,
                keywords = keywords
        )
    }
}
