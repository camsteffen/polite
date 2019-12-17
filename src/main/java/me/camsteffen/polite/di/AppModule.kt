package me.camsteffen.polite.di

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.media.AudioManager
import android.preference.PreferenceManager
import androidx.core.content.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import me.camsteffen.polite.Polite
import me.camsteffen.polite.R
import me.camsteffen.polite.data.PreferenceDefaults
import me.camsteffen.polite.data.db.AppDatabase
import me.camsteffen.polite.data.db.PoliteStateDao
import me.camsteffen.polite.service.CalendarRuleEventFinder
import me.camsteffen.polite.service.RuleEventFinder
import me.camsteffen.polite.service.ScheduleRuleEventFinder
import me.camsteffen.polite.service.receiver.AppBroadcastReceiver
import me.camsteffen.polite.service.receiver.CalendarChangeReceiver
import me.camsteffen.polite.ui.MainActivity
import me.camsteffen.polite.ui.rule.RuleMasterDetailViewModel
import me.camsteffen.polite.ui.rule.edit.EditCalendarRuleViewModel
import me.camsteffen.polite.ui.rule.edit.EditScheduleRuleViewModel
import me.camsteffen.polite.util.AppTimingConfig
import me.camsteffen.polite.util.SharedPreferenceBooleanLiveData
import me.camsteffen.polite.util.defaultAppTimingConfig
import org.threeten.bp.Clock
import javax.inject.Singleton

@Module
abstract class AppModule {

    @Binds
    abstract fun app(app: Polite): Application

    @Binds
    abstract fun provideContext(app: Application): Context

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [
            MainActivityModule::class,
            MainActivityFragmentsModule::class,
            FragmentBindingModule::class
        ]
    )
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeAppBroadcastReceiver(): AppBroadcastReceiver

    @ContributesAndroidInjector
    abstract fun contributeCalendarChangeReceiver(): CalendarChangeReceiver

    @Binds
    abstract fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(RuleMasterDetailViewModel::class)
    abstract fun bindRuleMasterDetailViewModel(
        ruleMasterDetailViewModel: RuleMasterDetailViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditCalendarRuleViewModel::class)
    abstract fun bindEditCalendarRuleViewModel(
        editCalendarRuleViewModel: EditCalendarRuleViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditScheduleRuleViewModel::class)
    abstract fun bindEditScheduleRuleViewModel(
        editScheduleRuleViewModel: EditScheduleRuleViewModel
    ): ViewModel

    @Binds
    @IntoSet
    abstract fun bindCalendarRuleEventFinder(
        calendarRuleEventFinder: CalendarRuleEventFinder
    ): RuleEventFinder<*>

    @Binds
    @IntoSet
    abstract fun bindScheduleRuleEventFinder(
        scheduleRuleEventFinder: ScheduleRuleEventFinder
    ): RuleEventFinder<*>
}

@Module
object AppModuleProviders {
    @Provides
    fun provideContentResolver(app: Application): ContentResolver = app.contentResolver

    @Provides
    fun provideNotificationManager(app: Application): NotificationManager =
        app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @Singleton
    fun database(context: Context): AppDatabase = AppDatabase.init(context)

    @Provides
    fun ruleDao(database: AppDatabase) = database.ruleDao

    @Provides
    fun politeStateDao(database: AppDatabase): PoliteStateDao = database.politeStateDao

    @Provides
    fun provideResources(app: Application): Resources = app.resources

    @Provides
    fun provideSharedPreferences(app: Application): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(app)

    @Provides
    fun provideEnableLiveData(
        sharedPreferences: SharedPreferences,
        resources: Resources
    ): LiveData<Boolean> {
        val key = resources.getString(R.string.preference_enable)
        return SharedPreferenceBooleanLiveData(
            sharedPreferences,
            key,
            PreferenceDefaults.ENABLE
        )
    }

    @Provides
    fun provideAlarmManager(context: Context): AlarmManager = context.getSystemService()!!

    @Provides
    fun provideAudioManager(context: Context): AudioManager = context.getSystemService()!!

    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    fun provideAppTimingConfig(): AppTimingConfig = defaultAppTimingConfig

    @Provides
    fun provideWorkManager(context: Context): WorkManager = WorkManager.getInstance(context)
}
