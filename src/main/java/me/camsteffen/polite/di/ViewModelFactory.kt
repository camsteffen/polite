package me.camsteffen.polite.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory
@Inject constructor(
    private val viewModelsMap: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = viewModelsMap[modelClass]
                ?: throw NoSuchElementException("no provider found for $modelClass")
        @Suppress("UNCHECKED_CAST")
        return creator.get() as T
    }
}
