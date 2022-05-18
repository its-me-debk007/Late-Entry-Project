package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.FragmentLoginBinding
import `in`.silive.lateentryproject.viewModels.LoginViewModel
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment(R.layout.fragment_login) {
	private lateinit var binding: FragmentLoginBinding

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

				val viewModel = ViewModelProvider(this@LoginFragment)[LoginViewModel::class.java]

				viewModel.login(email.text?.trim().toString(), password.text?.trim().toString())

				viewModel.loginLiveData.observe(viewLifecycleOwner) { response ->
					Snackbar.make(loginBtn, response, Snackbar.LENGTH_SHORT).show()
				}
			}
		}
	}

	private fun hideKeyboard(view: View) {
		val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(view.windowToken, 0)
	}
}