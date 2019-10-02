package me.camsteffen.polite.util

import androidx.annotation.WorkerThread
import me.camsteffen.polite.data.CalendarDao
import me.camsteffen.polite.data.CalendarEvent
import me.camsteffen.polite.db.PoliteStateDao
import me.camsteffen.polite.db.RuleDao
import me.camsteffen.polite.model.CalendarRule
import me.camsteffen.polite.model.EventCancel
import me.camsteffen.polite.settings.AppPreferences
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleEventFinders
@Inject constructor(
    val all: AllRuleEventFinder,
    val calendarRules: CalendarRuleEventFinder,
    val scheduleRules: ScheduleRuleEventFinder
)

@WorkerThread
interface RuleEventFinder<out T : RuleEvent> {

    fun allEventsAt(instant: Instant): List<T> {
        return eventsInRange(instant, instant).toList()
    }

    fun eventsInRange(begin: Instant, end: Instant): Sequence<T>
}

/**
 * Finds [RuleEvent]s for all rules
 */
@Singleton
@WorkerThread
class AllRuleEventFinder
@Inject constructor(
    private val finders: Set<@JvmSuppressWildcards RuleEventFinder<*>>
) : RuleEventFinder<RuleEvent> {

    override fun eventsInRange(begin: Instant, end: Instant): Sequence<RuleEvent> {
        return finders.map { it.eventsInRange(begin, end) }
            .mergeSortedBy { it.begin }
    }
}

@Singleton
@WorkerThread
class CalendarRuleEventFinder
@Inject constructor(
    private val appPreferences: AppPreferences,
    private val permissionChecker: AppPermissionChecker,
    private val politeStateDao: PoliteStateDao,
    private val ruleDao: RuleDao,
    private val calendarDao: CalendarDao
) : RuleEventFinder<CalendarRuleEvent> {

    override fun eventsInRange(begin: Instant, end: Instant): Sequence<CalendarRuleEvent> {
        val calendarRules = ruleDao.getEnabledCalendarRules()
        // don't check calendar permission unless there are enabled calendar rules
        if (calendarRules.isEmpty() || !permissionChecker.checkReadCalendarPermission()) {
            return emptySequence()
        }

        val deactivation = Duration.ofMinutes(appPreferences.deactivation.toLong())
        val activation = Duration.ofMinutes(appPreferences.activation.toLong())

        val eventCancels = politeStateDao.getEventCancels()
            .associate { it.key() to it.end }
        val events = calendarDao.getEventsInRange(begin - deactivation, end + activation)
        return events.asSequence()
            .flatMap { event ->
                calendarRules.asSequence()
                    .filter { rule ->
                        val cancelEnd = eventCancels[EventCancel.Key(rule.id, event.eventId)]
                        val cancelled = cancelEnd != null && event.begin - activation < cancelEnd
                        !cancelled && rule.matches(event)
                    }
                    .map { rule -> CalendarRuleEvent(rule, event, activation, deactivation) }
            }
    }
}

private fun CalendarRule.matches(event: CalendarEvent): Boolean {
    if (calendarIds.isNotEmpty() && !calendarIds.contains(event.calendarId)) {
        return false
    }
    if (busyOnly && !event.isBusy) {
        return false
    }
    if (matchBy.all) return true
    val fields = listOf(
        event.title.takeIf { matchBy.title },
        event.description.takeIf { matchBy.description })
    val match = fields.any { field ->
        !field.isNullOrBlank() && keywords.any { word -> field.contains(word, ignoreCase = true) }
    }
    return match.xor(inverseMatch)
}

@Singleton
@WorkerThread
class ScheduleRuleEventFinder
@Inject constructor(
    private val clock: Clock,
    private val ruleDao: RuleDao,
    private val stateDao: PoliteStateDao
) : RuleEventFinder<ScheduleRuleEvent> {

    override fun eventsInRange(begin: Instant, end: Instant): Sequence<ScheduleRuleEvent> {
        val rules = ruleDao.getEnabledScheduleRules()
        if (rules.isEmpty()) return emptySequence()
        val cancels = stateDao.getScheduleRuleCancels().associate { it.ruleId to it.end }

        val beginLocal = LocalDateTime.ofInstant(begin, clock.zone)
        val endLocal = LocalDateTime.ofInstant(end, clock.zone)

        return rules
            .mapNotNull { rule ->
                val cancelEnd = cancels[rule.id]
                var scheduleBegin = beginLocal
                if (cancelEnd != null) {
                    if (cancelEnd >= end) return@mapNotNull null
                    if (cancelEnd > begin) {
                        scheduleBegin = LocalDateTime.ofInstant(cancelEnd, clock.zone)
                    }
                }
                rule.schedule.eventSequence(scheduleBegin, endLocal, clock.zone)
                    .dropWhile { event -> cancelEnd != null && event.begin < cancelEnd }
                    .map { event -> ScheduleRuleEvent(rule, event.begin, event.end) }
            }
            .mergeSortedBy { it.begin }
    }
}
