package me.camsteffen.polite.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "calendar_rule",
    foreignKeys = [
        ForeignKey(
            entity = RuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
class CalendarRuleEntity(
    @PrimaryKey
    var id: Long,

    @Embedded(prefix = "match_")
    var matchBy: CalendarEventMatchByEntity,

    @ColumnInfo(name = "inverse_match")
    var inverseMatch: Boolean
)
