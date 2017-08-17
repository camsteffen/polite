package me.camsteffen.polite

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan

private const val padding = 10

class KeywordSpan(context: Context) : ReplacementSpan() {

    private val bgColor: Int
    private val textColor: Int

    init {
        val attributes = intArrayOf(R.attr.colorAccentBackground, android.R.attr.textColorPrimary)
        val values = context.theme.obtainStyledAttributes(attributes)
        bgColor = values.getColor(0, -1)
        textColor = values.getColor(1, -1)
        values.recycle()
    }

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        return (paint.measureText(text.subSequence(start, end).toString()) + 2 * padding).toInt()
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val width = paint.measureText(text.subSequence(start, end).toString())
        val rect = RectF(x, y + paint.ascent(), x + width + (2 * padding).toFloat(), y + paint.descent())
        paint.color = bgColor
        canvas.drawRoundRect(rect, padding.toFloat(), padding.toFloat(), paint)
        paint.color = textColor
        canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
    }
}
