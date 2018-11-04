package me.camsteffen.polite.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rule")
open class RuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val enabled: Boolean,
    val vibrate: Boolean
)
