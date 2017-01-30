package me.camsteffen.polite.settings

import android.content.Context
import android.util.AttributeSet
import me.camsteffen.polite.R

private class ActivationPicker(context: Context, attrs: AttributeSet) : RelativeTimePicker(context, attrs) {
    override val summaryPlural = R.plurals.minutes_before_event
}
