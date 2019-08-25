package me.camsteffen.polite

import android.os.AsyncTask
import me.camsteffen.polite.db.RuleDao
import me.camsteffen.polite.model.Rule
import me.camsteffen.polite.state.PoliteStateManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Performs CRUD operations on Polite rules and propagates changes to other components
 */
@Singleton
class RuleService
@Inject constructor(
    private val politeStateManager: PoliteStateManager,
    private val ruleDao: RuleDao
) {
    fun deleteRuleAsync(id: Long) {
        AsyncTask.execute {
            if (ruleDao.deleteRule(id) != 0) {
                politeStateManager.refresh()
            }
        }
    }

    fun saveRuleAsync(rule: Rule) {
        AsyncTask.execute {
            ruleDao.saveRule(rule)
            politeStateManager.refresh()
        }
    }

    fun updateCalendarRulesEnabledAsync(enabled: Boolean) {
        AsyncTask.execute {
            if (ruleDao.updateCalendarRulesEnabled(enabled) != 0) {
                politeStateManager.refresh()
            }
        }
    }

    fun updateRuleEnabledAsync(id: Long, enabled: Boolean) {
        AsyncTask.execute {
            if (ruleDao.updateRuleEnabled(id, enabled) != 0) {
                politeStateManager.refresh()
            }
        }
    }

    fun updateRuleNameAsync(id: Long, name: String) {
        AsyncTask.execute {
            if (ruleDao.updateRuleName(id, name) != 0) {
                politeStateManager.refresh()
            }
        }
    }
}
