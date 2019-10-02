package me.camsteffen.polite.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import org.threeten.bp.Instant

@Entity(
    tableName = "event_cancel",
    primaryKeys = ["rule_id", "event_id"],
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
class EventCancel(
    @ColumnInfo(name = "rule_id")
    val ruleId: Long,
    @ColumnInfo(name = "event_id")
    val eventId: Long,
    val end: Instant
) {
    fun key() = Key(ruleId, eventId)

    data class Key(val ruleId: Long, val eventId: Long)
}
