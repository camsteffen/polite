package me.camsteffen.polite.rule

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.TemporalAdjusters

class ScheduleRuleScheduleTest {

    private val nextMonday: LocalDate =
        LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY))

    @Test
    fun eventDuration_sameDay() {
        val duration = ScheduleRuleSchedule(1, 30, 10, 0).eventDuration()
        assertEquals(Duration.ofHours(8).plusMinutes(30), duration)
    }

    @Test
    fun eventDuration_overnight() {
        val duration = ScheduleRuleSchedule(22, 30, 6, 30).eventDuration()
        assertEquals(Duration.ofHours(8), duration)
    }

    @Test
    fun eventDuration_24Hours() {
        val duration = ScheduleRuleSchedule(1, 30, 1, 30).eventDuration()
        assertEquals(Duration.ofDays(1), duration)
    }

    @Test
    fun eventAt_begin() {
        val schedule = ScheduleRuleSchedule(12, 0, 13, 0, DayOfWeek.MONDAY)
        val event = schedule.eventAt(nextMonday.atTime(12, 0))
        val expected =
            ScheduleRuleSchedule.LocalEvent(nextMonday.atTime(12, 0), nextMonday.atTime(13, 0))
        assertEquals(expected, event)
    }

    @Test
    fun eventAt_end() {
        val schedule = ScheduleRuleSchedule(12, 0, 13, 0, DayOfWeek.MONDAY)
        val event = schedule.eventAt(nextMonday.atTime(13, 0))
        assertNull(event)
    }

    @Test
    fun eventAt_overnightFirstDay() {
        val schedule = ScheduleRuleSchedule(13, 0, 12, 0, DayOfWeek.MONDAY)
        val event = schedule.eventAt(nextMonday.atTime(14, 0))
        val expected = ScheduleRuleSchedule.LocalEvent(
            nextMonday.atTime(13, 0),
            nextMonday.plusDays(1).atTime(12, 0)
        )
        assertEquals(expected, event)
    }

    @Test
    fun eventAt_overnightNextDay() {
        val schedule = ScheduleRuleSchedule(13, 0, 12, 0, DayOfWeek.MONDAY)
        val event = schedule.eventAt(nextMonday.plusDays(1).atTime(1, 0))
        val expected = ScheduleRuleSchedule.LocalEvent(
            nextMonday.atTime(13, 0),
            nextMonday.plusDays(1).atTime(12, 0)
        )
        assertEquals(expected, event)
    }

    @Test
    fun eventAt_24Hours() {
        val schedule = ScheduleRuleSchedule(1, 30, 1, 30, DayOfWeek.MONDAY)
        val event = schedule.eventAt(nextMonday.atTime(2, 0))
        val expected = ScheduleRuleSchedule.LocalEvent(
            nextMonday.atTime(1, 30),
            nextMonday.plusDays(1).atTime(1, 30)
        )
        assertEquals(expected, event)
    }

    @Test
    fun eventAt_24Hours_tooLate() {
        val schedule = ScheduleRuleSchedule(1, 30, 1, 30, DayOfWeek.MONDAY)
        val event = schedule.eventAt(nextMonday.plusDays(1).atTime(2, 0))
        assertNull(event)
    }

    @Test
    fun eventAt_mismatchDayOfWeek() {
        val schedule = ScheduleRuleSchedule(12, 0, 13, 0, DayOfWeek.SUNDAY)
        val event =
            schedule.eventAt(LocalDateTime.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)))
        assertNull(event)
    }

    @Test
    fun eventAt_overnightTooEarly() {
        val schedule = ScheduleRuleSchedule(20, 0, 6, 0, DayOfWeek.SUNDAY)
        val event = schedule.eventAt(
            LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).atTime(19, 0)
        )
        assertNull(event)
    }

    @Test
    fun eventAt_overnightTooLate() {
        val schedule = ScheduleRuleSchedule(20, 0, 6, 0, DayOfWeek.SUNDAY)
        val event = schedule.eventAt(
            LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atTime(7, 0)
        )
        assertNull(event)
    }

    @Test
    fun firstEventAfter_soon() {
        val schedule = ScheduleRuleSchedule(12, 0, 13, 0, DayOfWeek.MONDAY)
        val event = schedule.firstEventAfter(nextMonday.atTime(11, 0))
        val expected =
            ScheduleRuleSchedule.LocalEvent(nextMonday.atTime(12, 0), nextMonday.atTime(13, 0))
        assertEquals(expected, event)
    }

    @Test
    fun firstEventAfter_begin() {
        val schedule = ScheduleRuleSchedule(12, 0, 13, 0, DayOfWeek.MONDAY)
        val event = schedule.firstEventAfter(nextMonday.atTime(12, 0))
        val expected =
            ScheduleRuleSchedule.LocalEvent(nextMonday.atTime(12, 0), nextMonday.atTime(13, 0))
        assertEquals(expected, event)
    }

    @Test
    fun firstEventAfter_laterTime() {
        val schedule = ScheduleRuleSchedule(12, 0, 13, 0, DayOfWeek.MONDAY)
        val event = schedule.firstEventAfter(nextMonday.atTime(12, 30))
        val nextWeekMonday = nextMonday.plusWeeks(1)
        val expected = ScheduleRuleSchedule.LocalEvent(
            nextWeekMonday.atTime(12, 0),
            nextWeekMonday.atTime(13, 0)
        )
        assertEquals(expected, event)
    }

    @Test
    fun firstEventAfter_afterBeginOtherDay() {
        val schedule = ScheduleRuleSchedule(
            12, 0,
            13, 0,
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
        )
        val event = schedule.firstEventAfter(nextMonday.minusDays(1).atTime(12, 30))
        val expected = ScheduleRuleSchedule.LocalEvent(
            nextMonday.atTime(12, 0),
            nextMonday.atTime(13, 0)
        )
        assertEquals(expected, event)
    }

    @Test
    fun firstEventAfter_noDays() {
        val event = ScheduleRuleSchedule(0, 0, 0, 0)
            .firstEventAfter(LocalDateTime.now())
        assertNull(event)
    }
}
