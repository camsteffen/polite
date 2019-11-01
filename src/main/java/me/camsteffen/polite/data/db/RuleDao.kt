package me.camsteffen.polite.data.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import me.camsteffen.polite.data.db.entity.CalendarRuleCalendar
import me.camsteffen.polite.data.db.entity.CalendarRuleEntity
import me.camsteffen.polite.data.db.entity.CalendarRuleKeyword
import me.camsteffen.polite.data.db.entity.RuleEntity
import me.camsteffen.polite.data.db.entity.ScheduleRuleEntity
import me.camsteffen.polite.data.model.CalendarRule
import me.camsteffen.polite.data.model.CalendarRuleEntry
import me.camsteffen.polite.data.model.Rule
import me.camsteffen.polite.data.model.ScheduleRule
import me.camsteffen.polite.data.model.ScheduleRuleEntry
import timber.log.Timber

@Dao
abstract class RuleDao {

    fun getEnabledCalendarRules(): List<CalendarRule> =
        getEnabledCalendarRulesEntries().map(CalendarRuleEntry::asCalendarRule)

    fun getEnabledScheduleRules(): List<ScheduleRule> =
        getEnabledScheduleRuleEntries().map(ScheduleRuleEntry::asScheduleRule)

    @Query(
        """select exists(
             select 1 from rule join calendar_rule on rule.id = calendar_rule.id where enabled = 1
           )"""
    )
    abstract fun getEnabledCalendarRulesExist(): Boolean

    @Query(
        """select exists(
             select 1 from rule join calendar_rule on rule.id = calendar_rule.id where enabled = 1
           )"""
    )
    abstract fun getEnabledCalendarRulesExistLive(): LiveData<Boolean>

    fun calendarRulesSortedByNameLive(): LiveData<List<CalendarRule>> {
        return Transformations.map(calendarRuleEntriesSortedByNameLive()) { entries ->
            entries.map(CalendarRuleEntry::asCalendarRule)
        }
    }

    fun calendarRulesSortedByName(): List<CalendarRule> {
        return calendarRuleEntriesSortedByName().map(CalendarRuleEntry::asCalendarRule)
    }

    fun scheduleRulesSortedByName(): List<ScheduleRule> {
        return scheduleRuleEntriesSortedByName().map(ScheduleRuleEntry::asScheduleRule)
    }

    fun scheduleRulesSortedByNameLive(): LiveData<List<ScheduleRule>> {
        return Transformations.map(scheduleRuleEntriesSortedByNameLive()) { entries ->
            entries.map(ScheduleRuleEntry::asScheduleRule)
        }
    }

    fun deleteRule(id: Long): Int {
        val count = doDeleteRule(id)
        if (count > 0) {
            Timber.i("Deleted Rule[id=%d]", id)
        }
        return count
    }

    fun updateCalendarRulesEnabled(enabled: Boolean): Int {
        val count = doUpdateCalendarRulesEnabled(enabled)
        if (count > 0) {
            Timber.i("Updated %d calendar rules with enabled=%b", count, enabled)
        }
        return count
    }

    fun updateRuleName(id: Long, name: String): Int {
        val count = doUpdateRuleName(id, name)
        if (count > 0) {
            Timber.d("Updated Rule[id=%d] with name=%s", id, name)
        }
        return count
    }

    fun updateRuleEnabled(id: Long, enabled: Boolean): Int {
        val count = doUpdateRuleEnabled(id, enabled)
        if (count > 0) {
            Timber.i("Updated Rule[id=%d] with enabled=%b", id, enabled)
        }
        return count
    }

    @Transaction
    open fun saveRule(rule: Rule) {
        if (rule.id != 0L) {
            deleteRule(rule.id)
        }
        Timber.i("Saving rule: %s", rule)
        val id = insertRuleEntity(rule.asRuleEntity())
        when (rule) {
            is CalendarRule -> {
                val ruleWithId = rule.copy(id = id)
                insertCalendarRule(ruleWithId.asCalendarRuleEntity())
                insertCalendarRuleCalendars(*ruleWithId.calendarRuleCalendars().toTypedArray())
                insertCalendarRuleKeywords(*ruleWithId.calendarRuleKeywords().toTypedArray())
            }
            is ScheduleRule -> {
                insertScheduleRule(rule.copy(id = id).asScheduleRuleEntity())
            }
        }
    }

    @Query("delete from rule where id = :id")
    protected abstract fun doDeleteRule(id: Long): Int

    @Query("update rule set name = :name where id = :id")
    protected abstract fun doUpdateRuleName(id: Long, name: String): Int

    @Query("update rule set enabled = :enabled where id = :id")
    protected abstract fun doUpdateRuleEnabled(id: Long, enabled: Boolean): Int

    @Query(
        """update rule set enabled = :enabled
           where id in (select id from calendar_rule) and enabled != :enabled"""
    )
    protected abstract fun doUpdateCalendarRulesEnabled(enabled: Boolean): Int

    @Insert
    protected abstract fun insertRuleEntity(ruleEntity: RuleEntity): Long

    @Insert
    protected abstract fun insertCalendarRuleCalendars(
        vararg calendarRuleCalendars: CalendarRuleCalendar
    )

    @Transaction
    @Query("select * from calendar_rule join rule on calendar_rule.id = rule.id where enabled = 1")
    protected abstract fun getEnabledCalendarRulesEntries(): List<CalendarRuleEntry>

    @Transaction
    @Query("select * from schedule_rule join rule on schedule_rule.id = rule.id where enabled = 1")
    protected abstract fun getEnabledScheduleRuleEntries(): List<ScheduleRuleEntry>

    @Transaction
    @Query("select * from calendar_rule join rule on calendar_rule.id = rule.id order by name asc")
    protected abstract fun calendarRuleEntriesSortedByName(): List<CalendarRuleEntry>

    @Transaction
    @Query("select * from calendar_rule join rule on calendar_rule.id = rule.id order by name asc")
    protected abstract fun calendarRuleEntriesSortedByNameLive(): LiveData<List<CalendarRuleEntry>>

    @Transaction
    @Query("select * from schedule_rule join rule on schedule_rule.id = rule.id order by name asc")
    protected abstract fun scheduleRuleEntriesSortedByName(): List<ScheduleRuleEntry>

    @Transaction
    @Query("select * from schedule_rule join rule on schedule_rule.id = rule.id order by name asc")
    protected abstract fun scheduleRuleEntriesSortedByNameLive(): LiveData<List<ScheduleRuleEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertCalendarRule(rule: CalendarRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertScheduleRule(rule: ScheduleRuleEntity): Long

    @Insert
    protected abstract fun insertCalendarRuleKeywords(
        vararg calendarRuleKeyword: CalendarRuleKeyword
    )
}
