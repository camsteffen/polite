package me.camsteffen.polite.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "calendar_rule_calendar",
    foreignKeys = [
        ForeignKey(
            entity = RuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["rule_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
class CalendarRuleCalendar(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "rule_id", index = true)
    val ruleId: Long,

    @ColumnInfo(name = "calendar_id")
    val calendarId: Long
)
