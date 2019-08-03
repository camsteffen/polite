package me.camsteffen.polite.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import androidx.preference.Preference
import me.camsteffen.polite.R
import me.camsteffen.polite.util.ChannelIds

@RequiresApi(Build.VERSION_CODES.O)
class NotificationsPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {

    override fun getTitle(): CharSequence {
        return context.getString(R.string.notifications)
    }

    override fun onClick() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .putExtra(Settings.EXTRA_CHANNEL_ID, ChannelIds.POLITE_ACTIVE)
        startActivity(context, intent, null)
    }
}
