package me.camsteffen.polite.di

import android.app.Application
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import me.camsteffen.polite.AppBroadcastReceiver
import me.camsteffen.polite.MainActivity
import me.camsteffen.polite.Polite
import me.camsteffen.polite.R
import me.camsteffen.polite.db.AppDatabase
import me.camsteffen.polite.rule.RuleMasterDetailViewModel
import me.camsteffen.polite.rule.edit.EditCalendarRuleViewModel
import me.camsteffen.polite.rule.edit.EditScheduleRuleViewModel
import me.camsteffen.polite.settings.PreferenceDefaults
import me.camsteffen.polite.util.SharedPreferenceBooleanLiveData
import javax.inject.Singleton

@Module
abstract class AppModule {

    @Binds
    abstract fun app(app: Polite): Application

    @Binds
    abstract fun provideContext(app: Application): Context

    @ContributesAndroidInjector(modules = [MainActivityModule::class, MainActivityFragmentsModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeAppBroadcastReceiver(): AppBroadcastReceiver

    @Binds
    abstract fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(RuleMasterDetailViewModel::class)
    abstract fun bindRuleMasterDetailViewModel(ruleMasterDetailViewModel: RuleMasterDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditCalendarRuleViewModel::class)
    abstract fun bindEditCalendarRuleViewModel(editCalendarRuleViewModel: EditCalendarRuleViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditScheduleRuleViewModel::class)
    abstract fun bindEditScheduleRuleViewModel(editScheduleRuleViewModel: EditScheduleRuleViewModel): ViewModel

    @Module
    companion object {

        @JvmStatic @Provides
        fun provideContentResolver(app: Application): ContentResolver = app.contentResolver

        @JvmStatic @Provides
        fun provideNotificationManager(app: Application): NotificationManager =
                app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        @JvmStatic @Provides @Singleton
        fun database(context: Context): AppDatabase {
            return AppDatabase.init(context)
        }

        @JvmStatic @Provides
        fun ruleDao(database: AppDatabase) = database.ruleDao

        @JvmStatic @Provides
        fun provideResources(app: Application): Resources = app.resources

        @JvmStatic @Provides
        fun provideSharedPreferences(app: Application): SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(app)

        @JvmStatic @Provides
        fun provideEnableLiveData(sharedPreferences: SharedPreferences, resources: Resources): LiveData<Boolean> {
            val key = resources.getString(R.string.preference_enable)
            return SharedPreferenceBooleanLiveData(sharedPreferences, key, PreferenceDefaults.ENABLE)
        }
    }
}
