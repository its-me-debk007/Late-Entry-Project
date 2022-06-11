package `in`.silive.lateentryproject.utils

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.ui.fragments.BarcodeFragment
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


class Utils {

	fun downloadImg(context: Context, imgUrl: String, dirPath: String, imgName: String): MutableLiveData<String> {
		PRDownloader.initialize(context)
		val resultLiveData = MutableLiveData<String>()

		PRDownloader.download(imgUrl, dirPath, imgName)
			.build()
			.start(object : OnDownloadListener {
				override fun onDownloadComplete() {
					Log.e("dddd", "Download complete")
					resultLiveData.postValue("Download Complete")
				}

				override fun onError(p0: com.downloader.Error?) {
					resultLiveData.postValue(p0.toString())
				}
			})
		return resultLiveData
	}

	fun showKeyboard(view: View, activity: FragmentActivity?) {
		val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
	}

	fun hideKeyboard(view: View, activity: FragmentActivity?) {
		val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(view.windowToken, 0)
	}

	fun currentTimeInIsoFormat(): String {
		val ISO_8601_24H_FULL_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ"

		val sdf = SimpleDateFormat(ISO_8601_24H_FULL_FORMAT, Locale.UK)
		var format = sdf.format(Date())

		format = format.substring(0, format.length - 2) + ':' + format.substring(format.length - 2)
		return format
	}

	fun currentTime(): String {
		val current = LocalDateTime.now()
		val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)

		return current.format(formatter)
	}
}