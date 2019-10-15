package me.camsteffen.polite.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import me.camsteffen.polite.R

open class ValueOption(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val labelTV: TextView
    val valueTV: TextView

    init {
        inflate(context, R.layout.value_option, this)
        labelTV = findViewById(R.id.label)
        valueTV = findViewById(R.id.value)

        val a = context.obtainStyledAttributes(attrs, R.styleable.ValueOption)
        labelTV.text = a.getString(R.styleable.SwitchOption_label)
        a.recycle()
    }

    var summary: CharSequence
        get() = valueTV.text
        // TODO test
        set(value) {
            valueTV.text = value
        }
}
