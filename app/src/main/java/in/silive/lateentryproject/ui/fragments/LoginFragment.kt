package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.utils.Utils
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
	private lateinit var datastore: Datastore
	private val viewModel by lazy { ViewModelProvider(this@LoginFragment)[LoginViewModel::class.java] }
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding = FragmentLoginBinding.bind(view)

		requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

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
						datastore = Datastore(requireContext())
						lifecycleScope.launch {
							datastore.changeLoginState(true)
							askPermission()
						}
					} else if (it is Response.Error) {
						val snackBar = Snackbar.make(loginBtn, it.errorMessage!!, Snackbar
							.LENGTH_SHORT)
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

	private val requestPermission = registerForActivityResult(
		ActivityResultContracts.RequestPermission()
	) {
		if (it) {
			activity?.supportFragmentManager?.beginTransaction()
				?.setCustomAnimations(R.anim.slide_in, R.anim.fade_out)
				?.replace(R.id.fragmentContainerView, BarcodeFragment())
				?.commit()
		}
		else {
			if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
				MainActivity().showGoToAppSettingsDialog(requireContext())

			else askPermission()
		}
	}
}