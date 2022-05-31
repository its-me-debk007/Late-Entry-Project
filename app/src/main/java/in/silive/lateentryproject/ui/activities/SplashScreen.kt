package `in`.silive.lateentryproject.ui.activities

import `in`.silive.lateentryproject.utils.Datastore
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this@SplashScreen, MainActivity::class.java)
        startActivity(intent)
        finish()

    }

}