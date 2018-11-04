package me.camsteffen.polite.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.camsteffen.polite.BuildConfig.DATABASE_VERSION
import me.camsteffen.polite.model.CalendarRuleCalendar
import me.camsteffen.polite.model.CalendarRuleEntity
import me.camsteffen.polite.model.CalendarRuleKeyword
import me.camsteffen.polite.model.RuleEntity
import me.camsteffen.polite.model.ScheduleRuleEntity

@Database(
    version = DATABASE_VERSION,
    entities = [
        RuleEntity::class,
        CalendarRuleEntity::class,
        CalendarRuleCalendar::class,
        CalendarRuleKeyword::class,
        ScheduleRuleEntity::class
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase()
