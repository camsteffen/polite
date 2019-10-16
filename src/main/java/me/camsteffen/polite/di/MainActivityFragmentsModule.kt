package me.camsteffen.polite.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import me.camsteffen.polite.rule.RenameDialogFragment
import me.camsteffen.polite.rule.edit.EditCalendarRuleFragment
import me.camsteffen.polite.rule.edit.EditScheduleRuleFragment
import me.camsteffen.polite.rule.master.RulesFragment
import me.camsteffen.polite.settings.SettingsFragment

@Module
abstract class MainActivityFragmentsModule {

    @ContributesAndroidInjector
    abstract fun contributeRulesFragment(): RulesFragment

    @ContributesAndroidInjector
    abstract fun contributeEditCalendarRuleFragment(): EditCalendarRuleFragment

    @ContributesAndroidInjector
    abstract fun contributeEditScheduleRuleFragment(): EditScheduleRuleFragment

    @ContributesAndroidInjector
    abstract fun contributeRenameDialogFragment(): RenameDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment
}
