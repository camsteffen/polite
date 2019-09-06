package me.camsteffen.polite.rule.edit

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerFragment
import me.camsteffen.polite.HelpFragment
import me.camsteffen.polite.MainActivity
import me.camsteffen.polite.R
import me.camsteffen.polite.RuleService
import me.camsteffen.polite.model.Rule
import me.camsteffen.polite.rule.RuleMasterDetailViewModel
import me.camsteffen.polite.rule.master.RulesFragment
import me.camsteffen.polite.util.RateAppPrompt
import me.camsteffen.polite.util.hideKeyboard
import javax.inject.Inject

abstract class EditRuleFragment<RuleType : Rule> : DaggerFragment() {

    companion object {
        const val FRAGMENT_TAG = "EditRule"
    }

    @Inject lateinit var rateAppPrompt: RateAppPrompt
    @Inject lateinit var ruleService: RuleService
    @Inject lateinit var viewModelProviderFactory: ViewModelProvider.Factory

    private lateinit var masterModel: RuleMasterDetailViewModel

    val mainActivity: MainActivity
        get() = activity as MainActivity
    val rulesFragment: RulesFragment
        get() = fragmentManager!!.findFragmentByTag(RulesFragment.FRAGMENT_TAG) as RulesFragment
    lateinit var rule: RuleType
    var newRule: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity.registerOnBackPressedListener(this, this::onBackPressed)
        masterModel = ViewModelProviders.of(activity!!, viewModelProviderFactory)[RuleMasterDetailViewModel::class.java]
        @Suppress("UNCHECKED_CAST")
        rule = masterModel.selectedRule.value!! as RuleType
        newRule = rule.id == Rule.NEW_RULE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit_rule, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId) {
            android.R.id.home -> validateSaveClose()
            R.id.delete -> {
                AlertDialog.Builder(activity!!)
                        .setTitle(R.string.delete_rule_title)
                        .setMessage(R.string.delete_rule_confirm)
                        .setPositiveButton(R.string.yes) { _, _ ->
                            fragmentManager!!.popBackStack()
                            if (!newRule) {
                                ruleService.deleteRuleAsync(rule.id)
                            }
                            masterModel.selectedRule.value = null
                        }
                        .setNegativeButton(R.string.no, null)
                        .create()
                        .show()
            }
            R.id.help -> {
                fragmentManager!!.beginTransaction()
                        .replace(R.id.fragment_container, HelpFragment())
                        .addToBackStack(null)
                        .commit()
            }
            else -> return false
        }
        return true
    }

    private fun onBackPressed(): Boolean {
        validateSaveClose()
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val enableSwitch = view.findViewById(R.id.enable) as Switch
        val vibrateSwitch = view.findViewById(R.id.vibrate) as Switch

        // Set initial state
        enableSwitch.isChecked = rule.enabled
        vibrateSwitch.isChecked = rule.vibrate

        // Enable Switch
        enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            rule.enabled = isChecked
        }

        // Vibrate Switch
        vibrateSwitch.setOnCheckedChangeListener { _, isChecked ->
            rule.vibrate = isChecked
        }
    }

    open fun validateSaveClose() {
        saveClose()
    }

    fun save() {
        ruleService.saveRuleAsync(rule)
        rateAppPrompt.conditionalPrompt(activity!!)
    }

    fun saveClose() {
        save()
        hideKeyboard(activity!!)
        fragmentManager!!.popBackStack()
        masterModel.selectedRule.value = null
    }

    private val titleListener: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable) {
            rule.name = s.toString()
        }
    }
}
