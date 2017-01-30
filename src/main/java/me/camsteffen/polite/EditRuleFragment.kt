package me.camsteffen.polite

import android.app.Fragment
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Switch
import me.camsteffen.polite.rule.Rule

abstract class EditRuleFragment<RuleType : Rule> : Fragment() {

    companion object {
        const val FRAGMENT_TAG = "EditRule"
        const val KEY_RULE = "rule"
    }

    val mainActivity: MainActivity
        get() = activity as MainActivity
    val rulesFragment: RulesFragment
        get() = fragmentManager.findFragmentByTag(RulesFragment.FRAGMENT_TAG) as RulesFragment
    lateinit var rule: RuleType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
        rule = arguments?.getParcelable(EditRuleFragment.KEY_RULE) ?: createRule()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity.supportActionBar!!.setDisplayShowTitleEnabled(false)
        val titleET = mainActivity.titleET
        titleET.visibility = View.VISIBLE
        titleET.setText(rule.name)
        titleET.addTextChangedListener(titleListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val titleET = mainActivity.titleET
        titleET.visibility = View.GONE
        titleET.removeTextChangedListener(titleListener)
        mainActivity.supportActionBar!!.setDisplayShowTitleEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EditRuleFragment.KEY_RULE, rule)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit_rule, menu)
        mainActivity.setMenuIcon(menu, R.id.delete, R.drawable.ic_delete_black_24dp)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val rulesFragment = rulesFragment
        when(item!!.itemId) {
            android.R.id.home -> validateSaveClose()
            R.id.delete -> {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.delete_rule_title)
                        .setMessage(R.string.delete_rule_confirm)
                        .setPositiveButton(R.string.yes) { dialog, which ->
                            fragmentManager.popBackStack()
                            if (rule.id != Rule.NEW_RULE)
                                rulesFragment.deleteRule(rule.id, rulesFragment.openRulePosition)
                        }
                        .setNegativeButton(R.string.no, null)
                        .create()
                        .show()
            }
            R.id.help -> {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HelpFragment())
                        .addToBackStack(null)
                        .commit()
            }
            else -> return false
        }
        return true
    }

    abstract fun createRule(): RuleType

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val enableSwitch = view!!.findViewById(R.id.enable) as Switch
        val vibrateSwitch = view.findViewById(R.id.vibrate) as Switch

        // Set initial state
        enableSwitch.isChecked = rule.enabled
        vibrateSwitch.isChecked = rule.vibrate

        // Enable Switch
        enableSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            rule.enabled = isChecked
        }

        // Vibrate Switch
        vibrateSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            rule.vibrate = isChecked
        }
    }

    open fun validateSaveClose() {
        saveClose()
    }

    abstract fun save()

    fun saveClose() {
        save()
        mainActivity.hideKeyboard()
        fragmentManager.popBackStack()
    }

    val titleListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            rule.name = s.toString()
        }
    }
}