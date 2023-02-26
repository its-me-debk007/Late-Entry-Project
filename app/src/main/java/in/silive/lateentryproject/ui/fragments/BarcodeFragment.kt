package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.adapters.VenueClickListenerInterface
import `in`.silive.lateentryproject.adapters.VenueRecyclerAdapter
import `in`.silive.lateentryproject.connectivity.ConnectivityLiveData
import `in`.silive.lateentryproject.databinding.FragmentBarcodeScannerBinding
import `in`.silive.lateentryproject.entities.OfflineLateEntry
import `in`.silive.lateentryproject.room_database.StudentDatabase
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.utils.*
import `in`.silive.lateentryproject.view_models.LateEntryViewModel
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.launch
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import java.io.File

@Suppress("DEPRECATION")
class BarcodeFragment : Fragment(R.layout.fragment_barcode_scanner), ZBarScannerView.ResultHandler,
    VenueClickListenerInterface {
    private lateinit var binding: FragmentBarcodeScannerBinding
    private val venueBottomSheetDialog by lazy { BottomSheetDialog(requireContext()) }
    private val datastore by lazy { Datastore(requireContext()) }
    private val studentDatabase by lazy { StudentDatabase.getDatabase(requireContext()) }
    private lateinit var venue: MutableMap<Int, String>
    private lateinit var venue2: Map<Int, String>
    private var student_No: String? = null
    private var bool: Boolean = false
    private lateinit var toast: Toast
    private val bottomSheetDialog by lazy { BottomSheetDialog(requireContext()) }
    private val lateEntryViewModel by lazy {
        ViewModelProvider(this)[LateEntryViewModel::class.java]
    }
    private lateinit var seeStudentDetailView: View
    private var isInternetAvailable = true
    private var lastClickTime = 0L
    private var isFirstTime = true
    private var isBottomSheetOpen = false
    private lateinit var scannerView: ZBarScannerView

    private val customView by lazy { layoutInflater.inflate(R.layout.dialog, null) }
    private val builder by lazy {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setView(customView)
            background = ColorDrawable(Color.TRANSPARENT)
        }
    }
    private val dialog by lazy { builder.show() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentBarcodeScannerBinding.bind(view)
        venue = HashMap()
        venue2 = HashMap()
        onClicks()
        initializeCamera()
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT)

        val checkNetworkConnection = context?.let { ConnectivityLiveData(it) }
        checkNetworkConnection?.observe(viewLifecycleOwner) {
            isInternetAvailable = it
            if (it) {
                val animation = AnimationUtils.loadAnimation(context, R.anim.long_fade_out)
                binding.noConnection.startAnimation(animation)
                binding.noConnection.visibility = View.GONE
            } else {
                val animation = AnimationUtils.loadAnimation(context, R.anim.long_fade_in)
                binding.noConnection.startAnimation(animation)
                binding.noConnection.visibility = View.VISIBLE
            }
        }

        binding.enterStudentNoBtn.setOnClickListener {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return@setOnClickListener
            lastClickTime = SystemClock.elapsedRealtime()

            if (binding.enterStudentNoBtn.isEnabled)
                showBottomSheet(null)

            binding.enterStudentNoBtn.isEnabled = false
            binding.enterStudentNoBtn.isClickable = false

            binding.scannerContainer.postDelayed({
                scannerView.stopCamera()
            }, 400)
        }

        binding.setting.setOnClickListener {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return@setOnClickListener
            lastClickTime = SystemClock.elapsedRealtime()

            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(
                    R.id.fragmentContainerView,
                    SettingsFragment()
                )
                ?.commit()
        }

        lifecycleScope.launch {
            venue2 =
                datastore.getVenueDetails()?.replace("\\s".toRegex(), "")!!
                    .split(",").associateTo(HashMap()) {
                        val (left, right) = it.split("=")
                        left.toInt() to right
                    }
            binding.location.text = datastore.getDefaultVenue()
        }
        binding.location.setOnClickListener {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return@setOnClickListener
            lastClickTime = SystemClock.elapsedRealtime()

            val venueItems =
                layoutInflater.inflate(R.layout.layout_venue_bottom_sheet, null)
            venueBottomSheetDialog.setContentView(venueItems)
            venueBottomSheetDialog.show()
            isBottomSheetOpen = true

            binding.scannerContainer.postDelayed({
                scannerView.stopCamera()
            }, 400)

            val adapter =
                VenueRecyclerAdapter(venue2, binding.location.text.toString(), this)
            val recyclerView = venueItems.findViewById<RecyclerView>(R.id.recyclerView)
            recyclerView.adapter = adapter

            venueBottomSheetDialog.setOnCancelListener {
                isBottomSheetOpen = false
                scannerView.setResultHandler(this)
                scannerView.startCamera()
            }
        }

    }

    private fun showToast(text: String) {
        if (!isInternetAvailable) {
            binding.noConnection.visibility = View.INVISIBLE
            binding.noConnection.postDelayed({
                binding.noConnection.visibility = View.VISIBLE
            }, 2200)
        }
        toast.cancel()
        toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun initializeCamera() {
        scannerView = ZBarScannerView(context)
        scannerView.apply {
            setResultHandler(this@BarcodeFragment)
            setBorderColor(ContextCompat.getColor(requireContext(), R.color.white))
            setLaserColor(ContextCompat.getColor(requireContext(), R.color.black))
            setBorderStrokeWidth(13)
            setMaskColor(ContextCompat.getColor(requireContext(), R.color.colorTranslucent))
            setLaserEnabled(true)
            setBorderLineLength(200)
            setupScanner()
            setAutoFocus(true)
            startCamera()
            binding.scannerContainer.addView(this)
        }
    }

    private fun onClicks() {
        binding.flashToggle.setOnClickListener {
            if (binding.flashToggle.isSelected) {
                offFlashLight()
                binding.flashToggle.apply {
                    backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(), R.color.colorTransparent
                        )
                    )
                    imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(), R.color.white
                        )
                    )
                }
            } else {
                binding.flashToggle.apply {
                    backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(), R.color.white
                        )
                    )
                    imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(), R.color.black
                        )
                    )
                }
                onFlashLight()
            }
        }
    }

    private fun onFlashLight() {
        binding.flashToggle.isSelected = true
        scannerView.flash = true
    }

    private fun offFlashLight() {
        binding.flashToggle.isSelected = false
        scannerView.flash = false
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity().checkSelfPermission(Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_DENIED && isFirstTime
        ) {
            isFirstTime = false

            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
                showGoToAppSettingsDialog(requireContext())
            else
                requestPermission.launch(Manifest.permission.CAMERA)
        }

        if (!isBottomSheetOpen) {
            scannerView.setResultHandler(this)
            scannerView.startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        toast.cancel()
        scannerView.stopCamera()
        dialog.dismiss()
    }

    override fun handleResult(rawResult: Result?) {
        student_No = rawResult?.contents.toString()
        bool = true
        rawResult?.contents?.let {
            if (it.length in 2..17) showBottomSheet(rawResult.contents)
            else scannerView.resumeCameraPreview(this)
        }
    }

    private fun showBottomSheet(studentNumber: String?) {
        val enterStudentNoView = layoutInflater.inflate(R.layout.layout_input_bottom_sheet, null)
        seeStudentDetailView =
            layoutInflater.inflate(R.layout.layout_entry_bottom_sheet, null)

        val studentNo = enterStudentNoView.findViewById<TextInputEditText>(R.id.studentNo)
        val okButton = enterStudentNoView.findViewById<MaterialButton>(R.id.okButton)
        val studentNoTextInputLayout =
            enterStudentNoView.findViewById<TextInputLayout>(R.id.studentNoTextInputLayout)
        val studentNoEditText =
            seeStudentDetailView.findViewById<MaterialTextView>(R.id.studentNoEditText)
        val studentNoInputLayout =
            seeStudentDetailView.findViewById<TextInputLayout>(R.id.studentNoInputLayout)
        val submitLateEntryBtn =
            seeStudentDetailView.findViewById<MaterialButton>(R.id.submitLateEntryBtn)
        val name = seeStudentDetailView.findViewById<MaterialTextView>(R.id.name)
        val branch = seeStudentDetailView.findViewById<MaterialTextView>(R.id.branch)
        val batch = seeStudentDetailView.findViewById<MaterialTextView>(R.id.batch)
        val studentno = seeStudentDetailView.findViewById<MaterialTextView>(R.id.studentNo)
        val studentImage = seeStudentDetailView.findViewById<ImageView>(R.id.studentImage)

        val viewDetailConstraint =
            seeStudentDetailView.findViewById<ConstraintLayout>(R.id.constraint)
        val studentConstraint =
            seeStudentDetailView.findViewById<ConstraintLayout>(R.id.studentConstraint)
        studentNoTextInputLayout.helperText = " "
        if (studentNumber == null) bottomSheetDialog.setContentView(enterStudentNoView)
        else {
            bottomSheetDialog.setContentView(seeStudentDetailView)
            studentNoEditText.text = studentNumber
            lifecycleScope.launch {
                val lateEntryList = studentDatabase.studentDao().getStudentDetails()
                var flag = false
                val studentNumber2 = studentNoEditText.text.toString().trim()
                for (student in lateEntryList) {
                    if (student.student_no == studentNumber2) {
                        flag = true
                        viewDetailConstraint.visibility = View.GONE
                        name.text = student.name
                        branch.text = student.branch
                        studentno.text = studentNumber2
                        batch.text = student.batch.toString()

                        student.student_image?.let {
                            if (!student.image_downloaded) {
                                val imgUrl = "https://lateentry.silive.in$it"
                                Glide.with(requireActivity())
                                    .applyDefaultRequestOptions(
                                        RequestOptions.placeholderOf(R.drawable.ic_placeholder)
                                            .error(R.drawable.ic_placeholder)
                                    )
                                    .load(imgUrl)
                                    .into(studentImage)

                                downloadImg(
                                    requireContext(), imgUrl,
                                    "${context?.filesDir}/Images/",
                                    "${student.student_no}_${student.name}" +
                                            ".jpg"
                                )
                                    .observe(viewLifecycleOwner) { result ->
                                        if (result == "Download complete")
                                            student.image_downloaded = true
                                    }
                            } else {
                                val file = File(context?.filesDir, "Images/")
                                Glide.with(requireContext())
                                    .applyDefaultRequestOptions(
                                        RequestOptions.placeholderOf(R.drawable.ic_placeholder)
                                            .error(R.drawable.ic_placeholder)
                                    )
                                    .load(file.absolutePath + "${student.student_no}_${student.name}.jpg")
                                    .into(studentImage)
                            }
                        }
                    }
                }
                if (!flag) {
                    studentConstraint.visibility = View.GONE
                }
            }
        }

        bottomSheetDialog.show()
        isBottomSheetOpen = true

        studentNo.requestFocus()
        studentNo.postDelayed({
            showKeyboard(studentNo, activity)
            binding.enterStudentNoBtn.isEnabled = true
            binding.enterStudentNoBtn.isClickable = true
        }, 200)

        studentNo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0 == null || p0.length < 3) {
                    okButton.isEnabled = false
                    okButton.setTextColor(Color.parseColor("#BFBFBF"))
                    okButton.setBackgroundResource(R.drawable.ok_button_curved_edge)
                    studentNoTextInputLayout.helperText = " "
                } else {
                    okButton.isEnabled = true
                    okButton.setTextColor(Color.parseColor("#FFFFFF"))
                    okButton.setBackgroundResource(R.drawable.ok_button_curved_edge_enable)
                    studentNoTextInputLayout.helperText = " "
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        okButton.setOnClickListener {
            lifecycleScope.launch {
                var flag = false
                val studentList = studentDatabase.studentDao().getStudentDetails()
                studentList.forEach {
                    if (it.student_no == studentNo.text.toString()) {
                        flag = true
                        return@forEach
                    }
                }

                if (!flag) {
                    studentNoTextInputLayout.helperText = "     " + "Invalid student number"
                    okButton.isEnabled = false
                    okButton.setTextColor(Color.parseColor("#BFBFBF"))
                    okButton.setBackgroundResource(R.drawable.ok_button_curved_edge)
                } else {
                    hideKeyboard(studentNo, activity)
                    bottomSheetDialog.setContentView(seeStudentDetailView)
                    studentNoEditText.text = studentNo.text!!.trim()
                    val lateEntryList = studentDatabase.studentDao().getStudentDetails()
                    val studentNumber2 = studentNoEditText.text.toString().trim()
                    for (student in lateEntryList) {
                        if (student.student_no == studentNumber2) {
                            viewDetailConstraint.visibility = View.GONE
                            name.text = student.name
                            branch.text = student.branch
                            studentno.text = studentNumber2
                            batch.text = student.batch.toString()

                            student.student_image?.let {
                                if (!student.image_downloaded) {
                                    val imgUrl = "https://lateentry.silive.in$it"
                                    Glide.with(requireActivity())
                                        .applyDefaultRequestOptions(
                                            RequestOptions.placeholderOf(R.drawable.ic_placeholder)
                                                .error(R.drawable.ic_placeholder)
                                        )
                                        .load(imgUrl)
                                        .into(studentImage)

                                    downloadImg(
                                        requireContext(), imgUrl,
                                        "${context?.filesDir}/Images/",
                                        "${student.student_no}_${student.name}" +
                                                ".jpg"
                                    )
                                        .observe(viewLifecycleOwner) { result ->
                                            if (result == "Download complete")
                                                student.image_downloaded = true
                                        }
                                } else {
                                    val file = File(context?.filesDir, "Images/")
                                    Glide.with(requireContext())
                                        .applyDefaultRequestOptions(
                                            RequestOptions.placeholderOf(R.drawable.ic_placeholder)
                                                .error(R.drawable.ic_placeholder)
                                        )
                                        .load(file.absolutePath + "${student.student_no}_${student.name}.jpg")
                                        .into(studentImage)
                                }
                            }
                        }
                    }

                    return@launch
                }
            }
        }

        bottomSheetDialog.setOnCancelListener {
            isBottomSheetOpen = false
            hideKeyboard(requireView(), activity)
            scannerView.setResultHandler(this)
            scannerView.startCamera()
        }

        var student: String
        var venueId: Int

        submitLateEntryBtn.setOnClickListener {
            lifecycleScope.launch {
                var flag = false
                val studt = if (bool) student_No.toString()
                else studentNo.text.toString()
                val studentList = studentDatabase.studentDao().getStudentDetails()
                studentList.forEach {
                    if (it.student_no == studt) {
                        flag = true
                        return@forEach
                    }
                }

                if (!flag && bool) studentNoInputLayout.helperText = "Sync your data"
                else {
                    showToast("Late entry registered")
                    bottomSheetDialog.cancel()
                    student = studentNoEditText.text.toString().trim()

                    venueId = datastore.getId("ID_KEY")!!

                    lateEntryViewModel.submitResult(student, venueId)
                    lateEntryViewModel._lateEntryResult.observe(viewLifecycleOwner) {
                        if (it is Response.Error && it.errorMessage == "Save to DB") {
                            lifecycleScope.launch {
                                studentDatabase.offlineLateEntryDao()
                                    .addLateEntry(
                                        OfflineLateEntry(
                                            student_no = student,
                                            timestamp = currentTimeInIsoFormat(),
                                            venue = venueId
                                        )
                                    )
                            }
                        }
                    }
                }
            }

        }

    }

    override fun venueClickListener(venue: String, id: Int) {
        lifecycleScope.launch {
            datastore.saveId("ID_KEY", id)
            datastore.saveDefaultVenue(venue)
        }
        scannerView.setResultHandler(this)
        scannerView.startCamera()

        Handler(Looper.getMainLooper()).postDelayed({
            isBottomSheetOpen = false
            venueBottomSheetDialog.dismiss()
        }, 150)
        val animation =
            AnimationUtils.loadAnimation(
                context,
                R.anim.long_fade_in
            )
        binding.location.startAnimation(animation)
        binding.location.text = venue
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })
    }

    private fun showExitDialog() {
        dialog.show()

        binding.scannerContainer.postDelayed({
            scannerView.stopCamera()
        }, 400)

        customView.findViewById<MaterialTextView>(R.id.dialogMessage).setText(R.string.exitMessage)
        val exit = customView.findViewById<MaterialButton>(R.id.positiveBtn)
        val cancel = customView.findViewById<MaterialButton>(R.id.cancel)

        exit.setOnClickListener { activity?.finishAffinity() }

        cancel.setOnClickListener { dialog.dismiss() }

        dialog.setOnDismissListener {
            scannerView.setResultHandler(this)
            scannerView.startCamera()
        }
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { if (!it) showGoToAppSettingsDialog(requireContext()) }

    private fun showGoToAppSettingsDialog(context: Context) {
        val customView = layoutInflater.inflate(R.layout.camera_permission_dialog, null)

        MaterialAlertDialogBuilder(context)
            .setView(customView)
            .setCancelable(false)
            .setBackground(ColorDrawable(Color.TRANSPARENT))
            .show()

        val grant = customView.findViewById<MaterialButton>(R.id.grant)
        val cancel = customView.findViewById<MaterialButton>(R.id.cancel)

        grant.setOnClickListener {
            goToAppSettings()
            activity?.finishAffinity()
        }

        cancel.setOnClickListener { activity?.finishAffinity() }
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