package me.camsteffen.polite.util

import android.content.Context
import android.text.format.DateFormat
import org.threeten.bp.LocalTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class TimeOfDay : Comparable<TimeOfDay> {

    private var minutes: Int

    val hour: Int
        get() = minutes / 60
    val minute: Int
        get() = minutes % 60

    constructor(minutes: Int) {
        this.minutes = minutes
    }

    constructor(hour: Int, minute: Int) {
        this.minutes = hour * 60 + minute
    }

    constructor(calendar: Calendar) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        this.minutes = hour * 60 + minute
    }

    operator fun minus(other: TimeOfDay): Int = minutes - other.minutes

    fun set(hours: Int, minutes: Int) {
        this.minutes = hours * 60 + minutes
    }

    fun toInt() = minutes

    fun toString(context: Context): String {
        val cal = GregorianCalendar()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        val pattern = if (DateFormat.is24HourFormat(context)) "k:mm" else "h:mm a"
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        format.calendar = cal
        return format.format(cal.time)
    }

    override fun compareTo(other: TimeOfDay): Int = minutes - other.minutes

    override fun toString(): String = String.format("%d:%02d", hour, minute)

    fun toLocalTime(): LocalTime {
        return LocalTime.of(hour, minute)
    }
}
