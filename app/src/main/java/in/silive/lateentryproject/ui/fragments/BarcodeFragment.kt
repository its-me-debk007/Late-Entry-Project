package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.adapters.VenueClickListenerInterface
import `in`.silive.lateentryproject.adapters.VenueRecyclerAdapter
import `in`.silive.lateentryproject.connectivity.ConnectivityLiveData
import `in`.silive.lateentryproject.databinding.FragmentBarcodeScannerBinding
import `in`.silive.lateentryproject.entities.OfflineLateEntry
import `in`.silive.lateentryproject.room_database.StudentDatabase
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.utils.Datastore
import `in`.silive.lateentryproject.utils.Utils
import `in`.silive.lateentryproject.view_models.LateEntryViewModel
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import kotlinx.coroutines.launch
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import java.io.File

class BarcodeFragment : Fragment(R.layout.fragment_barcode_scanner), ZBarScannerView
.ResultHandler, VenueClickListenerInterface {
    private lateinit var binding: FragmentBarcodeScannerBinding
    private val venueBottomSheetDialog by lazy { BottomSheetDialog(requireContext()) }
    private val datastore by lazy { Datastore(requireContext()) }
    private val studentDatabase by lazy { StudentDatabase.getDatabase(requireContext()) }
    private lateinit var venue: MutableMap<Int, String>
    private lateinit var venue2: Map<Int, String>
    private var student_No: String? = null
	private lateinit var toast: Toast
    private val bottomSheetDialog by lazy { BottomSheetDialog(requireContext()) }
    private val lateEntryViewModel by lazy {
        ViewModelProvider(this)[LateEntryViewModel::class.java]
    }
    private lateinit var seeStudentDetailView: View

    companion object {
        lateinit var scannerView: ZBarScannerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBarcodeScannerBinding.bind(view)
        venue = HashMap()
        venue2 = HashMap()
        onClicks()
        initializeCamera()
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT)

        val checkNetworkConnection = ConnectivityLiveData(requireActivity().application)
        checkNetworkConnection.observe(viewLifecycleOwner) {
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
            if (binding.enterStudentNoBtn.isEnabled)
                showBottomSheet(null)

            binding.enterStudentNoBtn.isEnabled = false
            binding.enterStudentNoBtn.isClickable = false

            binding.scannerContainer.postDelayed({
                scannerView.stopCamera()
            }, 400)
        }

        binding.setting.setOnClickListener {
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
            val venueItems =
                layoutInflater.inflate(R.layout.layout_venue_bottom_sheet, null)
            venueBottomSheetDialog.setContentView(venueItems)
            venueBottomSheetDialog.show()

            binding.scannerContainer.postDelayed({
                scannerView.stopCamera()
            }, 400)

            val adapter =
                VenueRecyclerAdapter(venue2, binding.location.text.toString(), this)
            val recyclerView = venueItems.findViewById<RecyclerView>(R.id.recyclerView)
            recyclerView.adapter = adapter

            venueBottomSheetDialog.setOnCancelListener {
                scannerView.setResultHandler(this)
                scannerView.startCamera()
            }
        }

    }

	private fun showToast(text: String) {
		toast.cancel()
		toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
		toast.show()
	}

    private fun initializeCamera() {
        scannerView = ZBarScannerView(context)
        scannerView.setResultHandler(this)

        scannerView.apply {
            setBorderColor(ContextCompat.getColor(requireContext(), R.color.white))
            setLaserColor(ContextCompat.getColor(requireContext(), R.color.black))
            setBorderStrokeWidth(13)
            setMaskColor(ContextCompat.getColor(requireContext(), R.color.colorTranslucent))
            setLaserEnabled(true)
            setBorderLineLength(200)
            setupScanner()
            setAutoFocus(true)
        }

        scannerView.startCamera()
        binding.scannerContainer.addView(scannerView)
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
        scannerView.setResultHandler(this)
        scannerView.startCamera()
    }

    override fun onPause() {
        super.onPause()
		toast.cancel()
        binding.scannerContainer.postDelayed({
            scannerView.stopCamera()
        }, 400)
    }

    override fun handleResult(rawResult: Result?) {
        student_No = rawResult?.contents.toString()
        rawResult?.contents?.let {
            if (it.length in 7..15) showBottomSheet(rawResult.contents)
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
        val studentNoEditText = seeStudentDetailView.findViewById<TextInputEditText>(R.id.studentNoEditText)
        val studentNoInputLayout = seeStudentDetailView.findViewById<TextInputLayout>(R.id.studentNoInputLayout)
        val submitLateEntryBtn =
            seeStudentDetailView.findViewById<MaterialButton>(R.id.submitLateEntryBtn)
        val viewDetails = seeStudentDetailView.findViewById<MaterialTextView>(R.id.viewDetails)
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
            studentNoEditText.setText(studentNumber)
        }

        bottomSheetDialog.show()

        studentNo.requestFocus()
        studentNo.postDelayed({
            Utils().showKeyboard(studentNo, activity)
            binding.enterStudentNoBtn.isEnabled = true
            binding.enterStudentNoBtn.isClickable = true
        }, 200)

        studentNo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0 == null || p0.length < 7) {
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
                    studentNoTextInputLayout.helperText = "Invalid student number"
                    okButton.isEnabled = false
                } else {
                    Utils().hideKeyboard(studentNo, activity)
                    bottomSheetDialog.setContentView(seeStudentDetailView)
                    student_No = studentNo.text.toString()
                    studentNoEditText.setText(studentNo.text!!.trim())
                    return@launch
                }
            }
        }

        bottomSheetDialog.setOnCancelListener {
                Utils().hideKeyboard(requireView(), activity)
                scannerView.setResultHandler(this)
                scannerView.startCamera()
        }

        var student: String
        var venueId = -1

        submitLateEntryBtn.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                var flag = false
                val studentList = studentDatabase.studentDao().getStudentDetails()
                studentList.forEach {
                    if (it.student_no == studentNo.text.toString()) {
                        flag = true
                        return@forEach
                    }
                }

                if (!flag) {
                    studentNoInputLayout.helperText = "Sync your data"
//                    submitLateEntryBtn.isEnabled = true
//                    viewDetails.isEnabled = true
//                    viewDetails.setTextColor(
//                        ContextCompat.getColor(
//                            requireContext(),
//                            R.color.disabledSettingsBtnColor
//                        )
//                    )
                }
                else
                {
                    showToast("Late entry registered")
                    bottomSheetDialog.cancel()
                    student = studentNoEditText.text.toString().trim()

                    lifecycleScope.launch {
                        venueId = datastore.getId("ID_KEY")!!
                    }

                    lateEntryViewModel.submitResult(student, venueId)
                    lateEntryViewModel._lateEntryResult.observe(viewLifecycleOwner) {
                        if (it is Response.Error && it.errorMessage == "Save to DB") {
                            lifecycleScope.launch {
                                val currentTime = Utils().currentTimeInIsoFormat()
                                studentDatabase.offlineLateEntryDao()
                                    .addLateEntry(
                                        OfflineLateEntry(
                                            student_no = student,
                                            timestamp = currentTime,
                                            venue = venueId
                                        )
                                    )
                            }
                        }
                    }
                }
            }

        }

        viewDetails.setOnClickListener {
            Utils().hideKeyboard(it, activity)
            viewDetails.isEnabled = false
            viewDetails.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.disabledSettingsBtnColor
                )
            )

            lifecycleScope.launch {
                val lateEntryList = studentDatabase.studentDao().getStudentDetails()
                var flag = false
                val studentNumber2 = studentNoEditText.text.toString().trim()
                for (student in lateEntryList) {
                    if (student.student_no == studentNumber2) {
                        flag = true
                        viewDetailConstraint.visibility = View.GONE
                        studentConstraint.visibility = View.VISIBLE

                        name.text = student.name
                        branch.text = student.branch
                        studentno.text = studentNumber2
                        batch.text = student.batch.toString()

                        student.student_image?.let {
                            if (!student.image_downloaded) {
                                val imgUrl = "https://lateentry.azurewebsites.net$it"
                                Glide.with(requireActivity())
                                    .applyDefaultRequestOptions(
                                        RequestOptions.placeholderOf(R.drawable.ic_placeholder)
                                            .error(R.drawable.ic_placeholder)
                                    )
                                    .load(imgUrl)
                                    .into(studentImage)

                                Utils().downloadImg(
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
                if (!flag) showToast("Invalid student number")
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
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })
    }

    private fun showExitDialog() {
        val customView = layoutInflater.inflate(R.layout.dialog, null)
        val builder = MaterialAlertDialogBuilder(requireContext()).apply {
            setView(customView)
            background = ColorDrawable(Color.TRANSPARENT)
        }
        val dialog = builder.show()

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
}

