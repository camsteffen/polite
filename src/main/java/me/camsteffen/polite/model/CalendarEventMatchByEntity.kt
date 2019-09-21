package me.camsteffen.polite.model

class CalendarEventMatchByEntity(
    var all: Boolean = false,
    var title: Boolean = false,
    var description: Boolean = false
) {
    fun asCalendarEventMatchBy() = CalendarEventMatchBy.having(all, title, description)
}
