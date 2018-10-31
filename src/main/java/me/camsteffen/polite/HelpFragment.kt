package me.camsteffen.polite

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.os.ConfigurationCompat.getLocales
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebViewFragment
import java.io.IOException

class HelpFragment : WebViewFragment() {

    private val mainActivity: MainActivity
        get() = activity as MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.supportActionBar!!.setTitle(R.string.help)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val language = getLocales(resources.configuration)[0].language
        var path = "help-$language.html"
        if(!assetExists(path)) {
            path = "help.html"
        }
        val url = "file:///android_asset/$path"
        webView.loadUrl(url)
        webView.setWebViewClient(webViewClient)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_help, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId) {
            android.R.id.home -> fragmentManager.popBackStack()
            R.id.email -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "message/rfc822"
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.help_email)))
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.help_email_subject))
                if (intent.resolveActivity(activity.packageManager) != null) {
                    startActivity(intent)
                }
            }
            R.id.beta -> {
                val intent = Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.join_beta_url)))
                startActivity(intent)
            }
            else -> return false
        }
        return true
    }

    private val webViewClient = object : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
            @Suppress("DEPRECATION")
            return shouldOverrideUrlLoading(view, request.url.toString())
        }

        @Suppress("OverridingDeprecatedMember")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url =="help://sound_settings") {
                val intent = Intent(android.provider.Settings.ACTION_SOUND_SETTINGS)
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                }
                return true
            }
            return false
        }
    }

    private fun assetExists(path: String): Boolean {
        val inputStream = try {
            resources.assets.open(path)
        } catch (ex: IOException) {
            return false
        }
        inputStream.close()
        return true
    }

}
