package me.camsteffen.polite.settings

import android.view.View
import android.widget.NumberPicker
import androidx.core.os.bundleOf
import androidx.preference.PreferenceDialogFragmentCompat
import me.camsteffen.polite.R

private const val MIN_VALUE = 0
private const val MAX_VALUE = 60

class RelativeTimePreferenceFragment : PreferenceDialogFragmentCompat() {

    companion object {
        const val TAG = "relative_time"

        fun newInstance(key: String): RelativeTimePreferenceFragment {
            return RelativeTimePreferenceFragment().apply {
                arguments = bundleOf(ARG_KEY to key)
            }
        }
    }

    private lateinit var picker: NumberPicker

    private val relativeTimePreference: RelativeTimePreference
        get() = super.getPreference() as RelativeTimePreference

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        picker = view.findViewById(R.id.minutes)
        picker.minValue = MIN_VALUE
        picker.maxValue = MAX_VALUE
        picker.value = relativeTimePreference.minutes
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            relativeTimePreference.minutes = picker.value
        }
    }
}
