package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.ui.activities.MainActivity
import `in`.silive.lateentryproject.utils.Datastore
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class SplashScreenFragment: Fragment(R.layout.activity_splash_screen) {
	lateinit var datastore: Datastore

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		datastore = Datastore(requireContext())

		lifecycleScope.launch {
			if (datastore.isLogin()) askPermission()
			else {
				Handler(Looper.getMainLooper()).postDelayed({
																goToNextFragment(LoginFragment())
															}, 1400)

			}
		}
	}

	private fun askPermission() {
		requestPermission.launch(Manifest.permission.CAMERA)
	}

	private val requestPermission = registerForActivityResult(
		ActivityResultContracts.RequestPermission()
	) {
		if (it) {
			Handler(Looper.getMainLooper()).postDelayed({
															goToNextFragment(BarcodeFragment())
														}, 1500)
		} else {
			if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
				showGoToAppSettingsDialog(requireContext())
			else askPermission()
		}
	}

	private fun showGoToAppSettingsDialog(context: Context) {
		MaterialAlertDialogBuilder(context, R.style.CustomAlertDialog)
			.setTitle(R.string.grant_permissions)
			.setMessage(R.string.we_need_permission)
			.setPositiveButton(R.string.grant) { _, _ ->
				goToAppSettings()
				activity?.finish()
			}
			.setNegativeButton(R.string.cancel) { _, _ ->
				activity?.finish()
			}
			.show()
	}

	private fun goToAppSettings() {
		val intent = Intent(
			Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
			Uri.fromParts("package", activity?.packageName, null)
		)
		intent.addCategory(Intent.CATEGORY_DEFAULT)
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		startActivity(intent)
	}

	private fun goToNextFragment(fragment: Fragment) {
		activity?.supportFragmentManager?.beginTransaction()
			?.setCustomAnimations(R.anim.fade_in, R.anim.long_fade_out)
			?.replace(R.id.fragmentContainerView, fragment)
			?.commit()
	}
}