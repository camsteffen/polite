package me.camsteffen.polite.rule.edit

import android.app.Application
import androidx.core.os.ConfigurationCompat.getLocales
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import me.camsteffen.polite.model.ScheduleRule
import me.camsteffen.polite.rule.ScheduleRuleTimes
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.EnumMap
import javax.inject.Inject

class EditScheduleRuleViewModel
@Inject constructor(application: Application) : EditRuleViewModel<ScheduleRule>(application) {

    val beginTime = ObservableField<LocalTime>()
    val endTime = ObservableField<LocalTime>()

    val daysOfWeek = EnumMap<DayOfWeek, ObservableBoolean>(DayOfWeek::class.java)

    init {
        for (day in DayOfWeek.values()) {
            daysOfWeek[day] = ObservableBoolean()
        }
    }

    val beginDisplay = localTimeDisplay(beginTime)
    val endDisplay = localTimeDisplay(endTime)

    val durationDisplay = object : ObservableField<String>(beginTime, endTime) {
        override fun get(): String? {
            return ScheduleRuleTimes(beginTime.get()!!, endTime.get()!!)
                .eventDurationDisplay(application.resources)
        }
    }

    override fun setRule(rule: ScheduleRule) {
        super.setRule(rule)
        beginTime.set(rule.schedule.beginTime)
        endTime.set(rule.schedule.endTime)
        for ((dayOfWeek, value) in daysOfWeek) {
            value.set(rule.schedule.daysOfWeek.contains(dayOfWeek))
        }
    }

    private fun localTimeDisplay(localTime: ObservableField<LocalTime>): ObservableField<String> {
        return object : ObservableField<String>(localTime) {
            override fun get(): String? {
                val application = getApplication<Application>()
                val locale = getLocales(application.resources.configuration)[0]
                val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                    .withLocale(locale)
                return localTime.get()!!.format(formatter)
            }
        }
    }
}
