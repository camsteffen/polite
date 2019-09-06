package me.camsteffen.polite

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import dagger.android.DaggerBroadcastReceiver
import me.camsteffen.polite.state.PoliteStateManager
import me.camsteffen.polite.util.finishAsync
import javax.inject.Inject

class RingerReceiver : DaggerBroadcastReceiver() {

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
            Intent.ACTION_BOOT_COMPLETED -> finishAsync { stateManager.refresh(null) }
            ACTION_REFRESH -> {
                val modifiedRuleId = intent.getLongExtra(MODIFIED_RULE_ID, -1L)
                    .takeIf { it != -1L }
                finishAsync { stateManager.refresh(modifiedRuleId) }
            }
            ACTION_CANCEL -> finishAsync(stateManager::cancel)
        }
    }

}
