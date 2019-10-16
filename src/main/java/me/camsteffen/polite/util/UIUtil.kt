package me.camsteffen.polite.util

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

fun hideKeyboard(activity: Activity) {
    val currentFocus = activity.currentFocus
    if (currentFocus != null) {
        val inputMethodManager = activity
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }
}
