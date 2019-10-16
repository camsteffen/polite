package me.camsteffen.polite.rule.master

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import me.camsteffen.polite.R

sealed class RuleMasterItem {

    class Heading
    private constructor(val textId: Int, val drawableId: Int) : RuleMasterItem() {
        companion object {
            val CALENDAR = Heading(R.string.calendar_rules, R.drawable.ic_calendar_rule_black_24dp)
            val SCHEDULE = Heading(R.string.schedule_rules, R.drawable.ic_schedule_rule_black_24dp)
        }
    }

    data class Rule<R : me.camsteffen.polite.model.Rule>(val rule: R) : RuleMasterItem() {

        override fun sameItemAs(other: RuleMasterItem): Boolean {
            return other is Rule<*> && other.rule.id == rule.id
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RuleMasterItem>() {
            override fun areItemsTheSame(oldItem: RuleMasterItem, newItem: RuleMasterItem):
                Boolean {
                return oldItem.sameItemAs(newItem)
            }

            // equals is not overridden for Heading
            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: RuleMasterItem, newItem: RuleMasterItem):
                Boolean {
                return oldItem == newItem
            }
        }
    }

    protected open fun sameItemAs(other: RuleMasterItem): Boolean {
        return other == this
    }
}
