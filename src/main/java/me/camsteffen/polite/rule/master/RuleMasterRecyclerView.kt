package me.camsteffen.polite.rule.master

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.camsteffen.polite.model.Rule

class RuleMasterRecyclerView(context: Context, attrs: AttributeSet? = null) :
    RecyclerView(context, attrs) {

    init {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        addItemDecoration(Divider())
    }

    var adapter: RuleMasterAdapter?
        get() = super.getAdapter() as RuleMasterAdapter?
        set(value) = super.setAdapter(value)

    private var ruleContextMenuInfo: RuleContextMenuInfo? = null

    override fun getContextMenuInfo(): RuleContextMenuInfo? = ruleContextMenuInfo

    override fun showContextMenuForChild(originalView: View): Boolean {
        val position = getChildAdapterPosition(originalView)
        if (position >= 0) {
            val rule = adapter!!.getRuleAt(position)
            ruleContextMenuInfo = RuleContextMenuInfo(rule)
            return super.showContextMenuForChild(originalView)
        }
        return false
    }

    class RuleContextMenuInfo(val rule: Rule) : ContextMenu.ContextMenuInfo

    class Divider : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
            outRect.bottom = 1
        }
    }
}
