package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.FragmentLoginBinding
import `in`.silive.lateentryproject.room_database.StudentDatabase
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.utils.Datastore
import `in`.silive.lateentryproject.utils.Utils
import `in`.silive.lateentryproject.view_model_factories.BulkDataViewModelFactory
import `in`.silive.lateentryproject.view_models.BulkDataViewModel
import `in`.silive.lateentryproject.view_models.LoginViewModel
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var toast:Toast
    private val datastore by lazy { Datastore(requireContext()) }
    private val viewModel by lazy { ViewModelProvider(this@LoginFragment)[LoginViewModel::class.java] }
    private val bulkViewModel by lazy {
        ViewModelProvider(
            this,
            BulkDataViewModelFactory(StudentDatabase.getDatabase(requireContext()))
        )[BulkDataViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        toast=Toast.makeText(context, "", Toast.LENGTH_SHORT)

        binding.apply {
            loginBtn.setOnClickListener {

                emailTextInputLayout.helperText = null
                passwordTextInputLayout.helperText = null

                if (email.text?.trim().isNullOrEmpty()) {
                    emailTextInputLayout.helperText = "Please enter an email"
                    return@setOnClickListener
                } else if (password.text?.trim().isNullOrEmpty()) {
                    passwordTextInputLayout.helperText = "Please enter an password"
                    return@setOnClickListener
                }

                Utils().hideKeyboard(requireView(), activity)
                disableViews(true)

                viewModel.login(email.text?.trim().toString(), password.text?.trim().toString())

                viewModel.loginLiveData.observe(viewLifecycleOwner) {
                    disableViews(false)


                    if (it is Response.Success) {
                        lifecycleScope.launch {
                            datastore.changeLoginState(true)
                            askPermission()
                        }
                    } else if (it is Response.Error) {
                        val snackBar = Snackbar.make(
                            loginBtn, it.errorMessage!!, Snackbar
                                .LENGTH_SHORT
                        )
                        snackBar.apply {
                            setAction(R.string.ok_btn_snackbar) {
                                dismiss()
                            }
                            animationMode = Snackbar.ANIMATION_MODE_SLIDE
                            show()
                        }
                    }
                }
            }


            lifecycleScope.launchWhenStarted {
                    if (datastore.isSync()) {
                        bulkViewModel.sendResult()
                        bulkViewModel._bulkDataResult.observe(viewLifecycleOwner) {
                            if (it is Response.Success) {
                                val venueMap = mutableMapOf<Int, String>()

                                it.data?.venue_data!!.forEach { venueData ->
                                    venueMap[venueData.id] = venueData.venue
                                }

                                lifecycleScope.launch {
                                    datastore.changeSyncState(false)
                                    datastore.saveVenueDetails(venueMap)
                                    val venue =
                                        datastore.getVenueDetails()
                                            ?.replace("\\s".toRegex(), "")!!
                                            .split(",").associateTo(mutableMapOf()) { str ->
                                                val (left, right) = str.split("=")
                                                left.toInt() to right
                                            }
                                    datastore.saveId("ID_KEY", venue.keys.toTypedArray()[0])
                                    datastore.saveDefaultVenue(venue.values.toTypedArray()[0])
                                }
                            }
                            else if (it is Response.Error) it.errorMessage?.let { it1 -> showToast(it1) }
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
                loginBtn.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.disabledSettingsBtnColor
                    )
                )
            } else {
                progressBar.visibility = View.INVISIBLE
                loginBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.custom_blue))
                password.text?.clear()
            }
        }
    }

    private fun askPermission() {
        requestPermission.launch(Manifest.permission.CAMERA)
    }

    private fun gotToBarcodeFragment() {
        activity?.supportFragmentManager?.beginTransaction()
            ?.setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
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

    override fun onPause() {
        super.onPause()
        toast.cancel()
    }
}