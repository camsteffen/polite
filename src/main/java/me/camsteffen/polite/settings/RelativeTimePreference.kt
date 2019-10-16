package me.camsteffen.polite.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import me.camsteffen.polite.R

class RelativeTimePreference(context: Context, attrs: AttributeSet) :
    DialogPreference(context, attrs) {

    private val summaryResId: Int

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.RelativeTimePreference)
        summaryResId = a.getResourceId(R.styleable.RelativeTimePreference_summary, 0)
        a.recycle()
    }

    var minutes: Int = 0
        set(value) {
            if (value != field) {
                field = value
                persistInt(value)
                notifyChanged()
            }
        }

    override fun getDialogLayoutResource() = R.layout.relative_time_picker

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, 0)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        minutes = if (restorePersistedValue) getPersistedInt(minutes) else defaultValue as Int
    }

    override fun getSummary(): CharSequence {
        return context.resources.getQuantityString(summaryResId, minutes, minutes)
    }
}
