package me.camsteffen.polite.rule

import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.camsteffen.polite.db.RuleDao
import me.camsteffen.polite.model.CalendarRule
import me.camsteffen.polite.model.Rule
import me.camsteffen.polite.model.ScheduleRule
import me.camsteffen.polite.rule.master.RuleMasterItem
import me.camsteffen.polite.rule.master.RuleMasterList
import javax.inject.Inject

class RuleMasterDetailViewModel
@Inject constructor(ruleDao: RuleDao, enableLiveData: LiveData<Boolean>) : ViewModel() {

    val toolbarEditText = MutableLiveData<String>()

    val politeEnabled: LiveData<Boolean> = enableLiveData

    val selectedRule = MutableLiveData<Rule>()

    private val calendarRules = ruleDao.calendarRulesSortedByNameLive()
    private val scheduleRules = ruleDao.scheduleRulesSortedByNameLive()

    val ruleMasterList: LiveData<List<RuleMasterItem>> = MediatorLiveData<List<RuleMasterItem>>()
        .apply {
            fun update(calendarRules: List<CalendarRule>?, scheduleRules: List<ScheduleRule>?) {
                calendarRules ?: return
                scheduleRules ?: return
                value = RuleMasterList.of(calendarRules, scheduleRules)
            }

            addSource(calendarRules) { c -> update(c, scheduleRules.value) }
            addSource(scheduleRules) { s -> update(calendarRules.value, s) }

            value = emptyList()
        }

    val enabledCalendarRulesExist: LiveData<Boolean> = ruleDao.getEnabledCalendarRulesExistLive()

    val toolbarEditTextVisibility: ObservableInt = ObservableInt(View.GONE)
}
