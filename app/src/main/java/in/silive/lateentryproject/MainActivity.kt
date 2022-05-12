package `in`.silive.lateentryproject

import `in`.silive.lateentryproject.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

		binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

		val modalBottomSheet = BottomSheetFragment()
		modalBottomSheet.show(supportFragmentManager, "BottomSheet")

//			supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, ())
//			.commit()
	}
}