package me.camsteffen.polite.rule

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import me.camsteffen.polite.MainActivity
import me.camsteffen.polite.R

abstract class Rule : Parcelable, RuleList.RuleListItem {

    companion object {
        const val NEW_RULE = -1L
    }

    final override var id: Long
    var name: String
    var enabled: Boolean
    var vibrate: Boolean

    constructor(context: Context) {
        id = NEW_RULE
        name = context.getString(R.string.rule_default_name)
        enabled = true
        vibrate = false
    }

    constructor(copy: Rule) {
        id = copy.id
        name = copy.name
        enabled = copy.enabled
        vibrate = copy.vibrate
    }

    constructor(id: Long, name: String, enabled: Boolean, vibrate: Boolean) {
        this.id = id
        this.name = name
        this.enabled = enabled
        this.vibrate = vibrate
    }

    constructor(parcel: Parcel) {
        id = parcel.readLong()
        name = parcel.readString()
        enabled = parcel.readByte() != 0.toByte()
        vibrate = parcel.readByte() != 0.toByte()
    }

    open fun getCaption(context: Context): CharSequence = ""

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(name)
        dest.writeByte(if(enabled) 1 else 0)
        dest.writeByte(if(vibrate) 1 else 0)
    }

    abstract fun addToAdapter(adapter: RuleAdapter)

    open fun saveDB(mainActivity: MainActivity, callback: () -> Unit) {
        if(id == Rule.NEW_RULE) {
            saveDBNew(mainActivity, { id ->
                this.id = id
                callback()
            })
        } else {
            saveDBExisting(mainActivity)
        }
    }

    abstract fun saveDBNew(context: Context, callback: (id: Long) -> Unit)
    abstract fun saveDBExisting(context: Context)

    open fun scrub() = Unit

}
