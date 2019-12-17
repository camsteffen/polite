package me.camsteffen.polite.di

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import me.camsteffen.polite.ui.rule.edit.EditCalendarRuleFragment
import me.camsteffen.polite.ui.rule.edit.EditScheduleRuleFragment
import me.camsteffen.polite.ui.rule.master.RulesFragment
import me.camsteffen.polite.ui.settings.SettingsFragment

@Module
abstract class FragmentBindingModule {

    @Binds
    @IntoMap
    @FragmentKey(RulesFragment::class)
    abstract fun bindRulesFragment(rulesFragment: RulesFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(EditCalendarRuleFragment::class)
    abstract fun bindEditCalendarRuleFragment(
        editCalendarRuleFragment: EditCalendarRuleFragment
    ): Fragment

    @Binds
    @IntoMap
    @FragmentKey(EditScheduleRuleFragment::class)
    abstract fun bindEditScheduleRuleFragment(
        editScheduleRuleFragment: EditScheduleRuleFragment
    ): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SettingsFragment::class)
    abstract fun bindSettingsFragment(settingsFragment: SettingsFragment): Fragment
}
