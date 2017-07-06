package me.camsteffen.polite

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.provider.BaseColumns
import me.camsteffen.polite.rule.Rule
import me.camsteffen.polite.rule.calendar.CalendarRule
import me.camsteffen.polite.rule.schedule.ScheduleRule

object DBActions {

    abstract class ModifyRule(val context: Context): AsyncTask<Void, Void, Long>() {

        var uiFunction: ((id: Long) -> Unit)? = null

        fun start(f: (id: Long) -> Unit) {
            uiFunction = f
            execute()
        }

        override fun doInBackground(vararg params: Void?): Long {
            val db = Polite.db!!.writableDatabase
            db.beginTransaction()
            val id = modify(db)
            db.setTransactionSuccessful()
            db.endTransaction()
            return id
        }

        override fun onPostExecute(id: Long) {
            uiFunction?.invoke(id)
            context.sendBroadcast(Intent(context, RingerReceiver::class.java)
                    .setAction(RingerReceiver.ACTION_REFRESH)
                    .putExtra(RingerReceiver.MODIFIED_RULE_ID, id))
        }

        abstract fun modify(db: SQLiteDatabase): Long
    }

    class CreateCalendarRule(context: Context, var rule: CalendarRule) : ModifyRule(context) {

        override fun modify(db: SQLiteDatabase): Long {
            val id = insertRule(db, rule)
            return id
        }
    }

    class CreateScheduleRule(context: Context, var rule: ScheduleRule) : ModifyRule(context) {

        override fun modify(db: SQLiteDatabase): Long {
            return insertRule(db, rule)
        }
    }

    class SaveCalendarRule(context: Context, var rule: CalendarRule) : ModifyRule(context) {

        override fun modify(db: SQLiteDatabase): Long {
            deleteRule(db, rule.id)
            return insertRule(db, rule)
        }
    }

    class SaveScheduleRule(context: Context, var rule: ScheduleRule) : ModifyRule(context) {

        override fun modify(db: SQLiteDatabase): Long {
            deleteRule(db, rule.id)
            return insertRule(db, rule)
        }
    }

    class DeleteRule(context: Context, val id: Long) : ModifyRule(context) {

        override fun modify(db: SQLiteDatabase): Long {
            deleteRule(db, id)
            return id
        }
    }

    open class RuleSetEnabled(context: Context, val id: Long, val enable: Boolean) : ModifyRule(context) {

        override fun modify(db: SQLiteDatabase): Long {
            ruleSetEnabled(db, id, enable)
            return id
        }
    }


    class RenameRule(context: Context, val id: Long, val name: String) : ModifyRule(context) {

        override fun modify(db: SQLiteDatabase): Long {
            renameRule(db, id, name)
            return id
        }
    }

    fun insertRuleBase(db: SQLiteDatabase, rule: Rule) : Long {
        val values = ContentValues()
        if (rule.id != Rule.NEW_RULE) {
            values.put(BaseColumns._ID, rule.id)
        }
        values.put(DB.Rule.COLUMN_NAME, rule.name)
        values.put(DB.Rule.COLUMN_ENABLE, rule.enabled)
        values.put(DB.Rule.COLUMN_VIBRATE, rule.vibrate)
        return db.insertOrThrow(DB.Rule.TABLE_NAME, null, values)
    }

    fun insertRule(db: SQLiteDatabase, rule: CalendarRule) : Long {
        val ruleID = insertRuleBase(db, rule)

        // Calendar Rule
        val values = ContentValues()
        values.put(BaseColumns._ID, ruleID)
        values.put(DB.CalendarRule.COLUMN_MATCH_ALL, rule.matchAll)
        values.put(DB.CalendarRule.COLUMN_MATCH_TITLE, rule.matchTitle)
        values.put(DB.CalendarRule.COLUMN_MATCH_DESCRIPTION, rule.matchDescription)
        values.put(DB.CalendarRule.COLUMN_INVERSE_MATCH, rule.inverseMatch)
        db.insertOrThrow(DB.CalendarRule.TABLE_NAME, null, values)

        // Calendar
        for (calID in rule.calendars) {
            values.clear()
            values.put(DB.CalendarRuleCalendar.COLUMN_RULE, ruleID)
            values.put(DB.CalendarRuleCalendar.COLUMN_CALENDAR_ID, calID)
            db.insertOrThrow(DB.CalendarRuleCalendar.TABLE_NAME, null, values)
        }

        // Keyword
        for (word in rule.keywords) {
            values.clear()
            values.put(DB.CalendarRuleKeyword.COLUMN_RULE, ruleID)
            values.put(DB.CalendarRuleKeyword.COLUMN_WORD, word)
            db.insertOrThrow(DB.CalendarRuleKeyword.TABLE_NAME, null, values)
        }

        return ruleID
    }

    fun insertRule(db: SQLiteDatabase, rule: ScheduleRule) : Long {
        val ruleID = insertRuleBase(db, rule)
        val values = ContentValues()
        values.put(BaseColumns._ID, ruleID)
        values.put(DB.ScheduleRule.COLUMN_BEGIN, rule.begin.toInt())
        values.put(DB.ScheduleRule.COLUMN_END, rule.end.toInt())
        values.put(DB.ScheduleRule.COLUMN_SUNDAY, rule.days[ScheduleRule.SUNDAY])
        values.put(DB.ScheduleRule.COLUMN_MONDAY, rule.days[ScheduleRule.MONDAY])
        values.put(DB.ScheduleRule.COLUMN_TUESDAY, rule.days[ScheduleRule.TUESDAY])
        values.put(DB.ScheduleRule.COLUMN_WEDNESDAY, rule.days[ScheduleRule.WEDNESDAY])
        values.put(DB.ScheduleRule.COLUMN_THURSDAY, rule.days[ScheduleRule.THURSDAY])
        values.put(DB.ScheduleRule.COLUMN_FRIDAY, rule.days[ScheduleRule.FRIDAY])
        values.put(DB.ScheduleRule.COLUMN_SATURDAY, rule.days[ScheduleRule.SATURDAY])
        db.insertOrThrow(DB.ScheduleRule.TABLE_NAME, null, values)
        return ruleID
    }

    fun deleteRule(db: SQLiteDatabase, id: Long) {
        db.delete(DB.Rule.TABLE_NAME, "${BaseColumns._ID}=?", arrayOf(id.toString()))
    }

    private fun renameRule(db: SQLiteDatabase, id: Long, name: String) {
        val values = ContentValues()
        values.put(DB.Rule.COLUMN_NAME, name)
        db.update(DB.Rule.TABLE_NAME, values, "${BaseColumns._ID}=?", arrayOf(id.toString()))
    }

    private fun ruleSetEnabled(db: SQLiteDatabase, id: Long, isChecked: Boolean) {
        val values = ContentValues()
        values.put(DB.Rule.COLUMN_ENABLE, isChecked)
        db.update(DB.Rule.TABLE_NAME, values, "${BaseColumns._ID}=?", arrayOf(java.lang.Long.toString(id)))
    }
}
