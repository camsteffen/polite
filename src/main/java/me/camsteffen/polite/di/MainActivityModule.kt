package me.camsteffen.polite.di

import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.Module
import dagger.Provides
import me.camsteffen.polite.MainActivity
import me.camsteffen.polite.R

@Module
abstract class MainActivityModule {

    @Module
    companion object {

        @JvmStatic @Provides
        fun provideNavController(mainActivity: MainActivity): NavController {
            val navHostFragment = mainActivity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    as NavHostFragment
            return navHostFragment.navController
        }
    }
}
