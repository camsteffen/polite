package me.camsteffen.polite.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.camsteffen.polite.BuildConfig.DATABASE_VERSION
import me.camsteffen.polite.model.ActiveRuleEvent
import me.camsteffen.polite.model.CalendarRuleCalendar
import me.camsteffen.polite.model.CalendarRuleEntity
import me.camsteffen.polite.model.CalendarRuleKeyword
import me.camsteffen.polite.model.EventCancel
import me.camsteffen.polite.model.RuleEntity
import me.camsteffen.polite.model.ScheduleRuleCancel
import me.camsteffen.polite.model.ScheduleRuleEntity

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
