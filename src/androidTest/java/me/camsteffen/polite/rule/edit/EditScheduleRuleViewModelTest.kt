package me.camsteffen.polite.rule.edit

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import me.camsteffen.polite.model.ScheduleRule
import me.camsteffen.polite.rule.ScheduleRuleSchedule
import org.junit.Test

class EditScheduleRuleViewModelTest {

    @Test
    fun rule_set_create_equality() {
        val rules = listOf(
            ScheduleRule(
                id = 1,
                name = "test",
                enabled = true,
                vibrate = true,
                schedule = ScheduleRuleSchedule(0, 10, 2, 20)
            ),
            ScheduleRule(
                id = 2,
                name = "test2",
                enabled = false,
                vibrate = false,
                schedule = ScheduleRuleSchedule(1, 30, 22, 0)
            )
        )

        val application = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as Application
        val viewModel = EditScheduleRuleViewModel(application)
        for (rule in rules) {
            viewModel.setRule(rule)
            Truth.assertThat(viewModel.createRule(rule.id, rule.name)).isEqualTo(rule)
        }
    }
}
