package me.camsteffen.polite.model

class CalendarEventMatchByEntity(
    val all: Boolean = false,
    val title: Boolean = false,
    val description: Boolean = false
) {
    fun asCalendarEventMatchBy() = CalendarEventMatchBy.having(all, title, description)
}
