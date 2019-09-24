package me.camsteffen.polite.data

import android.Manifest.permission.READ_CALENDAR
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.provider.CalendarContract
import android.provider.CalendarContract.Instances
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import me.camsteffen.polite.data.Query.calendarEventOf
import me.camsteffen.polite.model.CalendarEntity
import org.threeten.bp.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val CALENDAR_PROJECTION = arrayOf(
    CalendarContract.Calendars._ID,
    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)

private const val CALENDAR_ORDER_BY = "${CalendarContract.Calendars.NAME} COLLATE NOCASE ASC"

@Singleton
@WorkerThread
class CalendarDao
@Inject constructor(
    private val contentResolver: ContentResolver
) {

    @SuppressLint("MissingPermission")
    fun getCalendars(): List<CalendarEntity>? {
        val cursor = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI, CALENDAR_PROJECTION, null, null,
            CALENDAR_ORDER_BY
        )
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

    @RequiresPermission(READ_CALENDAR)
    fun getEventsInRange(begin: Instant, end: Instant): List<CalendarEvent> {
        return getEventsInRange(begin.toEpochMilli(), end.toEpochMilli())
    }

    private fun getEventsInRange(begin: Long, end: Long): List<CalendarEvent> {
        Query.execute(contentResolver, begin, end).use { cursor ->
            cursor ?: return emptyList()
            return generateSequence { cursor.takeIf(Cursor::moveToNext) }
                .map(::calendarEventOf)
                .toList()
        }
    }
}

private object Query {
    val PROJECTION = arrayOf(
        Instances.EVENT_ID,
        Instances.CALENDAR_ID,
        Instances.TITLE,
        Instances.DESCRIPTION,
        Instances.BEGIN,
        Instances.END,
        Instances.AVAILABILITY
    )

    val MAX_DURATION = TimeUnit.HOURS.toMillis(8)
    val SELECTION = "${Instances.ALL_DAY}=0" +
            " AND ${Instances.END} - ${Instances.BEGIN} < $MAX_DURATION"
    const val SORT = "${Instances.BEGIN} ASC"

    object Index {
        const val EVENT_ID = 0
        const val CALENDAR_ID = 1
        const val TITLE = 2
        const val DESCRIPTION = 3
        const val BEGIN = 4
        const val END = 5
        const val AVAILABILITY = 6
    }

    fun execute(contentResolver: ContentResolver, begin: Long, end: Long): Cursor? {
        val builder = Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, begin)
        ContentUris.appendId(builder, end)
        return contentResolver.query(builder.build(), PROJECTION, SELECTION, emptyArray(), SORT)
    }

    fun calendarEventOf(cursor: Cursor) = CalendarEvent(
        eventId = cursor.getLong(Index.EVENT_ID),
        calendarId = cursor.getLong(Index.CALENDAR_ID),
        title = cursor.getString(Index.TITLE),
        description = cursor.getString(Index.DESCRIPTION),
        begin = Instant.ofEpochMilli(cursor.getLong(Index.BEGIN)),
        end = Instant.ofEpochMilli(cursor.getLong(Index.END)),
        isBusy = cursor.getInt(Index.AVAILABILITY) == Instances.AVAILABILITY_BUSY
    )
}
