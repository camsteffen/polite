package me.camsteffen.polite.data

import android.Manifest.permission.READ_CALENDAR
import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.provider.CalendarContract.Instances
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import me.camsteffen.polite.data.Query.calendarEventOf
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@WorkerThread
class CalendarFacade
@Inject constructor(
    private val contentResolver: ContentResolver
) {

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
        Instances.END
    )

    const val SELECTION = "${Instances.ALL_DAY}=0"
    const val SORT = "${Instances.BEGIN} ASC"

    object Index {
        const val EVENT_ID = 0
        const val CALENDAR_ID = 1
        const val TITLE = 2
        const val DESCRIPTION = 3
        const val BEGIN = 4
        const val END = 5
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
        end = Instant.ofEpochMilli(cursor.getLong(Index.END))
    )
}
