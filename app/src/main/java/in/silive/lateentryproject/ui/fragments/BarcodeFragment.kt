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
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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
	private val bottomSheetDialog by lazy { BottomSheetDialog(requireContext()) }
	private val popup by lazy { PopupMenu(requireContext(), binding.icOverflowMenu) }
//	private lateinit var toast: Toast
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
		popup.menuInflater.inflate(R.menu.scanner_menu, popup.menu)

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
				showBottomSheet(null)

			binding.enterStudentNoBtn.isEnabled = false
			binding.enterStudentNoBtn.isClickable = false

			binding.scannerContainer.postDelayed({
													 scannerView.stopCamera()
												 }, 400)
		}

		binding.icOverflowMenu.setOnClickListener { showPopup() }

		lifecycleScope.launch {
			venue2 =
				datastore.getVenueDetails()?.replace("\\s".toRegex(), "")!!
					.split(",").associateTo(HashMap()) {
						val (left, right) = it.split("=")
						left.toInt() to right
					}
			binding.location.text = datastore.getDefaultVenue()
		}
	}

//	private fun showToast(text: String) {
//		val vibrator = getSystemService(requireContext(), Vibrator::class.java)
//		vibrator?.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
//
//		toast.cancel()
//		toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
//		toast.show()
//	}

	private fun showSnackbar(text: String) {
		val vibrator = getSystemService(requireContext(), Vibrator::class.java)
		vibrator?.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))

		val color = if (text == "Late entry registered") "#0BA712" else "#E81A1A"

		Snackbar.make(seeStudentDetailView, text, Snackbar.LENGTH_SHORT)
			.setDuration(1800)
			.setAnchorView(seeStudentDetailView)
			.setTextMaxLines(2)
			.setBackgroundTint(Color.parseColor(color))
			.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
			.show()
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
					backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(
						requireContext(), R.color.colorTransparent))
					imageTintList = ColorStateList.valueOf(ContextCompat.getColor(
						requireContext(), R.color.white))
				}
			} else {
				binding.flashToggle.apply {
					backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(
						requireContext(), R.color.white))
					imageTintList = ColorStateList.valueOf(ContextCompat.getColor(
						requireContext(), R.color.black))
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
//		toast.cancel()
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
		val studentNoEditText =
			seeStudentDetailView.findViewById<TextInputEditText>(R.id.studentNoEditText)
		val submitLateEntryBtn =
			seeStudentDetailView.findViewById<MaterialButton>(R.id.submitLateEntryBtn)
//		val progressBar =
//			seeStudentDetailView.findViewById<LinearProgressIndicator>(R.id.progressBar)
//		val profileProgressBar =
//			seeStudentDetailView.findViewById<LinearProgressIndicator>(R.id.profileProgressBar)
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
					studentNoTextInputLayout.helperText = " "
				} else {
					okButton.isEnabled = true
					okButton.setTextColor(Color.parseColor("#FFFFFF"))
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
					studentNoTextInputLayout.helperText = "Enter correct student no. or sync data"
					okButton.isEnabled = false
				}
				else {
					Utils().hideKeyboard(studentNo, activity)
					bottomSheetDialog.setContentView(seeStudentDetailView)
					student_No = studentNo.text.toString()
					studentNoEditText.setText(studentNo.text!!.trim())
					return@launch
				}
			}
		}

		studentNoEditText.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
				if (p0 == null || p0.length !in 7..15) {
					viewDetails.isEnabled = false
					viewDetails.setTextColor(ContextCompat.getColor(requireContext(),
																	R.color.disabledSettingsBtnColor))
					submitLateEntryBtn.isEnabled = false
				} else {
					viewDetails.isEnabled = true
					viewDetails.setTextColor(ContextCompat.getColor(requireContext(),
																	R.color.custom_blue))
					submitLateEntryBtn.isEnabled = true
				}
			}

			override fun afterTextChanged(p0: Editable?) {}
		})

		bottomSheetDialog.setOnCancelListener {
			scannerView.postDelayed({
										Utils().hideKeyboard(requireView(), activity)
										scannerView.setResultHandler(this)
										scannerView.startCamera()
									}, 100)

		}

		submitLateEntryBtn.setOnClickListener {
			submitLateEntryBtn.isEnabled = false
//			val linearProgressBar = if (studentConstraint.visibility == View.VISIBLE) profileProgressBar
//									else progressBar

//			linearProgressBar.visibility = View.VISIBLE

			val student = studentNoEditText.text.toString().trim()
			var venueId: Int?
			lifecycleScope.launch {
				venueId = datastore.getId("ID_KEY")!!
				lateEntryViewModel.venue.value = venueId

				var flag = false
				studentDatabase.studentDao().getStudentDetails().forEach {
					if (it.student_no == student) {
						flag = true
						return@forEach
					}
				}

				if (!flag)
					showSnackbar("The student no. doesn't exist. If this is not the case, then " +
										 "sync the data from Settings!")

				else {
					var lateEntryFlag = true
					studentDatabase.offlineLateEntryDao().getLateEntryDetails().forEach {
						if (student == it.student_no) {
							if (Utils().compareTimeInHrs(it.timestamp, Utils().currentTimeInIsoFormat
									()) < 20) lateEntryFlag = false
							return@forEach
						}
					}

					if (lateEntryFlag) {
						showSnackbar("Late entry registered")
						lateEntryViewModel.studentNo.value = student
						lateEntryViewModel.submitResult()
						lateEntryViewModel._lateEntryResult.observe(viewLifecycleOwner) {
							lifecycleScope.launch {
								val currentTime = Utils().currentTimeInIsoFormat()
								studentDatabase.offlineLateEntryDao().addLateEntry(
									OfflineLateEntry(student, currentTime, venueId!!))
							}
						}
					}
					else showSnackbar("Late entry already registered")
				}
			}

//				linearProgressBar.visibility = View.INVISIBLE
				submitLateEntryBtn.postDelayed({
												   submitLateEntryBtn.isEnabled = true
											   }, 2100)
//			}
		}

		viewDetails.setOnClickListener {
			viewDetails.isEnabled = false
			viewDetails.setTextColor(ContextCompat.getColor(requireContext(),
															R.color.disabledSettingsBtnColor))

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
									.applyDefaultRequestOptions(RequestOptions.placeholderOf(R.drawable.ic_placeholder)
																	.error(R.drawable.ic_placeholder))
									.load(imgUrl)
									.into(studentImage)

								Utils().downloadImg(requireContext(), imgUrl,
													"${context?.filesDir}/Images/",
													"${student.student_no}_${student.name}" +
															".jpg")
									.observe(viewLifecycleOwner) { result ->
										if (result == "Download complete")
											student.image_downloaded = true
									}
							} else {
								val file = File(context?.filesDir, "Images/")
								Glide.with(requireContext())
									.applyDefaultRequestOptions(RequestOptions.placeholderOf(R.drawable.ic_placeholder)
																	.error(R.drawable.ic_placeholder))
									.load(file.absolutePath + "${student.student_no}_${student.name}.jpg")
									.into(studentImage)
							}
						}
					}
				}
				if (!flag)
					showSnackbar("The student no. doesn't exist. If this is" +
										 " not the case, then sync the data from Settings!")
			}

			viewDetails.postDelayed({
										viewDetails.isEnabled = true
										viewDetails.setTextColor(ContextCompat.getColor
											(requireContext(), R.color.custom_blue))
									}, 2100)
		}
	}

	private fun showPopup() {
		popup.setOnMenuItemClickListener { menuItem ->
			when (menuItem.itemId) {
				R.id.settings -> {
					Handler(Looper.getMainLooper()).postDelayed({
							activity?.supportFragmentManager?.beginTransaction()
								?.setCustomAnimations(R.anim.slide_in, R.anim.fade_out,
													  R.anim.fade_in, R.anim.slide_out)
								?.replace(R.id.fragmentContainerView,
										  SettingsFragment())
								?.commit()
							}, 30)
				}

				R.id.venue -> {
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
			datastore.saveDefaultVenue(venue)
		}
		scannerView.setResultHandler(this)
		scannerView.startCamera()
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
