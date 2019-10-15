package me.camsteffen.polite.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.PopupMenu
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.InverseBindingMethod
import androidx.databinding.InverseBindingMethods
import me.camsteffen.polite.R

class DropDownOption(context: Context, attrs: AttributeSet) : ValueOption(context, attrs) {

    var valueChangeListener: (() -> Unit)? = null

    var value: Int
        get() = mValue
        set(value) {
            if (value != mValue) {
                mValue = value
                val popupMenu = PopupMenu(context, null)
                popupMenu.inflate(menuId)
                summary = popupMenu.menu.findItem(value)?.title ?: ""
                valueChangeListener?.invoke()
            }
        }

    private val menuId: Int
    private var mValue: Int = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DropDownOption)
        menuId = a.getResourceId(R.styleable.DropDownOption_menu, 0)
        a.recycle()

        setOnClickListener {
            PopupMenu(context, this).run {
                setOnMenuItemClickListener { menuItem ->
                    if (menuItem.itemId != mValue) {
                        mValue = menuItem.itemId
                        summary = menuItem.title
                        valueChangeListener?.invoke()
                    }
                    true
                }
                inflate(menuId)
                show()
            }
        }
    }
}

@InverseBindingMethods(
    value = [InverseBindingMethod(type = DropDownOption::class, attribute = "value")]
)
object DropDownOptionBindingAdapter {

    @JvmStatic @BindingAdapter(value = ["valueAttrChanged"])
    fun setValueListener(dropDownOption: DropDownOption, listener: InverseBindingListener?) {
        dropDownOption.valueChangeListener = listener?.let {
            {
                listener.onChange()
            }
        }
    }
}
