package me.camsteffen.polite.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.threeten.bp.Instant

@Entity(
    tableName = "schedule_rule_cancel",
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
class ScheduleRuleCancel(
    @PrimaryKey
    @ColumnInfo(name = "rule_id")
    val ruleId: Long,
    val end: Instant
)
