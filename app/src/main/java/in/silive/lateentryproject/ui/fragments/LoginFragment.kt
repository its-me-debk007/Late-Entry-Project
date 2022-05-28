package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.Utils
import `in`.silive.lateentryproject.databinding.FragmentLoginBinding
import `in`.silive.lateentryproject.models.Datastore
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.ui.activities.MainActivity
import `in`.silive.lateentryproject.view_models.LoginViewModel
import android.Manifest
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    lateinit var datastore: Datastore
    private lateinit var venue: MutableMap<Int, String>
    private lateinit var venue2: Map<Int, String>
    private var change: Boolean? = null
    private val viewModel by lazy { ViewModelProvider(this@LoginFragment)[LoginViewModel::class.java] }
    private val bulkViewModel by lazy {

        ViewModelProvider(
            this,
            BulkDataViewModelFactory(StudentDatabase.getDatabase(requireContext()))
        )[BulkDataViewModel::class.java]
    }
	requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        venue = HashMap()
        venue2 = HashMap()
        datastore = Datastore(requireContext())
        lifecycleScope.launch{
            Toast.makeText(context, datastore.isSync().toString(), Toast.LENGTH_SHORT).show()
        }
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
                try {
                    change = datastore.isSync()
                } finally {
                    if (change == true) {
                        bulkViewModel.sendResult()
                        bulkViewModel._bulkDataResult.observe(viewLifecycleOwner) {
                            when (it) {
                                is Response.Success -> {
                                    for (i in it.data?.venue_data!!) {
                                        venue.put(i.id!!, i.venue!!)
                                    }

                                    lifecycleScope.launch {
                                        datastore.changeSyncState(false)
                                        datastore.saveVenueDetails("VENUE_KEY", venue)
                                        val map =
                                            datastore.getVenueDetails("VENUE_KEY")
                                                ?.replace("\\s".toRegex(), "")!!
                                                .split(",").associateTo(HashMap()) {
                                                    val (left, right) = it.split("=")
                                                    left.toInt() to right
                                                }
                                        venue2 = map
                                        datastore.saveId("ID_KEY", venue2.keys.toTypedArray()[0])
                                        datastore.saveDefaultVenue("DEFAULT_VENUE_KEY", venue2.values.toTypedArray()[0])
                                    }
                                }
                                is Response.Error ->
                                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT)
                                        .show()
                            }
                        }
                    }
                }
            }

        }
    }

        private fun disableViews(bool: Boolean) {
            binding.apply {
                emailTextInputLayout.isEnabled = !bool
                passwordTextInputLayout.isEnabled = !bool
                loginBtn.isEnabled = !bool

                if (bool) {
                    progressBar.visibility = View.VISIBLE
                    loginBtn.setTextColor(Color.parseColor("#7191A1"))
                } else {
                    progressBar.visibility = View.INVISIBLE
                    loginBtn.setTextColor(Color.parseColor("#0077B6"))
                    password.text?.clear()
                }
            }
        }

        private fun askPermission() {
            requestPermission.launch(Manifest.permission.CAMERA)
        }

        private fun gotToBarcodeFragment() {
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

}