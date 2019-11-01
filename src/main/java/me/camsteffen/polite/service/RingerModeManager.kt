package me.camsteffen.polite.service

import android.media.AudioManager
import me.camsteffen.polite.data.AppPreferences
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RingerModeManager
@Inject constructor(
    private val audioManager: AudioManager,
    private val preferences: AppPreferences
) {

    fun clearSavedRingerMode() {
        Timber.i("Clearing saved ringer mode")
        preferences.previousRingerMode = -1
        preferences.notificationVolume = null
    }

    fun saveRingerMode() {
        val ringerMode = audioManager.ringerMode
        val notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        preferences.previousRingerMode = ringerMode
        preferences.notificationVolume = notificationVolume
        Timber.i(
            "Saved ringer mode, ringerMode=%s, notificationVolume=%d",
            ringerMode.ringerModeDebugString(), notificationVolume
        )
    }

    fun setRingerMode(vibrate: Boolean) {
        val ringerMode = if (vibrate) {
            AudioManager.RINGER_MODE_VIBRATE
        } else {
            AudioManager.RINGER_MODE_SILENT
        }
        doSetRingerMode(ringerMode)
    }

    fun restoreRingerMode() {
        val ringerMode = audioManager.ringerMode
        val previousRingerMode = preferences.previousRingerMode
        val notificationVol = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        val prevNotificationVol = preferences.notificationVolume
        Timber.i(
            "Restoring ringer mode, ringerMode=%s, previousRingerMode=%s, notificationVol=%d, " +
                    "previousNotificationVolume=%d", ringerMode.ringerModeDebugString(),
            previousRingerMode.ringerModeDebugString(), notificationVol, prevNotificationVol
        )
        // change to previous ringer mode only if louder than current mode
        if (previousRingerMode == AudioManager.RINGER_MODE_NORMAL ||
            previousRingerMode == AudioManager.RINGER_MODE_VIBRATE &&
            ringerMode == AudioManager.RINGER_MODE_SILENT
        ) {
            doSetRingerMode(previousRingerMode)
        }
        // restore notifications volume, only necessary for devices which have separate volume
        // controls for ringer and notifications
        if (prevNotificationVol != null && notificationVol < prevNotificationVol) {
            Timber.i("Setting notification volume to %d", prevNotificationVol)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, prevNotificationVol, 0)
        }
        clearSavedRingerMode()
    }

    private fun doSetRingerMode(ringerMode: Int) {
        Timber.i("Setting ringer mode to %s", ringerMode.ringerModeDebugString())
        audioManager.ringerMode = ringerMode
    }
}

private fun Int.ringerModeDebugString(): String {
    return when (this) {
        AudioManager.RINGER_MODE_NORMAL -> "normal"
        AudioManager.RINGER_MODE_SILENT -> "silent"
        AudioManager.RINGER_MODE_VIBRATE -> "vibrate"
        else -> toString(10)
    }
}
