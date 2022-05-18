package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.FragmentLoginBinding
import `in`.silive.lateentryproject.view_models.LoginViewModel
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

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

				hideKeyboard(requireView())
				disableViews(true)

				viewModel.login(email.text?.trim().toString(), password.text?.trim().toString())

				viewModel.loginLiveData.observe(viewLifecycleOwner) { response ->
					if (response != "Successfully logged in") {
						Toast.makeText(context, response, Toast.LENGTH_SHORT).show()
						disableViews(false)
					} else activity?.supportFragmentManager?.beginTransaction()?.replace(
						R.id.fragmentContainerView, BarcodeFragment())?.commit()
				}
			}
		}
	}

	private fun hideKeyboard(view: View) {
		val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(view.windowToken, 0)
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
}