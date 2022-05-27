package `in`.silive.lateentryproject.ui.activities

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.ActivityMainBinding
import `in`.silive.lateentryproject.models.Datastore
import `in`.silive.lateentryproject.ui.fragments.BarcodeFragment
import `in`.silive.lateentryproject.ui.fragments.LoginFragment
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var datastore:Datastore

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        datastore = Datastore(this@MainActivity)
        askPermission()
    }
    private fun askPermission() {
        requestPermission.launch(Manifest.permission.CAMERA)
    }

    private fun gotToBarcodeFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.fade_out)
            .replace(R.id.fragmentContainerView, BarcodeFragment())
            .commit()
    }
    private fun gotToLoginFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.fade_out)
            .replace(R.id.fragmentContainerView, LoginFragment())
            .commit()
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            lifecycleScope.launch{
                if (datastore.isLogin()) {
                    gotToBarcodeFragment()
                }
                else{
                    gotToLoginFragment()
                }
            }

        }
        else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
                showGoToAppSettingsDialog()
            else askPermission()
        }
    }

    private fun showGoToAppSettingsDialog() {
        AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setTitle(getString(R.string.grant_permissions))
            .setMessage(getString(R.string.we_need_permission))
            .setPositiveButton(getString(R.string.grant)) { _, _ ->
                goToAppSettings()
                finish()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
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

