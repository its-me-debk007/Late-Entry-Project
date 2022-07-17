package `in`.silive.lateentryproject.ui.activities

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.ui.fragments.SplashScreenFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

		if (savedInstanceState == null) {
			supportFragmentManager.beginTransaction()
				.replace(R.id.fragmentContainerView, SplashScreenFragment())
				.commit()
		}
	}

	override fun onBackPressed() {
		when (supportFragmentManager.findFragmentById(R.id.fragmentContainerView)) {

			is SplashScreenFragment -> {}

			else -> super.onBackPressed()
		}
	}
}
