package me.camsteffen.polite.rule

import android.content.Context
import android.content.res.Resources
import androidx.core.os.ConfigurationCompat.getLocales
import me.camsteffen.polite.R
import me.camsteffen.polite.util.daysAfter
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.TemporalAdjusters
import org.threeten.bp.temporal.WeekFields
import java.util.EnumSet

interface HasScheduleRuleTimes {
    val beginTime: LocalTime
    val endTime: LocalTime

    val isOvernight: Boolean get() = beginTime >= endTime

    /**
     * Returns the duration of events in this schedule (in the absence of a timezone offset change)
     */
    fun eventDuration(): Duration = Duration.between(beginTime, endTime)
        .run { if (isOvernight) plusDays(1) else this }

    fun eventDurationDisplay(resources: Resources): String {
        var duration = eventDuration()
        if (duration.isNegative) {
            duration = duration.plusDays(1)
        }
        val minutes = duration.toMinutes()
        return if (minutes < 60) {
            resources.getString(R.string.duration_format_minutes, minutes)
        } else {
            resources.getString(R.string.duration_format, duration.toHours(), minutes % 60)
        }
    }
}

class ScheduleRuleTimes(
    override val beginTime: LocalTime,
    override val endTime: LocalTime
) : HasScheduleRuleTimes

/**
 * A ScheduleRuleSchedule defines when a Schedule Rule is active based on a begin time, end time
 * and a set of days of the week.
 *
 * Events begin on every day in [daysOfWeek] at [beginTime].
 * Events end at the soonest occurrence of [endTime], which may be on the following day.
 */
data class ScheduleRuleSchedule(
    override val beginTime: LocalTime,
    override val endTime: LocalTime,
    val daysOfWeek: Set<DayOfWeek>
) : HasScheduleRuleTimes {

    constructor(
        beginHour: Int,
        beginMinute: Int,
        endHour: Int,
        endMinute: Int,
        vararg days: DayOfWeek
    ) : this(
        beginTime = LocalTime.of(beginHour, beginMinute),
        endTime = LocalTime.of(endHour, endMinute),
        daysOfWeek = if (days.isEmpty()) emptySet() else EnumSet.copyOf(days.asList())
    )

    data class Event(val begin: Instant, val end: Instant)

    data class LocalEvent(val begin: LocalDateTime, val end: LocalDateTime) {
        fun atZone(zone: ZoneId) = Event(
            begin.atZone(zone).toInstant(),
            end.atZone(zone).toInstant()
        )
    }

    /**
     * Finds an event occurrence that overlaps the specified date/time or null if none exists
     */
    fun eventAt(target: LocalDateTime): LocalEvent? {
        val date = target.toLocalDate()
        val time = target.toLocalTime()
        val dayOfWeek = target.dayOfWeek
        if (isOvernight) {
            if (daysOfWeek.contains(dayOfWeek) && time >= beginTime) {
                return LocalEvent(date.atTime(beginTime), date.plusDays(1).atTime(endTime))
            } else if (daysOfWeek.contains(dayOfWeek - 1) && time < endTime) {
                return LocalEvent(date.minusDays(1).atTime(beginTime), date.atTime(endTime))
            }
        } else if (daysOfWeek.contains(dayOfWeek) && time >= beginTime && time < endTime) {
            return LocalEvent(date.atTime(beginTime), date.atTime(endTime))
        }
        return null
    }

    /**
     * Finds the first event that begins after the specified date/time
     * or null if this schedule has no days
     */
    fun firstEventAfter(target: LocalDateTime): LocalEvent? {
        val soonestDate = target.toLocalDate().run {
            if (target.toLocalTime() <= beginTime) this else plusDays(1)
        }
        val soonestDay = soonestDate.dayOfWeek
        val dayOfWeek = daysOfWeek.asSequence().minBy { it.daysAfter(soonestDay) }
            ?: return null
        val beginDate = soonestDate.with(TemporalAdjusters.nextOrSame(dayOfWeek))
        val endDate = if (isOvernight) beginDate.plusDays(1) else beginDate
        return LocalEvent(beginDate.atTime(beginTime), endDate.atTime(endTime))
    }

    /**
     * Generates a lazy sequence of events that occur in the specified time range.
     * Events that are partially in the time range are included.
     */
    fun localEventSequence(begin: LocalDateTime, end: LocalDateTime): Sequence<LocalEvent> {
        if (daysOfWeek.isEmpty()) return emptySequence()
        var adjust = 0L
        if (begin.toLocalTime() >= endTime) adjust++
        if (isOvernight) adjust--
        val soonestDate = begin.toLocalDate().plusDays(adjust)
        val soonestDayOfWeek = soonestDate.dayOfWeek
        val sortedDaysOfWeek = daysOfWeek.sortedBy { it.daysAfter(soonestDayOfWeek) }
        var beginDate = soonestDate.minusDays(1)
        return sequence {
            for (dayOfWeek in generateSequence { sortedDaysOfWeek }.flatten()) {
                beginDate = beginDate.with(TemporalAdjusters.next(dayOfWeek))
                val beginDateTime = beginDate.atTime(beginTime)
                if (beginDateTime >= end) return@sequence
                val endDate = if (isOvernight) beginDate.plusDays(1) else beginDate
                yield(LocalEvent(beginDateTime, endDate.atTime(endTime)))
            }
        }
    }

    /**
     * Same as [localEventSequence] but with event times adjusted for the specified time zone
     */
    fun eventSequence(begin: LocalDateTime, end: LocalDateTime, zone: ZoneId): Sequence<Event> {
        return localEventSequence(begin, end)
            .map { event -> event.atZone(zone) }
    }

    fun summary(context: Context): String {
        val resources = context.resources
        val locale = getLocales(resources.configuration)[0]
        val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
        val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)

        fun DayOfWeek.shortName() = getDisplayName(TextStyle.SHORT, locale)

        fun DayRange.displayName(): String {
            val separator = when (length) {
                7 -> return resources.getString(R.string.every_day)
                1 -> return start.shortName()
                2 -> ", "
                else -> " - "
            }
            val end = start + (length - 1).toLong()
            return start.shortName() + separator + end.shortName()
        }

        val daysString = daysOfWeek.toDayRanges(firstDayOfWeek)
            .joinToString(transform = DayRange::displayName)

        return "$daysString ${beginTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}"
    }
}

private class DayRange(val start: DayOfWeek, val length: Int)

/** Returns a list of all day ranges within a set of days ordered by the number of
 * days after the [firstDayOfWeek] */
private fun Set<DayOfWeek>.toDayRanges(firstDayOfWeek: DayOfWeek): Sequence<DayRange> {
    return sequence {
        val days = sortedBy { it.daysAfter(firstDayOfWeek) }
        var begin = 0
        while (begin < days.size) {
            var end = (begin + 1 until days.size).asSequence()
                .takeWhile { i -> days[i] == days[i - 1] + 1 }
                .lastOrNull() ?: begin
            end++
            yield(DayRange(days[begin], end - begin))
            begin = end
        }
    }
}
