package me.camsteffen.polite.di

import android.app.Activity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import me.camsteffen.polite.MainActivity
import me.camsteffen.polite.R

@Module
abstract class MainActivityModule {

    @Binds
    abstract fun provideActivity(mainActivity: MainActivity): Activity

    @Module
    companion object {

        @JvmStatic
        @Provides
        fun provideNavController(mainActivity: MainActivity): NavController {
            val navHostFragment = mainActivity.supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            return navHostFragment.navController
        }
    }
}
