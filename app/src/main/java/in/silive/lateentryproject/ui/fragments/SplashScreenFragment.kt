package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.SplashScreenBinding
import `in`.silive.lateentryproject.utils.Datastore
import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class SplashScreenFragment : Fragment(R.layout.splash_screen) {
    private lateinit var datastore: Datastore
    private lateinit var binding: SplashScreenBinding

    companion object {
        var ACCESS_TOKEN: String? = null
        var REFRESH_TOKEN: String? = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SplashScreenBinding.bind(view)
        datastore = Datastore(requireContext())
        lifecycleScope.launch {
            ACCESS_TOKEN = datastore.getAccessToken()
            REFRESH_TOKEN = datastore.getRefreshToken()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch {
                goToNextFragment(if (datastore.loginStatus() == "no") LoginFragment() else BarcodeFragment())
            }
        }, 700)
    }

    private fun askPermission() {
        requestPermission.launch(Manifest.permission.CAMERA)
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) goToNextFragment(BarcodeFragment())
        else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
                showGoToAppSettingsDialog(requireContext())
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
            activity?.finishAffinity()
        }

        cancel.setOnClickListener { activity?.finishAffinity() }
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
            ?.replace(R.id.fragmentContainerView, fragment)
            ?.commit()
    }
}