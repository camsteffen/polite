package me.camsteffen.polite.state

import me.camsteffen.polite.db.PoliteStateDao
import me.camsteffen.polite.model.ActiveRuleEvent
import me.camsteffen.polite.util.AppNotificationManager
import me.camsteffen.polite.util.RuleEvent
import javax.inject.Inject

/**
 * Activates an deactivates Polite mode according to the current [ActiveRuleEvent]
 */
class PoliteModeController
@Inject constructor(
    private val notificationManager: AppNotificationManager,
    private val ringerModeManager: RingerModeManager,
    private val stateDao: PoliteStateDao
) {

    /**
     * Sets the currently active rule event and updates Polite Mode.
     */
    fun setCurrentEvent(currentEvent: RuleEvent?) {
        val previousEvent = stateDao.getActiveRuleEvent()

        if (currentEvent == null) {
            if (previousEvent != null) {
                deactivate()
            }
        } else if (previousEvent == null) {
            activate(currentEvent, true)
        } else if (ActiveRuleEvent(currentEvent) != previousEvent) {
            activate(currentEvent, false)
        } else {
            // just update the notification in case the text has changed
            notificationManager.notifyPoliteActive(currentEvent.notificationText)
        }
    }

    private fun activate(ruleEvent: RuleEvent, saveRingerMode: Boolean) {
        if (saveRingerMode) {
            ringerModeManager.saveRingerMode()
        }
        ringerModeManager.setRingerMode(ruleEvent.vibrate)
        notificationManager.notifyPoliteActive(ruleEvent.notificationText)
        stateDao.setActiveRuleEvent(ActiveRuleEvent(ruleEvent))
    }

    private fun deactivate() {
        ringerModeManager.restoreRingerMode()
        notificationManager.cancelPoliteActive()
        stateDao.setActiveRuleEvent(null)
    }
}
