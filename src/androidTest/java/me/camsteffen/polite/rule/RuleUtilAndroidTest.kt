package me.camsteffen.polite.rule

import android.content.Context
import android.os.LocaleList
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.DayOfWeek
import java.util.Locale

class RuleUtilAndroidTest {

    private val targetContext: Context get() =
        InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun scheduleSummary_everyDay() {
        val schedule = ScheduleRuleSchedule(12, 0, 14, 26, *DayOfWeek.values())
        val summary = schedule.summary(targetContext.withLocale(Locale.US))
        assertEquals("Every day 12:00 PM - 2:26 PM", summary)
    }

    // a day range of two days (e.g. Tue, Wed) should be comma-separated instead of a dash
    @Test
    fun scheduleSummary_twoDayRange() {
        val schedule = ScheduleRuleSchedule(
            16, 0,
            6, 30,
            DayOfWeek.SUNDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY
        )
        val summary = schedule.summary(targetContext.withLocale(Locale.US))
        assertEquals("Sun, Tue, Wed 4:00 PM - 6:30 AM", summary)
    }

    // if a range has the first day of the week in the middle, it should be split so that the first
    // day of the week is first
    @Test
    fun scheduleSummary_rangeOverFirstDayOfWeek() {
        val schedule = ScheduleRuleSchedule(
            16, 0,
            6, 30,
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY
        )
        val summary = schedule.summary(targetContext.withLocale(Locale.US))
        assertEquals("Sun, Mon, Sat 4:00 PM - 6:30 AM", summary)
    }

    @Test
    fun scheduleSummary_largeRange() {
        val schedule = ScheduleRuleSchedule(
            16, 0,
            6, 30,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY
        )
        val summary = schedule.summary(targetContext.withLocale(Locale.US))
        assertEquals("Sun - Tue, Thu - Sat 4:00 PM - 6:30 AM", summary)
    }
}

private fun Context.withLocale(locale: Locale): Context {
    val config = resources.configuration
    config.locales = LocaleList(locale)
    return createConfigurationContext(config)
}
