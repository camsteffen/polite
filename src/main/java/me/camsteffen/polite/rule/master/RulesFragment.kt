package me.camsteffen.polite.rule.master

import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.android.support.DaggerFragment
import me.camsteffen.polite.DB
import me.camsteffen.polite.DBActions
import me.camsteffen.polite.HelpFragment
import me.camsteffen.polite.MainActivity
import me.camsteffen.polite.Polite
import me.camsteffen.polite.R
import me.camsteffen.polite.model.CalendarRule
import me.camsteffen.polite.model.Rule
import me.camsteffen.polite.model.ScheduleRule
import me.camsteffen.polite.rule.RenameDialogFragment
import me.camsteffen.polite.rule.RuleAdapter
import me.camsteffen.polite.rule.RuleList
import me.camsteffen.polite.rule.edit.EditCalendarRuleFragment
import me.camsteffen.polite.rule.edit.EditRuleFragment
import me.camsteffen.polite.rule.edit.EditScheduleRuleFragment
import me.camsteffen.polite.settings.AppPreferences
import me.camsteffen.polite.settings.SettingsFragment
import me.camsteffen.polite.util.RateAppPrompt
import javax.inject.Inject

private const val RULE_LIST = "RuleList"

class RulesFragment : DaggerFragment() {

    @Inject lateinit var preferences: AppPreferences
    @Inject lateinit var rateAppPrompt: RateAppPrompt

    val polite: Polite
        get() = activity!!.application as Polite
    val mainActivity: MainActivity
        get() = activity as MainActivity
    private val noRulesView: View?
        get() = view?.findViewById(R.id.no_rules)
    private val fab: FloatingActionButton
        get() = activity!!.findViewById(R.id.fab) as FloatingActionButton
    var rulesLoader: LoadRules? = null

    lateinit var adapter: RuleAdapter
    private val rulesView: MyRecyclerView?
        get() = view?.findViewById(R.id.rules_view) as MyRecyclerView?
    private val disabledNotice: TextView?
        get() = view?.findViewById(R.id.disabled_notice) as TextView?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true

        if (savedInstanceState != null) {
            val rules = savedInstanceState.getParcelable<RuleList>(RULE_LIST)
            adapter = RuleAdapter(this, rules)
        } else {
            adapter = RuleAdapter(this)
            rulesLoader = LoadRules()
            rulesLoader!!.execute()
        }

        adapter.registerAdapterDataObserver(adapterObserver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(RULE_LIST, adapter.rules)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.rules_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDisabledNoticeVisibility()
        disabledNotice!!.setOnClickListener {
            openSettings()
        }
        registerForContextMenu(rulesView!!)
        fab.setOnClickListener(fabOnClick)
        if(rulesLoader == null || rulesLoader!!.status == AsyncTask.Status.FINISHED) {
            setNoRulesViewVisibility()
            rulesLoader = null
        }
        rulesView!!.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        fab.show()
        checkNotificationPolicyAccess()
    }

    private fun checkNotificationPolicyAccess() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            return
        val notificationManager = mainActivity.notificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.cancelNotificationPolicyAccessRequired()
        } else if (preferences.enable) {
            AlertDialog.Builder(activity!!)
                    .setTitle(R.string.notification_policy_access_required)
                    .setMessage(R.string.notification_policy_access_explain)
                    .setNegativeButton(R.string.disable_polite) { dialog, _ ->
                        dialog.dismiss()
                        preferences.enable = false
                    }
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        startActivity(intent)
                    }
                    .create()
                    .show()
        }
    }

    private fun setDisabledNoticeVisibility() {
        disabledNotice?.visibility =
                if (preferences.enable)
                    View.GONE
                else
                    View.VISIBLE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity.supportActionBar!!.setTitle(R.string.app_name)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> {
                val intent = Intent(Intent.ACTION_SEND)
                val text = getString(R.string.share_text, getString(R.string.play_store_url))
                intent.putExtra(Intent.EXTRA_TEXT, text)
                intent.type = "text/plain"
                startActivity(Intent.createChooser(intent, getString(R.string.share_polite)))
            }
            R.id.settings -> openSettings()
            R.id.help -> {
                fragmentManager!!.beginTransaction()
                        .replace(R.id.fragment_container, HelpFragment())
                        .addToBackStack(null)
                        .commit()
                fab.hide()
            }
            else -> return false
        }
        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        activity!!.menuInflater.inflate(R.menu.rule_context, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as MyRecyclerView.RecyclerViewContextMenuInfo
        when(item.itemId) {
            R.id.rename -> {
                RenameDialogFragment.newInstance(info.id, adapter.getRuleAt(info.position).name)
                        .show(fragmentManager!!, RenameDialogFragment.FRAGMENT_TAG)
            }
            R.id.delete -> {
                deleteRule(info.id)
            }
            else -> return false
        }
        return true
    }

    private fun openSettings() {
        fragmentManager!!.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment(), SettingsFragment.FRAGMENT_TAG)
                .addToBackStack(null)
                .commit()
        fab.hide()
    }

    fun ruleSetEnabled(rule: Rule, enable: Boolean) {
        rule.enabled = enable
        adapter.notifyRuleChanged(rule.id)
        DBActions.RuleSetEnabled(activity!!.applicationContext, rule.id, enable).execute()
    }

    fun deleteRule(id: Long) {
        DBActions.DeleteRule(activity!!.applicationContext, id).execute()
        adapter.deleteRule(id)
    }

    fun openRule(rule: Rule) {
        val fragment = when(rule) {
            is CalendarRule -> {
                if(!mainActivity.checkCalendarPermission())
                    return
                EditCalendarRuleFragment()
            }
            is ScheduleRule -> EditScheduleRuleFragment()
        }
        val args = Bundle()
        args.putParcelable(EditRuleFragment.KEY_RULE, rule)
        fragment.arguments = args
        fragmentManager!!.beginTransaction()
                .setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right)
                .replace(R.id.fragment_container, fragment, EditRuleFragment.FRAGMENT_TAG)
                .addToBackStack(null)
                .commit()
        fab.hide()
    }

    fun openNewCalendarRule() {
        openRule(CalendarRule(activity!!))
    }

    fun openNewScheduleRule() {
        openRule(ScheduleRule(activity!!))
    }

    fun saveRule(mainActivity: MainActivity, rule: Rule) {
        rule.saveDB(mainActivity, {
            rule.addToAdapter(adapter)
        })
        rateAppPrompt.conditionalPrompt(mainActivity)
    }

    fun renameRule(id: Long, name: String) {
        DBActions.RenameRule(activity!!.applicationContext, id, name).execute()
        adapter.renameRule(id, name)
    }

    private val fabOnClick = View.OnClickListener {
        val view = activity!!.layoutInflater.inflate(R.layout.create_rule, null)

        val dialog = AlertDialog.Builder(activity!!)
                .setTitle(R.string.create_a_rule)
                .setView(view)
                .create()

        val calendarRuleView = view.findViewById<View>(R.id.calendar_rule)
        val scheduleRuleView = view.findViewById<View>(R.id.schedule_rule)

        calendarRuleView.setOnClickListener {
            dialog.dismiss()
            if(mainActivity.checkCalendarPermission(MainActivity.REQUEST_PERMISSION_CREATE_CALENDAR_RULE))
                openNewCalendarRule()
        }

        scheduleRuleView.setOnClickListener {
            dialog.dismiss()
            openNewScheduleRule()
        }

        dialog.show()
    }

    fun setNoRulesViewVisibility() {
        noRulesView?.visibility = if(adapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    private val adapterObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            setNoRulesViewVisibility()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            setNoRulesViewVisibility()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            setNoRulesViewVisibility()
        }
    }

    data class LoadRulesResult (
            var scheduleRules: List<ScheduleRule>,
            var calendarRules: List<CalendarRule>
    )

    inner class LoadRules : AsyncTask<Void, Void, LoadRulesResult>() {

        override fun doInBackground(vararg params: Void?): LoadRulesResult {
            val db = Polite.db.readableDatabase
            val scheduleRules = ScheduleRule.queryList(db, null, null, "${DB.Rule.COLUMN_NAME} ASC")
            val calendarRules = CalendarRule.queryList(db, null, null, "${DB.Rule.COLUMN_NAME} ASC")
            return LoadRulesResult(scheduleRules, calendarRules)
        }

        override fun onPostExecute(result: LoadRulesResult) {
            if(view != null)
                rulesLoader = null
            adapter.setRules(result.scheduleRules, result.calendarRules)
            if (result.calendarRules.any { it.enabled })
                mainActivity.checkCalendarPermission()
        }
    }

    companion object {
        const val FRAGMENT_TAG = "rules"
    }
}
