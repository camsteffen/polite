package me.camsteffen.polite.util

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import me.camsteffen.polite.db.PoliteStateDao
import me.camsteffen.polite.db.RuleDao
import me.camsteffen.polite.model.ScheduleRule
import me.camsteffen.polite.model.ScheduleRuleCancel
import me.camsteffen.polite.rule.ScheduleRuleSchedule
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.Instant.EPOCH
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class ScheduleRuleEventFinderTest {

    /*
    Chicago timezone is used for predictable timezone rules with daylight savings

    here are a couple known offset transitions:
    Sun, 02 Apr 2000 03:00 -6:00 to -5:00
    Sun, 29 Oct 2000 02:00 -5:00 to -6:00
    */

    private lateinit var scheduleRuleEventFinder: ScheduleRuleEventFinder

    private val ruleDao: RuleDao = mockk()
    private val stateDao: PoliteStateDao = mockk()
    private val zone = ZoneId.of("America/Chicago")

    @Before
    fun setUp() {
        scheduleRuleEventFinder = ScheduleRuleEventFinder(
            clock = Clock.system(zone),
            ruleDao = ruleDao,
            stateDao = stateDao
        )
    }

    @Test
    fun `no rules yields no rule events`() {
        given(rules = emptyList())

        val ruleEvents = scheduleRuleEventFinder.eventsInRange(EPOCH, EPOCH).toList()

        assertThat(ruleEvents).isEmpty()
    }

    @Test
    fun `empty schedule yields no rule events`() {
        val begin = Instant.now()
        val end = Instant.now()

        given(
            rules = listOf(TestObjects.scheduleRule(ScheduleRuleSchedule(0, 0, 0, 0)))
        )

        val ruleEvents = scheduleRuleEventFinder.eventsInRange(begin, end).toList()

        assertThat(ruleEvents).isEmpty()
    }

    @Test
    fun `overnight schedule`() {
        val rule = TestObjects.scheduleRule(ScheduleRuleSchedule(20, 0, 6, 0, DayOfWeek.SUNDAY))
        val begin = LocalDateTime.of(2000, 1, 2, 12, 0) // sunday
        val end = LocalDateTime.of(2000, 1, 3, 12, 0)

        given(rules = listOf(rule))

        val ruleEvents = scheduleRuleEventFinder.eventsInRange(
            instant(begin), instant(end)
        ).toList()

        assertThat(ruleEvents).isEqualTo(
            listOf(
                ScheduleRuleEvent(
                    rule,
                    instant(LocalDateTime.of(2000, 1, 2, 20, 0)),
                    instant(LocalDateTime.of(2000, 1, 3, 6, 0))
                )
            )
        )
    }

    @Test
    fun `daylight savings transition event duration`() {
        val rule = TestObjects.scheduleRule(ScheduleRuleSchedule(20, 0, 6, 0, DayOfWeek.SATURDAY))
        val begin = LocalDateTime.of(2000, 4, 1, 12, 0)
        val end = LocalDateTime.of(2000, 4, 2, 12, 0)

        given(rules = listOf(rule))

        val ruleEvents = scheduleRuleEventFinder.eventsInRange(
            instant(begin), instant(end)
        ).toList()

        assertThat(ruleEvents).hasSize(1)
        assertThat(ruleEvents[0].duration).isEqualTo(Duration.ofHours(9))

        val ruleEventsNextWeek = scheduleRuleEventFinder.eventsInRange(
            instant(begin.plusWeeks(1)), instant(end.plusWeeks(1))
        ).toList()
        assertThat(ruleEventsNextWeek).hasSize(1)
        assertThat(ruleEventsNextWeek[0].duration).isEqualTo(Duration.ofHours(10))
    }

    @Test
    fun `complex rule set`() {
        val begin = LocalDateTime.of(2000, 1, 5, 19, 0) // wednesday
        val end = LocalDateTime.of(2000, 1, 12, 17, 0) // next week wednesday

        val rule1 = TestObjects.scheduleRule(
            ScheduleRuleSchedule(
                4, 0, 6, 30,
                DayOfWeek.SUNDAY, DayOfWeek.TUESDAY
            )
        )
        val rule2 = TestObjects.scheduleRule(
            ScheduleRuleSchedule(
                20, 0, 6, 0,
                DayOfWeek.WEDNESDAY
            )
        )
        val rule3 = TestObjects.scheduleRule(
            ScheduleRuleSchedule(
                18, 0, 22, 0,
                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY
            )
        )
        val rule4 = TestObjects.scheduleRule(
            ScheduleRuleSchedule(
                12, 0, 23, 0,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
            )
        )

        given(rules = listOf(rule1, rule2, rule3, rule4))

        val ruleEvents = scheduleRuleEventFinder.eventsInRange(
            instant(begin), instant(end)
        ).toList()

        assertThat(ruleEvents).isEqualTo(
            listOf(
                ScheduleRuleEvent(
                    rule3,
                    // wednesday
                    instant(LocalDateTime.of(2000, 1, 5, 18, 0)),
                    instant(LocalDateTime.of(2000, 1, 5, 22, 0))
                ),
                ScheduleRuleEvent(
                    rule2,
                    instant(LocalDateTime.of(2000, 1, 5, 20, 0)),
                    // thursday
                    instant(LocalDateTime.of(2000, 1, 6, 6, 0))
                ),
                ScheduleRuleEvent(
                    rule4,
                    instant(LocalDateTime.of(2000, 1, 6, 12, 0)),
                    instant(LocalDateTime.of(2000, 1, 6, 23, 0))
                ),
                ScheduleRuleEvent(
                    rule3,
                    instant(LocalDateTime.of(2000, 1, 6, 18, 0)),
                    instant(LocalDateTime.of(2000, 1, 6, 22, 0))
                ),
                ScheduleRuleEvent(
                    rule4,
                    // friday
                    instant(LocalDateTime.of(2000, 1, 7, 12, 0)),
                    instant(LocalDateTime.of(2000, 1, 7, 23, 0))
                ),
                ScheduleRuleEvent(
                    rule1,
                    // sunday
                    instant(LocalDateTime.of(2000, 1, 9, 4, 0)),
                    instant(LocalDateTime.of(2000, 1, 9, 6, 30))
                ),
                ScheduleRuleEvent(
                    rule1,
                    // tuesday
                    instant(LocalDateTime.of(2000, 1, 11, 4, 0)),
                    instant(LocalDateTime.of(2000, 1, 11, 6, 30))
                )
            )
        )
    }

    @Test
    fun `events barely in range`() {
        val rule = TestObjects.scheduleRule(ScheduleRuleSchedule(6, 0, 8, 0, DayOfWeek.SUNDAY))
        val begin = LocalDateTime.of(2000, 1, 2, 7, 59) // sunday
        val end = LocalDateTime.of(2000, 1, 9, 6, 1)

        given(rules = listOf(rule))

        val ruleEvents = scheduleRuleEventFinder.eventsInRange(
            instant(begin), instant(end)
        ).toList()

        assertThat(ruleEvents).isEqualTo(
            listOf(
                ScheduleRuleEvent(
                    rule,
                    instant(LocalDateTime.of(2000, 1, 2, 6, 0)),
                    instant(LocalDateTime.of(2000, 1, 2, 8, 0))
                ),
                ScheduleRuleEvent(
                    rule,
                    instant(LocalDateTime.of(2000, 1, 9, 6, 0)),
                    instant(LocalDateTime.of(2000, 1, 9, 8, 0))
                )
            )
        )

        val noEvents = scheduleRuleEventFinder.eventsInRange(
            instant(begin.plusMinutes(1)),
            instant(end.minusMinutes(1))
        ).toList()
        assertThat(noEvents).isEmpty()
    }

    @Test
    fun `events barely in range overnight schedule`() {
        val rule = TestObjects.scheduleRule(ScheduleRuleSchedule(22, 0, 2, 0, DayOfWeek.SUNDAY))
        val begin = LocalDateTime.of(2000, 1, 3, 1, 59) // monday
        val end = LocalDateTime.of(2000, 1, 9, 22, 1) // sunday

        given(rules = listOf(rule))

        val ruleEvents = scheduleRuleEventFinder.eventsInRange(
            instant(begin), instant(end)
        ).toList()

        assertThat(ruleEvents).isEqualTo(
            listOf(
                ScheduleRuleEvent(
                    rule,
                    instant(LocalDateTime.of(2000, 1, 2, 22, 0)),
                    instant(LocalDateTime.of(2000, 1, 3, 2, 0))
                ),
                ScheduleRuleEvent(
                    rule,
                    instant(LocalDateTime.of(2000, 1, 9, 22, 0)),
                    instant(LocalDateTime.of(2000, 1, 10, 2, 0))
                )
            )
        )

        val noEvents = scheduleRuleEventFinder.eventsInRange(
            instant(begin.plusMinutes(1)),
            instant(end.minusMinutes(1))
        ).toList()
        assertThat(noEvents).isEmpty()
    }

    @Test
    fun `some events canceled`() {
        val rule = TestObjects.scheduleRule(ScheduleRuleSchedule(6, 0, 20, 0, *DayOfWeek.values()))
        val begin = LocalDateTime.of(2000, 1, 1, 12, 0)
        val end = LocalDateTime.of(2000, 1, 2, 21, 0)

        given(
            rules = listOf(rule),
            cancels = mapOf(
                rule.id to instant(LocalDateTime.of(2000, 1, 1, 6, 1))
            )
        )

        val ruleEvents = scheduleRuleEventFinder.eventsInRange(
            instant(begin), instant(end)
        ).toList()

        assertThat(ruleEvents).isEqualTo(
            listOf(
                ScheduleRuleEvent(
                    rule,
                    instant(LocalDateTime.of(2000, 1, 2, 6, 0)),
                    instant(LocalDateTime.of(2000, 1, 2, 20, 0))
                )
            )
        )
    }

    private fun given(
        rules: List<ScheduleRule>,
        cancels: Map<Long, Instant> = emptyMap()
    ) {
        every { ruleDao.getEnabledScheduleRules() } returns rules
        every { stateDao.getScheduleRuleCancels() } returns
            cancels.map { ScheduleRuleCancel(it.key, it.value) }
    }

    private fun instant(localDateTime: LocalDateTime): Instant {
        return localDateTime.atZone(this.zone).toInstant()
    }
}
