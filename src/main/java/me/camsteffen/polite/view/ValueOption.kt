package me.camsteffen.polite.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import me.camsteffen.polite.R

class ValueOption(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val value: TextView

    init {
        inflate(context, R.layout.value_option, this)
        value = findViewById(R.id.value) as TextView
        val a = context.obtainStyledAttributes(attrs, R.styleable.ValueOption)
        val label = findViewById(R.id.label) as TextView
        label.text = a.getString(R.styleable.SwitchOption_label)
        a.recycle()
    }

}
