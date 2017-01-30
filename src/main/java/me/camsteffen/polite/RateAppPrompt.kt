package me.camsteffen.polite

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.widget.Toast
import java.util.concurrent.TimeUnit

class RateAppPrompt(polite: Polite) {

    val preferences = Polite.preferences
    val timeInstalled = polite.packageManager.getPackageInfo(polite.packageName, 0).firstInstallTime
    var launchCount = preferences.getInt(AppPreferences.LAUNCH_COUNT, 0)
        set(value) {
            field = value
            preferences.edit()
                    .putInt(AppPreferences.LAUNCH_COUNT, value)
                    .apply()
        }
    var askedToRate = preferences.getBoolean(AppPreferences.ASKED_TO_RATE, false)
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
                .setPositiveButton(android.R.string.ok, { dialogInterface, i ->
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

    companion object {
        const val MIN_LAUNCHES = 12
        const val MIN_DAYS_INSTALLED = 22
    }
}
