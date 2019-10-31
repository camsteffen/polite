package me.camsteffen.polite.ui.rule.edit

import android.app.Application
import androidx.annotation.CallSuper
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.AndroidViewModel
import me.camsteffen.polite.model.Rule

abstract class EditRuleViewModel<R : Rule>(application: Application) :
    AndroidViewModel(application) {

    val enabled = ObservableBoolean()
    val vibrate = ObservableBoolean()

    @CallSuper
    open fun setRule(rule: R) {
        enabled.set(rule.enabled)
        vibrate.set(rule.vibrate)
    }

    fun createRule(id: Long, name: String): R {
        return doCreateRule(id, name, enabled.get(), vibrate.get())
    }

    abstract fun doCreateRule(id: Long, name: String, enabled: Boolean, vibrate: Boolean): R
}
