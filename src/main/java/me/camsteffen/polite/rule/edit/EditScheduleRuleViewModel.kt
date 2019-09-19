package me.camsteffen.polite.rule.edit

import android.app.Application
import androidx.core.os.ConfigurationCompat.getLocales
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import me.camsteffen.polite.R
import me.camsteffen.polite.model.ScheduleRule
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Duration
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.EnumMap
import javax.inject.Inject

class EditScheduleRuleViewModel
@Inject constructor(application: Application) : EditRuleViewModel<ScheduleRule>(application) {

    val begin = ObservableField<LocalTime>()
    val end = ObservableField<LocalTime>()

    val days = EnumMap<DayOfWeek, ObservableBoolean>(DayOfWeek::class.java)

    init {
        for (day in DayOfWeek.values()) {
            days[day] = ObservableBoolean()
        }
    }

    val beginDisplay = localTimeDisplay(begin)
    val endDisplay = localTimeDisplay(end)

    val durationDisplay = object : ObservableField<String>(begin, end) {
        override fun get(): String? {
            var duration = Duration.between(begin.get(), end.get())
            if (duration.isNegative) {
                duration = duration.plusDays(1)
            }
            return if (duration < Duration.ofHours(1))
                application.getString(R.string.duration_format_minutes, duration.toMinutes())
            else
                application.getString(R.string.duration_format, duration.toHours(),
                    duration.toMinutes() % 60)
        }
    }

    override fun setRule(rule: ScheduleRule) {
        super.setRule(rule)
        begin.set(rule.beginTime)
        end.set(rule.endTime)
        for (entry in days.entries) {
            entry.value.set(rule.daysOfWeek.contains(entry.key))
        }
    }

    private fun localTimeDisplay(localTime: ObservableField<LocalTime>) =
            object : ObservableField<String>(localTime) {
                override fun get(): String? {
                    val application = getApplication<Application>()
                    val locale = getLocales(application.resources.configuration)[0]
                    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                        .withLocale(locale)
                    return localTime.get()!!.format(formatter)
                }
            }
}
