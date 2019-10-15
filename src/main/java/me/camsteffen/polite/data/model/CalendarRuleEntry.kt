package me.camsteffen.polite.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import me.camsteffen.polite.data.db.entity.CalendarRuleCalendar
import me.camsteffen.polite.data.db.entity.CalendarRuleKeyword
import me.camsteffen.polite.data.db.entity.RuleEntity

class CalendarRuleEntry(
    @Embedded
    val ruleBase: RuleEntity,

    @ColumnInfo(name = "busy_only")
    val busyOnly: Boolean,

    @Embedded(prefix = "match_")
    val matchBy: CalendarEventMatchByEntity,

    @ColumnInfo(name = "inverse_match")
    val inverseMatch: Boolean,

    @Relation(
        entity = CalendarRuleCalendar::class,
        entityColumn = "rule_id",
        parentColumn = "id",
        projection = ["calendar_id"]
    )
    val calendarIds: Set<Long>,

    @Relation(
        entity = CalendarRuleKeyword::class,
        entityColumn = "rule_id",
        parentColumn = "id",
        projection = ["keyword"]
    )
    val keywords: Set<String>
) {
    fun asCalendarRule() = CalendarRule(
        id = ruleBase.id,
        name = ruleBase.name,
        enabled = ruleBase.enabled,
        audioPolicy = ruleBase.audioPolicy,
        busyOnly = busyOnly,
        inverseMatch = inverseMatch,
        matchBy = matchBy.asCalendarEventMatchBy()!!,
        calendarIds = calendarIds,
        keywords = keywords
    )
}
