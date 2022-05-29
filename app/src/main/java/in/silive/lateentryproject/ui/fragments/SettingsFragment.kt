package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.FragmentSettingsBinding
import `in`.silive.lateentryproject.room_database.StudentDatabase
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.models.BulkReqDataClass
import `in`.silive.lateentryproject.models.LateEntryDataClass
import `in`.silive.lateentryproject.utils.Datastore
import `in`.silive.lateentryproject.utils.Utils
import `in`.silive.lateentryproject.view_model_factories.BulkDataViewModelFactory
import `in`.silive.lateentryproject.view_models.BulkDataViewModel
import `in`.silive.lateentryproject.view_models.FailedEntriesViewModel
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import kotlinx.coroutines.launch
import java.io.File

class SettingsFragment : Fragment(R.layout.fragment_settings) {
	private lateinit var binding: FragmentSettingsBinding
	private lateinit var datastore: Datastore
	private lateinit var venue: MutableMap<Int, String>
	private lateinit var venue2: Map<Int, String>
	private val bulkViewModel by lazy { ViewModelProvider(this,
		BulkDataViewModelFactory(StudentDatabase.getDatabase(requireContext()))
	)[BulkDataViewModel::class.java]
	}
	private val viewModel by lazy { ViewModelProvider(this)[FailedEntriesViewModel::class.java] }
	private lateinit var studentDatabase : StudentDatabase

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding = FragmentSettingsBinding.bind(view)
		venue = HashMap()
		venue2 = HashMap()
		datastore = Datastore(requireContext())
		studentDatabase = StudentDatabase.getDatabase(requireContext())

		binding.apply {

			backBtn.setOnClickListener { goToNextFragment(BarcodeFragment()) }

			logoutConstraintLayout.setOnClickListener {
				lifecycleScope.launch {
					datastore.changeLoginState(false)
				}
				goToNextFragment(LoginFragment())
			}

			syncBtn.setOnClickListener {
				bulkViewModel.sendResult()
				bulkViewModel._bulkDataResult.observe(viewLifecycleOwner) {
					when (it) {
						is Response.Success -> {
							for (i in it.data?.venue_data!!) {
								venue.put(i.id!!, i.venue!!)
							}

							lifecycleScope.launch {
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
							for (data in it.data.student_data) {
								data.student_image?.let { imgUrl ->
									Utils().download(activity, imgUrl, data.student_no)
								}
							}
						}
						is Response.Error ->
							Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT)
								.show()
					}
				}
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
						Toast.makeText(context, "Data uploaded successfully", Toast.LENGTH_SHORT)
							.show()
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

//			storage.setOnClickListener {
//				val file = File(context?.filesDir, "")
//				if (file.exists()){
//					Log.e("dddd", file.path)
//					for (f in file.listFiles()!!) {
//						Log.e("dddd", f.name)
//					}
//				} else 	Log.e("dddd", "No")
//
//				img.load(file)
//			}
		}
	}

	private fun goToNextFragment(fragment: Fragment) {
		activity?.supportFragmentManager?.beginTransaction()
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