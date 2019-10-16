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
import me.camsteffen.polite.AppBroadcastReceiver
import me.camsteffen.polite.AppTimingConfig
import me.camsteffen.polite.MainActivity
import me.camsteffen.polite.Polite
import me.camsteffen.polite.R
import me.camsteffen.polite.db.AppDatabase
import me.camsteffen.polite.db.PoliteStateDao
import me.camsteffen.polite.defaultAppTimingConfig
import me.camsteffen.polite.receiver.CalendarChangeReceiver
import me.camsteffen.polite.rule.RuleMasterDetailViewModel
import me.camsteffen.polite.rule.edit.EditCalendarRuleViewModel
import me.camsteffen.polite.rule.edit.EditScheduleRuleViewModel
import me.camsteffen.polite.settings.PreferenceDefaults
import me.camsteffen.polite.util.CalendarRuleEventFinder
import me.camsteffen.polite.util.RuleEventFinder
import me.camsteffen.polite.util.ScheduleRuleEventFinder
import me.camsteffen.polite.util.SharedPreferenceBooleanLiveData
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
        modules = [MainActivityModule::class, MainActivityFragmentsModule::class]
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

    @Module
    companion object {

        @JvmStatic
        @Provides
        fun provideContentResolver(app: Application): ContentResolver = app.contentResolver

        @JvmStatic
        @Provides
        fun provideNotificationManager(app: Application): NotificationManager =
            app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        @JvmStatic
        @Provides
        @Singleton
        fun database(context: Context): AppDatabase {
            return AppDatabase.init(context)
        }

        @JvmStatic
        @Provides
        fun ruleDao(database: AppDatabase) = database.ruleDao

        @JvmStatic
        @Provides
        fun politeStateDao(database: AppDatabase): PoliteStateDao = database.politeStateDao

        @JvmStatic
        @Provides
        fun provideResources(app: Application): Resources = app.resources

        @JvmStatic
        @Provides
        fun provideSharedPreferences(app: Application): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(app)

        @JvmStatic
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

        @JvmStatic
        @Provides
        fun provideAlarmManager(context: Context): AlarmManager {
            return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }

        @JvmStatic
        @Provides
        fun provideAudioManager(context: Context): AudioManager {
            return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }

        @JvmStatic
        @Provides
        fun provideClock(): Clock {
            return Clock.systemDefaultZone()
        }

        @JvmStatic
        @Provides
        fun provideAppTimingConfig(): AppTimingConfig {
            return defaultAppTimingConfig
        }

        @JvmStatic
        @Provides
        fun provideWorkManager(context: Context): WorkManager {
            return WorkManager.getInstance(context)
        }
    }
}
