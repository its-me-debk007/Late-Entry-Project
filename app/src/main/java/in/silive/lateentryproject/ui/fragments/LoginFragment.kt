package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.FragmentLoginBinding
import `in`.silive.lateentryproject.room_database.StudentDatabase
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.utils.Datastore
import `in`.silive.lateentryproject.utils.currentTime
import `in`.silive.lateentryproject.utils.hideKeyboard
import `in`.silive.lateentryproject.view_model_factories.BulkDataViewModelFactory
import `in`.silive.lateentryproject.view_models.BulkDataViewModel
import `in`.silive.lateentryproject.view_models.LoginViewModel
import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var toast: Toast
    private val datastore by lazy { Datastore(requireContext()) }
    private val viewModel by lazy { ViewModelProvider(this@LoginFragment)[LoginViewModel::class.java] }
    private val bulkViewModel by lazy {
        ViewModelProvider(
            this,
            BulkDataViewModelFactory(StudentDatabase.getDatabase(requireContext()))
        )[BulkDataViewModel::class.java]
    }
    private var dialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { it.window.decorView.systemUiVisibility = 0 }
        binding = FragmentLoginBinding.bind(view)
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT)

        binding.apply {
            loginBtn.setOnClickListener {

                emailTextInputLayout.helperText = null
                passwordTextInputLayout.helperText = null

                if (email.text?.trim().isNullOrEmpty()) {
                    emailTextInputLayout.helperText = "Please enter an email"
                    return@setOnClickListener
                } else if (password.text?.trim().isNullOrEmpty()) {
                    passwordTextInputLayout.helperText = "Please enter a password"
                    return@setOnClickListener
                }

                hideKeyboard(requireView(), activity)
                disableViews(true)

                viewModel.login(email.text?.trim().toString(), password.text?.trim().toString())

                viewModel.loginLiveData.observe(viewLifecycleOwner) { loginResponse ->

                    if (loginResponse is Response.Success) {
                        SplashScreenFragment.ACCESS_TOKEN = loginResponse.data!!.access
                        SplashScreenFragment.REFRESH_TOKEN = loginResponse.data.refresh
                        lifecycleScope.launch {

                            datastore.saveRefreshToken(loginResponse.data.refresh!!)
                            datastore.saveAccessToken(loginResponse.data.access!!)

                            if (datastore.isSync()) {
                                bulkViewModel.sendResult(requireContext())
                                bulkViewModel._bulkDataResult.observe(viewLifecycleOwner) {

                                    if (it is Response.Success) {
                                        val venueMap = mutableMapOf<Int, String>()

                                        it.data?.venue_data!!.forEach { venueData ->
                                            venueMap[venueData.id] = venueData.venue
                                        }
                                        lifecycleScope.launch {
                                            datastore.changeSyncState(false)
                                            datastore.saveVenueDetails(venueMap)
                                            val venue = datastore.getVenueDetails()
                                                ?.replace("\\s".toRegex(), "")!!
                                                .split(",")
                                                .associateTo(mutableMapOf()) { str ->
                                                    val (left, right) = str.split("=")
                                                    left.toInt() to right
                                                }
                                            datastore.saveId(
                                                "ID_KEY",
                                                venue.keys.toTypedArray()[0]
                                            )
                                            datastore.saveDefaultVenue(venue.values.toTypedArray()[0])
                                            datastore.saveSyncTime(currentTime())
                                            datastore.changeLoginState(
                                                loginResponse.data.type ?: "other"
                                            )
                                            disableViews(false)
                                            askPermission()
                                        }
                                    } else if (it is Response.Error) it.errorMessage?.let { it1 ->
                                        showToast(it1)
                                    }
                                }
                            } else {
                                datastore.changeLoginState(loginResponse.data.type ?: "other")
                                disableViews(false)
                                askPermission()
                            }
                        }
                    } else if (loginResponse is Response.Error) {
                        val snackBar = Snackbar.make(
                            loginBtn, loginResponse.errorMessage!!, Snackbar
                                .LENGTH_SHORT
                        )
                        snackBar.apply {
                            setAction(R.string.ok_btn_snackbar) {
                                dismiss()
                            }
                            animationMode = Snackbar.ANIMATION_MODE_SLIDE
                            show()
                        }
                        disableViews(false)
                    }
                }
            }
        }
    }

    private fun showToast(text: String) {
        toast.cancel()
        toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun disableViews(bool: Boolean) {
        binding.apply {
            emailTextInputLayout.isEnabled = !bool
            passwordTextInputLayout.isEnabled = !bool
            loginBtn.isEnabled = !bool

            if (bool) {
                progressBar.visibility = View.VISIBLE
                loginBtn.text = ""
            } else {
                progressBar.visibility = View.INVISIBLE
                loginBtn.text = "Login"
                password.text?.clear()
            }
        }
    }

    private fun askPermission() {
        requestPermission.launch(Manifest.permission.CAMERA)
    }

    private fun gotToBarcodeFragment() {
        dialog?.dismiss()
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragmentContainerView, BarcodeFragment())
            ?.commit()
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            gotToBarcodeFragment()
        } else {
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

    override fun onPause() {
        super.onPause()
        toast.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })
    }

    private fun showExitDialog() {
        val customView = layoutInflater.inflate(R.layout.dialog, null)
        val builder = MaterialAlertDialogBuilder(requireContext()).apply {
            setView(customView)
            background = ColorDrawable(Color.TRANSPARENT)
        }
        dialog = builder.show()

        val exit = customView.findViewById<MaterialButton>(R.id.positiveBtn)
        val cancel = customView.findViewById<MaterialButton>(R.id.cancel)

        exit.setOnClickListener { activity?.finishAffinity() }

        cancel.setOnClickListener { dialog?.dismiss() }
    }

}