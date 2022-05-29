package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.FragmentSettingsBinding
import `in`.silive.lateentryproject.models.BulkReqDataClass
import `in`.silive.lateentryproject.models.LateEntryDataClass
import `in`.silive.lateentryproject.room_database.StudentDatabase
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.utils.Datastore
import `in`.silive.lateentryproject.view_models.FailedEntriesViewModel
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {
	private lateinit var binding: FragmentSettingsBinding
	private lateinit var datastore: Datastore
	private val viewModel by lazy { ViewModelProvider(this)[FailedEntriesViewModel::class.java] }
	private lateinit var studentDatabase : StudentDatabase

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding = FragmentSettingsBinding.bind(view)
		studentDatabase = StudentDatabase.getDatabase(requireContext())

		binding.apply {

			backBtn.setOnClickListener { goToNextFragment(BarcodeFragment()) }

			logoutConstraintLayout.setOnClickListener {
				datastore = Datastore(requireContext())
				lifecycleScope.launch {
					datastore.changeLoginState(false)
				}
				goToNextFragment(LoginFragment())
			}

			uploadBtn.setOnClickListener {
				uploadBtn.isEnabled = false
				uploadBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.disabledSettingsBtnColor))
				uploadBtn.setIconTintResource(R.color.disabledSettingsBtnColor)

				val lateEntryList = mutableListOf<LateEntryDataClass>()
				studentDatabase.offlineLateEntryDao().getLateEntryDetails().observe(viewLifecycleOwner) {
					for (data in it)
						lateEntryList.add(LateEntryDataClass(data.student_no, data.timestamp, data.venue))

				}

				viewModel.bulkUpload(BulkReqDataClass(lateEntryList))

				viewModel.bulkLiveData.observe(viewLifecycleOwner) {
					if (it is Response.Success) {
						Toast.makeText(context, "Data synced successfully", Toast.LENGTH_SHORT).show()
						lifecycleScope.launch {
							studentDatabase.offlineLateEntryDao().clearLateEntryTable()
						}
					}

					else
						Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()

					uploadBtn.isEnabled = true
					uploadBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.custom_blue))
					uploadBtn.setIconTintResource(R.color.custom_blue)
				}
			}
		}
	}

	private fun goToNextFragment(fragment: Fragment) {
		activity?.supportFragmentManager?.beginTransaction()
			?.setCustomAnimations(R.anim.fade_in, R.anim.slide_out)
			?.replace(R.id.fragmentContainerView, fragment)
			?.commit()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				goToNextFragment(BarcodeFragment())
			}
		})
	}
}