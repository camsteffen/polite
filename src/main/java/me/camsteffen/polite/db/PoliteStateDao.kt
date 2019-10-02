package me.camsteffen.polite.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import me.camsteffen.polite.model.ActiveRuleEvent
import me.camsteffen.polite.model.EventCancel
import me.camsteffen.polite.model.ScheduleRuleCancel
import org.threeten.bp.Instant

@Dao
abstract class PoliteStateDao {

    @Query("select * from active_rule_event")
    abstract fun getActiveRuleEvent(): ActiveRuleEvent?

    @Transaction
    open fun setActiveRuleEvent(activeRuleEvent: ActiveRuleEvent?) {
        deleteActiveRuleEvent()
        if (activeRuleEvent != null) {
            insertActiveRuleEvent(activeRuleEvent)
        }
    }

    @Query("select * from event_cancel")
    abstract fun getEventCancels(): List<EventCancel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertEventCancels(vararg eventCancels: EventCancel)

    @Query("select * from schedule_rule_cancel")
    abstract fun getScheduleRuleCancels(): List<ScheduleRuleCancel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertScheduleRuleCancels(vararg scheduleRuleCancels: ScheduleRuleCancel)

    fun deleteDeadCancels(now: Instant) {
        deleteDeadEventCancels(now)
        deleteDeadScheduleRuleCancels(now)
    }

    @Insert
    protected abstract fun insertActiveRuleEvent(activeRuleEvent: ActiveRuleEvent)

    @Query("delete from active_rule_event")
    protected abstract fun deleteActiveRuleEvent(): Int

    @Query(
        """delete from event_cancel where `end` <= :now
           or rule_id in (select rule_id from event_cancel c
             left join rule r on c.rule_id = r.id
             where enabled != 1)""")
    protected abstract fun deleteDeadEventCancels(now: Instant): Int

    @Query(
        """delete from schedule_rule_cancel where `end` <= :now
           or rule_id in (
             select rule_id from schedule_rule_cancel c left join rule r on c.rule_id = r.id
               where enabled != 1
           )"""
    )
    protected abstract fun deleteDeadScheduleRuleCancels(now: Instant): Int
}
