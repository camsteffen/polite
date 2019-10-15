package me.camsteffen.polite.ui.rule.edit

import android.app.Application
import androidx.annotation.CallSuper
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.AndroidViewModel
import me.camsteffen.polite.R
import me.camsteffen.polite.data.db.entity.AudioPolicy
import me.camsteffen.polite.data.model.InterruptFilter
import me.camsteffen.polite.data.model.Rule

abstract class EditRuleViewModel<T : Rule>(application: Application) :
    AndroidViewModel(application) {

    val enabled = ObservableBoolean(true)
    val exceptions = ObservableInt(R.id.exceptions_priority)
    val vibrate = ObservableBoolean(false)
    val muteMedia = ObservableBoolean(false)

    @CallSuper
    open fun setRule(rule: T) {
        enabled.set(rule.enabled)
        exceptions.set(rule.audioPolicy.interruptFilter.resId)
        vibrate.set(rule.audioPolicy.vibrate)
        muteMedia.set(rule.audioPolicy.muteMedia)
    }

    fun createRule(id: Long, name: String): T {
        val audioPolicy = AudioPolicy(
            vibrate.get(),
            muteMedia.get(),
            InterruptFilter.fromResId(exceptions.get())!!
        )
        return doCreateRule(id, name, audioPolicy)
    }

    abstract fun doCreateRule(id: Long, name: String, audioPolicy: AudioPolicy): T
}
