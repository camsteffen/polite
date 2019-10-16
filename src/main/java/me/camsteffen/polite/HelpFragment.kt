package me.camsteffen.polite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import androidx.core.os.ConfigurationCompat.getLocales
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.io.IOException

class HelpFragment : Fragment() {

    private val mainActivity: MainActivity
        get() = activity as MainActivity

    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val webView = WebView(activity!!)
        val language = getLocales(resources.configuration)[0].language
        var path = "help-$language.html"
        if (!assetExists(path)) {
            path = "help.html"
        }
        val url = "file:///android_asset/$path"
        webView.loadUrl(url)
        webView.webViewClient = webViewClient
        this.webView = webView
        return webView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.supportActionBar!!.setTitle(R.string.help)
    }

    override fun onPause() {
        super.onPause()
        webView!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView!!.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView!!.destroy()
        webView = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_help, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().popBackStack()
            R.id.email -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "message/rfc822"
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.help_email)))
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.help_email_subject))
                if (intent.resolveActivity(activity!!.packageManager) != null) {
                    startActivity(intent)
                }
            }
            R.id.beta -> {
                val uri = getString(R.string.join_beta_url).toUri()
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            else -> return false
        }
        return true
    }

    private val webViewClient = object : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest
        ): Boolean {
            @Suppress("DEPRECATION")
            return shouldOverrideUrlLoading(view, request.url.toString())
        }

        @Suppress("OverridingDeprecatedMember")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url == "help://sound_settings") {
                val intent = Intent(android.provider.Settings.ACTION_SOUND_SETTINGS)
                if (intent.resolveActivity(activity!!.packageManager) != null) {
                    activity!!.startActivity(intent)
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
