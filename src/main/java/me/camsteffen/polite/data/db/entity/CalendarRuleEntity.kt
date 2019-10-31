package me.camsteffen.polite.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import me.camsteffen.polite.data.model.CalendarEventMatchByEntity

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
data class CalendarRuleEntity(
    @PrimaryKey
    val id: Long,

    @ColumnInfo(name = "busy_only")
    val busyOnly: Boolean,

    @Embedded(prefix = "match_")
    val matchBy: CalendarEventMatchByEntity,

    @ColumnInfo(name = "inverse_match")
    val inverseMatch: Boolean
)
