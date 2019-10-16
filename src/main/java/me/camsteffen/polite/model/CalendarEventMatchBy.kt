package me.camsteffen.polite.model

import me.camsteffen.polite.R

enum class CalendarEventMatchBy(
    val all: Boolean,
    val title: Boolean,
    val description: Boolean,
    val captionStringId: Int
) {
    ALL(true, false, false, R.string.all_events),
    TITLE(false, true, false, R.string.match_by_title),
    DESCRIPTION(false, false, true, R.string.match_by_desc),
    TITLE_AND_DESCRIPTION(false, true, true, R.string.match_by_title_desc),
    ;

    fun asEntity() = CalendarEventMatchByEntity(all, title, description)

    companion object {
        fun having(all: Boolean, title: Boolean, description: Boolean): CalendarEventMatchBy? {
            return values()
                .find { it.all == all && it.title == title && it.description == description }
        }
    }
}
