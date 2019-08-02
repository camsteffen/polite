package me.camsteffen.polite.util

import me.camsteffen.polite.data.CalendarEvent
import me.camsteffen.polite.model.CalendarRule
import me.camsteffen.polite.model.Rule
import me.camsteffen.polite.model.ScheduleRule
import org.threeten.bp.Duration
import org.threeten.bp.Instant

/** A span of time where a [Rule] is active */
interface RuleEvent {
    val rule: Rule
    val begin: Instant
    val end: Instant
    val duration: Duration get() = Duration.between(begin, end)
    val notificationText: String
    val vibrate get() = rule.vibrate
}

data class CalendarRuleEvent(
    override val rule: CalendarRule,
    val event: CalendarEvent
) : RuleEvent {
    val eventId = event.eventId
    override val begin get() = event.begin
    override val end get() = event.end
    override val notificationText get() = event.title ?: rule.name
}

data class ScheduleRuleEvent(
    override val rule: ScheduleRule,
    override val begin: Instant,
    override val end: Instant
) : RuleEvent {
    override val notificationText get() = rule.name
}
