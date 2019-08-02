package me.camsteffen.polite.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.camsteffen.polite.util.RuleEvent
import org.threeten.bp.Instant

/**
 * A single ActiveRuleEvent entity is stored in the database when Polite is active.
 */
@Entity(tableName = "active_rule_event")
data class ActiveRuleEvent(
    @PrimaryKey
    @ColumnInfo(name = "rule_id")
    val ruleId: Long,
    val begin: Instant,
    val end: Instant,
    val vibrate: Boolean
) {
    constructor(ruleEvent: RuleEvent) : this(
        ruleId = ruleEvent.rule.id,
        begin = ruleEvent.begin,
        end = ruleEvent.end,
        vibrate = ruleEvent.vibrate
    )
}
