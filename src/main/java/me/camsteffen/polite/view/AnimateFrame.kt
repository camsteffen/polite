package me.camsteffen.polite.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class AnimateFrame(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    var xFraction: Float
        get() = x / width
        set(value) {
            val width = width
            x = if (width > 0) value * width else -9999f
        }
}
