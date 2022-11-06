package `in`.silive.lateentryproject.ui.activities

import `in`.silive.lateentryproject.databinding.ActivityWebviewBinding
import `in`.silive.lateentryproject.utils.CHANGE_PASSWORD_URL
import `in`.silive.lateentryproject.utils.STAFF_PANEL_URL
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webview.apply {
            settings.javaScriptEnabled = true

            loadUrl(
                if (intent.getStringExtra("WEBSITE") == "STAFF") STAFF_PANEL_URL
                else CHANGE_PASSWORD_URL
            )
//            loadUrl("https://www.google.co.in/")
        }

        binding.closeBtn.setOnClickListener { finish() }
    }

    override fun onBackPressed() {
        binding.webview.goBack()
    }
}