package me.camsteffen.polite.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import me.camsteffen.polite.R

class ValueOption(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.value_option, this)
    }

    val label: TextView = findViewById(R.id.label)
    val value: TextView = findViewById(R.id.value)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ValueOption)
        label.text = a.getString(R.styleable.SwitchOption_label)
        a.recycle()
    }

    fun setValue(value: String) {
        this.value.text = value
    }
}
