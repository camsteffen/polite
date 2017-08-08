package me.camsteffen.polite.rule.calendar

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
import me.camsteffen.polite.rule.Rule
import me.camsteffen.polite.rule.RuleAdapter
import java.util.*

class CalendarRule : Rule {

    val calendars: MutableList<Long>
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
        calendars = mutableListOf()
        keywords = TreeSet<String>()
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
        this.calendars = calendars.toMutableList()
        this.match = match
        this.inverseMatch = inverseMatch
        this.keywords = TreeSet(keywords)
    }

    constructor(parcel: Parcel) : super(parcel) {
        calendars = mutableListOf()
        parcel.readList(calendars, null)
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

    override fun saveDBExisting(context: Context, callback: () -> Unit) {
        DBActions.SaveCalendarRule(context, this).execute()
    }

    override fun getCaption(context: Context): CharSequence {
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
        dest.writeList(calendars)
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

            override fun createFromParcel(source: Parcel?): CalendarRule? {
                return CalendarRule(source!!)
            }

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
        const val INDEX_ID = 0
        const val INDEX_NAME = 1
        const val INDEX_ENABLE = 2
        const val INDEX_VIBRATE = 3
        const val INDEX_MATCH_ALL = 4
        const val INDEX_MATCH_TITLE = 5
        const val INDEX_MATCH_DESCRIPTION = 6
        const val INDEX_INVERSE_MATCH = 7

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
                val calendars = ArrayList<Long>(calendarCursor.count)
                while (calendarCursor.moveToNext()) {
                    calendars.add(calendarCursor.getLong(0))
                }
                calendarCursor.close()

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
                val keywords = ArrayList<String>(keywordCursor.count)
                while (keywordCursor.moveToNext()) {
                    keywords.add(keywordCursor.getString(0))
                }
                keywordCursor.close()

                list.add(CalendarRule(id, name, enabled, vibrate, calendars, match, inverseMatch, keywords))
            }
            ruleCursor.close()

            return list
        }
    }

}

