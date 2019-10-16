package me.camsteffen.polite.view

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import me.camsteffen.polite.util.KeywordSpan

@BindingMethods(
    BindingMethod(
        type = WordSetView::class,
        attribute = "onClickWord",
        method = "setOnClickWordListener"
    )
)
class WordSetView(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs) {

    private var onClickWordListener: OnClickWordListener? = null

    init {
        movementMethod = LinkMovementMethod.getInstance()
    }

    fun setOnClickWordListener(listener: OnClickWordListener?) {
        onClickWordListener = listener
    }

    fun setWords(words: Set<String>) {
        val keywordSpan = SpannableStringBuilder()
        for (word in words) {
            val start = keywordSpan.length
            keywordSpan.append(word)
            val end = start + word.length
            keywordSpan.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onClickWordListener?.onClickWord(word)
                }
            }, start, end, 0)
            keywordSpan.setSpan(KeywordSpan(context), start, end, 0)
            keywordSpan.append(" ")
        }
        text = keywordSpan
    }
}

interface OnClickWordListener {
    fun onClickWord(word: String)
}
