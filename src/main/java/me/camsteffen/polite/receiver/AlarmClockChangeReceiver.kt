package me.camsteffen.polite.receiver;

import android.app.AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED
import android.content.Context
import android.content.Intent
import dagger.android.DaggerBroadcastReceiver
import me.camsteffen.polite.state.PoliteStateManager
import me.camsteffen.polite.util.finishAsync
import javax.inject.Inject

class AlarmClockChangeReceiver : DaggerBroadcastReceiver() {
    @Inject lateinit var politeStateManager: PoliteStateManager

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_NEXT_ALARM_CLOCK_CHANGED -> finishAsync(politeStateManager::refresh)
        }
    }
}
