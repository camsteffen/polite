package me.camsteffen.polite.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
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
}
