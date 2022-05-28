package `in`.silive.lateentryproject.ui.activities

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.ActivityMainBinding
import `in`.silive.lateentryproject.models.Datastore
import `in`.silive.lateentryproject.ui.fragments.BarcodeFragment
import `in`.silive.lateentryproject.ui.fragments.LoginFragment
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private lateinit var datastore: Datastore

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

		binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
		datastore = Datastore(this@MainActivity)

		lifecycleScope.launch {
			if (datastore.isLoggedIn()) askPermission()
			else gotToFragment(LoginFragment())
		}
	}

	private fun askPermission() {
		requestPermission.launch(Manifest.permission.CAMERA)
	}

	private val requestPermission = registerForActivityResult(
		ActivityResultContracts.RequestPermission()
	) {
		if (it) {
			gotToFragment(BarcodeFragment())
		} else {
			if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
				showGoToAppSettingsDialog(this)
			else askPermission()
		}
	}

	private fun gotToFragment(fragment: Fragment) {
		supportFragmentManager.beginTransaction()
			.replace(R.id.fragmentContainerView, fragment)
			.commit()
	}

	fun showGoToAppSettingsDialog(context: Context) {
		MaterialAlertDialogBuilder(context, R.style.CustomAlertDialog)
			.setTitle(R.string.grant_permissions)
			.setMessage(R.string.we_need_permission)
			.setPositiveButton(R.string.grant) { _, _ ->
				goToAppSettings()
				finish()
			}
			.setNegativeButton(R.string.cancel) { _, _ ->
				finish()
			}
			.show()
	}

	private fun goToAppSettings() {
		val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
							Uri.fromParts("package", packageName, null))
		intent.addCategory(Intent.CATEGORY_DEFAULT)
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		startActivity(intent)
	}
}

