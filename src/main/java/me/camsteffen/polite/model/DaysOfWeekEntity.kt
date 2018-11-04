package me.camsteffen.polite.model

import org.threeten.bp.DayOfWeek
import org.threeten.bp.DayOfWeek.FRIDAY
import org.threeten.bp.DayOfWeek.MONDAY
import org.threeten.bp.DayOfWeek.SATURDAY
import org.threeten.bp.DayOfWeek.SUNDAY
import org.threeten.bp.DayOfWeek.THURSDAY
import org.threeten.bp.DayOfWeek.TUESDAY
import org.threeten.bp.DayOfWeek.WEDNESDAY
import java.util.EnumSet

class DaysOfWeekEntity(
    val monday: Int,
    val tuesday: Int,
    val wednesday: Int,
    val thursday: Int,
    val friday: Int,
    val saturday: Int,
    val sunday: Int
) {
    constructor(days: Set<DayOfWeek>) : this(
        monday = days.contains(MONDAY).toInt(),
        tuesday = days.contains(TUESDAY).toInt(),
        wednesday = days.contains(WEDNESDAY).toInt(),
        thursday = days.contains(THURSDAY).toInt(),
        friday = days.contains(FRIDAY).toInt(),
        saturday = days.contains(SATURDAY).toInt(),
        sunday = days.contains(SUNDAY).toInt()
    )

    fun toDayOfWeekSet(): Set<DayOfWeek> {
        return EnumSet.noneOf(DayOfWeek::class.java).apply {
            if (monday != 0) add(MONDAY)
            if (tuesday != 0) add(TUESDAY)
            if (wednesday != 0) add(WEDNESDAY)
            if (thursday != 0) add(THURSDAY)
            if (friday != 0) add(FRIDAY)
            if (saturday != 0) add(SATURDAY)
            if (sunday != 0) add(SUNDAY)
        }
    }
}

private fun Boolean.toInt(): Int = if (this) 1 else 0
