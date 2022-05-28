package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.Utils
import `in`.silive.lateentryproject.adapters.VenueClickListenerInterface
import `in`.silive.lateentryproject.adapters.VenueRecyclerAdapter
import `in`.silive.lateentryproject.connectivity.ConnectivityLiveData
import `in`.silive.lateentryproject.databinding.FragmentBarcodeScannerBinding
import `in`.silive.lateentryproject.models.Datastore
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.view_models.BulkDataViewModel
import `in`.silive.lateentryproject.view_models.LateEntryViewModel
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView

class BarcodeFragment : Fragment(R.layout.fragment_barcode_scanner), ZBarScannerView
.ResultHandler, VenueClickListenerInterface {
	private lateinit var binding: FragmentBarcodeScannerBinding
	private lateinit var scannerView: ZBarScannerView
	private lateinit var venueBottomSheetDialog: BottomSheetDialog
	private lateinit var datastore: Datastore
	private lateinit var venue: MutableMap<Int, String>
	private lateinit var venue2: Map<Int, String>
	private var change: Boolean? = null

	private val lateEntryViewModel by lazy {
		ViewModelProvider(this)[LateEntryViewModel::class.java]
	}
	private val bulkViewModel by lazy { ViewModelProvider(this)[BulkDataViewModel::class.java] }

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding = FragmentBarcodeScannerBinding.bind(view)

		datastore = Datastore(requireContext())

		venue = HashMap()
		venue2 = HashMap()
		onClicks()
		initializeCamera()
//		binding.scannerContainer.setOnClickListener {
//			scannerView.resumeCameraPreview(this)
//		}

		val checkNetworkConnection = ConnectivityLiveData(requireActivity().application)
		checkNetworkConnection.observe(viewLifecycleOwner) {
			if (it) {
				val animation = AnimationUtils.loadAnimation(context, R.anim.long_fade_out)
				binding.noConnection.startAnimation(animation)
				binding.noConnection.visibility = View.INVISIBLE
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

		binding.icOverflowMenu.setOnClickListener { showPopup(it, requireContext()) }

		lifecycleScope.launchWhenStarted {
			try {
				change = datastore.isSync()
			} finally {
				if (change == true) {
					bulkViewModel.sendResult()
					bulkViewModel._bulkDataResult.observe(viewLifecycleOwner) { response ->
						when (response) {
							is Response.Success -> {
								for (i in response.data?.venue_data!!) {
									venue[i.id!!] = i.venue!!
								}

								lifecycleScope.launch {
									datastore.changeSyncState(false)
									datastore.saveVenueDetails("VENUE_KEY", venue)

									val map = datastore.getVenueDetails("VENUE_KEY")
										?.replace("\\s".toRegex(), "")!!.split(",")
										.associateTo(HashMap()) {
											val (left, right) = it.split("=")
											left.toInt() to right
										}

									venue2 = map

								}
							}
							is Response.Error ->
								Toast.makeText(context, response.errorMessage, Toast.LENGTH_SHORT)
									.show()
						}
					}
				}
			}
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
		val viewDetails = seeStudentDetailView.findViewById<MaterialTextView>(R.id.viewDetails)

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
							  }, 300)

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
			okButton.setTextColor(Color.parseColor("#3392C5"))
			val student = studentNoEditText.text.toString().trim()

			lateEntryViewModel.studentNo.value = student
			lateEntryViewModel.venue.value = 1
			lateEntryViewModel.submitResult()
			lateEntryViewModel._lateEntryResult.observe(viewLifecycleOwner) {
				when (it) {
					is Response.Success ->
						Toast.makeText(context, it.data?.message, Toast.LENGTH_SHORT).show()
					is Response.Error ->
						Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
				}

				submitLateEntryBtn.postDelayed({
												   submitLateEntryBtn.isEnabled = true
												   okButton.setTextColor(Color.parseColor("#FFFFFF"))
											   }, 2000)

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
						?.addToBackStack(null)
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

					val venueList = listOf("CS/IT", "LTs", "Main Gate")
					val adapter = VenueRecyclerAdapter(venueList, binding.location.text.toString
						(), this)
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

	override fun venueClickListener(venue: String) {
		scannerView.setResultHandler(this)
		startCamera()
		binding.location.postDelayed({
										 venueBottomSheetDialog.dismiss()
										 val animation =
											 AnimationUtils.loadAnimation(context,
																		  R.anim.long_fade_in)
										 binding.location.startAnimation(animation)
										 binding.location.text = venue
									 }, 400)
	}
}