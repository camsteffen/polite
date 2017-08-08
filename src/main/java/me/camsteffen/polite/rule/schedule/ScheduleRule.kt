package me.camsteffen.polite.rule.schedule

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import me.camsteffen.polite.*
import me.camsteffen.polite.rule.Rule
import me.camsteffen.polite.rule.RuleAdapter
import java.util.*

class ScheduleRule : Rule {

    val begin: TimeOfDay
    val end: TimeOfDay
    val days: BooleanArray

    constructor(context: Context) : super(context) {
        begin = TimeOfDay(12, 0)
        end = TimeOfDay(13, 0)
        days = booleanArrayOf(false, true, true, true, true, true, false) // Mon-Fri
    }

    constructor(id: Long, name: String, enabled: Boolean, vibrate: Boolean, begin: TimeOfDay, end: TimeOfDay, days: BooleanArray) : super(id, name, enabled, vibrate) {
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

    override fun saveDBExisting(context: Context, callback: () -> Unit) {
        DBActions.SaveScheduleRule(context, this).execute()
    }

    override fun getCaption(context: Context): CharSequence {
        val days = context.resources.getStringArray(R.array.day_abbreviations)
        val atoms = ArrayList<String>()
        var count = 0
        val ranges = ArrayList<Pair<Int, Int>>()
        for (i in 0..days.size - 1) {
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

            override fun createFromParcel(source: Parcel?): ScheduleRule? {
                return ScheduleRule(source!!)
            }

            override fun newArray(size: Int): Array<out ScheduleRule>? {
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
