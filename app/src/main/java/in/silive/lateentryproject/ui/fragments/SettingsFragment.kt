package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.FragmentSettingsBinding
import `in`.silive.lateentryproject.entities.OfflineLateEntry
import `in`.silive.lateentryproject.models.BulkReqDataClass
import `in`.silive.lateentryproject.room_database.StudentDatabase
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.utils.Datastore
import `in`.silive.lateentryproject.utils.Utils
import `in`.silive.lateentryproject.view_model_factories.BulkDataViewModelFactory
import `in`.silive.lateentryproject.view_models.BulkDataViewModel
import `in`.silive.lateentryproject.view_models.FailedEntriesViewModel
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var binding: FragmentSettingsBinding
    private val datastore by lazy { Datastore(requireContext()) }
    private lateinit var toast: Toast
    private val bulkViewModel by lazy {
        ViewModelProvider(
            this,
            BulkDataViewModelFactory(StudentDatabase.getDatabase(requireContext()))
        )[BulkDataViewModel::class.java]
    }
    private val viewModel by lazy { ViewModelProvider(this)[FailedEntriesViewModel::class.java] }
    private val studentDatabase by lazy { StudentDatabase.getDatabase(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        toast=Toast.makeText(context, "", Toast.LENGTH_SHORT)
        binding.apply {

            lifecycleScope.launch {
                lastSyncTime.text = "Last synced: ${datastore.getSyncTime()}"
                lastUploadTime.text = "Last uploaded: ${datastore.getUploadTime()}"
            }

            backBtn.setOnClickListener { goToNextFragment(BarcodeFragment()) }

            logoutBtn.setOnClickListener { showLogoutDialog() }

            syncBtn.setOnClickListener {
                disableBtn(syncBtn, true)
                bulkViewModel.sendResult()
                bulkViewModel._bulkDataResult.observe(viewLifecycleOwner) {
                    if (it is Response.Success) {
                        val venueMap = mutableMapOf<Int, String>()

                        it.data?.venue_data!!.forEach { venueData ->
                            venueMap[venueData.id] = venueData.venue
                        }

                        lifecycleScope.launch {
                            datastore.saveVenueDetails(venueMap)
                            val venue =
                                datastore.getVenueDetails()
                                    ?.replace("\\s".toRegex(), "")!!
                                    .split(",").associateTo(mutableMapOf()) { str ->
                                        val (left, right) = str.split("=")
                                        left.toInt() to right
                                    }
                            datastore.saveId("ID_KEY", venue.keys.toTypedArray()[0])

                            val currentTime = Utils().currentTime()
                            datastore.saveSyncTime(Utils().currentTime())
                            lastSyncTime.text = "Last synced: $currentTime"
                        }

                        showToast("Data synced successfully")

                    }
                    else if (it is Response.Error) it.errorMessage?.let { it1 -> showToast(it1) }

                    disableBtn(syncBtn, false)
                }
            }

            uploadBtn.setOnClickListener {
                disableBtn(uploadBtn, true)

                var lateEntryList: List<OfflineLateEntry>
                lifecycleScope.launch {
                    lateEntryList = studentDatabase.offlineLateEntryDao().getLateEntryDetails()
                    viewModel.bulkUpload(BulkReqDataClass(lateEntryList))
                    viewModel.bulkLiveData.observe(viewLifecycleOwner) {
                        if (it is Response.Success) {
                            showToast("Data uploaded successfully")

                            lifecycleScope.launch {
                            if (it.data?.result?.failed == 0)
                                    studentDatabase.offlineLateEntryDao().clearLateEntryTable()

                                val currentTime = Utils().currentTime()
                                datastore.saveUploadTime(currentTime)
                                lastUploadTime.text = "Last uploaded: $currentTime"
                            }
                        }
                        else it.errorMessage?.let { errorMessage -> showToast(errorMessage) }

                        disableBtn(uploadBtn, false)
                    }
                }
            }
        }
    }

    private fun showLogoutDialog() {
        val customView = layoutInflater.inflate(R.layout.logout_dialog, null)
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setView(customView)
        builder.background = ColorDrawable(Color.TRANSPARENT)
        val dialog = builder.show()

        val logout = customView.findViewById<MaterialButton>(R.id.logout)
        val cancel = customView.findViewById<MaterialButton>(R.id.cancel)

        logout.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                try {
                    datastore.changeLoginState(false)
                } finally {
                    goToNextFragment(LoginFragment())
                }
            }
            dialog.dismiss()
        }

        cancel.setOnClickListener { dialog.dismiss() }
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

    private fun disableBtn(btn: MaterialButton, bool: Boolean) {
        btn.isEnabled = !bool

        if (bool) {
            btn.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.disabledSettingsBtnColor
                )
            )
            btn.setIconTintResource(R.color.disabledSettingsBtnColor)
            binding.progressBar.visibility = View.VISIBLE
        } else {
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.custom_blue))
            btn.setIconTintResource(R.color.custom_blue)
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun showToast(text: String) {
        toast.cancel() // cancel previous toast
        toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onPause() {
        super.onPause()
        toast.cancel()
    }
}