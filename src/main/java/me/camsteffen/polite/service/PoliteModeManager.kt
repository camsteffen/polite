package me.camsteffen.polite.service

import androidx.annotation.WorkerThread
import me.camsteffen.polite.data.AppPreferences
import me.camsteffen.polite.data.db.PoliteStateDao
import me.camsteffen.polite.data.db.RuleDao
import me.camsteffen.polite.data.db.entity.EventCancel
import me.camsteffen.polite.data.db.entity.ScheduleRuleCancel
import me.camsteffen.polite.data.model.RuleEvent
import me.camsteffen.polite.util.AppPermissionChecker
import me.camsteffen.polite.util.AppTimingConfig
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The [PoliteModeManager] observes all relevant data to determine and update the state of Polite
 * Mode.
 */
@Singleton
@WorkerThread
class PoliteModeManager
@Inject constructor(
    private val clock: Clock,
    private val permissionChecker: AppPermissionChecker,
    private val politeModeActuator: PoliteModeActuator,
    private val preferences: AppPreferences,
    private val refreshScheduler: RefreshScheduler,
    private val ruleDao: RuleDao,
    private val ruleEventFinders: RuleEventFinders,
    private val stateDao: PoliteStateDao,
    private val timingConfig: AppTimingConfig
) {

    fun cancel() {
        Timber.i("Cancelling Polite Mode")
        doRefresh(true)
    }

    fun refresh() {
        Timber.i("Refreshing Polite Mode")
        doRefresh(false)
    }

    private fun doRefresh(cancel: Boolean) {
        val now = clock.instant()

        if (cancel) {
            cancelCurrentEvents(now)
        }

        if (!preferences.enable ||
            !ruleDao.getEnabledRulesExist() ||
            !permissionChecker.checkNotificationPolicyAccess()
        ) {
            politeModeActuator.setCurrentEvent(null)
            // No need to schedule a refresh since a refresh will be triggered when the user enables
            // Polite, creates a Rule, or gives needed permissions
            refreshScheduler.cancelAll()
            return
        }

        val (currentEvent, nextEvent) = findCurrentAndNextEvents(now)
        Timber.d("Current rule event: %s", currentEvent)
        Timber.d("Next rule event: %s", nextEvent)

        politeModeActuator.setCurrentEvent(currentEvent)

        val refreshTime = sequenceOf(currentEvent?.end, nextEvent?.begin).filterNotNull().min()
        if (refreshTime == null) {
            refreshScheduler.scheduleRefreshInWindow()
        } else {
            refreshScheduler.scheduleRefresh(refreshTime)
        }
        refreshScheduler.setRefreshOnCalendarChange(ruleDao.getEnabledCalendarRulesExist())

        stateDao.deleteDeadCancels(now)
        Timber.i("Refresh completed")
    }

    private fun findCurrentAndNextEvents(now: Instant): CurrentAndNextEvents {
        Timber.d("Finding current and next rule events")
        val events = ruleEventFinders.all.eventsInRange(
            now + timingConfig.ruleEventBoundaryTolerance,
            now + timingConfig.lookahead
        )

        var currentEvent: RuleEvent? = null
        var nextEvent: RuleEvent? = null

        for (event in events) {
            Timber.d("Rule event: %s", event)
            // if the event is occurring now
            if (event.begin <= now + timingConfig.ruleEventBoundaryTolerance) {
                if (currentEvent == null || event.vibrate < currentEvent.vibrate) {
                    currentEvent = event
                }
            } else {
                if (currentEvent == null ||
                    event.begin >= currentEvent.end ||
                    event.vibrate <= currentEvent.vibrate
                ) {
                    nextEvent = event
                }
                // disregard any events further in the future
                break
            }
        }

        return CurrentAndNextEvents(currentEvent, nextEvent)
    }

    private fun cancelCurrentEvents(now: Instant) {
        val eventCancels = ruleEventFinders.calendarRules.allEventsAt(now)
            .map { EventCancel(it.rule.id, it.event.eventId, it.end) }
        if (eventCancels.isNotEmpty()) {
            stateDao.insertEventCancels(*eventCancels.toTypedArray())
        }

        val scheduleRuleCancels = ruleEventFinders.scheduleRules.allEventsAt(now)
            .map { ScheduleRuleCancel(it.rule.id, it.end) }
        if (scheduleRuleCancels.isNotEmpty()) {
            stateDao.insertScheduleRuleCancels(*scheduleRuleCancels.toTypedArray())
        }
    }
}

private data class CurrentAndNextEvents(val currentEvent: RuleEvent?, val nextEvent: RuleEvent?)
