package me.camsteffen.polite

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import me.camsteffen.polite.di.DaggerAppComponent
import javax.inject.Inject

class Polite : Application(), HasAndroidInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector() = dispatchingAndroidInjector

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        DaggerAppComponent.factory().create(this).inject(this)
    }

}
