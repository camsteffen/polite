package me.camsteffen.polite

import android.app.Application
import androidx.work.Configuration
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import me.camsteffen.polite.di.DaggerAppComponent
import javax.inject.Inject

class Polite : Application(), HasAndroidInjector, Configuration.Provider {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    @Inject lateinit var workerFactory: AppWorkerFactory

    override fun androidInjector() = dispatchingAndroidInjector

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        DaggerAppComponent.factory().create(this).inject(this)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
