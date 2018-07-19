package me.camsteffen.polite.util

import android.app.Activity
import android.content.Context
import android.support.annotation.ColorInt
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.TypedValue
import android.view.Menu
import android.view.inputmethod.InputMethodManager
import me.camsteffen.polite.R

fun hideKeyboard(activity: Activity) {
    val currentFocus = activity.currentFocus
    if (currentFocus != null) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }
}

fun tintMenuIcons(context: Context, menu: Menu) {
    val colorValue = TypedValue()
    val resolved = context.theme.resolveAttribute(R.attr.colorControlNormal, colorValue, true)
    assert(resolved)
    @ColorInt val color = colorValue.data
    for (i in 0 until menu.size()) {
        val item = menu.getItem(i)
        val drawable = item.icon
        if (drawable != null) {
            val wrapped = DrawableCompat.wrap(drawable)
            drawable.mutate()
            DrawableCompat.setTint(wrapped, color)
            item.icon = drawable
        }
    }
}
