package me.camsteffen.polite

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.FragmentManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import dagger.android.support.DaggerAppCompatActivity
import me.camsteffen.polite.model.CalendarRule
import me.camsteffen.polite.rule.edit.EditRuleFragment
import me.camsteffen.polite.rule.master.RulesFragment
import me.camsteffen.polite.util.RateAppPrompt
import me.camsteffen.polite.util.hideKeyboard
import javax.inject.Inject

private const val ACTIVITY_CALENDAR_PERMISSION = 1

class MainActivity : DaggerAppCompatActivity(), FragmentManager.OnBackStackChangedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        const val REQUEST_PERMISSION_CALENDAR = 0
        const val REQUEST_PERMISSION_CREATE_CALENDAR_RULE = 1

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    @Inject lateinit var rateAppPrompt: RateAppPrompt

    val notificationManager: NotificationManager
        get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val titleET: EditText
        get() = findViewById(R.id.title) as EditText
    private val rulesFragment: RulesFragment?
        get() = fragmentManager.findFragmentByTag(RulesFragment.FRAGMENT_TAG) as RulesFragment?
    private val editRuleFragment: EditRuleFragment<*>?
        get() = fragmentManager.findFragmentByTag(EditRuleFragment.FRAGMENT_TAG) as EditRuleFragment<*>?

    override fun onCreate(savedInstanceState: Bundle?) {
        setThemeFromPreference()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, 0)
                    .add(R.id.fragment_container, RulesFragment(), RulesFragment.FRAGMENT_TAG)
                    .commit()
            AppBroadcastReceiver.sendRefresh(this)
        }

        rateAppPrompt.launchCount++

        fragmentManager.addOnBackStackChangedListener(this)

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

    override fun onBackPressed() {
        val editRuleFragment = editRuleFragment
        if(editRuleFragment != null && editRuleFragment.isVisible) {
            editRuleFragment.validateSaveClose()
        } else if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else {
            finish()
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
                                notificationManager.cancel(Polite.NOTIFY_ID_CALENDAR_PERMISSION)
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

    fun setThemeFromPreference() {
        val themeLight = getString(R.string.theme_light)
        val themeDark = getString(R.string.theme_dark)
        val themeName = Polite.preferences.getString(getString(R.string.preference_theme), themeLight)
        val theme = when(themeName) {
            themeDark -> R.style.AppTheme
            else -> R.style.AppThemeLight
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
        notificationManager.cancel(Polite.NOTIFY_ID_CALENDAR_PERMISSION)
        return true
    }

    private fun setHomeAsUp() {
        val backStackEntryCount = fragmentManager.backStackEntryCount
        supportActionBar!!.setDisplayHomeAsUpEnabled(backStackEntryCount > 0)
    }

    fun setMenuIcon(menu: Menu, itemId: Int, iconId: Int) {
        var drawable = ContextCompat.getDrawable(this, iconId)!!
        drawable = DrawableCompat.wrap(drawable).mutate()
        DrawableCompat.setTint(drawable, Color.WHITE)
        menu.findItem(itemId).icon = drawable
    }

}
