package me.camsteffen.polite.rule

import android.graphics.Paint
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import me.camsteffen.polite.R
import me.camsteffen.polite.RulesFragment
import me.camsteffen.polite.rule.calendar.CalendarRule
import me.camsteffen.polite.rule.schedule.ScheduleRule

private const val SUBHEAD = 0
private const val CALENDAR_RULE = 1
private const val SCHEDULE_RULE = 2

class RuleAdapter(val rulesFragment: RulesFragment, val rules: RuleList = RuleList()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    inner class SubheadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView = itemView.findViewById(R.id.text) as TextView
    }

    inner class RuleViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        var rule: Rule? = null
        val nameTV = view.findViewById(R.id.name) as TextView
        val caption = view.findViewById(R.id.caption) as TextView
        val enableSwitch = view.findViewById(R.id.enable_switch) as Switch

        init {
            view.setOnClickListener {
                rulesFragment.openRule(rule!!, adapterPosition)
            }
            enableSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
                if (rule!!.enabled == isChecked)
                    return@OnCheckedChangeListener
                if (isChecked && rule!! is CalendarRule)
                    rulesFragment.mainActivity.checkCalendarPermission()
                rulesFragment.ruleSetEnabled(adapterPosition, enableSwitch.isChecked)
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            SUBHEAD -> SubheadViewHolder(inflater.inflate(R.layout.subhead_rules, parent, false))
            CALENDAR_RULE, SCHEDULE_RULE -> RuleViewHolder(inflater.inflate(R.layout.rule_list_item, parent, false))
            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = rules[position]
        when(item) {
            is RuleList.Subhead -> {
                holder as SubheadViewHolder
                val drawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, item.drawableId)).mutate()
                val attribute = TypedValue()
                context.theme.resolveAttribute(android.R.attr.textColorSecondary, attribute, true)
                val tint = ContextCompat.getColor(context, attribute.resourceId)
                DrawableCompat.setTint(drawable, tint)
                holder.textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                holder.textView.setText(item.textId)
            }
            is Rule -> {
                holder as RuleViewHolder
                holder.rule = item
                holder.nameTV.text = item.name
                holder.caption.text = item.getCaption(context)
                holder.enableSwitch.isChecked = item.enabled
                val textAppearance: Int
                val paintFlags = holder.nameTV.paintFlags
                if(item.enabled) {
                    textAppearance = R.style.EnabledRuleName
                    holder.nameTV.paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                } else {
                    textAppearance = R.style.DisabledRuleName
                    holder.nameTV.paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                @Suppress("DEPRECATION")
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                    holder.nameTV.setTextAppearance(context, textAppearance)
                else
                    holder.nameTV.setTextAppearance(textAppearance)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(rules[position]) {
            is ScheduleRule -> SCHEDULE_RULE
            is CalendarRule -> CALENDAR_RULE
            is RuleList.Subhead -> SUBHEAD
            else -> throw IllegalStateException()
        }
    }

    override fun getItemId(position: Int): Long {
        return rules[position].id
    }

    override fun getItemCount(): Int {
        return rules.size
    }

    fun getRuleAt(position: Int): Rule {
        return rules[position] as Rule
    }

    fun setRules(scheduleRules: List<ScheduleRule>, calendarRules: List<CalendarRule>) {
        rules.setRules(scheduleRules, calendarRules)
        notifyDataSetChanged()
    }

    fun deleteRule(position: Int) {
        val removed = rules.removeAt(position)
        val numRules = when(removed) {
            is ScheduleRule -> rules.scheduleRuleCount
            is CalendarRule -> rules.calendarRuleCount
            else -> throw IllegalStateException()
        }
        if(numRules == 0)
            notifyItemRangeRemoved(position - 1, 2)
        else
            notifyItemRemoved(position)
    }

    fun addRule(rule: ScheduleRule) {
        val index = rules.insert(rule)
        if(rules.scheduleRuleCount == 1)
            notifyItemInserted(0)
        notifyItemInserted(index)
    }

    fun addRule(rule: CalendarRule) {
        val index = rules.insert(rule)
        if(rules.calendarRuleCount == 1)
            notifyItemInserted(index - 1)
        notifyItemInserted(index)
    }

    fun swapRule(rule: Rule, position: Int) {
        rules[position] = rule
        notifyItemChanged(position)
    }

    fun renameRule(position: Int, name: String) {
        getRuleAt(position).name = name
        notifyItemChanged(position)
    }
}
