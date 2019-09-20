package me.camsteffen.polite.rule.edit

import android.content.Context
import android.os.Bundle
import android.support.v4.os.ConfigurationCompat.getLocales
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import me.camsteffen.polite.R
import me.camsteffen.polite.model.ScheduleRule
import me.camsteffen.polite.util.TimeOfDay
import me.camsteffen.polite.util.TimePickerDialogFragment
import me.camsteffen.polite.view.ValueOption
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.WeekFields
import java.util.concurrent.TimeUnit

private const val BEGIN = 0
private const val END = 1

class EditScheduleRuleFragment : EditRuleFragment<ScheduleRule>(), TimePickerDialogFragment.OnTimeSetListener {

    private val beginTime : ValueOption
        get() = view.findViewById(R.id.start_time) as ValueOption
    private val endTime : ValueOption
        get() = view.findViewById(R.id.end_time) as ValueOption

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.edit_schedule_rule_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val daysView = view!!.findViewById(R.id.days) as LinearLayout
        val locale = getLocales(resources.configuration)[0]
        val firstDay = WeekFields.of(locale).firstDayOfWeek
        for (i in 0..6L) {
            val day = firstDay + i
            val dayView = layoutInflater.inflate(R.layout.day_button, daysView, false) as TextView
            dayView.isSelected = rule.days.contains(day)
            dayView.text = day.getDisplayName(TextStyle.NARROW, locale)
            dayView.setOnClickListener {
                dayView.isSelected = !dayView.isSelected
                if (dayView.isSelected) {
                    rule.days.add(day)
                } else {
                    rule.days.remove(day)
                }
            }
            daysView.addView(dayView)
            if (i < 6) {
                val space = layoutInflater.inflate(R.layout.day_button_space, daysView, false)
                daysView.addView(space)
            }
        }

        val beginTime = beginTime
        val endTime = endTime
        beginTime.value.text = rule.begin.toString(activity)
        endTime.value.text = rule.end.toString(activity)

        beginTime.setOnClickListener(setTimeListener(BEGIN, rule.begin))
        endTime.setOnClickListener(setTimeListener(END, rule.end))
        setDuration()
    }

    override fun createRule(): ScheduleRule = ScheduleRule(activity)

    override fun save() {
        rulesFragment.saveRule(mainActivity, rule)
    }

    private fun setDuration() {
        val duration = if (rule.begin < rule.end)
            rule.end - rule.begin
        else
            TimeUnit.DAYS.toMinutes(1).toInt() + (rule.end - rule.begin)
        val durationTV = view.findViewById(R.id.duration) as TextView
        durationTV.text = if (duration < 60)
            getString(R.string.duration_format_minutes, duration)
        else
            getString(R.string.duration_format, duration / 60, duration % 60)
    }

    private fun setTimeListener(code: Int, time: TimeOfDay) = View.OnClickListener {
        TimePickerDialogFragment.newInstance(this, code, time)
                .show(fragmentManager, TimePickerDialogFragment.FRAGMENT_TAG)
    }

    override fun validateSaveClose() {
        if (rule.days.isEmpty()) {
            Toast.makeText(activity, R.string.no_days_selected, Toast.LENGTH_SHORT).show()
        } else {
            saveClose()
        }
    }

    override fun onTimeSet(hourOfDay: Int, minute: Int, requestCode: Int) {
        val time: TimeOfDay
        val tv: TextView
        when(requestCode) {
            BEGIN -> {
                time = rule.begin
                tv = beginTime.value
            }
            END -> {
                time = rule.end
                tv = endTime.value
            }
            else -> throw IllegalStateException()
        }
        time.set(hourOfDay, minute)
        tv.text = time.toString(activity)
        setDuration()
    }
}

