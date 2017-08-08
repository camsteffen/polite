package me.camsteffen.polite

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager

class Polite : Application() {

    companion object {
        const val NOTIFY_ID_CALENDAR_PERMISSION = 0
        const val NOTIFY_ID_ACTIVE = 1
        const val NOTIFY_ID_SCHEDULE_FEATURE = 2
        const val NOTIFY_ID_NOTIFICATION_POLICY_ACCESS = 3

        lateinit var db: DB
        lateinit var preferences: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()
        db = DB(this)
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

}
