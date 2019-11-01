package me.camsteffen.polite.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import me.camsteffen.polite.data.db.entity.ActiveRuleEvent
import me.camsteffen.polite.data.db.entity.EventCancel
import me.camsteffen.polite.data.db.entity.ScheduleRuleCancel
import org.threeten.bp.Instant
import timber.log.Timber

@Dao
abstract class PoliteStateDao {

    @Query("select * from active_rule_event")
    abstract fun getActiveRuleEvent(): ActiveRuleEvent?

    @Transaction
    open fun setActiveRuleEvent(activeRuleEvent: ActiveRuleEvent?) {
        if (deleteActiveRuleEvent() > 0) {
            Timber.i("Deleted active rule event")
        }
        if (activeRuleEvent != null) {
            Timber.i("Saving active rule event: %s", activeRuleEvent)
            insertActiveRuleEvent(activeRuleEvent)
        }
    }

    @Query("select * from event_cancel")
    abstract fun getEventCancels(): List<EventCancel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEventCancels(vararg eventCancels: EventCancel) {
        Timber.i("Saving event cancels: %s", eventCancels)
        doInsertEventCancels(*eventCancels)
    }

    @Query("select * from schedule_rule_cancel")
    abstract fun getScheduleRuleCancels(): List<ScheduleRuleCancel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertScheduleRuleCancels(vararg scheduleRuleCancels: ScheduleRuleCancel) {
        Timber.i("Saving schedule rule cancels: %s", scheduleRuleCancels)
        doInsertScheduleRuleCancels(*scheduleRuleCancels)
    }

    fun deleteDeadCancels(now: Instant) {
        var eventCancels = deleteDeadEventCancels(now)
        if (eventCancels > 0) {
            Timber.i("Deleted %d event cancels", eventCancels)
        }
        val scheduleRuleCancels = deleteDeadScheduleRuleCancels(now)
        if (scheduleRuleCancels > 0) {
            Timber.i("Deleted %d schedule rule cancels", scheduleRuleCancels)
        }
    }

    @Insert
    protected abstract fun insertActiveRuleEvent(activeRuleEvent: ActiveRuleEvent)

    @Query("delete from active_rule_event")
    protected abstract fun deleteActiveRuleEvent(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun doInsertEventCancels(vararg eventCancels: EventCancel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun doInsertScheduleRuleCancels(
        vararg scheduleRuleCancels: ScheduleRuleCancel
    )

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
