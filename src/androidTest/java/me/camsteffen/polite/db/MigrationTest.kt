package me.camsteffen.polite.db

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import me.camsteffen.polite.BuildConfig.DATABASE_VERSION
import me.camsteffen.polite.model.CalendarEventMatchBy
import me.camsteffen.polite.util.TimeOfDay
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime
import java.util.EnumSet

private const val TEST_DATABASE_NAME = "test.db"

class MigrationTest {

    private val allMigrations = allMigrations(getInstrumentation().targetContext)

    @Rule @JvmField
    val migrationHelper = MigrationTestHelper(
        getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @After
    @Throws(Exception::class)
    fun tearDown() {
        getInstrumentation().targetContext.deleteDatabase(TEST_DATABASE_NAME)
    }

    @Test
    fun migrationFrom1_containsCorrectData() {
        val helper = getSqliteTestDbOpenHelper(1)
        insertRuleV1(
            id = 1,
            name = "name1",
            enable = true,
            vibrate = false,
            matchAll = false,
            matchTitle = true,
            matchDesc = true,
            helper = helper
        )
        insertRuleV1(
            id = 2,
            name = "name2",
            enable = false,
            vibrate = true,
            matchAll = true,
            matchTitle = false,
            matchDesc = false,
            helper = helper
        )
        insertRuleCalendar(1, 11L, helper)
        insertRuleCalendar(1, 12L, helper)
        insertRuleKeyword(1, "keyword1", helper)
        insertRuleKeyword(1, "keyword2", helper)

        migrationHelper.runMigrationsAndValidate(
            TEST_DATABASE_NAME, DATABASE_VERSION, true,
            *allMigrations
        )

        val dao = getMigratedRoomDatabase().ruleDao

        val calendarRules = dao.calendarRulesSortedByName()
        assertEquals(2, calendarRules.size)
        val calendarRule1 = calendarRules[0]
        val calendarRule2 = calendarRules[1]

        assertEquals(1, calendarRule1.id)
        assertEquals("name1", calendarRule1.name)
        assertTrue(calendarRule1.enabled)
        assertFalse(calendarRule1.vibrate)
        assertEquals(CalendarEventMatchBy.TITLE_AND_DESCRIPTION, calendarRule1.matchBy)
        assertFalse(calendarRule1.inverseMatch)
        assertEquals(setOf(11L, 12L), calendarRule1.calendarIds)
        assertEquals(setOf("keyword1", "keyword2"), calendarRule1.keywords)

        assertEquals(2, calendarRule2.id)
        assertEquals("name2", calendarRule2.name)
        assertFalse(calendarRule2.enabled)
        assertTrue(calendarRule2.vibrate)
        assertEquals(CalendarEventMatchBy.ALL, calendarRule2.matchBy)
        assertFalse(calendarRule2.inverseMatch)
        assertEquals(emptySet<Long>(), calendarRule2.calendarIds)
        assertEquals(emptySet<String>(), calendarRule2.keywords)
    }

    @Test
    fun migrationFrom2_containsCorrectData() {
        val helper = getSqliteTestDbOpenHelper(2)
        insertRuleV2(
            id = 1,
            name = "name1",
            enable = true,
            vibrate = false,
            helper = helper
        )
        insertCalendarRuleV2(
            id = 1,
            matchAll = false,
            matchTitle = true,
            matchDesc = true,
            helper = helper
        )
        insertCalendarRuleCalendarV2(1, 11L, helper)
        insertCalendarRuleCalendarV2(1, 12L, helper)
        insertCalendarRuleKeywordV2(1, "keyword1", helper)
        insertCalendarRuleKeywordV2(1, "keyword2", helper)
        insertRuleV2(
            id = 2,
            name = "name2",
            enable = false,
            vibrate = true,
            helper = helper
        )
        insertCalendarRuleV2(
            id = 2,
            matchAll = true,
            matchTitle = false,
            matchDesc = false,
            helper = helper
        )
        insertRuleV2(
            id = 3,
            enable = true,
            name = "name3",
            vibrate = true,
            helper = helper
        )
        insertScheduleRuleV2(
            id = 3,
            begin = LocalTime.of(13, 4),
            end = LocalTime.of(18, 59),
            days = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.THURSDAY),
            helper = helper
        )

        migrationHelper.runMigrationsAndValidate(
            TEST_DATABASE_NAME, DATABASE_VERSION, true,
            *allMigrations
        )
    }

    @Test
    fun migrationFrom3_containsCorrectData() {
        val helper = getSqliteTestDbOpenHelper(3)
        insertRuleV2(
            id = 1,
            name = "name1",
            enable = true,
            vibrate = false,
            helper = helper
        )
        insertCalendarRuleV3(
            id = 1,
            matchAll = false,
            matchTitle = true,
            matchDesc = true,
            inverseMatch = true,
            helper = helper
        )
        insertCalendarRuleCalendarV2(1, 11L, helper)
        insertCalendarRuleCalendarV2(1, 12L, helper)
        insertCalendarRuleKeywordV2(1, "keyword1", helper)
        insertCalendarRuleKeywordV2(1, "keyword2", helper)
        insertRuleV2(
            id = 2,
            name = "name2",
            enable = false,
            vibrate = true,
            helper = helper
        )
        insertCalendarRuleV3(
            id = 2,
            matchAll = true,
            matchTitle = false,
            matchDesc = false,
            inverseMatch = false,
            helper = helper
        )
        insertRuleV2(
            id = 3,
            enable = true,
            name = "name3",
            vibrate = true,
            helper = helper
        )
        insertScheduleRuleV2(
            id = 3,
            begin = LocalTime.of(13, 4),
            end = LocalTime.of(18, 59),
            days = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.THURSDAY),
            helper = helper
        )

        migrationHelper.runMigrationsAndValidate(
            TEST_DATABASE_NAME, DATABASE_VERSION, true,
            *allMigrations
        )

        val dao = getMigratedRoomDatabase().ruleDao

        val calendarRules = dao.calendarRulesSortedByName()
        assertEquals(2, calendarRules.size)
        val calendarRule1 = calendarRules[0]
        val calendarRule2 = calendarRules[1]

        assertEquals(1, calendarRule1.id)
        assertEquals("name1", calendarRule1.name)
        assertTrue(calendarRule1.enabled)
        assertFalse(calendarRule1.vibrate)
        assertEquals(CalendarEventMatchBy.TITLE_AND_DESCRIPTION, calendarRule1.matchBy)
        assertTrue(calendarRule1.inverseMatch)
        assertEquals(setOf(11L, 12L), calendarRule1.calendarIds)
        assertEquals(setOf("keyword1", "keyword2"), calendarRule1.keywords)

        assertEquals(2, calendarRule2.id)
        assertEquals("name2", calendarRule2.name)
        assertFalse(calendarRule2.enabled)
        assertTrue(calendarRule2.vibrate)
        assertEquals(CalendarEventMatchBy.ALL, calendarRule2.matchBy)
        assertFalse(calendarRule2.inverseMatch)
        assertEquals(emptySet<Long>(), calendarRule2.calendarIds)
        assertEquals(emptySet<String>(), calendarRule2.keywords)

        val scheduleRules = dao.scheduleRulesSortedByName()
        assertEquals(1, scheduleRules.size)
        val scheduleRule1 = scheduleRules[0]

        assertEquals(3, scheduleRule1.id)
        assertEquals("name3", scheduleRule1.name)
        assertTrue(scheduleRule1.enabled)
        assertTrue(scheduleRule1.vibrate)
        assertEquals(TimeOfDay(13, 4), scheduleRule1.begin)
        assertEquals(TimeOfDay(18, 59), scheduleRule1.end)
        assertEquals(EnumSet.of(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY, DayOfWeek.THURSDAY),
            scheduleRule1.days)
    }

    private fun getSqliteTestDbOpenHelper(version: Int): SqliteTestDbOpenHelper {
        return SqliteTestDbOpenHelper(
            getInstrumentation().context,
            getInstrumentation().targetContext,
            TEST_DATABASE_NAME, version)
    }

    private fun getMigratedRoomDatabase(): AppDatabase {
        val database = Room.databaseBuilder(
            getInstrumentation().targetContext,
            AppDatabase::class.java, TEST_DATABASE_NAME)
            .addMigrations(*allMigrations)
            .build()
        migrationHelper.closeWhenFinished(database)
        return database
    }
}
