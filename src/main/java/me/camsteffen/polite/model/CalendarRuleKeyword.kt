package me.camsteffen.polite.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(
    tableName = "calendar_rule_keyword",
    foreignKeys = [
        ForeignKey(
            entity = RuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["rule_id"],
            onDelete = CASCADE,
            onUpdate = CASCADE
        )
    ]
)
class CalendarRuleKeyword(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "rule_id", index = true)
    val ruleId: Long,

    val keyword: String
) {
    @Ignore
    constructor(ruleId: Long, keyword: String) : this(0L, ruleId, keyword)
}
