package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.FragmentSettingsBinding
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment(R.layout.fragment_settings) {
	private lateinit var binding: FragmentSettingsBinding

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding = FragmentSettingsBinding.bind(view)
		binding.apply {

			backBtn.setOnClickListener { goToNextFragment(BarcodeFragment()) }

			logoutConstraintLayout.setOnClickListener { goToNextFragment(LoginFragment()) }
		}
	}

	private fun goToNextFragment(fragment: Fragment) {
		activity?.supportFragmentManager?.beginTransaction()
			?.setCustomAnimations(R.anim.fade_in, R.anim.slide_out)
			?.replace(R.id.fragmentContainerView, fragment)
			?.commit()
	}
}