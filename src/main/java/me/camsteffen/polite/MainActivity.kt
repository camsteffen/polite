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
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import me.camsteffen.polite.model.CalendarRule
import me.camsteffen.polite.rule.edit.EditRuleFragment
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

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    @Inject lateinit var notificationManager: AppNotificationManager
    @Inject lateinit var preferences: AppPreferences
    val titleET: EditText
        get() = findViewById(R.id.title) as EditText
    private val rulesFragment: RulesFragment?
        get() = supportFragmentManager.findFragmentByTag(RulesFragment.FRAGMENT_TAG) as RulesFragment?
    private val editRuleFragment: EditRuleFragment<*>?
        get() = supportFragmentManager.findFragmentByTag(EditRuleFragment.FRAGMENT_TAG) as EditRuleFragment<*>?
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
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, 0)
                    .add(R.id.fragment_container, RulesFragment(), RulesFragment.FRAGMENT_TAG)
                    .commit()
            AppBroadcastReceiver.sendRefresh(this)
        }

        preferences.launchCount++

        supportFragmentManager.addOnBackStackChangedListener(this)

        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)
        setHomeAsUp()

        titleET.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(this)
                titleET.clearFocus()
            }
            false
        }

        if (editRuleFragment != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            titleET.visibility = View.VISIBLE
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
                    rulesFragment?.openNewCalendarRule()
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
                                val rulesFragment = rulesFragment
                                rulesFragment?.adapter!!.rules.forEach {
                                    if (it is CalendarRule && it.enabled) {
                                        rulesFragment.ruleSetEnabled(it, false)
                                    }
                                }
                                notificationManager.cancelCalendarPermissionRequired()
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

}

typealias OnBackPressedListener = () -> Boolean
