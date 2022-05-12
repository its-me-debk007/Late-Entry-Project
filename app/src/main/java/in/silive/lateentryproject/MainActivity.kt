package `in`.silive.lateentryproject

import `in`.silive.lateentryproject.databinding.ActivityMainBinding
import `in`.silive.lateentryproject.ui.BarcodeFragment
import `in`.silive.lateentryproject.ui.BottomSheetFragment
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		requestPermission.launch(Manifest.permission.CAMERA)

	}

	private fun gotToMainActivity() {
		val transaction = supportFragmentManager.beginTransaction()
		transaction.replace(R.id.fragmentContainerView, BarcodeFragment())
		transaction.commit()
	}

	private val requestPermission = registerForActivityResult(
		ActivityResultContracts
			.RequestPermission()
	) {
		when (it) {
			true -> {
				gotToMainActivity()
			}
			false -> {
				showGoToAppSettingsDialog()
			}
		}
	}

	private fun showGoToAppSettingsDialog() {
		AlertDialog.Builder(this, R.style.CustomAlertDialog)
			.setTitle(getString(R.string.grant_permissions))
			.setMessage(getString(R.string.we_need_permission))
			.setPositiveButton(getString(R.string.grant)) { _, _ ->
				goToAppSettings()
			}
			.setNegativeButton(getString(R.string.cancel)) { _, _ ->
				run {
					finish()
				}
			}.show()
	}

	private fun goToAppSettings() {
		val intent = Intent(
			Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
			Uri.fromParts("package", packageName, null)
		)
		intent.addCategory(Intent.CATEGORY_DEFAULT)
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		startActivity(intent)
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

		binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

		val modalBottomSheet = BottomSheetFragment()
		modalBottomSheet.show(supportFragmentManager, "BottomSheet")

	}
}