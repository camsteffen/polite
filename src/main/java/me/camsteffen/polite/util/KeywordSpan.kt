package me.camsteffen.polite.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan
import me.camsteffen.polite.R

private const val padding = 10

class KeywordSpan(context: Context) : ReplacementSpan() {

    private val bgColor: Int
    private val textColor: Int

    init {
        val values = context.theme
            .obtainStyledAttributes(R.style.KeywordSpan, R.styleable.KeywordSpan)
        bgColor = values.getColor(R.styleable.KeywordSpan_android_colorBackground, -1)
        textColor = values.getColor(R.styleable.KeywordSpan_android_colorForeground, -1)
        values.recycle()
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return (paint.measureText(text.subSequence(start, end).toString()) + 2 * padding).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val width = paint.measureText(text.subSequence(start, end).toString())
        val rect = RectF(
            x, y + paint.ascent(),
            x + width + (2 * padding).toFloat(), y + paint.descent()
        )
        paint.color = bgColor
        canvas.drawRoundRect(rect, padding.toFloat(), padding.toFloat(), paint)
        paint.color = textColor
        canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
    }
}
