package me.camsteffen.polite.model

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
    val rule_id: Long,
    val end: Instant
)
