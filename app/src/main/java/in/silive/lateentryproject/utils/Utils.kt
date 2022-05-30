package `in`.silive.lateentryproject.utils

import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import java.text.SimpleDateFormat
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

	fun currentTime(): String {
		val ISO_8601_24H_FULL_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ"

		val sdf = SimpleDateFormat(ISO_8601_24H_FULL_FORMAT, Locale.UK)
		var format = sdf.format(Date())

		format = format.substring(0, format.length - 2) + ':' + format.substring(format.length - 2)
		return format
	}

}