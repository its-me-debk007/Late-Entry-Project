package `in`.silive.lateentryproject.ui

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.FragmentBarcodeSannerBinding
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView

class BarcodeFragment: Fragment(), ZBarScannerView.ResultHandler {
    lateinit var binding: FragmentBarcodeSannerBinding

    companion object {
        fun newInstance(): BarcodeFragment {
            return BarcodeFragment()
        }
    }

    private lateinit var mView: View

    lateinit var scannerView: ZBarScannerView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_barcode_sanner,
            container,
            false
        )
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val view = binding.root
        val scan = binding.containerScanner
        onClicks()
        initializeQRCamera()
        scan.setOnClickListener {
            scannerView.resumeCameraPreview(this)
        }

        binding.enterStudentNo.setOnClickListener {
            val modalBottomSheet = BottomSheetFragment()
            modalBottomSheet.show(requireActivity().supportFragmentManager,
                                  "BottomSheetDialogFragment")
        }

        return view
    }


    private fun initializeQRCamera() {
        scannerView = ZBarScannerView(context)
        scannerView.setResultHandler(this)
        scannerView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTranslucent
            )
        )
        scannerView.setBorderColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        scannerView.setLaserColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        scannerView.setBorderStrokeWidth(10)
        scannerView.setMaskColor(ContextCompat.getColor(requireContext(), R.color.colorTranslucent))
        scannerView.setLaserEnabled(true)
        scannerView.setBorderLineLength(200)
        scannerView.setupScanner()
        scannerView.setAutoFocus(true)
        startQRCamera()
        binding.containerScanner.addView(scannerView)
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
        Toast.makeText(requireContext(), rawResult?.contents, Toast.LENGTH_SHORT).show()
    }
}