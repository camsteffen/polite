package me.camsteffen.polite.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import me.camsteffen.polite.AppBroadcastReceiver
import me.camsteffen.polite.MainActivity
import me.camsteffen.polite.R

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val FRAGMENT_TAG = "settings"
    }

    private val mainActivity: MainActivity
        get() = activity as MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity.supportActionBar!!.setTitle(R.string.settings)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        AppBroadcastReceiver.sendRefresh(activity!!)
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> fragmentManager!!.popBackStack()
            else -> return false
        }
        return true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key == getString(R.string.preference_theme)) {
            mainActivity.recreate()
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is RelativeTimePreference) {
            val fragment = RelativeTimePreferenceFragment.newInstance(preference.key)
            fragment.setTargetFragment(this, 0)
            fragment.show(fragmentManager, RelativeTimePreferenceFragment.TAG)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }
}
