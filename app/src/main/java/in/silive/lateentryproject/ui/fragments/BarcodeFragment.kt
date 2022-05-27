package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.Utils
import `in`.silive.lateentryproject.adapters.VenueItemsAdapter
import `in`.silive.lateentryproject.databinding.FragmentBarcodeScannerBinding
import `in`.silive.lateentryproject.sealed_class.Response
import `in`.silive.lateentryproject.view_models.LateEntryViewModel
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView

class BarcodeFragment : Fragment(R.layout.fragment_barcode_scanner), ZBarScannerView.ResultHandler {
	private lateinit var binding: FragmentBarcodeScannerBinding
	private lateinit var scannerView: ZBarScannerView
	private lateinit var venueArrayList: ArrayList<String>

	private val lateEntryViewModel by lazy {
		ViewModelProvider(this)[LateEntryViewModel::class.java]
	}

	@SuppressLint("SourceLockedOrientationActivity")
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding = FragmentBarcodeScannerBinding.bind(view)

		requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

		venueArrayList = ArrayList()
		venueArrayList = arrayListOf("CSIT", "LTs", "Main Gate")
		onClicks()
		initializeCamera()
		binding.scannerContainer.setOnClickListener {
			scannerView.resumeCameraPreview(this)
		}

		binding.enterStudentNoBtn.setOnClickListener {
			scannerView.stopCamera()
			if (binding.enterStudentNoBtn.isEnabled)
				showBottomSheet(requireContext(), null)

			binding.enterStudentNoBtn.isEnabled = false
			binding.enterStudentNoBtn.isClickable = false
		}

		binding.icOverflowMenu.setOnClickListener { showPopup(it, requireContext()) }
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
		scannerView.stopCamera()
	}

	override fun handleResult(rawResult: Result?) {
		if (rawResult?.contents?.length == 7) showBottomSheet(requireContext(), rawResult.contents)
		else scannerView.resumeCameraPreview(this)
	}

	private fun showBottomSheet(context: Context, studentNumber: String?) {
		val bottomSheetDialog = BottomSheetDialog(context)
		val enterStudentNoView = layoutInflater.inflate(R.layout.layout_input_bottom_sheet, null)
		val seeStudentDetailView =
			layoutInflater.inflate(R.layout.layout_entry_bottom_sheet_fragment, null)

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

			studentNoEditText.setText(studentNo.text!!.trim())
		}

		bottomSheetDialog.setOnCancelListener {
			Utils().hideKeyboard(requireView(), activity)

			scannerView.setResultHandler(this)
			startCamera()
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

				}
				R.id.venue -> {
					scannerView.stopCamera()
					val bottomSheetDialog = BottomSheetDialog(context)
					val venueItems =
						layoutInflater.inflate(R.layout.layout_venue_bottom_sheet_fragment, null)
					bottomSheetDialog.setContentView(venueItems)
					val listview: ListView = venueItems.findViewById(R.id.listview)
					listview.isClickable = true
					listview.divider = null
					val adapter = VenueItemsAdapter(requireActivity(), venueArrayList)
					listview.adapter = adapter


//					listview.setOnItemClickListener { parent,view,position,id ->
//						Toast.makeText(requireContext(), listview.onItemClickListener.toString(), Toast.LENGTH_SHORT).show()
//						Log.i("venue", "showPopup:$position")
//					}

					bottomSheetDialog.show()
//						listview.setOnItemClickListener { adapterView, view, i, l ->
//						}
					Toast.makeText(requireContext(), listview.count.toString(), Toast.LENGTH_SHORT)
						.show()

					bottomSheetDialog.setOnCancelListener {
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
}