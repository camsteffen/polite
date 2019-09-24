package me.camsteffen.polite.settings

import android.content.SharedPreferences
import android.content.res.Resources
import me.camsteffen.polite.R
import javax.inject.Inject
import javax.inject.Singleton

object PreferenceDefaults {
    const val ENABLE = true
}

private object PreferenceKeys {
    const val PREVIOUS_RINGER_MODE = "previous_ringer_mode"
    const val LAUNCH_COUNT = "launch_count"
    const val ASKED_TO_RATE = "asked_to_rate"
    const val NOTIFICATION_VOLUME = "notification_volume"
}

@Singleton
class AppPreferences
@Inject constructor(private val preferences: SharedPreferences, resources: Resources) {

    private val activationKey = resources.getString(R.string.preference_activation)
    private val deactivationKey = resources.getString(R.string.preference_deactivation)
    private val enableKey = resources.getString(R.string.preference_enable)
    private val notificationsKey = resources.getString(R.string.preference_notifications)
    private val themeKey = resources.getString(R.string.preference_theme)
    private val defaultTheme = resources.getString(R.string.theme_light)

    private val editor = preferences.edit()

    var theme: String
        get() = preferences.getString(themeKey, defaultTheme)!!
        set(value) = editor.putString(themeKey, value).apply()

    var notifications: Boolean
        get() = preferences.getBoolean(notificationsKey, true)
        set(value) = editor.putBoolean(notificationsKey, value).apply()

    var askedToRate: Boolean
        get() = preferences.getBoolean(PreferenceKeys.ASKED_TO_RATE, false)
        set(value) {
            editor.putBoolean(PreferenceKeys.ASKED_TO_RATE, value).apply()
        }

    var launchCount: Int
        get() = preferences.getInt(PreferenceKeys.LAUNCH_COUNT, 0)
        set(value) = editor.putInt(PreferenceKeys.LAUNCH_COUNT, value).apply()

    var previousRingerMode: Int
        get() = preferences.getInt(PreferenceKeys.PREVIOUS_RINGER_MODE, -1)
        set(value) = editor.putInt(PreferenceKeys.PREVIOUS_RINGER_MODE, value).apply()

    var enable: Boolean
        get() = preferences.getBoolean(enableKey, PreferenceDefaults.ENABLE)
        set(value) = editor.putBoolean(enableKey, value).apply()

    var activation: Int
        get() = preferences.getInt(activationKey, 0)
        set(value) = editor.putInt(activationKey, value).apply()

    var deactivation: Int
        get() = preferences.getInt(deactivationKey, 0)
        set(value) = editor.putInt(deactivationKey, value).apply()

    var notificationVolume: Int?
        get() = preferences.getString(PreferenceKeys.NOTIFICATION_VOLUME, null)?.toIntOrNull()
        set(value) = editor.putString(PreferenceKeys.NOTIFICATION_VOLUME, value?.toString(10))
            .apply()
}
