package me.camsteffen.polite.rule.master

import me.camsteffen.polite.model.CalendarRule
import me.camsteffen.polite.model.Rule
import me.camsteffen.polite.model.ScheduleRule

// TODO does not need to be a class
class RuleMasterList
private constructor() {
    companion object {
        fun of(calendarRules: List<CalendarRule>, scheduleRules: List<ScheduleRule>):
                List<RuleMasterItem> {
            val items = mutableListOf<RuleMasterItem>()
            if (scheduleRules.isNotEmpty()) Section.schedule(scheduleRules).addTo(items)
            if (calendarRules.isNotEmpty()) Section.calendar(calendarRules).addTo(items)
            return items
        }
    }

    private class Section<T : Rule>
    private constructor(private val heading: RuleMasterItem.Heading, private val rules: List<T>) {
        companion object {
            fun calendar(calendarRules: List<CalendarRule>): Section<CalendarRule> {
                return Section(RuleMasterItem.Heading.CALENDAR, calendarRules)
            }

            fun schedule(scheduleRules: List<ScheduleRule>): Section<ScheduleRule> {
                return Section(RuleMasterItem.Heading.SCHEDULE, scheduleRules)
            }
        }

        fun addTo(destination: MutableCollection<RuleMasterItem>) {
            destination.add(heading)
            rules.mapTo(destination) { RuleMasterItem.Rule(it) }
        }
    }
}
