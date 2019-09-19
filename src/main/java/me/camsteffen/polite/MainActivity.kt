package me.camsteffen.polite

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import me.camsteffen.polite.databinding.ActivityMainBinding
import me.camsteffen.polite.model.DefaultRules
import me.camsteffen.polite.rule.RuleMasterDetailViewModel
import me.camsteffen.polite.rule.master.RulesFragment
import me.camsteffen.polite.settings.AppPreferences
import me.camsteffen.polite.util.AppNotificationManager
import me.camsteffen.polite.util.hideKeyboard
import me.camsteffen.polite.util.tintMenuIcons
import javax.inject.Inject

private const val ACTIVITY_CALENDAR_PERMISSION = 1

class MainActivity : AppCompatActivity(), HasAndroidInjector, FragmentManager.OnBackStackChangedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        const val REQUEST_PERMISSION_CALENDAR = 0
        const val REQUEST_PERMISSION_CREATE_CALENDAR_RULE = 1

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    @Inject lateinit var defaultRules: DefaultRules
    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    @Inject lateinit var notificationManager: AppNotificationManager
    @Inject lateinit var preferences: AppPreferences
    @Inject lateinit var ruleService: RuleService
    @Inject lateinit var viewModelProviderFactory: ViewModelProvider.Factory

    private lateinit var model: RuleMasterDetailViewModel
    private var onBackPressedListener: OnBackPressedListener? = null

    @Inject
    fun inject(preferences: AppPreferences) {
        this.preferences = preferences
    }

    override fun androidInjector() = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        setThemeFromPreference()
        super.onCreate(savedInstanceState)
        model = ViewModelProviders.of(this, viewModelProviderFactory)[RuleMasterDetailViewModel::class.java]
        model.selectedRule.value = null
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.model = model
        setSupportActionBar(binding.toolbar)
        val fab = binding.fab

        fab.setOnClickListener {
            onClickFloatingActionButton()
        }

        model.enabledCalendarRulesExist.observe(this, Observer { enabledCalendarRulesExist ->
            if (enabledCalendarRulesExist!!) {
                checkCalendarPermission()
            }
        })

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, 0)
                    .add(R.id.fragment_container, RulesFragment(), RulesFragment.FRAGMENT_TAG)
                    .commit()
            fab.show()
            fab.bringToFront()
            AppBroadcastReceiver.sendRefresh(this)
        }

        preferences.launchCount++

        supportFragmentManager.addOnBackStackChangedListener(this)

        binding.toolbarEditText.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(this)
                view!!.clearFocus()
            }
            false
        }
    }

    override fun onBackStackChanged() {
        setHomeAsUp()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        tintMenuIcons(this, menu)
        return true
    }

    override fun onBackPressed() {
        when {
            onBackPressedListener.run { this != null && this.invoke() } -> Unit
            else -> super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK)
            return

        when (requestCode) {
            ACTIVITY_CALENDAR_PERMISSION -> checkCalendarPermission()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if(allGranted) {
            when(requestCode) {
                REQUEST_PERMISSION_CREATE_CALENDAR_RULE -> {
                    model.selectedRule.value = defaultRules.calendar()
                }
                REQUEST_PERMISSION_CALENDAR -> {
                    AppBroadcastReceiver.sendRefresh(this)
                }
            }
        } else {
            when (requestCode) {
                REQUEST_PERMISSION_CREATE_CALENDAR_RULE,
                REQUEST_PERMISSION_CALENDAR -> {
                    val alertBuilder = AlertDialog.Builder(this)
                            .setTitle(R.string.calendar_permission_required)
                            .setMessage(R.string.calendar_permission_explain)
                            .setNegativeButton(android.R.string.no) { dialog, _ ->
                                dialog.dismiss()
                                notificationManager.cancelCalendarPermissionRequired()
                                ruleService.updateCalendarRulesEnabledAsync(false)
                            }

                    // request calendar permission or open settings directly
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR)) {
                        alertBuilder.setPositiveButton(android.R.string.ok) { _, _ ->
                            requestPermissions(permissions, requestCode)
                        }
                    } else {
                        alertBuilder.setPositiveButton(R.string.open_app_settings) { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.fromParts("package", packageName, null)
                            startActivityForResult(intent, ACTIVITY_CALENDAR_PERMISSION)
                        }
                    }
                    alertBuilder.create().show()
                }
            }
        }
    }

    fun registerOnBackPressedListener(
        lifecycleOwner: LifecycleOwner,
        onBackPressedListener: OnBackPressedListener
    ) {
        this.onBackPressedListener = onBackPressedListener
        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                unRegisterOnBackPressedListener(onBackPressedListener)
            }
        })
    }

    fun unRegisterOnBackPressedListener(onBackPressedListener: OnBackPressedListener) {
        if (this.onBackPressedListener == onBackPressedListener) {
            this.onBackPressedListener = null
        }
    }

    fun setThemeFromPreference() {
        val themeDark = getString(R.string.theme_dark)
        val theme = when (preferences.theme) {
            themeDark -> R.style.AppTheme
            else -> R.style.AppTheme_Light
        }
        setTheme(theme)
    }

    fun checkCalendarPermission(requestCode: Int = REQUEST_PERMISSION_CALENDAR): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        } else if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR), requestCode)
            return false
        }
        notificationManager.cancelCalendarPermissionRequired()
        return true
    }

    private fun setHomeAsUp() {
        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        supportActionBar!!.setDisplayHomeAsUpEnabled(backStackEntryCount > 0)
    }

    private fun onClickFloatingActionButton() {
        val view = layoutInflater.inflate(R.layout.create_rule, null)

        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.create_a_rule)
                .setView(view)
                .create()

        val calendarRuleView = view.findViewById<View>(R.id.calendar_rule)
        val scheduleRuleView = view.findViewById<View>(R.id.schedule_rule)

        calendarRuleView.setOnClickListener {
            dialog.dismiss()
            if(checkCalendarPermission(REQUEST_PERMISSION_CREATE_CALENDAR_RULE))
                model.selectedRule.value = defaultRules.calendar()
        }

        scheduleRuleView.setOnClickListener {
            dialog.dismiss()
            model.selectedRule.value = defaultRules.schedule()
        }

        dialog.show()
    }

}

typealias OnBackPressedListener = () -> Boolean
