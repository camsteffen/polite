package me.camsteffen.polite.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation

class CalendarRuleEntry(
    @Embedded
    var ruleBase: RuleEntity,
    @Embedded(prefix = "match_")
    var matchBy: CalendarEventMatchByEntity,
    @ColumnInfo(name = "inverse_match")
    var inverseMatch: Boolean,
    @Relation(entity = CalendarRuleCalendar::class, entityColumn = "rule_id", parentColumn = "id", projection = ["calendar_id"])
    var calendarIds: Set<Long>,
    @Relation(entity = CalendarRuleKeyword::class, entityColumn = "rule_id", parentColumn = "id", projection = ["keyword"])
    var keywords: Set<String>
) {
    fun asCalendarRule(): CalendarRule {
        return CalendarRule(
                id = ruleBase.id,
                name = ruleBase.name,
                enabled = ruleBase.enabled,
                vibrate = ruleBase.vibrate,
                inverseMatch = inverseMatch,
                matchBy = matchBy.asCalendarEventMatchBy()!!,
                calendarIds = calendarIds.toMutableSet(),
                keywords = keywords.toMutableSet()
        )
    }
}
