package me.camsteffen.polite.state

import androidx.annotation.WorkerThread
import me.camsteffen.polite.AppTimingConfig
import me.camsteffen.polite.db.PoliteStateDao
import me.camsteffen.polite.db.RuleDao
import me.camsteffen.polite.model.EventCancel
import me.camsteffen.polite.model.ScheduleRuleCancel
import me.camsteffen.polite.settings.AppPreferences
import me.camsteffen.polite.util.AppPermissionChecker
import me.camsteffen.polite.util.RuleEvent
import me.camsteffen.polite.util.RuleEventFinders
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@WorkerThread
class PoliteStateManager
@Inject constructor(
    private val clock: Clock,
    private val permissionChecker: AppPermissionChecker,
    private val politeModeController: PoliteModeController,
    private val preferences: AppPreferences,
    private val refreshScheduler: RefreshScheduler,
    private val ruleDao: RuleDao,
    private val ruleEventFinders: RuleEventFinders,
    private val stateDao: PoliteStateDao,
    private val timingConfig: AppTimingConfig
) {

    fun cancel() {
        doRefresh(true)
    }

    fun refresh() {
        doRefresh(false)
    }

    private fun doRefresh(cancel: Boolean) {
        val now = clock.instant()

        if (cancel) {
            cancelCurrentEvents(now)
        }

        if (!preferences.enable || !permissionChecker.checkNotificationPolicyAccess()) {
            politeModeController.setCurrentEvent(null)
            // No need to schedule a refresh since a refresh will occur when the user enables
            // polite mode and/or gives needed permissions
            refreshScheduler.cancelAll()
            return
        }

        val (currentEvent, nextEvent) = findCurrentAndNextEvents(now)

        politeModeController.setCurrentEvent(currentEvent)

        refreshScheduler.run {
            scheduleRefresh(sequenceOf(currentEvent?.end, nextEvent?.begin).filterNotNull().min())
            setRefreshOnCalendarChange(ruleDao.getEnabledCalendarRulesExist())
        }

        stateDao.deleteDeadCancels(now)
    }

    private fun findCurrentAndNextEvents(now: Instant): CurrentAndNextEvents {
        val events = ruleEventFinders.all.eventsInRange(
            now + timingConfig.ruleEventBoundaryTolerance,
            now + timingConfig.lookahead
        )

        var currentEvent: RuleEvent? = null
        var nextEvent: RuleEvent? = null

        for (event in events) {
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
