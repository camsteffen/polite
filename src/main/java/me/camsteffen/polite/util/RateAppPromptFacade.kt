package me.camsteffen.polite.util

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import me.camsteffen.polite.R
import me.camsteffen.polite.di.ActivityScope
import me.camsteffen.polite.settings.AppPreferences
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import javax.inject.Inject

private const val MIN_LAUNCHES = 12
private val MIN_INSTALLED_DURATION = Duration.ofDays(22)

@ActivityScope
class RateAppPromptFacade
@Inject constructor(private val activity: Activity, private val preferences: AppPreferences) {

    private val appInstalledTime = appInstalledTime(activity)

    fun conditionalPrompt() {
        if (shouldShowPrompt()) {
            showPrompt()
        }
    }

    private fun appInstalledDuration() = Duration.between(appInstalledTime, Instant.now())

    private fun shouldShowPrompt(): Boolean {
        return !preferences.askedToRate &&
            preferences.launchCount >= MIN_LAUNCHES &&
            appInstalledDuration() > MIN_INSTALLED_DURATION
    }

    private fun showPrompt() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.rate_app_title)
            .setMessage(R.string.rate_app_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val uri = activity.getString(R.string.market_url).toUri()
                val intent = Intent(Intent.ACTION_VIEW, uri)
                if (activity.packageManager.resolveActivity(intent, 0) != null) {
                    activity.startActivity(intent)
                } else {
                    Toast.makeText(activity, R.string.error_open_play_store, Toast.LENGTH_LONG)
                        .show()
                }
            }
            .setNegativeButton(R.string.no, null)
            .create()
            .show()
        preferences.askedToRate = true
    }
}
