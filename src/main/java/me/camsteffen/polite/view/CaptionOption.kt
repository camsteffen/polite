package me.camsteffen.polite.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import me.camsteffen.polite.R

class CaptionOption(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val caption: TextView

    init {
        inflate(context, R.layout.caption_option, this)
        caption = findViewById(R.id.caption) as TextView
        val a = context.obtainStyledAttributes(attrs, R.styleable.CaptionOption)
        val label = findViewById(R.id.label) as TextView
        label.text = a.getString(R.styleable.SwitchOption_label)
        a.recycle()
    }

}
