package me.camsteffen.polite

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.View

class MyRecyclerView(context: Context, attrs: AttributeSet? = null) : RecyclerView(context, attrs) {

    init {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        addItemDecoration(Divider())
    }

    private var mContextMenuInfo: RecyclerViewContextMenuInfo? = null

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo? = mContextMenuInfo

    override fun showContextMenuForChild(originalView: View): Boolean {
        val position = getChildAdapterPosition(originalView)
        if (position >= 0) {
            val id = adapter!!.getItemId(position)
            mContextMenuInfo = RecyclerViewContextMenuInfo(position, id)
            return super.showContextMenuForChild(originalView)
        }
        return false
    }

    class RecyclerViewContextMenuInfo(val position: Int, val id: Long) : ContextMenu.ContextMenuInfo

    class Divider: RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
            outRect.bottom = 1
        }
    }
}
