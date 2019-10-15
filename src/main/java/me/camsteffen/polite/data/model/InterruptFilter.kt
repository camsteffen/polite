package me.camsteffen.polite.data.model

import android.app.NotificationManager
import androidx.annotation.IdRes
import me.camsteffen.polite.R

enum class InterruptFilter(
    val value: Int,
    val androidValue: Int,
    @IdRes val resId: Int
) {
    NONE(
        0,
        NotificationManager.INTERRUPTION_FILTER_NONE,
        R.id.exceptions_none
    ),
    ALARMS(
        1,
        NotificationManager.INTERRUPTION_FILTER_ALARMS,
        R.id.exceptions_alarms
    ),
    PRIORITY(
        2,
        NotificationManager.INTERRUPTION_FILTER_PRIORITY,
        R.id.exceptions_priority
    );

    companion object {
        fun fromAndroidValue(interruptionFilter: Int): InterruptFilter? {
            return values().find { it.androidValue == interruptionFilter }
        }

        fun fromResId(@IdRes idRes: Int): InterruptFilter? {
            return values().find { it.resId == idRes }
        }

        fun fromValue(value: Int): InterruptFilter? {
            return values().find { it.value == value }
        }
    }
}
