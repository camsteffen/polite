package me.camsteffen.polite.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.Instant

@Entity(tableName = "event_cancel")
class EventCancel(
    @PrimaryKey
    @ColumnInfo(name = "event_id")
    val eventId: Long,
    val end: Instant
)
