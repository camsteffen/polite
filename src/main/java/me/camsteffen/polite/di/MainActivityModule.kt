package me.camsteffen.polite.di

import android.app.Activity
import androidx.fragment.app.FragmentFactory
import dagger.Binds
import dagger.Module
import me.camsteffen.polite.ui.MainActivity

@Module
abstract class MainActivityModule {

    @Binds
    abstract fun provideActivity(mainActivity: MainActivity): Activity

    @Binds
    abstract fun bindFragmentFactory(factory: AppFragmentFactory): FragmentFactory
}
