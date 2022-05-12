package `in`.silive.lateentryproject

import `in`.silive.lateentryproject.ui.BarcodeFragment
import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		requestPermission.launch(
			Manifest.permission.CAMERA
		)

	}



	private fun gotToMainActivity() {
		val transaction = supportFragmentManager.beginTransaction()
		transaction.replace(R.id.container, BarcodeFragment())
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
	}
}