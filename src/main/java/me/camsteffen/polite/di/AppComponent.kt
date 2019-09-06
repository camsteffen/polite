package me.camsteffen.polite.di

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import me.camsteffen.polite.Polite
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class
    ]
)
interface AppComponent : AndroidInjector<Polite> {
    @Component.Factory
    interface Factory : AndroidInjector.Factory<Polite>
}
