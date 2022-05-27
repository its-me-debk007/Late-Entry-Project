package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.Utils
import `in`.silive.lateentryproject.databinding.FragmentLoginBinding
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.view_models.LoginViewModel
import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment(R.layout.fragment_login) {
	private lateinit var binding: FragmentLoginBinding
	private val viewModel by lazy { ViewModelProvider(this@LoginFragment)[LoginViewModel::class.java] }

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding = FragmentLoginBinding.bind(view)
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

					if (it is Response.Success) askPermission()
					else if (it is Response.Error) {
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

			download.setOnClickListener { Utils().download(activity) }
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
			?.setCustomAnimations(R.anim.slide_in, R.anim.fade_out)
			?.replace(R.id.fragmentContainerView, BarcodeFragment())
			?.commit()
	}

	private val requestPermission =
		registerForActivityResult(ActivityResultContracts.RequestPermission()) {
			if (it) gotToBarcodeFragment()
			else {
				if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
					showGoToAppSettingsDialog()
				else askPermission()
			}
		}

	private fun showGoToAppSettingsDialog() {
		AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
			.setTitle(getString(R.string.grant_permissions))
			.setMessage(R.string.we_need_permission)
			.setPositiveButton(getString(R.string.grant)) { _, _ ->
				goToAppSettings()
				activity?.finish()
			}
			.setNegativeButton(getString(R.string.cancel)) { _, _ ->
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