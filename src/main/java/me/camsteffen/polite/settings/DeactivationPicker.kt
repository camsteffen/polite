package me.camsteffen.polite.settings

import android.content.Context
import android.util.AttributeSet
import me.camsteffen.polite.R

private class DeactivationPicker(context: Context, attrs: AttributeSet) : RelativeTimePicker(context, attrs) {
    override val summaryPlural = R.plurals.minutes_after_event
}
