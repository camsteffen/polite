package me.camsteffen.polite.rule.calendar

import android.os.Bundle
import android.provider.CalendarContract
import android.support.v7.app.AlertDialog
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import me.camsteffen.polite.EditRuleFragment
import me.camsteffen.polite.KeywordSpan
import me.camsteffen.polite.R
import me.camsteffen.polite.rule.Rule
import me.camsteffen.polite.widget.CaptionOption

class EditCalendarRuleFragment : EditRuleFragment<CalendarRule>() {

    val inverseMatch: CaptionOption
        get() = view.findViewById(R.id.inverse) as CaptionOption
    val keywordsSection: View
        get() = view.findViewById(R.id.keywords_section)
    val addKeywordEditText: EditText
        get() = view.findViewById(R.id.new_keyword) as EditText
    var wordsTV: TextView? = null
    var removeKeywordsTip: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.edit_calendar_rule_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendars = view!!.findViewById(R.id.calendars) as CaptionOption
        val eventsMatch = view.findViewById(R.id.events) as CaptionOption
        val addKeywordButton = view.findViewById(R.id.add_keyword_button) as Button
        wordsTV = view.findViewById(R.id.words) as TextView
        removeKeywordsTip = view.findViewById(R.id.remove_keywords_tip) as TextView

        calendars.setOnClickListener {
            selectCalendars()
        }

        // Match
        eventsMatch.setOnClickListener { view ->
            selectEventMatch(view)
        }

        inverseMatch.setOnClickListener {
            setInverseMatch()
        }

        // Keywords
        val addKeywordEditText = addKeywordEditText
        addKeywordEditText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                100 -> addKeyword()
                else -> return@OnEditorActionListener false
            }
            true
        })
        addKeywordButton.setOnClickListener {
            addKeyword()
            addKeywordEditText.requestFocus()
        }
        wordsTV!!.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (rule.id != Rule.NEW_RULE) {
            addKeywordEditText.requestFocus()
        }
        onUpdateCalendars()
        onUpdateEventMatch()
        onUpdateInverseMatch()
        onUpdateKeywords()
    }

    override fun createRule(): CalendarRule {
        return CalendarRule(activity)
    }

    override fun save() {
        rulesFragment.saveRule(mainActivity, rule)
    }

    private fun addKeyword() {
        val addKeywordEditText = addKeywordEditText
        val word = addKeywordEditText.text.toString()
                .trim()
                .toLowerCase()
        if (word.isEmpty())
            return
        if (rule.keywords.add(word)) {
            addKeywordEditText.setText("")
            onUpdateKeywords()
            view.post { // scroll after keywords draw
                val scrollView = view.findViewById(R.id.scroll_view) as ScrollView
                val keywordsSection = keywordsSection
                if(keywordsSection.top > scrollView.scrollY) {
                    scrollView.smoothScrollTo(0, keywordsSection.top)
                }
            }
        } else {
            Toast.makeText(activity, getString(R.string.word_already_added, word), Toast.LENGTH_SHORT).show()
        }
    }

    private fun onUpdateKeywords() {
        val keywordSpan = SpannableStringBuilder()
        for (word in rule.keywords) {
            val start = keywordSpan.length
            keywordSpan.append(word)
            val end = start + word.length
            keywordSpan.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    rule.keywords.remove(word)
                    onUpdateKeywords()
                }
            }, start, end, 0)
            keywordSpan.setSpan(KeywordSpan(activity), start, end, 0)
            keywordSpan.append(" ")
        }
        wordsTV!!.text = keywordSpan

        removeKeywordsTip!!.visibility = if (rule.keywords.size > 0) View.VISIBLE else View.GONE
    }

    private fun selectCalendars() {
        if(!mainActivity.checkCalendarPermission())
            return

        val CALENDAR_PROJECTION = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)

        val cr = activity.contentResolver
        val calCur = cr.query(CalendarContract.Calendars.CONTENT_URI, CALENDAR_PROJECTION, null, null, null)
        if(calCur == null) {
            Toast.makeText(activity, R.string.error_read_calendars, Toast.LENGTH_SHORT).show()
            return
        }
        val count = calCur.count
        if(count == 0) {
            Toast.makeText(activity, R.string.no_calendars_found, Toast.LENGTH_SHORT).show()
            calCur.close()
            return
        }
        val calIDs = LongArray(count)
        val calNames = arrayOfNulls<String>(count)
        val checked = BooleanArray(count)

        for (i in 0..count - 1) {
            calCur.moveToNext()
            val id = calCur.getLong(0)
            calIDs[i] = id
            calNames[i] = calCur.getString(1)
            checked[i] = rule.calendars.contains(id)
        }
        calCur.close()

        AlertDialog.Builder(activity)
                .setTitle(R.string.select_calendars)
                .setMultiChoiceItems(calNames, checked) { _, which, isChecked ->
                    checked[which] = isChecked
                }
                .setNeutralButton(R.string.all) { _, _ ->
                    rule.calendars.clear()
                    onUpdateCalendars()
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    rule.calendars.clear()
                    checked.indices
                            .filter { checked[it] }
                            .forEach { rule.calendars.add(calIDs[it]) }
                    onUpdateCalendars()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show()
    }

    private fun onUpdateCalendars() {
        val calendars = view.findViewById(R.id.calendars) as CaptionOption
        val calendarsCaption = calendars.caption
        if (rule.calendars.size == 0) {
            calendarsCaption.setText(R.string.all)
        } else {
            calendarsCaption.text = getString(R.string.n_selected, rule.calendars.size)
        }
    }

    private fun selectEventMatch(view: View) {
        val popup = PopupMenu(activity, view)
        popup.setOnMenuItemClickListener { item ->
            rule.match = when (item!!.itemId) {
                R.id.match_all -> CalendarRule.MATCH_ALL
                R.id.match_by_title -> CalendarRule.MATCH_TITLE
                R.id.match_by_desc -> CalendarRule.MATCH_DESCRIPTION
                R.id.match_by_title_desc -> CalendarRule.MATCH_TITLE or CalendarRule.MATCH_DESCRIPTION
                else -> throw IllegalStateException()
            }
            val keywordsSection = keywordsSection
            val keywordsWasVisible = keywordsSection.visibility == View.VISIBLE
            onUpdateEventMatch()
            if(!keywordsWasVisible && keywordsSection.visibility == View.VISIBLE) {
                addKeywordEditText.requestFocus()
            }
            true
        }
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.event_match, popup.menu)
        popup.show()
    }

    fun setInverseMatch() {
        AlertDialog.Builder(activity)
                .setTitle(R.string.inverse_match)
                .setMessage(R.string.inverse_match_message)
                .setNegativeButton(R.string.disable, { _, _ ->
                    rule.inverseMatch = false
                    onUpdateInverseMatch()
                })
                .setPositiveButton(R.string.enable, { _, _ ->
                    rule.inverseMatch = true
                    onUpdateInverseMatch()
                })
                .create()
                .show()
    }

    private fun onUpdateEventMatch() {
        // set caption text
        val stringId = when (rule.match) {
            CalendarRule.MATCH_ALL -> R.string.all_events
            CalendarRule.MATCH_TITLE -> R.string.match_by_title
            CalendarRule.MATCH_DESCRIPTION -> R.string.match_by_desc
            CalendarRule.MATCH_TITLE or CalendarRule.MATCH_DESCRIPTION -> R.string.match_by_title_desc
            else -> throw IllegalStateException("illegal rule match value: " + rule.match)
        }
        val events = view.findViewById(R.id.events) as CaptionOption
        events.caption.text = getString(stringId)

        // show or hide keywords section
        keywordsSection.visibility = if (rule.match == CalendarRule.MATCH_ALL)
            View.GONE
        else
            View.VISIBLE
    }

    fun onUpdateInverseMatch() {
        inverseMatch.caption.text = getString(when(rule.inverseMatch) {
            true -> R.string.enabled
            false -> R.string.disabled
        })
    }

    override fun validateSaveClose() {
        val word = addKeywordEditText.text.trim()
        if(word.isEmpty()) {
            saveClose()
        } else {
            AlertDialog.Builder(activity)
                    .setTitle(R.string.unsaved_keyword_title)
                    .setMessage(getString(R.string.unsaved_keyword_message, word))
                    .setPositiveButton(R.string.continue_, { _, _ ->
                        saveClose()
                    })
                    .setNegativeButton(R.string.go_back, null)
                    .create()
                    .show()
        }
    }

}
