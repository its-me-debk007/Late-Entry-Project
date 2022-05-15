package `in`.silive.lateentryproject.ui.fragments

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.FragmentBarcodeScannerBinding
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView

class BarcodeFragment : Fragment(), ZBarScannerView.ResultHandler {
	private lateinit var binding: FragmentBarcodeScannerBinding
	private lateinit var scannerView: ZBarScannerView
	var c = 0

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = DataBindingUtil.inflate(
			layoutInflater,
			R.layout.fragment_barcode_scanner,
			container,
			false
		)
		requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

		val view = binding.root
		val scan = binding.scannerContainer
		onClicks()
		initializeQRCamera()
		scan.setOnClickListener {
			scannerView.resumeCameraPreview(this)
		}

		binding.enterStudentNoBtn.setOnClickListener {
			if (binding.enterStudentNoBtn.isEnabled)
				showBottomSheet(requireContext(), null)

			binding.enterStudentNoBtn.isEnabled = false
			binding.enterStudentNoBtn.isClickable = false
		}

		return view
	}

	private fun initializeQRCamera() {
		scannerView = ZBarScannerView(context)
		scannerView.setResultHandler(this)

		scannerView.apply {
			setBackgroundColor(
				ContextCompat.getColor(requireContext(), R.color.colorTranslucent))
			setBorderColor(
				ContextCompat.getColor(
					requireContext(),
					R.color.white
				)
			)
			setLaserColor(
				ContextCompat.getColor(
					requireContext(),
					R.color.black
				)
			)
			setBorderStrokeWidth(10)
			setMaskColor(ContextCompat.getColor(requireContext(), R.color.colorTranslucent))
			setLaserEnabled(true)
			setBorderLineLength(200)
			setupScanner()
			setAutoFocus(true)
		}

		startQRCamera()
		binding.scannerContainer.addView(scannerView)
	}

	private fun startQRCamera() {
		scannerView.startCamera()
	}

	private fun onClicks() {
		binding.flashToggle.setOnClickListener {
			if (binding.flashToggle.isSelected) {
				offFlashLight()
			} else {
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
		scannerView.stopCamera()
	}

	override fun onDestroy() {
		super.onDestroy()
		scannerView.stopCamera()
	}

	override fun handleResult(rawResult: Result?) {
		showBottomSheet(requireContext(), rawResult?.contents)
	}

	private fun showBottomSheet(context: Context, studentNumber: String?) {
		val bottomSheetDialog = BottomSheetDialog(context)
		val enterStudentNoView = layoutInflater.inflate(R.layout.fragment_enter_bottom_sheet, null)
		val seeStudentDetailView = layoutInflater.inflate(
			R.layout.fragment_view_bottom_sheet_fragment, null)

		val studentNo = enterStudentNoView.findViewById<TextInputEditText>(R.id.studentNo)
		val okButton = enterStudentNoView.findViewById<MaterialButton>(R.id.okButton)
		val studentNoTextView =
			seeStudentDetailView.findViewById<MaterialTextView>(R.id.studentNoTextView)
		val submitLateEntryBtn =
			seeStudentDetailView.findViewById<MaterialButton>(R.id.submitLateEntryBtn)
		val viewDetails = seeStudentDetailView.findViewById<MaterialTextView>(R.id.viewDetails)

		if (studentNumber == null) bottomSheetDialog.setContentView(enterStudentNoView)
		else {
			bottomSheetDialog.setContentView(seeStudentDetailView)
			studentNoTextView.text = studentNumber
		}

		bottomSheetDialog.show()

		studentNo.requestFocus()
		studentNo.postDelayed({
								  showKeyboard(studentNo)
								  scannerView.stopCamera()
								  binding.enterStudentNoBtn.isEnabled = true
								  binding.enterStudentNoBtn.isClickable = true
							  }, 200)

		studentNo.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
				if (p0.isNullOrEmpty()) {
					okButton.isEnabled = false
					okButton.setTextColor(Color.parseColor("#6882EF"))
				} else {
					okButton.isEnabled = true
					okButton.setTextColor(Color.parseColor("#FFFFFF"))
				}
			}

			override fun afterTextChanged(p0: Editable?) {}
		})

		okButton.setOnClickListener {
			hideKeyboard(studentNo)
			bottomSheetDialog.setContentView(seeStudentDetailView)

			studentNoTextView.text = studentNo.text
		}

		bottomSheetDialog.setOnCancelListener {
			hideKeyboard(requireView())

			scannerView.setResultHandler(this)
			startQRCamera()
		}
	}

	private fun showKeyboard(view: View) {
		val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
	}

	private fun hideKeyboard(view: View) {
		val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(view.windowToken, 0)
	}
}