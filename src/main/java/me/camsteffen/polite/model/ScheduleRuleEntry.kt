package me.camsteffen.polite.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import org.threeten.bp.LocalTime

class ScheduleRuleEntry(
    @Embedded
    val ruleBase: RuleEntity,
    @ColumnInfo(name = "begin_time")
    val beginTime: LocalTime,
    @ColumnInfo(name = "end_time")
    val endTime: LocalTime,
    @Embedded
    val days: DaysOfWeekEntity
) {
    fun asScheduleRule(): ScheduleRule {
        return ScheduleRule(
            id = ruleBase.id,
            name = ruleBase.name,
            enabled = ruleBase.enabled,
            vibrate = ruleBase.vibrate,
            beginTime = beginTime,
            endTime = endTime,
            daysOfWeek = days.toDayOfWeekSet()
        )
    }
}
