package me.camsteffen.polite.rule.edit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import dagger.android.support.DaggerFragment
import me.camsteffen.polite.MainActivity
import me.camsteffen.polite.R
import me.camsteffen.polite.RuleService
import me.camsteffen.polite.databinding.EditRuleBinding
import me.camsteffen.polite.model.Rule
import me.camsteffen.polite.rule.RuleMasterDetailViewModel
import me.camsteffen.polite.util.RateAppPromptFacade
import me.camsteffen.polite.util.hideKeyboard
import javax.inject.Inject

abstract class EditRuleFragment<RuleType : Rule> : DaggerFragment() {

    @Inject lateinit var navController: NavController
    @Inject lateinit var rateAppPrompt: RateAppPromptFacade
    @Inject lateinit var ruleService: RuleService
    @Inject lateinit var viewModelProviderFactory: ViewModelProvider.Factory

    private lateinit var masterModel: RuleMasterDetailViewModel
    private lateinit var editRuleModel: EditRuleViewModel<RuleType>

    val mainActivity: MainActivity
        get() = activity as MainActivity
    protected lateinit var scrollView: ScrollView
    lateinit var rule: RuleType
    var newRule: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity.registerOnBackPressedListener(this, this::onBackPressed)
        masterModel = ViewModelProviders
            .of(activity!!, viewModelProviderFactory)[RuleMasterDetailViewModel::class.java]
        @Suppress("UNCHECKED_CAST")
        rule = masterModel.selectedRule.value!! as RuleType
        newRule = rule.id == Rule.NEW_ID
        masterModel.toolbarEditText.value = rule.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
        editRuleModel = onCreateEditRuleViewModel()
        editRuleModel.setRule(rule)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil
            .inflate<EditRuleBinding>(layoutInflater, R.layout.edit_rule, container, false)
        binding.lifecycleOwner = this
        scrollView = binding.scrollView
        binding.model = editRuleModel
        val editRuleView = onCreateEditRuleView(inflater, binding.editRule, savedInstanceState)
        binding.editRule.addView(editRuleView)
        return binding.root
    }

    override fun onDetach() {
        super.onDetach()
        masterModel.toolbarEditText.value = ""
    }

    abstract fun onCreateEditRuleViewModel(): EditRuleViewModel<RuleType>

    abstract fun onCreateEditRuleView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit_rule, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> validateSaveClose()
            R.id.delete -> {
                AlertDialog.Builder(activity!!)
                    .setTitle(R.string.delete_rule_title)
                    .setMessage(R.string.delete_rule_confirm)
                    .setPositiveButton(R.string.yes) { _, _ ->
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
                navController.navigate(R.id.action_global_helpFragment)
            }
            else -> return false
        }
        return true
    }

    private fun onBackPressed(): Boolean {
        validateSaveClose()
        return true
    }

    open fun validateSaveClose() {
        saveClose()
    }

    fun saveClose() {
        val rule = ruleFromUi()
        if (newRule || rule != masterModel.selectedRule.value) {
            ruleService.saveRuleAsync(rule)
            rateAppPrompt.conditionalPrompt()
        }
        hideKeyboard(activity!!)
        masterModel.selectedRule.value = null
    }

    abstract fun ruleFromUi(id: Long, name: String, enabled: Boolean, vibrate: Boolean): RuleType

    private fun ruleFromUi(): RuleType {
        val name = masterModel.toolbarEditText.value!!
        val enabled = editRuleModel.enabled.get()
        val vibrate = editRuleModel.vibrate.get()
        return ruleFromUi(rule.id, name, enabled, vibrate)
    }
}
