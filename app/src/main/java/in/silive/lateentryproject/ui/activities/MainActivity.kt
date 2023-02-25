package `in`.silive.lateentryproject.ui.activities

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.ui.fragments.BarcodeFragment
import `in`.silive.lateentryproject.ui.fragments.SplashScreenFragment
import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
	private var dialog: AlertDialog? = null

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
	fun askPermission() {
		requestPermission.launch(Manifest.permission.CAMERA)
	}

	private fun gotToBarcodeFragment() {
		dialog?.dismiss()
		supportFragmentManager.beginTransaction()
			.replace(R.id.fragmentContainerView, BarcodeFragment())
			.commit()
	}

	private val requestPermission = registerForActivityResult(
		ActivityResultContracts.RequestPermission()
	) {
		if (it) {
			gotToBarcodeFragment()
		} else {
			if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
				showGoToAppSettingsDialog(this)
			else askPermission()
		}
	}

	private fun showGoToAppSettingsDialog(context: Context) {
		val customView = layoutInflater.inflate(R.layout.camera_permission_dialog, null)

		MaterialAlertDialogBuilder(context)
			.setView(customView)
			.setCancelable(false)
			.setBackground(ColorDrawable(Color.TRANSPARENT))
			.show()

		val grant = customView.findViewById<MaterialButton>(R.id.grant)
		val cancel = customView.findViewById<MaterialButton>(R.id.cancel)

		grant.setOnClickListener {
			goToAppSettings()
			finishAffinity()
		}

		cancel.setOnClickListener { finishAffinity() }
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
	fun showExitDialog(context: Context) {
		val customView = layoutInflater.inflate(R.layout.dialog, null)
		val builder = MaterialAlertDialogBuilder(context).apply {
			setView(customView)
			background = ColorDrawable(Color.TRANSPARENT)
		}
		dialog = builder.show()

		val exit = customView.findViewById<MaterialButton>(R.id.positiveBtn)
		val cancel = customView.findViewById<MaterialButton>(R.id.cancel)

		exit.setOnClickListener { finishAffinity() }

		cancel.setOnClickListener { dialog?.dismiss() }
	}
}
