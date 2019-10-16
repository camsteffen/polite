package me.camsteffen.polite.util

import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.camsteffen.polite.data.CalendarDao
import me.camsteffen.polite.data.CalendarEvent
import me.camsteffen.polite.db.PoliteStateDao
import me.camsteffen.polite.db.RuleDao
import me.camsteffen.polite.model.CalendarEventMatchBy
import me.camsteffen.polite.model.CalendarRule
import me.camsteffen.polite.model.EventCancel
import me.camsteffen.polite.settings.AppPreferences
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant

class CalendarRuleEventFinderTest {

    private val appPreferences: AppPreferences = mockk()
    private val calendarDao: CalendarDao = mockk()
    private val permissionChecker: AppPermissionChecker = mockk()
    private val politeStateDao: PoliteStateDao = mockk()
    private val ruleDao: RuleDao = mockk()

    private lateinit var calendarRuleEventFinder: CalendarRuleEventFinder

    // these values are arbitrary since they are only used as input to mocks
    private val begin = Instant.EPOCH
    private val end = Instant.EPOCH

    @Before
    fun setUp() {
        calendarRuleEventFinder = CalendarRuleEventFinder(
            appPreferences = appPreferences,
            permissionChecker = permissionChecker,
            politeStateDao = politeStateDao,
            ruleDao = ruleDao,
            calendarDao = calendarDao
        )
    }

    @Test
    fun `no rules produces no rule events`() {
        given()

        val events = calendarRuleEventFinder.eventsInRange(
            Instant.EPOCH,
            Instant.now()
        ).toList()

        assertThat(events).isEmpty()
    }

    @Test
    fun `no calendar permission`() {
        given(
            calendarRules = listOf(TestObjects.calendarRule()),
            hasReadCalendarPermission = false
        )

        val events = calendarRuleEventFinder.eventsInRange(begin, end).toList()

        assertThat(events).isEmpty()
        verify {
            permissionChecker.checkReadCalendarPermission()
            calendarDao wasNot Called
        }
    }

    @Test
    fun `no events produces no rule events`() {
        given(
            calendarRules = listOf(TestObjects.calendarRule())
        )

        val events = calendarRuleEventFinder.eventsInRange(begin, end).toList()

        assertThat(events).isEmpty()
    }

    @Test
    fun `all events cancelled produces no rule events`() {
        val rule = TestObjects.calendarRule()
        val calendarEvents = listOf(
            TestObjects.calendarEvent(),
            TestObjects.calendarEvent()
        )
        val eventCancels = calendarEvents.map { EventCancel(rule.id, it.eventId, Instant.MAX) }
        given(
            calendarRules = listOf(rule),
            calendarEvents = calendarEvents,
            eventCancels = eventCancels
        )

        val events = calendarRuleEventFinder.eventsInRange(begin, end).toList()

        assertThat(events).isEmpty()
    }

    @Test
    fun `some events cancelled produces some rule events`() {
        val calendarRule = TestObjects.calendarRule()
        val calendarEvents = listOf(
            TestObjects.calendarEvent(),
            TestObjects.calendarEvent(),
            TestObjects.calendarEvent()
        )
        val nonCancelledEvents = calendarEvents.subList(2, 3)

        given(
            calendarRules = listOf(calendarRule),
            calendarEvents = calendarEvents,
            eventCancels = calendarEvents.take(2)
                .map { EventCancel(calendarRule.id, it.eventId, Instant.MAX) }
        )

        val ruleEvents = calendarRuleEventFinder.eventsInRange(begin, end).toList()

        val expectedRuleEvents = nonCancelledEvents
            .map { event -> CalendarRuleEvent(calendarRule, event) }
        assertThat(ruleEvents).isEqualTo(expectedRuleEvents)
    }

    @Test
    fun `some events cancelled some don't match produces no rule events`() {
        val rule = TestObjects.calendarRule(
            matchBy = CalendarEventMatchBy.TITLE
        )
        val calendarEvents = listOf(
            TestObjects.calendarEvent(),
            TestObjects.calendarEvent()
        )
        val eventCancels = calendarEvents.take(1)
            .map { EventCancel(rule.id, it.eventId, Instant.MAX) }

        given(
            calendarRules = listOf(rule),
            calendarEvents = calendarEvents,
            eventCancels = eventCancels
        )

        val ruleEvents = calendarRuleEventFinder.eventsInRange(begin, end).toList()

        assertThat(ruleEvents).isEmpty()
    }

    @Test
    fun `case insensitive match`() {
        val calendarRule = TestObjects.calendarRule(
            matchBy = CalendarEventMatchBy.TITLE,
            keywords = setOf("derp")
        )
        val calendarEvents = listOf(
            TestObjects.calendarEvent(title = "DERp")
        )

        given(
            calendarRules = listOf(calendarRule),
            calendarEvents = calendarEvents
        )

        val ruleEvents = calendarRuleEventFinder.eventsInRange(begin, end).toList()

        assertThat(ruleEvents).isEqualTo(
            listOf(CalendarRuleEvent(calendarRule, calendarEvents[0]))
        )
    }

    @Test
    fun `partial word match`() {
        val calendarRule = TestObjects.calendarRule(
            matchBy = CalendarEventMatchBy.DESCRIPTION,
            keywords = setOf("derp")
        )
        val calendarEvents = listOf(
            TestObjects.calendarEvent(description = "DERpY")
        )

        given(
            calendarRules = listOf(calendarRule),
            calendarEvents = calendarEvents
        )

        val ruleEvents = calendarRuleEventFinder.eventsInRange(begin, end).toList()

        assertThat(ruleEvents).isEqualTo(
            listOf(CalendarRuleEvent(calendarRule, calendarEvents[0]))
        )
    }

    @Test
    fun `title and description rule match`() {
        val calendarRule = TestObjects.calendarRule(
            matchBy = CalendarEventMatchBy.TITLE_AND_DESCRIPTION,
            keywords = setOf("derp")
        )
        val calendarEvents = listOf(
            TestObjects.calendarEvent(title = "derp"),
            TestObjects.calendarEvent(),
            TestObjects.calendarEvent(description = "DERpY")
        )

        given(
            calendarRules = listOf(calendarRule),
            calendarEvents = calendarEvents
        )

        val ruleEvents = calendarRuleEventFinder.eventsInRange(begin, end).toList()

        assertThat(ruleEvents).isEqualTo(listOf(0, 2).map { i ->
            CalendarRuleEvent(calendarRule, calendarEvents[i])
        })
    }

    @Test
    fun testInverseMatch() {
        val calendarRule = TestObjects.calendarRule(
            inverseMatch = true,
            matchBy = CalendarEventMatchBy.TITLE_AND_DESCRIPTION,
            keywords = setOf("herp", "derp", "seashells")
        )
        val calendarEvents = listOf(
            TestObjects.calendarEvent(title = "der herp"),
            TestObjects.calendarEvent(description = "have a nice day"),
            TestObjects.calendarEvent(title = "wow mom", description = "she sells seashells")
        )

        given(
            calendarRules = listOf(calendarRule),
            calendarEvents = calendarEvents
        )

        val ruleEvents = calendarRuleEventFinder.eventsInRange(begin, end).toList()

        assertThat(ruleEvents).isEqualTo(
            listOf(CalendarRuleEvent(calendarRule, calendarEvents[1]))
        )
    }

    private fun given(
        calendarRules: List<CalendarRule> = emptyList(),
        calendarEvents: List<CalendarEvent> = emptyList(),
        eventCancels: List<EventCancel> = emptyList(),
        hasReadCalendarPermission: Boolean = true,
        activation: Int = 0,
        deactivation: Int = 0
    ) {
        every { ruleDao.getEnabledCalendarRules() } returns calendarRules
        every { permissionChecker.checkReadCalendarPermission() } returns hasReadCalendarPermission
        every { politeStateDao.getEventCancels() } returns eventCancels
        every { calendarDao.getEventsInRange(begin, end) } returns calendarEvents
        every { appPreferences.activation } returns activation
        every { appPreferences.deactivation } returns deactivation
    }
}
