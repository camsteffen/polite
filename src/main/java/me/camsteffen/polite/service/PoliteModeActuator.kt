package me.camsteffen.polite.service

import android.os.Build
import me.camsteffen.polite.data.AppPreferences
import me.camsteffen.polite.data.db.PoliteStateDao
import me.camsteffen.polite.data.db.entity.ActiveRuleEvent
import me.camsteffen.polite.data.model.RuleEvent
import me.camsteffen.polite.util.AppNotificationManager
import me.camsteffen.polite.util.AppTimingConfig
import org.threeten.bp.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The [PoliteModeActuator] produces the effects of activating a deactivating Polite Mode.
 * More specifically, it changes the ringer mode and shows a notification.
 */
class PoliteModeActuator
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
        Timber.i("Setting current rule event: %s", currentEvent)
        val previousEvent = stateDao.getActiveRuleEvent()
        Timber.d("Previous rule event: %s", previousEvent)

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
        Timber.i("Activating Polite Mode")
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
        Timber.i("Deactivating Polite Mode")
        if (restoreRingerMode) {
            ringerModeManager.restoreRingerMode()
        } else {
            ringerModeManager.clearSavedRingerMode()
        }
        notificationManager.cancelPoliteActive()
        stateDao.setActiveRuleEvent(null)
    }
}
