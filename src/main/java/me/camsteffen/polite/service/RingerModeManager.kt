package me.camsteffen.polite.service

import android.app.NotificationManager
import android.app.NotificationManager.INTERRUPTION_FILTER_ALL
import android.media.AudioManager
import android.os.Build
import me.camsteffen.polite.data.AppPreferences
import me.camsteffen.polite.data.db.entity.AudioPolicy
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RingerModeManager
@Inject constructor(
    private val audioManager: AudioManager,
    private val notificationManager: NotificationManager,
    private val preferences: AppPreferences
) {

    fun clear() {
        Timber.i("Clearing saved ringer mode")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            preferences.interruptionFilter = null
        }
        preferences.mediaVolume = null
        preferences.previousRingerMode = -1
        preferences.notificationVolume = null
    }

    // TODO rename to save()
    fun save() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            preferences.interruptionFilter = notificationManager.currentInterruptionFilter
        }
        preferences.mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val ringerMode = audioManager.ringerMode
        val notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        preferences.previousRingerMode = ringerMode
        preferences.notificationVolume = notificationVolume
        Timber.i(
            "Saved ringer mode, ringerMode=%s, notificationVolume=%d",
            ringerMode.ringerModeDebugString(), notificationVolume
        )
    }

    fun beQuiet(policy: AudioPolicy) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.setInterruptionFilter(policy.interruptFilter.androidValue)
        }
        val ringerMode = if (policy.vibrate) {
            AudioManager.RINGER_MODE_VIBRATE
        } else {
            AudioManager.RINGER_MODE_SILENT
        }
        doSetRingerMode(ringerMode)

        if (policy.muteMedia) {
            Timber.i("Muting media volume")
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        }
    }

    // TODO rename to restore()
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
        if (previousRingerMode > audioManager.ringerMode) {
            doSetRingerMode(previousRingerMode)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.setInterruptionFilter(INTERRUPTION_FILTER_ALL)
        }

        // restore notifications volume, only necessary for devices which have separate volume
        // controls for ringer and notifications
        if (prevNotificationVol != null && notificationVol < prevNotificationVol) {
            Timber.i("Setting notification volume to %d", prevNotificationVol)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, prevNotificationVol, 0)
        }

        preferences.mediaVolume?.let { mediaVolume ->
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaVolume, 0)
        }

        clear()
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
