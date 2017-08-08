package me.camsteffen.polite.rule

import android.os.Parcel
import android.os.Parcelable
import me.camsteffen.polite.R
import me.camsteffen.polite.rule.calendar.CalendarRule
import me.camsteffen.polite.rule.schedule.ScheduleRule
import java.util.*

private const val SCHEDULE_RULES_HEADING = -1L
private const val CALENDAR_RULES_HEADING = -2L

class RuleList : ArrayList<RuleList.RuleListItem>(), Parcelable {

    interface RuleListItem {
        val id: Long
    }

    data class Subhead(override val id: Long, val textId: Int, val drawableId: Int): RuleListItem

    var scheduleRuleCount = 0
    var calendarRuleCount = 0
    val scheduleRulesSubhead = Subhead(SCHEDULE_RULES_HEADING, R.string.schedule_rules, R.drawable.ic_schedule_rule_black_24dp)
    val calendarRulesSubhead = Subhead(CALENDAR_RULES_HEADING, R.string.calendar_rules, R.drawable.ic_calendar_rule_black_24dp)

    override fun removeAt(index: Int): RuleListItem {
        val removed = super.removeAt(index)
        when(removed) {
            is ScheduleRule -> if(--scheduleRuleCount == 0) super.removeAt(0)
            is CalendarRule -> if(--calendarRuleCount == 0) super.removeAt(index - 1)
            else -> throw IllegalStateException()
        }
        return removed
    }

    fun insert(rule: ScheduleRule): Int {
        if(scheduleRuleCount == 0)
            add(0, scheduleRulesSubhead)
        val index = insert(rule, 1)
        ++scheduleRuleCount
        return index
    }

    fun insert(rule: CalendarRule): Int {
        if(calendarRuleCount == 0) {
            val insertIndex = if(scheduleRuleCount == 0) 0 else scheduleRuleCount + 1
            add(insertIndex, calendarRulesSubhead)
        }
        val insertIndex = if(scheduleRuleCount == 0) 1 else scheduleRuleCount + 2
        val index = insert(rule, insertIndex)
        ++calendarRuleCount
        return index
    }

    private fun insert(rule: Rule, start: Int): Int {
        var index = start
        while(index < size) {
            val item = get(index)
            if(item is Rule && item.name <= rule.name)
                ++index
            else
                break
        }
        add(index, rule)
        return index
    }

    fun setRules(scheduleRules: List<ScheduleRule>, calendarRules: List<CalendarRule>) {
        clear()
        if(scheduleRules.isNotEmpty()) {
            add(scheduleRulesSubhead)
            addAll(scheduleRules)
        }
        if(calendarRules.isNotEmpty()) {
            add(calendarRulesSubhead)
            addAll(calendarRules)
        }
        scheduleRuleCount = scheduleRules.size
        calendarRuleCount = calendarRules.size
    }

    override fun writeToParcel(parcel: Parcel?, flags: Int) {
        parcel!!.writeInt(scheduleRuleCount)
        val scheduleRuleArray = if (scheduleRuleCount == 0)
            emptyList<ScheduleRule>()
        else
            subList(1, scheduleRuleCount + 1)
                .map { it as ScheduleRule }
        parcel.writeTypedList(scheduleRuleArray)
        parcel.writeInt(calendarRuleCount)
        val calendarRuleList = if (calendarRuleCount == 0)
            emptyList<CalendarRule>()
        else
            subList(size - calendarRuleCount, size)
                .map { it as CalendarRule }
        parcel.writeTypedList(calendarRuleList)
    }

    override fun describeContents() = 0

    companion object {
        @Suppress("unused") // required by Parcelable
        @JvmField val CREATOR = object : Parcelable.Creator<RuleList> {

            override fun createFromParcel(source: Parcel): RuleList? {
                val scheduleRuleCount = source.readInt()
                val scheduleRuleList = ArrayList<ScheduleRule>(scheduleRuleCount)
                source.readTypedList(scheduleRuleList, ScheduleRule.CREATOR)
                val calendarRuleCount = source.readInt()
                val calendarRuleList = ArrayList<CalendarRule>(calendarRuleCount)
                source.readTypedList(calendarRuleList, CalendarRule.CREATOR)
                val list = RuleList()
                list.setRules(scheduleRuleList, calendarRuleList)
                return list
            }

            override fun newArray(size: Int): Array<out RuleList>? {
                return newArray(size)
            }
        }
    }
}
