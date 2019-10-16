package me.camsteffen.polite.settings

import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.android.support.AndroidSupportInjection
import me.camsteffen.polite.MainActivity
import me.camsteffen.polite.R
import me.camsteffen.polite.state.PoliteStateManager
import javax.inject.Inject

class SettingsFragment :
    PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var politeStateManager: PoliteStateManager

    private val mainActivity: MainActivity
        get() = activity as MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity.supportActionBar!!.setTitle(R.string.settings)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        AsyncTask.execute(politeStateManager::refresh)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().popBackStack()
            else -> return false
        }
        return true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(R.string.preference_theme)) {
            // apply the theme change immediately
            mainActivity.recreate()
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is RelativeTimePreference) {
            val fragment = RelativeTimePreferenceFragment.newInstance(preference.key)
            fragment.setTargetFragment(this, 0)
            fragment.show(fragmentManager!!, RelativeTimePreferenceFragment.TAG)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }
}
