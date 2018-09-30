package me.camsteffen.polite.util

import org.threeten.bp.DayOfWeek

fun DayOfWeek.daysAfter(other: DayOfWeek): Int {
    val diff = this.value - other.value
    return if (diff < 0) diff + 7 else diff
}
