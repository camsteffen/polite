package me.camsteffen.polite.rule.master

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import me.camsteffen.polite.R
import me.camsteffen.polite.model.CalendarRule
import me.camsteffen.polite.model.Rule
import me.camsteffen.polite.model.ScheduleRule

private object ViewTypes {
    const val HEADING: Int = 0
    const val CALENDAR_RULE: Int = 1
    const val SCHEDULE_RULE: Int = 2
}

class RuleMasterAdapter(
    private val ruleClickListener: RuleClickListener,
    private val ruleCheckedChangeListener: RuleCheckedChangeListener
) : ListAdapter<RuleMasterItem, RuleMasterViewHolder<*>>(RuleMasterItem.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleMasterViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ViewTypes.HEADING -> {
                val view = inflater.inflate(R.layout.subhead_rules, parent, false)
                RuleMasterViewHolder.HeadingViewHolder(view)
            }
            ViewTypes.CALENDAR_RULE, ViewTypes.SCHEDULE_RULE -> {
                val view = inflater.inflate(R.layout.rule_list_item, parent, false)
                RuleMasterViewHolder
                    .RuleViewHolder(view, ruleClickListener, ruleCheckedChangeListener)
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RuleMasterViewHolder<*>, position: Int) {
        bindViewHolder(holder, getItem(position))
    }

    @Suppress("UNCHECKED_CAST")
    private fun <E : RuleMasterItem> bindViewHolder(
        holder: RuleMasterViewHolder<E>,
        item: RuleMasterItem
    ) {
        holder.bind(item as E)
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is RuleMasterItem.Heading -> ViewTypes.HEADING
            is RuleMasterItem.Rule<*> -> when (item.rule) {
                is CalendarRule -> ViewTypes.CALENDAR_RULE
                is ScheduleRule -> ViewTypes.SCHEDULE_RULE
            }
        }
    }

    fun getRuleAt(position: Int): Rule = (getItem(position) as RuleMasterItem.Rule<*>).rule
}
