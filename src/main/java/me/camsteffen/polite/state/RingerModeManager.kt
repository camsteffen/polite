package me.camsteffen.polite.state

import android.media.AudioManager
import me.camsteffen.polite.settings.AppPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RingerModeManager
@Inject constructor(
    private val audioManager: AudioManager,
    private val preferences: AppPreferences
) {

    fun clearSavedRingerMode() {
        preferences.previousRingerMode = -1
        preferences.notificationVolume = null
    }

    fun saveRingerMode() {
        preferences.previousRingerMode = audioManager.ringerMode
        preferences.notificationVolume =
            audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
    }

    fun setRingerMode(vibrate: Boolean) {
        audioManager.ringerMode = if (vibrate) {
            AudioManager.RINGER_MODE_VIBRATE
        } else {
            AudioManager.RINGER_MODE_SILENT
        }
    }

    fun restoreRingerMode() {
        val previousRingerMode = preferences.previousRingerMode
        // change to previous ringer mode only if louder than current mode
        if (previousRingerMode == AudioManager.RINGER_MODE_NORMAL ||
            previousRingerMode == AudioManager.RINGER_MODE_VIBRATE &&
            audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
        ) {
            audioManager.ringerMode = previousRingerMode
        }
        // restore notifications volume, only necessary for devices which have separate volume
        // controls for ringer and notifications
        val notificationVol = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        val prevNotificationVol = preferences.notificationVolume
        if (prevNotificationVol != null && notificationVol < prevNotificationVol) {
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, prevNotificationVol, 0)
        }
        clearSavedRingerMode()
    }
}
