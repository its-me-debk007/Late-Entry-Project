package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.FragmentSettingsBinding
import `in`.silive.lateentryproject.models.BulkReqDataClass
import `in`.silive.lateentryproject.models.LateEntryDataClass
import `in`.silive.lateentryproject.room_database.StudentDatabase
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.ui.activities.WebViewActivity
import `in`.silive.lateentryproject.utils.Datastore
import `in`.silive.lateentryproject.utils.currentTime
import `in`.silive.lateentryproject.utils.isDialogShown
import `in`.silive.lateentryproject.view_model_factories.BulkDataViewModelFactory
import `in`.silive.lateentryproject.view_models.BulkDataViewModel
import `in`.silive.lateentryproject.view_models.FailedEntriesViewModel
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            activity?.window?.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        else activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        binding = FragmentSettingsBinding.bind(view)
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT)
        binding.apply {

            lifecycleScope.launch {
                lastSyncTime.text = "Last synced: ${datastore.getSyncTime()}"
                lastUploadTime.text =
                    "Failed entries count: ${studentDatabase.offlineLateEntryDao().getCount()}"
            }

            backBtn.setOnClickListener { goToNextFragment(BarcodeFragment()) }

            changePasswordBtn.setOnClickListener {
                goToWebViewActivity("CHANGE_PASSWORD")
            }

            staffPanelBtn.setOnClickListener {
                goToWebViewActivity("STAFF")
            }

            logoutBtn.setOnClickListener {
                it.isEnabled = false
                showLogoutDialog()
                logoutBtn.postDelayed({
                    it.isEnabled = true
                }, 300)
            }

            lifecycleScope.launch {
                if (datastore.loginStatus() == "staff") staffPanelBtn.visibility = View.VISIBLE
            }

            syncBtn.setOnClickListener {
//				lifecycleScope.launch {
//					studentDatabase.studentDao().clearStudentTable()
//				}
                disableBtn(true, syncBtn, 'S')
                context?.let { it1 -> bulkViewModel.sendResult(it1) }
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

                            val currentTime = currentTime()
                            datastore.saveSyncTime(currentTime)
                            lastSyncTime.text = "Last synced: $currentTime"
                        }

                        showToast("Data fetched successfully")

                    } else if (it is Response.Error) it.errorMessage?.let { it1 -> showToast(it1) }

                    disableBtn(false, syncBtn, 'S')
                }
            }

            uploadBtn.setOnClickListener {
                disableBtn(true, uploadBtn, 'U')

                val entries = mutableListOf<LateEntryDataClass>()
                runBlocking {
                    studentDatabase.offlineLateEntryDao().getLateEntryDetails().forEach {
                        entries.add(LateEntryDataClass(it.student_no!!, it.timestamp!!, it.venue!!))
                    }
                }
                if (entries.size == 0) {
                    showToast("No failed entries")
                    disableBtn(false, uploadBtn, 'U')
                } else {
                    context?.let { it1 -> viewModel.bulkUpload(BulkReqDataClass(entries), it1) }
                    viewModel._bulkLiveData.observe(viewLifecycleOwner) {
                        if (it is Response.Success) {
                            lifecycleScope.launch {
                                studentDatabase.offlineLateEntryDao().clearLateEntryTable()
                                lastUploadTime.text = "Failed entries count: ${
                                    studentDatabase.offlineLateEntryDao().getCount()
                                }"
                            }
                            showToast("Failed entries registered")
                        } else it.errorMessage?.let { errorMessage ->
                            showToast(
                                if (errorMessage == "No Internet")
                                    "No Internet connection" else errorMessage
                            )
                        }

                        disableBtn(false, uploadBtn, 'U')
                    }

                }

            }
        }
    }

    private fun showLogoutDialog() {
        val customView = layoutInflater.inflate(R.layout.dialog, null)
        val builder = MaterialAlertDialogBuilder(requireContext()).apply {
            setView(customView)
            background = ColorDrawable(Color.TRANSPARENT)
            setCancelable(false)
        }
        isDialogShown = true
        val dialog = builder.show()

        customView.findViewById<MaterialTextView>(R.id.dialogMessage)
            .setText(R.string.logoutMessage)
        val logout = customView.findViewById<MaterialButton>(R.id.positiveBtn)
        val cancel = customView.findViewById<MaterialButton>(R.id.cancel)
        val progressBar = customView.findViewById<CircularProgressIndicator>(R.id.progressBar)

        logout.text = "Logout"
        logout.setOnClickListener {
            lifecycleScope.launch {
                val entries = mutableListOf<LateEntryDataClass>()
                studentDatabase.offlineLateEntryDao().getLateEntryDetails().forEach {
                    entries.add(LateEntryDataClass(it.student_no!!, it.timestamp!!, it.venue!!))
                }
                if (entries.size != 0) {
                    logout.text = ""
                    progressBar.visibility = View.VISIBLE
                    context?.let { it1 -> viewModel.bulkUpload(BulkReqDataClass(entries), it1) }
                    viewModel._bulkLiveData.observe(viewLifecycleOwner) {
                        lifecycleScope.launch {
                            if (it is Response.Success) {
                                studentDatabase.offlineLateEntryDao().clearLateEntryTable()
                                if (isDialogShown) {
                                    datastore.saveAccessToken("_")
                                    datastore.saveRefreshToken("_")
                                    datastore.changeLoginState("no")
                                    goToNextFragment(LoginFragment())
                                    dialog.dismiss()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    if (it.errorMessage?.substring(
                                            0,
                                            2
                                        ) == "No"
                                    ) "Unable to register failed entries! No or poor internet connection"
                                    else it.errorMessage, Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                            }
                        }
                    }
                } else {
                    if (isDialogShown) {
                        datastore.saveAccessToken("_")
                        datastore.saveRefreshToken("_")
                        datastore.changeLoginState("no")
                        goToNextFragment(LoginFragment())
                        dialog.dismiss()
                    }
                }
            }
        }

        cancel.setOnClickListener {
            isDialogShown = false
            dialog.dismiss()
        }
    }

    private fun goToNextFragment(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragmentContainerView, fragment)
            ?.commit()
    }

    private fun goToWebViewActivity(option: String) {
        Intent(context, WebViewActivity::class.java).also {
            it.putExtra("WEBSITE", option)
            startActivity(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goToNextFragment(BarcodeFragment())
            }
        })
    }

    private fun disableBtn(bool: Boolean, btn: MaterialButton, type: Char) {
        btn.isEnabled = !bool
        val progressBar = if (type == 'S') binding.syncProgressBar
        else binding.uploadProgressBar
        if (bool) {
            btn.text = ""
            btn.setIconTintResource(R.color.colorTransparent)
            progressBar.show()
        } else {
            btn.text = if (type == 'S') "Sync Data" else "Upload Data"
            btn.setIconTintResource(R.color.custom_blue)
            progressBar.hide()
        }
    }

    private fun showToast(text: String) {
        toast.cancel()
        toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onPause() {
        super.onPause()
        toast.cancel()
    }
}