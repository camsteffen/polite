package me.camsteffen.polite.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.camsteffen.polite.BuildConfig.DATABASE_VERSION
import me.camsteffen.polite.data.db.entity.ActiveRuleEvent
import me.camsteffen.polite.data.db.entity.CalendarRuleCalendar
import me.camsteffen.polite.data.db.entity.CalendarRuleEntity
import me.camsteffen.polite.data.db.entity.CalendarRuleKeyword
import me.camsteffen.polite.data.db.entity.EventCancel
import me.camsteffen.polite.data.db.entity.RuleEntity
import me.camsteffen.polite.data.db.entity.ScheduleRuleCancel
import me.camsteffen.polite.data.db.entity.ScheduleRuleEntity

@Database(
    version = DATABASE_VERSION,
    entities = [
        RuleEntity::class,
        CalendarRuleEntity::class,
        CalendarRuleCalendar::class,
        CalendarRuleKeyword::class,
        ScheduleRuleEntity::class,
        ActiveRuleEvent::class,
        EventCancel::class,
        ScheduleRuleCancel::class
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val ruleDao: RuleDao
    abstract val politeStateDao: PoliteStateDao

    companion object {
        fun init(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addMigrations(*allMigrations(context)).build()
        }
    }
}

const val DATABASE_NAME = "Polite.db"
