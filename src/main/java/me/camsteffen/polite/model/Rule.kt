package me.camsteffen.polite.model

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import me.camsteffen.polite.DB
import me.camsteffen.polite.DBActions
import me.camsteffen.polite.MainActivity
import me.camsteffen.polite.Polite
import me.camsteffen.polite.R
import me.camsteffen.polite.rule.RuleAdapter
import me.camsteffen.polite.rule.RuleList
import me.camsteffen.polite.util.TimeOfDay
import java.util.ArrayList
import java.util.TreeSet

sealed class Rule : Parcelable, RuleList.RuleListItem {

    companion object {
        const val NEW_RULE = -1L
    }

    final override var id: Long
    var name: String
    var enabled: Boolean
    var vibrate: Boolean

    constructor(context: Context) {
        id = NEW_RULE
        name = context.getString(R.string.rule_default_name)
        enabled = true
        vibrate = false
    }

    constructor(copy: Rule) {
        id = copy.id
        name = copy.name
        enabled = copy.enabled
        vibrate = copy.vibrate
    }

    constructor(id: Long, name: String, enabled: Boolean, vibrate: Boolean) {
        this.id = id
        this.name = name
        this.enabled = enabled
        this.vibrate = vibrate
    }

    constructor(parcel: Parcel) {
        id = parcel.readLong()
        name = parcel.readString()
        enabled = parcel.readByte() != 0.toByte()
        vibrate = parcel.readByte() != 0.toByte()
    }

    open fun getCaption(context: Context): CharSequence = ""

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(name)
        dest.writeByte(if(enabled) 1 else 0)
        dest.writeByte(if(vibrate) 1 else 0)
    }

    abstract fun addToAdapter(adapter: RuleAdapter)

    open fun saveDB(mainActivity: MainActivity, callback: () -> Unit) {
        if(id == NEW_RULE) {
            saveDBNew(mainActivity, { id ->
                this.id = id
                callback()
            })
        } else {
            saveDBExisting(mainActivity)
        }
    }

    abstract fun saveDBNew(context: Context, callback: (id: Long) -> Unit)
    abstract fun saveDBExisting(context: Context)

    open fun scrub() = Unit

}

class CalendarRule : Rule {

    val calendarIds: MutableList<Long>
    var match: Int
    var inverseMatch: Boolean
    val keywords: TreeSet<String>

    val matchAll: Boolean
        get() = match and MATCH_ALL == MATCH_ALL
    val matchTitle: Boolean
        get() = match and MATCH_TITLE == MATCH_TITLE
    val matchDescription: Boolean
        get() = match and MATCH_DESCRIPTION == MATCH_DESCRIPTION

    constructor(context: Context) : super(context) {
        match = MATCH_ALL
        inverseMatch = false
        calendarIds = mutableListOf()
        keywords = TreeSet()
    }

    constructor(
            id: Long,
            name: String,
            enabled: Boolean,
            vibrate: Boolean,
            calendars: Collection<Long>,
            match: Int,
            inverseMatch: Boolean,
            keywords: Collection<String>) : super(id, name, enabled, vibrate) {
        this.calendarIds = calendars.toMutableList()
        this.match = match
        this.inverseMatch = inverseMatch
        this.keywords = TreeSet(keywords)
    }

    constructor(parcel: Parcel) : super(parcel) {
        calendarIds = mutableListOf()
        parcel.readList(calendarIds, null)
        match = parcel.readInt()
        inverseMatch = parcel.readInt() != 0
        val keywordsArr = mutableListOf<String>()
        parcel.readStringList(keywordsArr)
        keywords = TreeSet(keywordsArr)
    }

    override fun addToAdapter(adapter: RuleAdapter) {
        adapter.addRule(this)
    }

    override fun saveDB(mainActivity: MainActivity, callback: () -> Unit) {
        super.saveDB(mainActivity, callback)
        if(enabled) {
            mainActivity.checkCalendarPermission()
        }
    }

    override fun saveDBNew(context: Context, callback: (Long) -> Unit) {
        DBActions.CreateCalendarRule(context, this).start(callback)
    }

    override fun saveDBExisting(context: Context) {
        DBActions.SaveCalendarRule(context, this).execute()
    }

    override fun getCaption(context: Context): String {
        if (keywords.isEmpty())
            return ""
        val it = keywords.iterator()
        val builder = StringBuilder(it.next())
        for (word in it) {
            builder.append(", $word")
        }
        return builder.toString()
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeList(calendarIds)
        dest.writeInt(match)
        dest.writeInt(if(inverseMatch) 1 else 0)
        dest.writeStringList(keywords.toMutableList())
    }

    override fun scrub() {
        if (match == MATCH_ALL) {
            keywords.clear()
        }
    }

    companion object {
        const val MATCH_ALL = 1
        const val MATCH_TITLE = 2
        const val MATCH_DESCRIPTION = 4

        @Suppress("unused") // required by Parcelable
        @JvmField val CREATOR = object : Parcelable.Creator<CalendarRule> {

            override fun createFromParcel(source: Parcel?): CalendarRule? = CalendarRule(source!!)

            override fun newArray(size: Int): Array<out CalendarRule>? {
                return newArray(size)
            }
        }

        fun query(
                db: SQLiteDatabase = Polite.db.readableDatabase,
                selection: String? = null,
                selectionArgs: Array<String>? = null,
                orderBy: String? = null): Cursor {
            var sql = "SELECT ${DB.Rule.TABLE_NAME}.${BaseColumns._ID}," +
                    "${DB.Rule.COLUMN_NAME}," +
                    "${DB.Rule.COLUMN_ENABLE}, " +
                    "${DB.Rule.COLUMN_VIBRATE}," +
                    "${DB.CalendarRule.COLUMN_MATCH_ALL}," +
                    "${DB.CalendarRule.COLUMN_MATCH_TITLE}," +
                    "${DB.CalendarRule.COLUMN_MATCH_DESCRIPTION}, " +
                    "${DB.CalendarRule.COLUMN_INVERSE_MATCH} " +
                    "from ${DB.Rule.TABLE_NAME} INNER JOIN ${DB.CalendarRule.TABLE_NAME} USING(${BaseColumns._ID})"
            if(selection != null) {
                sql += " WHERE $selection"
            }
            if(orderBy != null) {
                sql += " ORDER BY $orderBy"
            }
            return db.rawQuery(sql, selectionArgs)
        }
        private const val INDEX_ID = 0
        private const val INDEX_NAME = 1
        private const val INDEX_ENABLE = 2
        private const val INDEX_VIBRATE = 3
        private const val INDEX_MATCH_ALL = 4
        private const val INDEX_MATCH_TITLE = 5
        private const val INDEX_MATCH_DESCRIPTION = 6
        private const val INDEX_INVERSE_MATCH = 7

        fun queryList(
                db: SQLiteDatabase = Polite.db.readableDatabase,
                selection: String? = null,
                selectionArgs: Array<String>? = null,
                orderBy: String? = null): List<CalendarRule> {
            val ruleCursor = query(db, selection, selectionArgs, orderBy)
            val list = ArrayList<CalendarRule>(ruleCursor.count)
            while(ruleCursor.moveToNext()) {
                val id = ruleCursor.getLong(INDEX_ID)
                val name = ruleCursor.getString(INDEX_NAME)
                val enabled = ruleCursor.getInt(INDEX_ENABLE) != 0
                val vibrate = ruleCursor.getInt(INDEX_VIBRATE) != 0
                val calendarCursor = db.query(DB.CalendarRuleCalendar.TABLE_NAME,
                        arrayOf(DB.CalendarRuleCalendar.COLUMN_CALENDAR_ID),
                        "${DB.CalendarRuleCalendar.COLUMN_RULE}=?",
                        arrayOf(java.lang.Long.toString(id)),
                        null,
                        null,
                        null)
                val calendars = calendarCursor.use {
                    val calendars = ArrayList<Long>(calendarCursor.count)
                    while (calendarCursor.moveToNext()) {
                        calendars.add(calendarCursor.getLong(0))
                    }
                    calendars
                }

                var match = 0
                if (ruleCursor.getInt(INDEX_MATCH_ALL) != 0) {
                    match = MATCH_ALL
                } else if (ruleCursor.getInt(INDEX_MATCH_TITLE) != 0) {
                    match = MATCH_TITLE
                }
                if (ruleCursor.getInt(INDEX_MATCH_DESCRIPTION) != 0) {
                    match = match or MATCH_DESCRIPTION
                }

                val inverseMatch = ruleCursor.getInt(INDEX_INVERSE_MATCH) != 0

                val keywordCursor = db.query(DB.CalendarRuleKeyword.TABLE_NAME,
                        arrayOf(DB.CalendarRuleKeyword.COLUMN_WORD),
                        "${DB.CalendarRuleKeyword.COLUMN_RULE}=?",
                        arrayOf(java.lang.Long.toString(id)),
                        null,
                        null,
                        null)
                val keywords = keywordCursor.use {
                    val keywords = ArrayList<String>(keywordCursor.count)
                    while (keywordCursor.moveToNext()) {
                        keywords.add(keywordCursor.getString(0))
                    }
                    keywords
                }

                list.add(CalendarRule(id, name, enabled, vibrate, calendars, match, inverseMatch, keywords))
            }
            ruleCursor.close()

            return list
        }
    }

}

class ScheduleRule : Rule {

    val begin: TimeOfDay
    val end: TimeOfDay
    val days: BooleanArray

    constructor(context: Context) : super(context) {
        begin = TimeOfDay(12, 0)
        end = TimeOfDay(13, 0)
        days = booleanArrayOf(false, true, true, true, true, true, false) // Mon-Fri
    }

    constructor(
            id: Long,
            name: String,
            enabled: Boolean,
            vibrate: Boolean,
            begin: TimeOfDay,
            end: TimeOfDay,
            days: BooleanArray
    ) : super(id, name, enabled, vibrate) {
        this.begin = begin
        this.end = end
        this.days = days
    }

    constructor(parcel: Parcel) : super(parcel) {
        begin = TimeOfDay(parcel.readInt())
        end = TimeOfDay(parcel.readInt())
        days = BooleanArray(7)
        parcel.readBooleanArray(days)
    }

    override fun addToAdapter(adapter: RuleAdapter) {
        adapter.addRule(this)
    }

    override fun saveDBNew(context: Context, callback: (Long) -> Unit) {
        DBActions.CreateScheduleRule(context, this).start(callback)
    }

    override fun saveDBExisting(context: Context) {
        DBActions.SaveScheduleRule(context, this).execute()
    }

    override fun getCaption(context: Context): CharSequence {
        val days = context.resources.getStringArray(R.array.day_abbreviations)
        val atoms = ArrayList<String>()
        var count = 0
        val ranges = ArrayList<Pair<Int, Int>>()
        for (i in 0 until days.size) {
            if (this.days[i]) {
                ++count
            } else {
                ranges.add(Pair(i-count, count))
                count = 0
            }
        }
        if (count == days.size) {
            atoms.add(context.getString(R.string.every_day))
        } else if (count > 0) {
            ranges.add(Pair(days.size-count, count))
        }
        for ((first, second) in ranges) {
            val last = first + second - 1
            if (second > 2) {
                atoms.add("${days[first]} - ${days[last]}")
            } else {
                (first..last).mapTo(atoms) { days[it] }
            }
        }
        if (atoms.isEmpty())
            return ""
        val it = atoms.iterator()
        val builder = StringBuilder(it.next())
        for (atom in it) {
            builder.append(", $atom")
        }
        builder.append("  ${begin.toString(context)} - ${end.toString(context)}")
        return builder
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(begin.toInt())
        dest.writeInt(end.toInt())
        dest.writeBooleanArray(days)
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScheduleRule

        if (begin != other.begin) return false
        if (end != other.end) return false
        if (days != other.days) return false

        return true
    }

    override fun hashCode(): Int {
        var result = begin.hashCode()
        result = 31 * result + end.hashCode()
        result = 31 * result + days.hashCode()
        return result
    }

    override fun toString(): String {
        return "ScheduleRule(begin=$begin, end=$end, days=$days)"
    }

    companion object {
        const val SUNDAY = 0
        const val MONDAY = 1
        const val TUESDAY = 2
        const val WEDNESDAY = 3
        const val THURSDAY = 4
        const val FRIDAY = 5
        const val SATURDAY = 6

        @Suppress("unused") // required by Parcelable
        @JvmField val CREATOR = object : Parcelable.Creator<ScheduleRule> {

            override fun createFromParcel(source: Parcel?): ScheduleRule? = ScheduleRule(source!!)

            override fun newArray(size: Int): Array<out ScheduleRule>? {
                return newArray(size)
            }
        }

        private fun query(
                db: SQLiteDatabase = Polite.db.readableDatabase,
                selection: String? = null,
                selectionArgs: Array<String>? = null,
                orderBy: String? = null): Cursor {
            var sql = "SELECT ${DB.Rule.TABLE_NAME}.${BaseColumns._ID}," +
                    "${DB.Rule.COLUMN_NAME}," +
                    "${DB.Rule.COLUMN_ENABLE}," +
                    "${DB.Rule.COLUMN_VIBRATE}," +
                    "${DB.ScheduleRule.COLUMN_BEGIN}," +
                    "${DB.ScheduleRule.COLUMN_END}," +
                    "${DB.ScheduleRule.COLUMN_SUNDAY}," +
                    "${DB.ScheduleRule.COLUMN_MONDAY}," +
                    "${DB.ScheduleRule.COLUMN_TUESDAY}," +
                    "${DB.ScheduleRule.COLUMN_WEDNESDAY}," +
                    "${DB.ScheduleRule.COLUMN_THURSDAY}," +
                    "${DB.ScheduleRule.COLUMN_FRIDAY}," +
                    DB.ScheduleRule.COLUMN_SATURDAY +
                    " from ${DB.Rule.TABLE_NAME} INNER JOIN ${DB.ScheduleRule.TABLE_NAME} USING(${BaseColumns._ID})"

            if(selection != null) {
                sql += " WHERE $selection"
            }
            if(orderBy != null) {
                sql += " ORDER BY $orderBy"
            }
            return db.rawQuery(sql, selectionArgs)
        }

        fun queryList(
                db: SQLiteDatabase = Polite.db.readableDatabase,
                selection: String? = null,
                selectionArgs: Array<String>? = null,
                orderBy: String? = null): List<ScheduleRule> {
            val cursor = query(db, selection, selectionArgs, orderBy)
            val list = ArrayList<ScheduleRule>(cursor.count)
            while(cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val name = cursor.getString(1)
                val enabled = cursor.getInt(2) != 0
                val vibrate = cursor.getInt(3) != 0
                val begin = TimeOfDay(cursor.getInt(4))
                val end = TimeOfDay(cursor.getInt(5))
                val days = BooleanArray(7)
                for(i in 0..6) {
                    days[i] = cursor.getInt(i + 6) != 0
                }
                list.add(ScheduleRule(id, name, enabled, vibrate, begin, end, days))
            }

            return list
        }
    }
}
