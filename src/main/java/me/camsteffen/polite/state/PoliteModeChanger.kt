package me.camsteffen.polite.state

import me.camsteffen.polite.db.PoliteStateDao
import me.camsteffen.polite.model.ActiveRuleEvent
import me.camsteffen.polite.util.AppNotificationManager
import me.camsteffen.polite.util.RuleEvent
import javax.inject.Inject

class PoliteModeChanger
@Inject constructor(
    private val notificationManager: AppNotificationManager,
    private val ringerModeManager: RingerModeManager,
    private val stateDao: PoliteStateDao
) {
    fun activate(ruleEvent: RuleEvent, reactivate: Boolean) {
        if (!reactivate) {
            ringerModeManager.saveRingerMode()
        }
        ringerModeManager.setRingerMode(ruleEvent.vibrate)
        notificationManager.notifyPoliteActive(ruleEvent.notificationText)
        stateDao.insertActiveRuleEvent(ActiveRuleEvent(ruleEvent))
    }

    fun deactivate() {
        ringerModeManager.restoreRingerMode()
        notificationManager.cancelPoliteActive()
        stateDao.deleteActiveRuleEvent()
    }
}
