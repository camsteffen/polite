package me.camsteffen.polite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

private const val VERSION = 3
private const val NAME = "Polite.db"

class DB(val context: Context) : SQLiteOpenHelper(context, NAME, null, VERSION) {

    object Rule {
        const val TABLE_NAME = "Rule"
        const val COLUMN_NAME = "name"
        const val COLUMN_ENABLE = "enable"
        const val COLUMN_VIBRATE = "vibrate"
        const val CREATE_TABLE = "CREATE TABLE $TABLE_NAME(" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY NOT NULL," +
                "$COLUMN_NAME TEXT NOT NULL," +
                "$COLUMN_ENABLE INTEGER NOT NULL," +
                "$COLUMN_VIBRATE INTEGER NOT NULL)"
    }

    object CalendarRule {
        const val TABLE_NAME = "CalendarRule"
        const val COLUMN_MATCH_ALL = "matchAll"
        const val COLUMN_MATCH_TITLE = "matchTitle"
        const val COLUMN_MATCH_DESCRIPTION = "matchDesc"
        const val COLUMN_INVERSE_MATCH = "inverseMatch"
        const val CREATE_TABLE = "CREATE TABLE $TABLE_NAME(" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY NOT NULL," +
                "$COLUMN_MATCH_ALL INTEGER NOT NULL," +
                "$COLUMN_MATCH_TITLE INTEGER NOT NULL," +
                "$COLUMN_MATCH_DESCRIPTION INTEGER NOT NULL," +
                "$COLUMN_INVERSE_MATCH INTEGER NOT NULL NOT NULL," +
                "FOREIGN KEY (${BaseColumns._ID}) REFERENCES " +
                "${Rule.TABLE_NAME} (${BaseColumns._ID}) ON UPDATE CASCADE ON DELETE CASCADE)"
    }

    object CalendarRuleCalendar {
        const val TABLE_NAME = "CalendarRuleCalendar"
        const val COLUMN_RULE = "rule"
        const val COLUMN_CALENDAR_ID = "calendarID"
        const val CREATE_TABLE = "CREATE TABLE $TABLE_NAME(" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY NOT NULL," +
                "$COLUMN_RULE INTEGER NOT NULL," +
                "$COLUMN_CALENDAR_ID INTEGER NOT NULL," +
                "FOREIGN KEY ($COLUMN_RULE) REFERENCES " +
                "${Rule.TABLE_NAME} (${BaseColumns._ID}) ON UPDATE CASCADE ON DELETE CASCADE)"
    }

    object CalendarRuleKeyword {
        const val TABLE_NAME = "CalendarRuleKeyword"
        const val COLUMN_RULE = "rule"
        const val COLUMN_WORD = "word"
        const val CREATE_TABLE = "CREATE TABLE $TABLE_NAME(" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY NOT NULL," +
                "$COLUMN_RULE INTEGER NOT NULL," +
                "$COLUMN_WORD TEXT NOT NULL," +
                "FOREIGN KEY ($COLUMN_RULE) REFERENCES " +
                "${Rule.TABLE_NAME} (${BaseColumns._ID}) ON UPDATE CASCADE ON DELETE CASCADE)"
    }

    object ScheduleRule {
        const val TABLE_NAME = "ScheduleRule"
        const val COLUMN_BEGIN = "begin"
        const val COLUMN_END = "end"
        const val COLUMN_SUNDAY = "sunday"
        const val COLUMN_MONDAY = "monday"
        const val COLUMN_TUESDAY = "tuesday"
        const val COLUMN_WEDNESDAY = "wednesday"
        const val COLUMN_THURSDAY = "thursday"
        const val COLUMN_FRIDAY = "friday"
        const val COLUMN_SATURDAY = "saturday"
        const val CREATE_TABLE = "CREATE TABLE $TABLE_NAME(" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY NOT NULL," +
                "$COLUMN_BEGIN INTEGER NOT NULL," +
                "$COLUMN_END INTEGER NOT NULL," +
                "$COLUMN_SUNDAY INTEGER NOT NULL," +
                "$COLUMN_MONDAY INTEGER NOT NULL," +
                "$COLUMN_TUESDAY INTEGER NOT NULL," +
                "$COLUMN_WEDNESDAY INTEGER NOT NULL," +
                "$COLUMN_THURSDAY INTEGER NOT NULL," +
                "$COLUMN_FRIDAY INTEGER NOT NULL," +
                "$COLUMN_SATURDAY INTEGER NOT NULL," +
                "FOREIGN KEY (${BaseColumns._ID}) REFERENCES " +
                "${Rule.TABLE_NAME} (${BaseColumns._ID}) ON UPDATE CASCADE ON DELETE CASCADE)"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(Rule.CREATE_TABLE)
        db.execSQL(CalendarRule.CREATE_TABLE)
        db.execSQL(CalendarRuleCalendar.CREATE_TABLE)
        db.execSQL(CalendarRuleKeyword.CREATE_TABLE)
        db.execSQL(ScheduleRule.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.beginTransaction()
        for(version in oldVersion + 1..newVersion) {
            upgradeToVersion(db, version)
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    private fun upgradeToVersion(db: SQLiteDatabase, version: Int) {
        val filename = String.format("upgrade_%d.sql", version)
        context.assets.open(filename).use { inStr ->
            inStr.buffered().use { bufInStr ->
                val sb = StringBuilder()
                while (true) {
                    val i = bufInStr.read()
                    if (i == -1)
                        break
                    val c = i.toChar()
                    if (c == ';') {
                        val str = sb.toString()
                        sb.setLength(0)
                        db.execSQL(str)
                    } else {
                        sb.append(c)
                    }
                }
                if (sb.isNotBlank()) {
                    throw IllegalStateException("unexpected EOF in $filename")
                }
            }
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = ON")
    }
}
