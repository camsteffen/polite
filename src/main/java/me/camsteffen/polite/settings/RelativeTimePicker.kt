package me.camsteffen.polite.settings

import android.content.Context
import android.preference.DialogPreference
import android.preference.Preference
import android.util.AttributeSet
import android.view.View
import android.widget.NumberPicker
import me.camsteffen.polite.R

private val DEFAULT_VALUE = 0
private val MAX_VALUE = 15
private val MIN_VALUE = 0

internal abstract class RelativeTimePicker(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs), Preference.OnPreferenceChangeListener {

    private var picker: NumberPicker? = null
    abstract val summaryPlural: Int

    init {
        dialogLayoutResource = R.layout.relative_time_picker
        @Suppress("LeakingThis")
        onPreferenceChangeListener = this
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        picker = view.findViewById(R.id.minutes) as NumberPicker
        picker!!.minValue = MIN_VALUE
        picker!!.maxValue = MAX_VALUE
        picker!!.value = getPersistedInt(DEFAULT_VALUE)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val newValue = picker!!.value
            if (callChangeListener(newValue)) {
                persistInt(newValue)
            }
            summary = summary
        }
    }

    override fun getSummary(): CharSequence {
        val minutes = getPersistedInt(0)
        return context.resources.getQuantityString(summaryPlural, minutes, minutes)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        summary = summary
        return true
    }
}
