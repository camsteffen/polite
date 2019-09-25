package me.camsteffen.polite

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.core.os.ConfigurationCompat.getLocales
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.webkit.WebViewClientCompat
import java.io.IOException

class HelpFragment : Fragment() {

    private val mainActivity: MainActivity
        get() = activity as MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.help, container, false)
        val webView = view.findViewById<WebView>(R.id.webview)
        val language = getLocales(resources.configuration)[0].language
        var path = "help-$language.html"
        if(!assetExists(path)) {
            path = "help.html"
        }
        val url = "file:///android_asset/$path"
        webView.loadUrl(url)
        webView.webViewClient = HelpWebViewClient
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.supportActionBar!!.setTitle(R.string.help)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_help, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
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
                val intent = Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.join_beta_url)))
                startActivity(intent)
            }
            else -> return false
        }
        return true
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

private object HelpWebViewClient : WebViewClientCompat() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if (request.url.toString() == "help://sound_settings") {
            val intent = Intent(android.provider.Settings.ACTION_SOUND_SETTINGS)
            if (intent.resolveActivity(view.context.packageManager) != null) {
                view.context.startActivity(intent)
            }
            return true
        }
        return false
    }
}

