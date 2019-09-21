package me.camsteffen.polite.state

import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import me.camsteffen.polite.db.PoliteStateDao
import me.camsteffen.polite.db.RuleDao
import me.camsteffen.polite.defaultAppTimingConfig
import me.camsteffen.polite.settings.AppPreferences
import me.camsteffen.polite.util.AppPermissionChecker
import me.camsteffen.polite.util.RuleEvent
import me.camsteffen.polite.util.RuleEventFinders
import me.camsteffen.polite.util.TestObjects
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

class PoliteStateManagerTest {

    private lateinit var politeStateManager: PoliteStateManager

    private val permissionChecker: AppPermissionChecker = mockk()
    private val politeModeController: PoliteModeController = mockk(relaxUnitFun = true)
    private val preferences: AppPreferences = mockk()
    private val refreshScheduler: RefreshScheduler = mockk(relaxUnitFun = true)
    private val ruleDao: RuleDao = mockk()
    private val ruleEventFinders: RuleEventFinders = mockk()
    private val stateDao: PoliteStateDao = mockk(relaxUnitFun = true)
    private val timingConfig = defaultAppTimingConfig

    private val now = Instant.now()

    @Before
    fun setUp() {
        politeStateManager = PoliteStateManager(
            clock = Clock.fixed(now, ZoneId.systemDefault()),
            permissionChecker = permissionChecker,
            politeModeController = politeModeController,
            preferences = preferences,
            refreshScheduler = refreshScheduler,
            ruleDao = ruleDao,
            ruleEventFinders = ruleEventFinders,
            stateDao = stateDao,
            timingConfig = timingConfig
        )

        every { ruleDao.getEnabledCalendarRulesExist() } returns false
    }

    @Test
    fun `refresh polite disabled`() {
        refreshGiven(
            politeEnabled = false,
            hasNotificationPolicyAccess = true
        )

        politeStateManager.refresh()

        verifyAll {
            ruleEventFinders wasNot Called
            politeModeController.setCurrentEvent(null)
            refreshScheduler.cancelAll()
        }
    }

    @Test
    fun `no rule events`() {
        refreshGiven()

        politeStateManager.refresh()

        verify {
            eventsInRange()
            politeModeController.setCurrentEvent(null)
            refreshScheduler.scheduleRefresh()
        }
    }

    @Test
    fun `one current event`() {
        val eventEnd = now + Duration.ofHours(1)
        val ruleEvent = TestObjects.calendarRuleEvent(
            begin = now,
            end = eventEnd
        )
        refreshGiven(
            events = listOf(ruleEvent)
        )

        politeStateManager.refresh()

        verify {
            eventsInRange()
            politeModeController.setCurrentEvent(ruleEvent)
            refreshScheduler.scheduleRefresh(eventEnd)
        }
    }

    @Test
    fun `silent event overlaps vibrate event`() {
        val currentEvent = TestObjects.calendarRuleEvent(
            begin = now - Duration.ofMinutes(30),
            end = now + Duration.ofMinutes(30),
            vibrate = false
        )
        val nextEvent = TestObjects.calendarRuleEvent(
            begin = now,
            // ends before the active event but should not affect refresh time
            end = now + Duration.ofMinutes(20),
            vibrate = true
        )
        refreshGiven(
            events = listOf(currentEvent, nextEvent)
        )

        politeStateManager.refresh()

        verify {
            eventsInRange()
            politeModeController.setCurrentEvent(currentEvent)
            refreshScheduler.scheduleRefresh(now + Duration.ofMinutes(30))
        }
    }

    @Test
    fun `vibrate event overlaps silent event`() {
        val currentEvent = TestObjects.calendarRuleEvent(
            begin = now - Duration.ofMinutes(30),
            end = now + Duration.ofMinutes(30),
            vibrate = true
        )
        val nextEvent = TestObjects.calendarRuleEvent(
            begin = now,
            end = now + Duration.ofMinutes(20),
            vibrate = false
        )
        refreshGiven(
            events = listOf(currentEvent, nextEvent)
        )

        politeStateManager.refresh()

        verify {
            eventsInRange()
            politeModeController.setCurrentEvent(nextEvent)
            refreshScheduler.scheduleRefresh(now + Duration.ofMinutes(20))
        }
    }

    @Test
    fun `future event only`() {
        refreshGiven(
            events = listOf(TestObjects.calendarRuleEvent(begin = now + Duration.ofHours(1)))
        )

        politeStateManager.refresh()

        verify {
            eventsInRange()
            politeModeController.setCurrentEvent(null)
            refreshScheduler.scheduleRefresh(now + Duration.ofHours(1))
        }
    }

    @Test
    fun `current event ends before future event`() {
        val currentEvent = TestObjects.calendarRuleEvent(
            begin = now,
            end = now + Duration.ofMinutes(10),
            vibrate = true
        )
        refreshGiven(
            events = listOf(
                currentEvent,
                TestObjects.calendarRuleEvent(
                    begin = now + Duration.ofMinutes(20),
                    end = now + Duration.ofMinutes(30),
                    // higher precedence should not matter
                    vibrate = false
                )
            )
        )

        politeStateManager.refresh()

        verify {
            eventsInRange()
            politeModeController.setCurrentEvent(currentEvent)
            refreshScheduler.scheduleRefresh(now + Duration.ofMinutes(10))
        }
    }

    @Test
    fun `future silent event begins before current silent event ends`() {
        val activeEvent = TestObjects.calendarRuleEvent(
            begin = now - Duration.ofMinutes(10),
            end = now + Duration.ofMinutes(20),
            vibrate = false
        )
        refreshGiven(
            events = listOf(
                activeEvent,
                TestObjects.calendarRuleEvent(
                    begin = now + Duration.ofMinutes(10),
                    end = now + Duration.ofMinutes(30),
                    vibrate = false
                )
            )
        )

        politeStateManager.refresh()

        verify {
            eventsInRange()
            politeModeController.setCurrentEvent(activeEvent)
            refreshScheduler.scheduleRefresh(now + Duration.ofMinutes(10))
        }
    }

    @Test
    fun `future vibrate event begins before current silent event ends`() {
        val currentEvent = TestObjects.calendarRuleEvent(
            begin = now - Duration.ofMinutes(10),
            end = now + Duration.ofMinutes(20),
            vibrate = false
        )
        refreshGiven(
            events = listOf(
                currentEvent,
                TestObjects.calendarRuleEvent(
                    begin = now + Duration.ofMinutes(10),
                    end = now + Duration.ofMinutes(30),
                    vibrate = true
                )
            )
        )

        politeStateManager.refresh()

        verify {
            eventsInRange()
            politeModeController.setCurrentEvent(currentEvent)
            refreshScheduler.scheduleRefresh(now + Duration.ofMinutes(20))
        }
    }

    private fun eventsInRange() {
        ruleEventFinders.all.eventsInRange(
            now + timingConfig.ruleEventBoundaryTolerance,
            now + timingConfig.lookahead
        )
    }

    private fun refreshGiven(
        events: List<RuleEvent> = emptyList(),
        politeEnabled: Boolean = true,
        hasNotificationPolicyAccess: Boolean = true
    ) {
        every { preferences.enable } returns politeEnabled
        every { permissionChecker.checkNotificationPolicyAccess() } returns
            hasNotificationPolicyAccess
        every {
            ruleEventFinders.all.eventsInRange(
                now + timingConfig.ruleEventBoundaryTolerance,
                now + timingConfig.lookahead
            )
        } returns events.asSequence()
    }
}
