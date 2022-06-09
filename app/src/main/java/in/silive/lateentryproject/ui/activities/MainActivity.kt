package `in`.silive.lateentryproject.ui.activities

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.ui.fragments.BarcodeFragment
import `in`.silive.lateentryproject.ui.fragments.SplashScreenFragment
import `in`.silive.lateentryproject.utils.Datastore
import `in`.silive.lateentryproject.utils.Utils
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    lateinit var datastore: Datastore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, SplashScreenFragment())
                .commit()
        }
    }
}
