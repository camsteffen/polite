package me.camsteffen.polite.di

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.preference.PreferenceManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import me.camsteffen.polite.AppBroadcastReceiver
import me.camsteffen.polite.MainActivity
import me.camsteffen.polite.Polite

@Module
abstract class AppModule {

    @Binds
    abstract fun app(app: Polite): Application

    @Binds
    abstract fun provideContext(app: Application): Context

    @ContributesAndroidInjector(modules = [MainActivityFragmentsModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeAppBroadcastReceiver(): AppBroadcastReceiver

    @Module
    companion object {

        @JvmStatic @Provides
        fun provideNotificationManager(app: Application): NotificationManager =
                app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        @JvmStatic @Provides
        fun provideResources(app: Application): Resources = app.resources

        @JvmStatic @Provides
        fun provideSharedPreferences(app: Application): SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(app)
    }
}
