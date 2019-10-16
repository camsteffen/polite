package me.camsteffen.polite.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime

private val ALL_TABLES = listOf(
    "Rule", "RuleCalendar", "RuleKeyword", "CalendarRule",
    "CalendarRuleCalendar", "CalendarRuleKeyword", "ScheduleRule"
)

fun insertRuleV1(
    id: Int,
    name: String,
    enable: Boolean,
    vibrate: Boolean,
    matchAll: Boolean,
    matchTitle: Boolean,
    matchDesc: Boolean,
    helper: SQLiteOpenHelper
) {
    helper.writableDatabase.use { db ->
        val values = ContentValues().apply {
            put("_id", id)
            put("name", name)
            put("enable", enable.asInt())
            put("vibrate", vibrate.asInt())
            put("matchAll", matchAll.asInt())
            put("matchTitle", matchTitle.asInt())
            put("matchDesc", matchDesc.asInt())
        }
        db.insertOrThrow("Rule", null, values)
    }
}

fun insertRuleCalendar(ruleId: Long, calendarId: Long, helper: SQLiteOpenHelper) {
    helper.writableDatabase.use { db ->
        val values = ContentValues().apply {
            put("rule", ruleId)
            put("calendarID", calendarId)
        }
        db.insertOrThrow("RuleCalendar", null, values)
    }
}

fun insertRuleKeyword(ruleId: Long, keyword: String, helper: SQLiteOpenHelper) {
    helper.writableDatabase.use { db ->
        val values = ContentValues().apply {
            put("rule", ruleId)
            put("word", keyword)
        }
        db.insertOrThrow("RuleKeyword", null, values)
    }
}

fun insertRuleV2(
    id: Long,
    name: String,
    enable: Boolean,
    vibrate: Boolean,
    helper: SQLiteOpenHelper
) {
    helper.writableDatabase.use { db ->
        val values = ContentValues()
        values.put("_id", id)
        values.put("name", name)
        values.put("enable", enable.asInt())
        values.put("vibrate", vibrate.asInt())
        db.insertOrThrow("rule", null, values)
    }
}

fun insertRuleV5(
    id: Long,
    name: String,
    enable: Boolean,
    vibrate: Boolean,
    db: SupportSQLiteDatabase
) {
    val values = ContentValues()
    values.put("_id", id)
    values.put("name", name)
    values.put("enable", enable.asInt())
    values.put("vibrate", vibrate.asInt())
    db.insert("rule", SQLiteDatabase.CONFLICT_ABORT, values)
}

fun insertCalendarRuleV2(
    id: Long,
    matchAll: Boolean,
    matchTitle: Boolean,
    matchDesc: Boolean,
    helper: SQLiteOpenHelper
) {
    helper.writableDatabase.use { db ->
        val values = ContentValues()
        values.put("_id", id)
        values.put("matchAll", matchAll.asInt())
        values.put("matchTitle", matchTitle.asInt())
        values.put("matchDesc", matchDesc.asInt())
        db.insertOrThrow("CalendarRule", null, values)
    }
}

fun insertCalendarRuleV3(
    id: Long,
    matchAll: Boolean,
    matchTitle: Boolean,
    matchDesc: Boolean,
    inverseMatch: Boolean,
    helper: SQLiteOpenHelper
) {
    helper.writableDatabase.use { db ->
        val values = calendarRuleV3(id, matchAll, matchTitle, matchDesc, inverseMatch)
        db.insertOrThrow("CalendarRule", null, values)
    }
}

fun insertCalendarRuleV5(
    id: Long,
    matchAll: Boolean,
    matchTitle: Boolean,
    matchDesc: Boolean,
    inverseMatch: Boolean,
    db: SupportSQLiteDatabase
) {
    val values = calendarRuleV3(id, matchAll, matchTitle, matchDesc, inverseMatch)
    db.insert("CalendarRule", SQLiteDatabase.CONFLICT_ABORT, values)
}

private fun calendarRuleV3(
    id: Long,
    matchAll: Boolean,
    matchTitle: Boolean,
    matchDesc: Boolean,
    inverseMatch: Boolean
): ContentValues {
    val values = ContentValues()
    values.put("_id", id)
    values.put("matchAll", matchAll.asInt())
    values.put("matchTitle", matchTitle.asInt())
    values.put("matchDesc", matchDesc.asInt())
    values.put("inverseMatch", inverseMatch.asInt())
    return values
}

fun insertCalendarRuleCalendarV2(ruleId: Long, calendarId: Long, helper: SQLiteOpenHelper) {
    helper.writableDatabase.use { db ->
        val values = ContentValues()
        values.put("rule", ruleId)
        values.put("calendarId", calendarId)
        db.insertOrThrow("CalendarRuleCalendar", null, values)
    }
}

fun insertCalendarRuleCalendarV5(ruleId: Long, calendarId: Long, db: SupportSQLiteDatabase) {
    val values = ContentValues()
    values.put("rule", ruleId)
    values.put("calendarId", calendarId)
    db.insert("CalendarRuleCalendar", SQLiteDatabase.CONFLICT_ABORT, values)
}

fun insertCalendarRuleKeywordV2(ruleId: Long, keyword: String, helper: SQLiteOpenHelper) {
    helper.writableDatabase.use { db ->
        val values = ContentValues()
        values.put("rule", ruleId)
        values.put("word", keyword)
        db.insertOrThrow("CalendarRuleKeyword", null, values)
    }
}

fun insertCalendarRuleKeywordV5(ruleId: Long, keyword: String, db: SupportSQLiteDatabase) {
    val values = ContentValues()
    values.put("rule", ruleId)
    values.put("word", keyword)
    db.insert("CalendarRuleKeyword", SQLiteDatabase.CONFLICT_ABORT, values)
}

fun insertScheduleRuleV2(
    id: Long,
    begin: LocalTime,
    end: LocalTime,
    days: Set<DayOfWeek>,
    helper: SQLiteOpenHelper
) {
    helper.writableDatabase.use { db ->
        val values = ContentValues()
        values.put("_id", id)
        values.put("begin", begin.toSecondOfDay() / 60)
        values.put("end", end.toSecondOfDay() / 60)
        values.put("sunday", days.contains(DayOfWeek.SUNDAY).asInt())
        values.put("monday", days.contains(DayOfWeek.MONDAY).asInt())
        values.put("tuesday", days.contains(DayOfWeek.TUESDAY).asInt())
        values.put("wednesday", days.contains(DayOfWeek.WEDNESDAY).asInt())
        values.put("thursday", days.contains(DayOfWeek.THURSDAY).asInt())
        values.put("friday", days.contains(DayOfWeek.FRIDAY).asInt())
        values.put("saturday", days.contains(DayOfWeek.SATURDAY).asInt())
        db.insertOrThrow("ScheduleRule", null, values)
    }
}

fun insertScheduleRuleV5(
    id: Long,
    begin: LocalTime,
    end: LocalTime,
    days: Set<DayOfWeek>,
    db: SupportSQLiteDatabase
) {
    val values = ContentValues()
    values.put("_id", id)
    values.put("begin", begin.toSecondOfDay() / 60)
    values.put("end", end.toSecondOfDay() / 60)
    values.put("sunday", days.contains(DayOfWeek.SUNDAY).asInt())
    values.put("monday", days.contains(DayOfWeek.MONDAY).asInt())
    values.put("tuesday", days.contains(DayOfWeek.TUESDAY).asInt())
    values.put("wednesday", days.contains(DayOfWeek.WEDNESDAY).asInt())
    values.put("thursday", days.contains(DayOfWeek.THURSDAY).asInt())
    values.put("friday", days.contains(DayOfWeek.FRIDAY).asInt())
    values.put("saturday", days.contains(DayOfWeek.SATURDAY).asInt())
    db.insert("ScheduleRule", SQLiteDatabase.CONFLICT_ABORT, values)
}

private fun Boolean.asInt(): Int {
    return if (this) 1 else 0
}
