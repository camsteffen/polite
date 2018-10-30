package me.camsteffen.polite.rule.edit

import android.app.Application
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import me.camsteffen.polite.R
import me.camsteffen.polite.model.ScheduleRule
import me.camsteffen.polite.util.TimeOfDay
import org.threeten.bp.DayOfWeek
import java.util.EnumMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EditScheduleRuleViewModel
@Inject constructor(application: Application) : EditRuleViewModel<ScheduleRule>(application) {

    val begin = ObservableField<TimeOfDay>()
    val end = ObservableField<TimeOfDay>()

    val days = EnumMap<DayOfWeek, ObservableBoolean>(DayOfWeek::class.java)

    init {
        for (day in DayOfWeek.values()) {
            days[day] = ObservableBoolean()
        }
    }

    val beginDisplay = timeOfDayDisplay(begin)
    val endDisplay = timeOfDayDisplay(end)

    val durationDisplay = object : ObservableField<String>(begin, end) {
        override fun get(): String? {
            val begin = begin.get()!!
            val end = end.get()!!
            val duration = if (begin < end)
                end - begin
            else
                TimeUnit.DAYS.toMinutes(1).toInt() + (end - begin)
            return if (duration < 60)
                application.getString(R.string.duration_format_minutes, duration)
            else
                application.getString(R.string.duration_format, duration / 60, duration % 60)
        }
    }

    override fun setRule(rule: ScheduleRule) {
        super.setRule(rule)
        begin.set(rule.begin)
        end.set(rule.end)
        for (entry in days.entries) {
            entry.value.set(rule.days.contains(entry.key))
        }
    }

    private fun timeOfDayDisplay(timeOfDay: ObservableField<TimeOfDay>) =
            object : ObservableField<String>(timeOfDay) {
                override fun get(): String? {
                    return timeOfDay.get()!!.toString(getApplication())
                }
            }
}
