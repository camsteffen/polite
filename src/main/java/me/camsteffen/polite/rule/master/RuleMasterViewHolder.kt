package me.camsteffen.polite.rule.master

import android.graphics.Paint
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import me.camsteffen.polite.R
import me.camsteffen.polite.model.Rule

typealias RuleClickListener = (rule: Rule) -> Unit
typealias RuleCheckedChangeListener = (rule: Rule, isChecked: Boolean) -> Unit

abstract class RuleMasterViewHolder<E : RuleMasterItem>(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    abstract fun bind(item: E)

    class HeadingViewHolder(itemView: View) :
        RuleMasterViewHolder<RuleMasterItem.Heading>(itemView) {

        private val textView = this.itemView.findViewById(R.id.text) as TextView

        override fun bind(item: RuleMasterItem.Heading) {
            val context = itemView.context
            val drawable = DrawableCompat
                .wrap(ContextCompat.getDrawable(context, item.drawableId)!!).mutate()
            val attribute = TypedValue()
            context.theme.resolveAttribute(android.R.attr.textColorSecondary, attribute, true)
            val tint = ContextCompat.getColor(context, attribute.resourceId)
            DrawableCompat.setTint(drawable, tint)
            textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
            textView.setText(item.textId)
        }
    }

    class RuleViewHolder(
        itemView: View,
        onClick: RuleClickListener,
        onCheckedChange: RuleCheckedChangeListener
    ) : RuleMasterViewHolder<RuleMasterItem.Rule<*>>(itemView) {

        override fun bind(item: RuleMasterItem.Rule<*>) {
            val context = itemView.context
            rule = item.rule
            val rule = rule!!
            nameTV.text = rule.name
            caption.text = rule.getCaption(context)
            enableSwitch.isChecked = rule.enabled
            val textAppearance: Int
            val paintFlags = nameTV.paintFlags
            if (rule.enabled) {
                textAppearance = R.style.EnabledRuleName
                nameTV.paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            } else {
                textAppearance = R.style.DisabledRuleName
                nameTV.paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                nameTV.setTextAppearance(textAppearance)
            } else {
                @Suppress("DEPRECATION")
                nameTV.setTextAppearance(context, textAppearance)
            }
        }

        private var rule: Rule? = null
        private val nameTV = itemView.findViewById(R.id.name) as TextView
        private val caption = itemView.findViewById(R.id.caption) as TextView
        private val enableSwitch = itemView.findViewById(R.id.enable_switch) as Switch

        init {
            itemView.setOnClickListener {
                onClick(rule!!)
            }
            enableSwitch.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(rule!!, isChecked)
            }
        }
    }
}
