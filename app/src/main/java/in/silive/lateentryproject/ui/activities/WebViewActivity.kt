@file:Suppress("DEPRECATION")

package `in`.silive.lateentryproject.ui.activities

import `in`.silive.lateentryproject.databinding.ActivityWebviewBinding
import `in`.silive.lateentryproject.utils.CHANGE_PASSWORD_URL
import `in`.silive.lateentryproject.utils.STAFF_PANEL_URL
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsetsController
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
//            window.insetsController?.setSystemBarsAppearance(
//                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
//                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
//            )
//        else window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        binding = ActivityWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        var pageFirstTimeLoaded = true

        val intentArg = intent.getStringExtra("WEBSITE")
        supportActionBar?.title = if (intentArg == "STAFF") "Staff Panel" else "Change Password"

        binding.webview.apply {
            settings.javaScriptEnabled = true

            val customWebViewClient = object : WebViewClient() {

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    if (!pageFirstTimeLoaded) binding.upperProgressBar.show()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (pageFirstTimeLoaded) {
                        pageFirstTimeLoaded = false
                        binding.progressBar.hide()
                    } else binding.upperProgressBar.hide()
                }

                override fun doUpdateVisitedHistory(
                    view: WebView?,
                    url: String?,
                    isReload: Boolean
                ) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    if (url == "https://lateentry.silive.in/change/password_change/done/") {
                        Handler(Looper.getMainLooper()).postDelayed({
                            Toast.makeText(
                                context,
                                "password changed successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            finish()
                        }, 500)
                    }
                }
            }

            webViewClient = customWebViewClient
            loadUrl(
                if (intentArg == "STAFF") STAFF_PANEL_URL
                else CHANGE_PASSWORD_URL
            )

        }

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onBackPressed() {
        if (binding.webview.canGoBack()) binding.webview.goBack()
        else finish()
    }
}