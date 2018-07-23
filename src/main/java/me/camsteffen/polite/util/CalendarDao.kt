package me.camsteffen.polite.util

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.CalendarContract
import me.camsteffen.polite.model.CalendarEntity
import javax.inject.Inject

private val CALENDAR_PROJECTION = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)

private const val CALENDAR_ORDER_BY = "${CalendarContract.Calendars.NAME} COLLATE NOCASE ASC"

class CalendarDao
@Inject constructor(private val contentResolver: ContentResolver) {

    @SuppressLint("MissingPermission")
    fun getCalendars(): List<CalendarEntity>? {
        val cursor = contentResolver.query(CalendarContract.Calendars.CONTENT_URI, CALENDAR_PROJECTION, null, null,
                CALENDAR_ORDER_BY)
                ?: return null
        cursor.use {
            val calendars = mutableListOf<CalendarEntity>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val name = cursor.getString(1)
                val calendar = CalendarEntity(id, name)
                calendars.add(calendar)
            }
            return calendars
        }
    }
}
