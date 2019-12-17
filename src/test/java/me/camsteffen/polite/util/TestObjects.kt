package me.camsteffen.polite.util

import me.camsteffen.polite.data.model.CalendarEvent
import me.camsteffen.polite.data.model.CalendarEventMatchBy
import me.camsteffen.polite.data.model.CalendarRule
import me.camsteffen.polite.data.model.CalendarRuleEvent
import me.camsteffen.polite.data.model.ScheduleRule
import me.camsteffen.polite.data.model.ScheduleRuleSchedule
import org.threeten.bp.Instant

object TestObjects {
    private var calendarEventId = 1L
    private var calendarRuleId = 100L

    fun calendarEvent(
        eventId: Long? = null,
        calendarId: Long = 1000L,
        title: String = "",
        description: String = "",
        begin: Instant = Instant.EPOCH,
        end: Instant = Instant.EPOCH,
        isBusy: Boolean = false
    ): CalendarEvent {
        return CalendarEvent(
            eventId = eventId ?: calendarEventId++,
            calendarId = calendarId,
            title = title,
            description = description,
            begin = begin,
            end = end,
            isBusy = isBusy
        )
    }

    fun calendarRule(
        vibrate: Boolean = false,
        busyOnly: Boolean = false,
        matchBy: CalendarEventMatchBy = CalendarEventMatchBy.ALL,
        inverseMatch: Boolean = false,
        calendarIds: Set<Long> = emptySet(),
        keywords: Set<String> = emptySet()
    ): CalendarRule {
        return CalendarRule(
            id = calendarRuleId++,
            name = "",
            enabled = true,
            vibrate = vibrate,
            busyOnly = busyOnly,
            matchBy = matchBy,
            inverseMatch = inverseMatch,
            calendarIds = calendarIds,
            keywords = keywords
        )
    }

    fun scheduleRule(schedule: ScheduleRuleSchedule): ScheduleRule {
        return ScheduleRule(
            id = 0,
            name = "",
            enabled = true,
            vibrate = false,
            schedule = schedule
        )
    }

    fun calendarRuleEvent(
        begin: Instant = Instant.EPOCH,
        end: Instant = Instant.EPOCH,
        vibrate: Boolean = false
    ): CalendarRuleEvent {
        return CalendarRuleEvent(
            calendarRule(vibrate = vibrate),
            calendarEvent(begin = begin, end = end))
    }
}
