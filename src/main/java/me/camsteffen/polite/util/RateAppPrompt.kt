package me.camsteffen.polite.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.widget.Toast
import me.camsteffen.polite.Polite
import me.camsteffen.polite.R
import me.camsteffen.polite.settings.AppPreferences
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val MIN_LAUNCHES = 12
private const val MIN_DAYS_INSTALLED = 22

@Singleton
class RateAppPrompt
@Inject constructor(context: Context) {

    private val preferences = Polite.preferences
    private val timeInstalled = context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
    var launchCount = preferences.getInt(AppPreferences.LAUNCH_COUNT, 0)
        set(value) {
            field = value
            preferences.edit()
                    .putInt(AppPreferences.LAUNCH_COUNT, value)
                    .apply()
        }
    private var askedToRate = preferences.getBoolean(AppPreferences.ASKED_TO_RATE, false)
        set(value) {
            field = value
            preferences.edit()
                    .putBoolean(AppPreferences.ASKED_TO_RATE, value)
                    .apply()
        }

    fun conditionalPrompt(activity: Activity) {
        val daysInstalled = TimeUnit.DAYS.convert(System.currentTimeMillis() - timeInstalled, TimeUnit.MILLISECONDS)
        if(askedToRate
                || launchCount < MIN_LAUNCHES
                || daysInstalled < MIN_DAYS_INSTALLED) {
            return
        }
        AlertDialog.Builder(activity)
                .setTitle(R.string.rate_app_title)
                .setMessage(R.string.rate_app_message)
                .setPositiveButton(android.R.string.ok, { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.market_url)))
                    if (activity.packageManager.resolveActivity(intent, 0) != null) {
                        activity.startActivity(intent)
                    } else {
                        Toast.makeText(activity, R.string.error_open_play_store, Toast.LENGTH_LONG).show()
                    }
                })
                .setNegativeButton(R.string.no, null)
                .create()
                .show()
        askedToRate = true
    }
}
