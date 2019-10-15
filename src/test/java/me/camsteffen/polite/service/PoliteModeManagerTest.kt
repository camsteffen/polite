package me.camsteffen.polite.service

import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import me.camsteffen.polite.data.AppPreferences
import me.camsteffen.polite.data.db.PoliteStateDao
import me.camsteffen.polite.data.db.RuleDao
import me.camsteffen.polite.data.db.entity.AudioPolicy
import me.camsteffen.polite.data.model.InterruptFilter
import me.camsteffen.polite.data.model.RuleEvent
import me.camsteffen.polite.util.AppPermissionChecker
import me.camsteffen.polite.util.TestObjects
import me.camsteffen.polite.util.defaultAppTimingConfig
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

class PoliteModeManagerTest {

    private lateinit var politeModeManager: PoliteModeManager

    private val permissionChecker: AppPermissionChecker = mockk()
    private val politeModeActuator: PoliteModeActuator = mockk(relaxUnitFun = true)
    private val preferences: AppPreferences = mockk()
    private val refreshScheduler: RefreshScheduler = mockk(relaxUnitFun = true)
    private val ruleDao: RuleDao = mockk()
    private val ruleEventFinders: RuleEventFinders = mockk()
    private val stateDao: PoliteStateDao = mockk(relaxUnitFun = true)
    private val timingConfig = defaultAppTimingConfig

    private val now = Instant.now()

    @Before
    fun setUp() {
        politeModeManager = PoliteModeManager(
            clock = Clock.fixed(now, ZoneId.systemDefault()),
            permissionChecker = permissionChecker,
            politeModeActuator = politeModeActuator,
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

        politeModeManager.refresh()

        verifyAll {
            ruleEventFinders wasNot Called
            politeModeActuator.setCurrentEvent(null)
            refreshScheduler.cancelAll()
        }
    }

    @Test
    fun `no rule events`() {
        refreshGiven()

        politeModeManager.refresh()

        verify {
            eventsInRange()
            politeModeActuator.setCurrentEvent(null)
            refreshScheduler.scheduleRefreshInWindow()
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

        politeModeManager.refresh()

        verify {
            eventsInRange()
            politeModeActuator.setCurrentEvent(ruleEvent)
            refreshScheduler.scheduleRefresh(eventEnd)
        }
    }

    @Test
    fun `silent event overlaps vibrate event`() {
        val currentEvent = TestObjects.calendarRuleEvent(
            begin = now - Duration.ofMinutes(30),
            end = now + Duration.ofMinutes(30),
            audioPolicy = AudioPolicy(false, false, InterruptFilter.PRIORITY)
        )
        val nextEvent = TestObjects.calendarRuleEvent(
            begin = now,
            // ends before the active event but should not affect refresh time
            end = now + Duration.ofMinutes(20),
            audioPolicy = AudioPolicy(true, false, InterruptFilter.PRIORITY)
        )
        refreshGiven(
            events = listOf(currentEvent, nextEvent)
        )

        politeModeManager.refresh()

        verify {
            eventsInRange()
            politeModeActuator.setCurrentEvent(currentEvent)
            refreshScheduler.scheduleRefresh(now + Duration.ofMinutes(30))
        }
    }

    @Test
    fun `vibrate event overlaps silent event`() {
        val currentEvent = TestObjects.calendarRuleEvent(
            begin = now - Duration.ofMinutes(30),
            end = now + Duration.ofMinutes(30),
            audioPolicy = AudioPolicy(true, false, InterruptFilter.PRIORITY)
        )
        val nextEvent = TestObjects.calendarRuleEvent(
            begin = now,
            end = now + Duration.ofMinutes(20),
            audioPolicy = AudioPolicy(false, false, InterruptFilter.PRIORITY)
        )
        refreshGiven(
            events = listOf(currentEvent, nextEvent)
        )

        politeModeManager.refresh()

        verify {
            eventsInRange()
            politeModeActuator.setCurrentEvent(nextEvent)
            refreshScheduler.scheduleRefresh(now + Duration.ofMinutes(20))
        }
    }

    @Test
    fun `future event only`() {
        refreshGiven(
            events = listOf(TestObjects.calendarRuleEvent(begin = now + Duration.ofHours(1)))
        )

        politeModeManager.refresh()

        verify {
            eventsInRange()
            politeModeActuator.setCurrentEvent(null)
            refreshScheduler.scheduleRefresh(now + Duration.ofHours(1))
        }
    }

    @Test
    fun `current event ends before future event`() {
        val currentEvent = TestObjects.calendarRuleEvent(
            begin = now,
            end = now + Duration.ofMinutes(10),
            audioPolicy = AudioPolicy(true, false, InterruptFilter.PRIORITY)
        )
        refreshGiven(
            events = listOf(
                currentEvent,
                TestObjects.calendarRuleEvent(
                    begin = now + Duration.ofMinutes(20),
                    end = now + Duration.ofMinutes(30),
                    // higher precedence should not matter
                    audioPolicy = AudioPolicy(false, false, InterruptFilter.PRIORITY)
                )
            )
        )

        politeModeManager.refresh()

        verify {
            eventsInRange()
            politeModeActuator.setCurrentEvent(currentEvent)
            refreshScheduler.scheduleRefresh(now + Duration.ofMinutes(10))
        }
    }

    @Test
    fun `future silent event begins before current silent event ends`() {
        val activeEvent = TestObjects.calendarRuleEvent(
            begin = now - Duration.ofMinutes(10),
            end = now + Duration.ofMinutes(20),
            audioPolicy = AudioPolicy(false, false, InterruptFilter.PRIORITY)
        )
        refreshGiven(
            events = listOf(
                activeEvent,
                TestObjects.calendarRuleEvent(
                    begin = now + Duration.ofMinutes(10),
                    end = now + Duration.ofMinutes(30),
                    audioPolicy = AudioPolicy(false, false, InterruptFilter.PRIORITY)
                )
            )
        )

        politeModeManager.refresh()

        verify {
            eventsInRange()
            politeModeActuator.setCurrentEvent(activeEvent)
            refreshScheduler.scheduleRefresh(now + Duration.ofMinutes(10))
        }
    }

    @Test
    fun `future vibrate event begins before current silent event ends`() {
        val currentEvent = TestObjects.calendarRuleEvent(
            begin = now - Duration.ofMinutes(10),
            end = now + Duration.ofMinutes(20),
            audioPolicy = AudioPolicy(false, false, InterruptFilter.PRIORITY)
        )
        refreshGiven(
            events = listOf(
                currentEvent,
                TestObjects.calendarRuleEvent(
                    begin = now + Duration.ofMinutes(10),
                    end = now + Duration.ofMinutes(30),
                    audioPolicy = AudioPolicy(true, false, InterruptFilter.PRIORITY)
                )
            )
        )

        politeModeManager.refresh()

        verify {
            eventsInRange()
            politeModeActuator.setCurrentEvent(currentEvent)
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
        every { ruleDao.getEnabledRulesExist() } returns true
        every {
            ruleEventFinders.all.eventsInRange(
                now + timingConfig.ruleEventBoundaryTolerance,
                now + timingConfig.lookahead
            )
        } returns events.asSequence()
    }
}
