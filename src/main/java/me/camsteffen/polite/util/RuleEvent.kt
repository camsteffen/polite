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
    override val begin: Instant,
    override val end: Instant,
    val event: CalendarEvent
) : RuleEvent {
    override val notificationText = event.title.takeUnless(String?::isNullOrBlank) ?: rule.name

    constructor(
        rule: CalendarRule,
        event: CalendarEvent,
        activation: Duration = Duration.ZERO,
        deactivation: Duration = Duration.ZERO
    ) : this(
        rule,
        event.begin - activation,
        event.end + deactivation,
        event
    )
}

data class ScheduleRuleEvent(
    override val rule: ScheduleRule,
    override val begin: Instant,
    override val end: Instant
) : RuleEvent {
    override val notificationText get() = rule.name
}
