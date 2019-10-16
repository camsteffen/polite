package me.camsteffen.polite.rule.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.ConfigurationCompat.getLocales
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import me.camsteffen.polite.R
import me.camsteffen.polite.databinding.DayButtonBinding
import me.camsteffen.polite.databinding.EditScheduleRuleBinding
import me.camsteffen.polite.model.ScheduleRule
import me.camsteffen.polite.rule.ScheduleRuleSchedule
import me.camsteffen.polite.util.TimePickerDialogFragment
import org.threeten.bp.LocalTime
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.WeekFields

class EditScheduleRuleFragment : EditRuleFragment<ScheduleRule>(),
    TimePickerDialogFragment.OnTimeSetListener {

    private lateinit var model: EditScheduleRuleViewModel

    override fun onCreateEditRuleViewModel(): EditScheduleRuleViewModel {
        model = ViewModelProviders
            .of(activity!!, viewModelProviderFactory)[EditScheduleRuleViewModel::class.java]
        return model
    }

    override fun onCreateEditRuleView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<EditScheduleRuleBinding>(
            layoutInflater, R.layout.edit_schedule_rule, container, false
        )
        binding.lifecycleOwner = this
        binding.handlers = this
        binding.model = model
        inflateDays(binding.days)
        return binding.root
    }

    private fun inflateDays(parent: ViewGroup) {
        val locale = getLocales(resources.configuration)[0]
        val firstDay = WeekFields.of(locale).firstDayOfWeek
        for (i in 0..6L) {
            val day = firstDay + i
            val binding = DataBindingUtil
                .inflate<DayButtonBinding>(layoutInflater, R.layout.day_button, parent, true)
            binding.text = day.getDisplayName(TextStyle.NARROW, locale)
            binding.checked = model.daysOfWeek[day]
            if (i < 6) {
                layoutInflater.inflate(R.layout.day_button_space, parent, true)
            }
        }
    }

    override fun ruleFromUi(id: Long, name: String, enabled: Boolean, vibrate: Boolean):
        ScheduleRule {
        val days = model.daysOfWeek.asSequence()
            .filter { it.value.get() }
            .map { it.key }
            .toSet()
        val schedule = ScheduleRuleSchedule(model.beginTime.get()!!, model.endTime.get()!!, days)
        return ScheduleRule(id, name, enabled, vibrate, schedule)
    }

    fun onClickBeginTime() {
        showTimePicker(TimePickerCodes.BEGIN, model.beginTime.get()!!)
    }

    fun onClickEndTime() {
        showTimePicker(TimePickerCodes.END, model.endTime.get()!!)
    }

    private fun showTimePicker(code: Int, localTime: LocalTime) {
        TimePickerDialogFragment.newInstance(this, code, localTime)
            .show(fragmentManager!!, TimePickerDialogFragment.FRAGMENT_TAG)
    }

    override fun validateSaveClose() {
        if (model.daysOfWeek.isEmpty()) {
            Toast.makeText(activity, R.string.no_days_selected, Toast.LENGTH_SHORT).show()
        } else {
            saveClose()
        }
    }

    override fun onTimeSet(hourOfDay: Int, minute: Int, requestCode: Int) {
        val timeOfDay = when (requestCode) {
            TimePickerCodes.BEGIN -> model.beginTime
            TimePickerCodes.END -> model.endTime
            else -> throw IllegalArgumentException()
        }
        timeOfDay.set(LocalTime.of(hourOfDay, minute))
    }
}

private object TimePickerCodes {
    const val BEGIN = 0
    const val END = 1
}
