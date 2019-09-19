package me.camsteffen.polite.rule

import android.content.Context
import android.content.res.Resources
import androidx.core.os.ConfigurationCompat.getLocales
import me.camsteffen.polite.R
import me.camsteffen.polite.util.daysAfter
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.WeekFields
import java.util.Locale

fun scheduleSummary(
    context: Context,
    days: Set<DayOfWeek>,
    beginTime: LocalTime,
    endTime: LocalTime
): String {
    val resources = context.resources
    val locale = getLocales(resources.configuration)[0]
    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)

    val daysString = days.toDayRanges(firstDayOfWeek).joinToString { dayRange ->
        dayRange.displayName(resources, locale)
    }

    return "$daysString ${beginTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}"
}

/** Returns a list of all day ranges within a set of days ordered by the number of
 * days after the [firstDayOfWeek] */
private fun Set<DayOfWeek>.toDayRanges(firstDayOfWeek: DayOfWeek): List<DayRange> {
    if (isEmpty()) {
        return emptyList()
    }

    val ranges = mutableListOf<DayRange>()
    val iterator = sortedBy { it.daysAfter(firstDayOfWeek) }.iterator()
    var start = iterator.next()
    var length = 1L

    for (dayOfWeek in iterator) {
        if (dayOfWeek == start + length) {
            length++
        } else {
            ranges.add(DayRange(start, length))
            start = dayOfWeek
            length = 1
        }
    }
    ranges.add(DayRange(start, length))

    return ranges
}

private class DayRange(val start: DayOfWeek, val length: Long) {

    fun displayName(resources: Resources, locale: Locale): String {
        val separator = when (length) {
            7L -> return resources.getString(R.string.every_day)
            1L -> return start.getDisplayName(TextStyle.SHORT, locale)
            2L -> ", "
            else -> " - "
        }
        val end = start + (length - 1)
        return start.getDisplayName(TextStyle.SHORT, locale) + separator +
            end.getDisplayName(TextStyle.SHORT, locale)
    }
}
