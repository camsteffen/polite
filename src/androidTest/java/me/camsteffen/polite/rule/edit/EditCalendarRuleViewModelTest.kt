package me.camsteffen.polite.rule.edit

import android.app.Application
import androidx.test.annotation.UiThreadTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import me.camsteffen.polite.model.CalendarEventMatchBy
import me.camsteffen.polite.model.CalendarRule
import me.camsteffen.polite.ui.rule.edit.EditCalendarRuleViewModel
import org.junit.Test

class EditCalendarRuleViewModelTest {

    @Test
    @UiThreadTest
    fun rule_set_create_equality() {
        val rules = listOf(
            CalendarRule(
                id = 1,
                name = "test",
                enabled = true,
                vibrate = true,
                busyOnly = true,
                matchBy = CalendarEventMatchBy.ALL,
                inverseMatch = true,
                calendarIds = setOf(1L),
                keywords = setOf("foo")
            ),
            CalendarRule(
                id = 2,
                name = "test2",
                enabled = false,
                vibrate = false,
                busyOnly = false,
                matchBy = CalendarEventMatchBy.TITLE_AND_DESCRIPTION,
                inverseMatch = false,
                calendarIds = setOf(2L, 3L),
                keywords = emptySet()
            )
        )

        val application = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as Application
        val viewModel = EditCalendarRuleViewModel(application)
        for (rule in rules) {
            viewModel.setRule(rule)
            Truth.assertThat(viewModel.createRule(rule.id, rule.name)).isEqualTo(rule)
        }
    }
}
