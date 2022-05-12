package `in`.silive.lateentryproject.ui

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.FragmentEnterBottomSheetBinding
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetFragment : BottomSheetDialogFragment() {
	private lateinit var binding: FragmentEnterBottomSheetBinding

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_enter_bottom_sheet,
										  container, false)

		binding.apply {
			studentNo.requestFocus()
			studentNo.postDelayed({
				showKeyboard(studentNo)
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
				hideKeyboard(requireView())

				viewButton.visibility=View.VISIBLE
				submitEntryButton.visibility=View.VISIBLE
				okButton.visibility=View.GONE
				studentNo.isEnabled=false
//				val studentNo = studentNo.text.toString().trim()
//				enterStudentNoLayout.visibility = View.GONE
//				studentDetailLayout.visibility = View.VISIBLE
//				studentNoTextView.text = studentNo

//				val dialog = BottomSheetDialog(requireContext())
//				val layout = layoutInflater.inflate(R.layout.fragment_view_bottom_sheet_fragment,
//													null)
//				dialog.setContentView(layout)
//				dialog.setCancelable(true)
//				dialog.show()
//
//				Toast.makeText(requireContext(), studentNo, Toast.LENGTH_SHORT).show()
//				val studentNumber = layout.findViewById<MaterialTextView>(R.id.studentNoTextView)
//				studentNumber.text  = studentNo
//				val viewButton = layout.findViewById<MaterialButton>(R.id.viewButton)
//				viewButton.setOnClickListener {
//					Toast.makeText(requireContext(), "view button clicked", Toast.LENGTH_SHORT).show()
//				}

			}
		}

		return binding.root
	}

	private fun showKeyboard(view: View) {
		val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
	}

	private fun hideKeyboard(view: View) {
		val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(view.windowToken, 0)
	}


}