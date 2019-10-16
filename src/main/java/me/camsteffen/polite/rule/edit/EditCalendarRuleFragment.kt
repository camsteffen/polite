package me.camsteffen.polite.rule.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import me.camsteffen.polite.R
import me.camsteffen.polite.data.CalendarDao
import me.camsteffen.polite.databinding.EditCalendarRuleBinding
import me.camsteffen.polite.model.CalendarEventMatchBy
import me.camsteffen.polite.model.CalendarRule
import java.util.Locale
import javax.inject.Inject

class EditCalendarRuleFragment : EditRuleFragment<CalendarRule>() {

    private lateinit var keywordsSection: View
    private lateinit var addKeywordEditText: EditText
    private lateinit var model: EditCalendarRuleViewModel
    @Inject lateinit var calendarDao: CalendarDao

    override fun onCreateEditRuleViewModel(): EditCalendarRuleViewModel {
        model = ViewModelProviders
            .of(activity!!, viewModelProviderFactory)[EditCalendarRuleViewModel::class.java]
        model.showKeywords.observe(this, Observer { showKeywords ->
            if (showKeywords!!) {
                addKeywordEditText.requestFocus()
            }
        })
        return model
    }

    override fun onCreateEditRuleView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<EditCalendarRuleBinding>(
            layoutInflater, R.layout.edit_calendar_rule, container, false
        )
        binding.lifecycleOwner = this
        binding.handlers = this
        binding.model = model
        keywordsSection = binding.keywordsSection
        addKeywordEditText = binding.newKeyword
        addKeywordEditText.setOnEditorActionListener(
            TextView.OnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    100 -> addKeyword()
                    else -> return@OnEditorActionListener false
                }
                true
            })
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!newRule) {
            addKeywordEditText.requestFocus()
        }
    }

    override fun ruleFromUi(id: Long, name: String, enabled: Boolean, vibrate: Boolean):
        CalendarRule {
        return CalendarRule(
            id = id,
            name = name,
            enabled = enabled,
            vibrate = vibrate,
            busyOnly = model.busyOnly.value!!,
            matchBy = model.matchBy.value!!,
            inverseMatch = model.inverseMatch.value!!,
            calendarIds = model.calendarIds.value!!,
            keywords = model.keywords.value!!
        )
    }

    private fun addKeyword() {
        val addKeywordEditText = addKeywordEditText
        val word = addKeywordEditText.text.toString()
            .trim()
            .toLowerCase(Locale.getDefault())
        if (word.isEmpty()) {
            return
        }
        if (model.addKeyword(word)) {
            addKeywordEditText.setText("")
            view!!.post {
                // scroll after keywords draw
                if (keywordsSection.top > scrollView.scrollY) {
                    scrollView.smoothScrollTo(0, keywordsSection.top)
                }
            }
        } else {
            val text = getString(R.string.word_already_added, word)
            Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
        }
    }

    fun onClickCalendars() {
        if (!mainActivity.checkCalendarPermission()) {
            return
        }

        val calendars = calendarDao.getCalendars()
        if (calendars == null) {
            Toast.makeText(activity, R.string.error_read_calendars, Toast.LENGTH_SHORT).show()
            return
        }
        if (calendars.isEmpty()) {
            Toast.makeText(activity, R.string.no_calendars_found, Toast.LENGTH_SHORT).show()
            return
        }

        val calendarNames = calendars.map { it.name }.toTypedArray()
        val checked = calendars.map { model.calendarIds.value!!.contains(it.id) }.toBooleanArray()

        AlertDialog.Builder(activity!!)
            .setTitle(R.string.select_calendars)
            .setMultiChoiceItems(calendarNames, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setNeutralButton(R.string.all) { _, _ ->
                model.calendarIds.value = emptySet()
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                model.calendarIds.value = checked.indices
                    .filter { checked[it] }
                    .map { calendars[it].id }
                    .toSet()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show()
    }

    override fun validateSaveClose() {
        val word = addKeywordEditText.text.trim()
        if (word.isEmpty()) {
            saveClose()
        } else {
            AlertDialog.Builder(activity!!)
                .setTitle(R.string.unsaved_keyword_title)
                .setMessage(getString(R.string.unsaved_keyword_message, word))
                .setPositiveButton(R.string.continue_) { _, _ ->
                    saveClose()
                }
                .setNegativeButton(R.string.go_back, null)
                .create()
                .show()
        }
    }

    fun onClickMatchBy(view: View) {
        val popup = PopupMenu(context, view)
        popup.setOnMenuItemClickListener { item ->
            model.matchBy.value = when (item!!.itemId) {
                R.id.match_all -> CalendarEventMatchBy.ALL
                R.id.match_by_title -> CalendarEventMatchBy.TITLE
                R.id.match_by_desc -> CalendarEventMatchBy.DESCRIPTION
                R.id.match_by_title_desc -> CalendarEventMatchBy.TITLE_AND_DESCRIPTION
                else -> throw IllegalArgumentException()
            }
            true
        }
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.event_match, popup.menu)
        popup.show()
    }

    fun onClickInverseMatch() {
        AlertDialog.Builder(activity!!)
            .setTitle(R.string.inverse_match)
            .setMessage(R.string.inverse_match_message)
            .setNegativeButton(R.string.disable) { _, _ ->
                model.setInverseMatch(false)
            }
            .setPositiveButton(R.string.enable) { _, _ ->
                model.setInverseMatch(true)
            }
            .create()
            .show()
    }

    fun onClickAddKeyword() {
        addKeyword()
        addKeywordEditText.requestFocus()
    }

    fun onClickKeyword(word: String) {
        model.removeWord(word)
    }
}
