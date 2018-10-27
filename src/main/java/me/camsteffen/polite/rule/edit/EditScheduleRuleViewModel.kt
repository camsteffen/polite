package me.camsteffen.polite.rule.edit

import android.app.Application
import me.camsteffen.polite.model.ScheduleRule
import javax.inject.Inject

class EditScheduleRuleViewModel
@Inject constructor(application: Application) : EditRuleViewModel<ScheduleRule>(application) {

}

