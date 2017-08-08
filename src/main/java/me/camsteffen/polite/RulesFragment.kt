package me.camsteffen.polite

import android.app.Fragment
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import me.camsteffen.polite.rule.Rule
import me.camsteffen.polite.rule.RuleAdapter
import me.camsteffen.polite.rule.RuleList
import me.camsteffen.polite.rule.calendar.CalendarRule
import me.camsteffen.polite.rule.calendar.EditCalendarRuleFragment
import me.camsteffen.polite.rule.schedule.EditScheduleRuleFragment
import me.camsteffen.polite.rule.schedule.ScheduleRule
import me.camsteffen.polite.settings.SettingsFragment

private const val RULE_LIST = "RuleList"
private const val OPEN_RULE_POS = "openRulePos"

class RulesFragment : Fragment() {

    val polite: Polite
        get() = activity!!.application as Polite
    val mainActivity: MainActivity
        get() = activity as MainActivity
    val noRulesView: View?
        get() = view?.findViewById(R.id.no_rules)
    val fab: FloatingActionButton
        get() = activity.findViewById(R.id.fab) as FloatingActionButton
    var rulesLoader: LoadRules? = null

    lateinit var adapter: RuleAdapter
    var openRulePosition = -1
    val rulesView: MyRecyclerView?
        get() = view?.findViewById(R.id.rules_view) as MyRecyclerView?
    val disabledNotice: TextView?
        get() = view?.findViewById(R.id.disabled_notice) as TextView?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true

        if (savedInstanceState != null) {
            val rules = savedInstanceState.getParcelable<RuleList>(RULE_LIST)
            openRulePosition = savedInstanceState.getInt(OPEN_RULE_POS)
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
        outState.putInt(OPEN_RULE_POS, openRulePosition)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.rules_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDisabledNoticeVisibility()
        disabledNotice!!.setOnClickListener {
            openSettings()
        }
        registerForContextMenu(rulesView)
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
        if(notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.cancel(Polite.NOTIFY_ID_NOTIFICATION_POLICY_ACCESS)
        } else if(Polite.preferences.getBoolean(getString(R.string.preference_enable), true)) {
            AlertDialog.Builder(activity)
                    .setTitle(R.string.notification_policy_access_required)
                    .setMessage(R.string.notification_policy_access_explain)
                    .setNegativeButton(R.string.disable_polite) { dialog, which ->
                        dialog.dismiss()
                        Polite.preferences.edit().putBoolean(getString(R.string.preference_enable), false).apply()
                    }
                    .setPositiveButton(android.R.string.ok) { dialog, which ->
                        val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        startActivity(intent)
                    }
                    .create()
                    .show()
        }
    }

    fun setDisabledNoticeVisibility() {
        disabledNotice?.visibility =
                if (Polite.preferences.getBoolean(getString(R.string.preference_enable), true))
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
        mainActivity.setMenuIcon(menu, R.id.share, R.drawable.ic_share_black_24dp)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
                intent.type = "text/plain"
                startActivity(Intent.createChooser(intent, getString(R.string.share_polite)))
            }
            R.id.settings -> openSettings()
            R.id.help -> {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HelpFragment())
                        .addToBackStack(null)
                        .commit()
                fab.hide()
            }
            else -> return false
        }
        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu?, view: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        activity.menuInflater.inflate(R.menu.rule_context, menu)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        val info = item!!.menuInfo as MyRecyclerView.RecyclerViewContextMenuInfo
        when(item.itemId) {
            R.id.rename -> {
                RenameDialogFragment.newInstance(info.id, info.position, adapter.getRuleAt(info.position).name)
                        .show(fragmentManager, RenameDialogFragment.FRAGMENT_TAG)
            }
            R.id.delete -> {
                deleteRule(info.id, info.position)
            }
            else -> return false
        }
        return true
    }

    fun openSettings() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment(), SettingsFragment.FRAGMENT_TAG)
                .addToBackStack(null)
                .commit()
        fab.hide()
    }

    fun ruleSetEnabled(position: Int, enable: Boolean) {
        val rule = adapter.getRuleAt(position)
        rule.enabled = enable
        adapter.notifyItemChanged(position)
        DBActions.RuleSetEnabled(activity.applicationContext, rule.id, enable).execute()
    }

    fun deleteRule(id: Long, position: Int) {
        DBActions.DeleteRule(activity.applicationContext, id).execute()
        adapter.deleteRule(position)
    }

    fun openRule(rule: Rule, position: Int) {
        val fragment = when(rule) {
            is CalendarRule -> {
                if(!mainActivity.checkCalendarPermission())
                    return
                EditCalendarRuleFragment()
            }
            is ScheduleRule -> EditScheduleRuleFragment()
            else -> throw IllegalStateException()
        }
        openRulePosition = position
        val args = Bundle()
        args.putParcelable(EditRuleFragment.KEY_RULE, rule)
        fragment.arguments = args
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right)
                .replace(R.id.fragment_container, fragment, EditRuleFragment.FRAGMENT_TAG)
                .addToBackStack(null)
                .commit()
        fab.hide()
    }

    fun openNewCalendarRule() {
        openRule(CalendarRule(activity), -1)
    }

    fun openNewScheduleRule() {
        openRule(ScheduleRule(activity), -1)
    }

    fun saveRule(mainActivity: MainActivity, rule: Rule) {
        val newRule = rule.id == Rule.NEW_RULE
        rule.saveDB(mainActivity, {
            if(newRule) {
                rule.addToAdapter(adapter)
            } else {
                adapter.swapRule(rule, openRulePosition)
            }
        })
        mainActivity.rateAppPrompt.conditionalPrompt(mainActivity)
    }

    fun renameRule(id: Long, position: Int, name: String) {
        DBActions.RenameRule(activity.applicationContext, id, name).execute()
        adapter.renameRule(position, name)
    }

    val fabOnClick = View.OnClickListener {
        val view = activity.layoutInflater.inflate(R.layout.create_rule, null)

        val dialog = AlertDialog.Builder(activity)
                .setTitle(R.string.create_a_rule)
                .setView(view)
                .create()

        val calendarRuleView = view.findViewById(R.id.calendar_rule)
        val scheduleRuleView = view.findViewById(R.id.schedule_rule)

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

    val adapterObserver = object : RecyclerView.AdapterDataObserver() {
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
