package me.camsteffen.polite.state

import android.os.Build
import me.camsteffen.polite.AppTimingConfig
import me.camsteffen.polite.db.PoliteStateDao
import me.camsteffen.polite.model.ActiveRuleEvent
import me.camsteffen.polite.settings.AppPreferences
import me.camsteffen.polite.util.AppNotificationManager
import me.camsteffen.polite.util.RuleEvent
import org.threeten.bp.Instant
import javax.inject.Inject

/**
 * Activates an deactivates Polite mode according to the current [ActiveRuleEvent]
 */
class PoliteModeController
@Inject constructor(
    private val notificationManager: AppNotificationManager,
    private val preferences: AppPreferences,
    private val ringerModeManager: RingerModeManager,
    private val stateDao: PoliteStateDao,
    private val timingConfig: AppTimingConfig
) {

    /**
     * Sets the currently active rule event and updates Polite Mode.
     */
    fun setCurrentEvent(currentEvent: RuleEvent?) {
        val previousEvent = stateDao.getActiveRuleEvent()

        when {
            currentEvent == null -> {
                val restoreRingerMode = previousEvent != null &&
                    Instant.now() <= previousEvent.end + timingConfig.maxRingerRestoreDelay
                deactivate(restoreRingerMode)
            }
            previousEvent == null -> activate(currentEvent, true)
            ActiveRuleEvent(currentEvent) != previousEvent -> activate(currentEvent, false)
            else -> // just update the notification in case the text has changed
                notificationManager.notifyPoliteActive(currentEvent.notificationText, true)
        }
    }

    private fun activate(ruleEvent: RuleEvent, saveRingerMode: Boolean) {
        if (saveRingerMode) {
            ringerModeManager.saveRingerMode()
        }
        ringerModeManager.setRingerMode(ruleEvent.vibrate)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || preferences.notifications) {
            notificationManager.notifyPoliteActive(ruleEvent.notificationText, false)
        }
        stateDao.setActiveRuleEvent(ActiveRuleEvent(ruleEvent))
    }

    private fun deactivate(restoreRingerMode: Boolean) {
        if (restoreRingerMode) {
            ringerModeManager.restoreRingerMode()
        } else {
            ringerModeManager.clearSavedRingerMode()
        }
        notificationManager.cancelPoliteActive()
        stateDao.setActiveRuleEvent(null)
    }
}
