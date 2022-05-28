package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.Utils
import `in`.silive.lateentryproject.databinding.FragmentSettingsBinding
import `in`.silive.lateentryproject.models.Datastore
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {
	private lateinit var binding: FragmentSettingsBinding
	private lateinit var datastore: Datastore

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding = FragmentSettingsBinding.bind(view)
		binding.apply {

			backBtn.setOnClickListener { goToNextFragment(BarcodeFragment()) }

			logoutConstraintLayout.setOnClickListener {
				datastore = Datastore(requireContext())
				lifecycleScope.launch {
					datastore.changeLoginState(false)
				}
				goToNextFragment(LoginFragment())
			}

			syncBtn.setOnClickListener {

				Utils().download(activity, "/filer_public/2019/compressed_2012107.jpg", "2012107")
			}
		}
	}

	private fun goToNextFragment(fragment: Fragment) {
		activity?.supportFragmentManager?.beginTransaction()
			?.setCustomAnimations(R.anim.fade_in, R.anim.slide_out)
			?.replace(R.id.fragmentContainerView, fragment)
			?.commit()
	}
}