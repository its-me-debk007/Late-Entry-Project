package `in`.silive.lateentryproject.ui.activities

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.ui.fragments.BarcodeFragment
import `in`.silive.lateentryproject.ui.fragments.LoginFragment
import `in`.silive.lateentryproject.utils.Datastore
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var datastore: Datastore

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        datastore = Datastore(this)

        lifecycleScope.launch {
            if (datastore.isLogin()) {
                askPermission()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, LoginFragment())
                    .commit()
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
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, BarcodeFragment())
                .commit()
        } else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
                showGoToAppSettingsDialog(this)
            else askPermission()
        }
    }

    private fun showGoToAppSettingsDialog(context: Context) {
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
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
