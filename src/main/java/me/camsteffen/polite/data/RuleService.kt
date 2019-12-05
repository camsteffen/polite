package me.camsteffen.polite.data

import android.os.AsyncTask
import me.camsteffen.polite.data.db.RuleDao
import me.camsteffen.polite.data.model.Rule
import me.camsteffen.polite.service.PoliteModeManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Performs CRUD operations on Polite rules and propagates changes to other components
 */
@Singleton
class RuleService
@Inject constructor(
    private val politeModeManager: PoliteModeManager,
    private val ruleDao: RuleDao
) {
    fun deleteRuleAsync(id: Long) {
        AsyncTask.execute {
            if (ruleDao.deleteRule(id) != 0) {
                politeModeManager.refresh()
            }
        }
    }

    fun saveRuleAsync(rule: Rule) {
        AsyncTask.execute {
            ruleDao.saveRule(rule)
            politeModeManager.refresh()
        }
    }

    fun updateCalendarRulesEnabledAsync(enabled: Boolean) {
        AsyncTask.execute {
            if (ruleDao.updateCalendarRulesEnabled(enabled) != 0) {
                politeModeManager.refresh()
            }
        }
    }

    fun updateRuleEnabledAsync(id: Long, enabled: Boolean) {
        AsyncTask.execute {
            if (ruleDao.updateRuleEnabled(id, enabled) != 0) {
                politeModeManager.refresh()
            }
        }
    }

    fun updateRuleNameAsync(id: Long, name: String) {
        AsyncTask.execute {
            if (ruleDao.updateRuleName(id, name) != 0) {
                politeModeManager.refresh()
            }
        }
    }
}
