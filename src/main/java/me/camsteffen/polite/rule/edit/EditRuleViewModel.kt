package me.camsteffen.polite.rule.edit

import android.app.Application
import androidx.annotation.CallSuper
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.AndroidViewModel
import me.camsteffen.polite.model.Rule

open class EditRuleViewModel<R : Rule>(application: Application) : AndroidViewModel(application) {

    val enabled = ObservableBoolean()
    val vibrate = ObservableBoolean()

    @CallSuper
    open fun setRule(rule: R) {
        enabled.set(rule.enabled)
        vibrate.set(rule.vibrate)
    }
}
