package me.camsteffen.polite.receiver

import android.content.Context
import android.content.Intent
import dagger.android.DaggerBroadcastReceiver
import me.camsteffen.polite.state.PoliteStateManager
import me.camsteffen.polite.util.finishAsync
import javax.inject.Inject

class CalendarChangeReceiver : DaggerBroadcastReceiver() {
    @Inject lateinit var politeStateManager: PoliteStateManager

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            Intent.ACTION_PROVIDER_CHANGED -> finishAsync(politeStateManager::refresh)
        }
    }
}
