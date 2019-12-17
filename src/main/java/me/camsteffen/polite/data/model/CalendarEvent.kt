package me.camsteffen.polite.data.model

import org.threeten.bp.Instant

data class CalendarEvent(
    val eventId: Long,
    val calendarId: Long,
    val title: String?,
    val description: String?,
    val begin: Instant,
    val end: Instant,
    val isBusy: Boolean
)
