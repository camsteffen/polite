package me.camsteffen.polite.data.db.entity

import android.annotation.SuppressLint
import androidx.room.ColumnInfo
import me.camsteffen.polite.data.model.InterruptFilter

@SuppressLint("InlinedApi")
val defaultAudioPolicy = AudioPolicy(
    vibrate = true,
    muteMedia = false,
    interruptFilter = InterruptFilter.PRIORITY
)

data class AudioPolicy(

    /// If true, ringer mode and notifications are set to vibrate
    val vibrate: Boolean,

    @ColumnInfo(name = "mute_media")
    val muteMedia: Boolean,

    @ColumnInfo(name = "interrupt_filter")
    val interruptFilter: InterruptFilter
)
