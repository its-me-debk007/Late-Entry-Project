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
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
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
import com.google.android.material.progressindicator.LinearProgressIndicator
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
    private lateinit var scannerView: ZBarScannerView
    private lateinit var venueBottomSheetDialog: BottomSheetDialog
    private lateinit var datastore: Datastore
    private lateinit var venue: MutableMap<Int, String>
    private lateinit var venue2: Map<Int, String>
    private var student_No: String? = null

    private val lateEntryViewModel by lazy {
        ViewModelProvider(this)[LateEntryViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBarcodeScannerBinding.bind(view)

        datastore = Datastore(requireContext())

        venue = HashMap()
        venue2 = HashMap()
        onClicks()
        initializeCamera()

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
                showBottomSheet(requireContext(), null)

            binding.enterStudentNoBtn.isEnabled = false
            binding.enterStudentNoBtn.isClickable = false

            binding.scannerContainer.postDelayed({
                scannerView.stopCamera()
            }, 500)
        }

        binding.icOverflowMenu.setOnClickListener {

            binding.icOverflowMenu.postDelayed({
                showPopup(it, requireContext())
            }, 50)

        }


        lifecycleScope.launch {
            val map =
                datastore.getVenueDetails("VENUE_KEY")?.replace("\\s".toRegex(), "")!!
                    .split(",").associateTo(HashMap()) {
                        val (left, right) = it.split("=")
                        left.toInt() to right
                    }
            venue2 = map
            binding.location.text = datastore.getVenueDetails("DEFAULT_VENUE_KEY")
        }
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

        startCamera()
        binding.scannerContainer.addView(scannerView)
    }

    private fun startCamera() {
        scannerView.startCamera()
    }

    private fun onClicks() {
        binding.flashToggle.setOnClickListener {
            if (binding.flashToggle.isSelected) offFlashLight()
            else onFlashLight()
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
        binding.scannerContainer.postDelayed({
            scannerView.stopCamera()
        }, 500)
    }

    override fun handleResult(rawResult: Result?) {
        student_No = rawResult?.contents.toString()
        if (rawResult?.contents?.length == 7) showBottomSheet(requireContext(), rawResult.contents)
        else scannerView.resumeCameraPreview(this)
    }

    private fun showBottomSheet(context: Context, studentNumber: String?) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val enterStudentNoView = layoutInflater.inflate(R.layout.layout_input_bottom_sheet, null)
        val seeStudentDetailView =
            layoutInflater.inflate(R.layout.layout_entry_bottom_sheet, null)

        val studentNo = enterStudentNoView.findViewById<TextInputEditText>(R.id.studentNo)
        val okButton = enterStudentNoView.findViewById<MaterialButton>(R.id.okButton)
        val studentNoTextInputLayout =
            enterStudentNoView.findViewById<TextInputLayout>(R.id.studentNoTextInputLayout)
        val studentNoEditText =
            seeStudentDetailView.findViewById<TextInputEditText>(R.id.studentNoEditText)
        val submitLateEntryBtn =
            seeStudentDetailView.findViewById<MaterialButton>(R.id.submitLateEntryBtn)
        val progressBar = seeStudentDetailView.findViewById<LinearProgressIndicator>(R.id.progressBar)
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
                    okButton.setTextColor(Color.parseColor("#3392C5"))
                    studentNoTextInputLayout.helperText = "Wrong student number"
                } else {
                    okButton.isEnabled = true
                    okButton.setTextColor(Color.parseColor("#FFFFFF"))
                    studentNoTextInputLayout.helperText = " "
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        okButton.setOnClickListener {
            Utils().hideKeyboard(studentNo, activity)
            bottomSheetDialog.setContentView(seeStudentDetailView)
            student_No = studentNo.text.toString()
            studentNoEditText.setText(studentNo.text!!.trim())
        }

        bottomSheetDialog.setOnCancelListener {
            scannerView.postDelayed({
                Utils().hideKeyboard(requireView(), activity)
                scannerView.setResultHandler(this)
                startCamera()
            }, 100)

        }

        submitLateEntryBtn.setOnClickListener {
            submitLateEntryBtn.isEnabled = false
            progressBar.visibility = View.VISIBLE

            okButton.setTextColor(Color.parseColor("#3392C5"))
            val student = studentNoEditText.text.toString().trim()
            var venueId: Int? = null
            lifecycleScope.launch {
                venueId = datastore.getId("ID_KEY")!!
                lateEntryViewModel.venue.value = venueId
            }
            lateEntryViewModel.studentNo.value = student
            lateEntryViewModel.submitResult()
            lateEntryViewModel._lateEntryResult.observe(viewLifecycleOwner) {
                when (it) {
                    is Response.Success ->
                        Toast.makeText(context, it.data?.message, Toast.LENGTH_SHORT).show()
                    is Response.Error -> {
                        if (it.errorMessage == "Save to DB") {
                            saveToDb(student, venueId!!)
                        } else Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                    }

                }

                progressBar.visibility = View.INVISIBLE
                submitLateEntryBtn.postDelayed({
                    submitLateEntryBtn.isEnabled = true
                    okButton.setTextColor(Color.parseColor("#FFFFFF"))
                }, 2000)

            }
        }
        viewDetails.setOnClickListener {
            val studentDatabase = StudentDatabase.getDatabase(requireContext())
            studentDatabase.studentDao().getStudentDetails()
                .observe(viewLifecycleOwner) { studentList ->
                    var flag = false
                    for (student in studentList) {
                        if (student.student_no == student_No) {
                            flag = true
                            viewDetailConstraint.visibility = View.GONE
                            studentConstraint.visibility = View.VISIBLE

                            name.text = student.name
                            branch.text = student.branch
                            studentno.text = student_No
                            batch.text = student.batch.toString()

                            student.student_image?.let {
                                if (!student.image_downloaded) {
                                    val imgUrl = "https://lateentry.azurewebsites.net$it"
                                    Glide.with(requireActivity())
                                        .applyDefaultRequestOptions(RequestOptions.placeholderOf(R.drawable.ic_placeholder).error(R.drawable.ic_placeholder))
                                        .load(imgUrl)
                                        .into(studentImage)

                                    Utils().downloadImg(requireContext(), imgUrl,
                                                        "${context.filesDir}/Images/",
                                                        "${student.student_no}_${student.name}" +
                                                                ".jpg").observe(viewLifecycleOwner) { result ->
                                        if (result == "Download complete")
                                            student.image_downloaded = true
                                        Log.e("dddd", student.image_downloaded.toString())
                                    }
                                }
                                else {
                                    val file = File(context.filesDir, "Images/")
                                    Glide.with(requireContext())
                                        .applyDefaultRequestOptions(RequestOptions.placeholderOf(R.drawable.ic_placeholder).error(R.drawable.ic_placeholder))
                                        .load(file.absolutePath+"${student.student_no}_${student.name}.jpg")
                                        .into(studentImage)

                                }

                            }
                        }
                    }
                    if (!flag) Toast.makeText(context, "The student no. doesn't exist\nIf this is" +
                            " not the case, then sync the data from Settings!",
                                        Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun showPopup(view: View, context: Context) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.scanner_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> {
                    activity?.supportFragmentManager?.beginTransaction()
                        ?.setCustomAnimations(R.anim.slide_in, R.anim.fade_out)
                        ?.replace(R.id.fragmentContainerView, SettingsFragment())
                        ?.commit()
                }

                R.id.venue -> {
                    venueBottomSheetDialog = BottomSheetDialog(context)
                    val venueItems =
                        layoutInflater.inflate(R.layout.layout_venue_bottom_sheet, null)
                    venueBottomSheetDialog.setContentView(venueItems)
                    venueBottomSheetDialog.show()

                    binding.scannerContainer.postDelayed({
                        scannerView.stopCamera()
                    }, 500)

                    val adapter =
                        VenueRecyclerAdapter(venue2, binding.location.text.toString(), this)
                    val recyclerView = venueItems.findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.adapter = adapter

                    venueBottomSheetDialog.setOnCancelListener {
                        scannerView.setResultHandler(this)
                        startCamera()
                    }
                }
                R.id.history -> {

                }
            }
            true
        }

        val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
        fieldMPopup.isAccessible = true
        val mPopup = fieldMPopup.get(popup)
        mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
            .invoke(mPopup, true)

        popup.show()
    }

    override fun venueClickListener(venue: String, id: Int) {
        lifecycleScope.launch {
            datastore.saveId("ID_KEY", id)
            datastore.saveDefaultVenue("DEFAULT_VENUE_KEY", venue)
        }
        scannerView.setResultHandler(this)
        startCamera()
        binding.location.postDelayed({
            venueBottomSheetDialog.dismiss()
            val animation =
                AnimationUtils.loadAnimation(
                    context,
                    R.anim.long_fade_in
                )
            binding.location.startAnimation(animation)
            binding.location.text = venue

        }, 400)

    }

    private fun saveToDb(studentNo: String, venue: Int) {
        val studentDatabase = StudentDatabase.getDatabase(requireContext())
        val currentTime = Utils().currentTime()
        var flag = false
        studentDatabase.studentDao().getStudentDetails()
            .observe(viewLifecycleOwner) { studentList ->
                for (student in studentList) {
                    if (student.student_no == studentNo) {
                        flag = true
                        break
                    }
                }

                if (!flag) Toast.makeText(context, "The student no. doesn't exist\nIf this is" +
                        " not the case, then sync the data from Settings!", Toast.LENGTH_SHORT)
                    .show()
                else {
                    lifecycleScope.launch {
                        studentDatabase.offlineLateEntryDao()
                            .addLateEntry(OfflineLateEntry(studentNo, currentTime, venue))
                        Toast.makeText(
                            context,
                            "Late entry scanned successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
}
