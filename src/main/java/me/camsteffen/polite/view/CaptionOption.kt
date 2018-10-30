package me.camsteffen.polite.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import me.camsteffen.polite.R

class CaptionOption(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.caption_option, this)
    }

    val label: TextView = findViewById(R.id.label)
    val caption: TextView = findViewById(R.id.caption)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CaptionOption)
        label.text = a.getString(R.styleable.CaptionOption_label)
        a.recycle()
    }

    fun setCaption(caption: String) {
        this.caption.text = caption
    }
}
