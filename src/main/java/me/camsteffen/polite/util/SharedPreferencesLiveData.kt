package me.camsteffen.polite.util

import android.content.SharedPreferences
import androidx.lifecycle.LiveData

abstract class SharedPreferenceLiveData<T>(
    protected val sharedPrefs: SharedPreferences,
    private val key: String,
    private val defValue: T
) : LiveData<T>() {

    private val preferenceChangeListener = SharedPreferences
        .OnSharedPreferenceChangeListener { _, key ->
            if (key == this.key) {
                value = getValueFromPreferences(key, defValue)
            }
        }

    abstract fun getValueFromPreferences(key: String, defValue: T): T

    override fun onActive() {
        super.onActive()
        value = getValueFromPreferences(key, defValue)
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }
}

class SharedPreferenceBooleanLiveData(
    sharedPrefs: SharedPreferences,
    key: String,
    defValue: Boolean
) : SharedPreferenceLiveData<Boolean>(sharedPrefs, key, defValue) {

    override fun getValueFromPreferences(key: String, defValue: Boolean): Boolean =
        sharedPrefs.getBoolean(key, defValue)
}
