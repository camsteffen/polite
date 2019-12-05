package me.camsteffen.polite.service.receiver

import android.content.Context
import android.content.Intent
import dagger.android.DaggerBroadcastReceiver
import me.camsteffen.polite.service.PoliteModeManager
import me.camsteffen.polite.util.finishAsync
import timber.log.Timber
import javax.inject.Inject

class CalendarChangeReceiver : DaggerBroadcastReceiver() {
    @Inject lateinit var politeModeManager: PoliteModeManager

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        Timber.i("Received broadcast: %s", intent)
        when (intent.action) {
            Intent.ACTION_PROVIDER_CHANGED -> finishAsync(politeModeManager::refresh)
        }
    }
}
