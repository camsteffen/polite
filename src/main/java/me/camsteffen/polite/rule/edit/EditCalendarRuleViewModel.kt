package me.camsteffen.polite.rule.edit

import android.app.Application
import me.camsteffen.polite.model.CalendarRule
import javax.inject.Inject

class EditCalendarRuleViewModel
@Inject constructor(application: Application) : EditRuleViewModel<CalendarRule>(application) {

}
