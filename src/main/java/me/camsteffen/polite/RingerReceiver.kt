package me.camsteffen.polite

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import dagger.android.DaggerBroadcastReceiver
import me.camsteffen.polite.state.PoliteStateManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RingerReceiver
@Inject constructor() : DaggerBroadcastReceiver() {

    companion object {
        const val ACTION_CANCEL = "cancel"
        const val ACTION_REFRESH = "refresh"

        const val MODIFIED_RULE_ID = "modified_rule_id"
    }

    @Inject lateinit var stateManager: PoliteStateManager

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED,
            Intent.ACTION_PROVIDER_CHANGED,
            Intent.ACTION_BOOT_COMPLETED -> stateManager.refresh(-1L)
            ACTION_REFRESH -> {
                val modifiedRuleId = intent.getLongExtra(MODIFIED_RULE_ID, -1L)
                stateManager.refresh(modifiedRuleId)
            }
            ACTION_CANCEL -> stateManager.cancel()
        }
    }

}
