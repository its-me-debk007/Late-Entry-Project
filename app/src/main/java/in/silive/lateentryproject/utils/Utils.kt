package `in`.silive.lateentryproject.utils

import `in`.silive.lateentryproject.models.TokenDataClass
import `in`.silive.lateentryproject.network.ServiceBuilder
import `in`.silive.lateentryproject.ui.fragments.SplashScreenFragment
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

var isDialogShown = false

fun downloadImg(context: Context,
				imgUrl: String,
				dirPath: String,
				imgName: String): MutableLiveData<String> {
	PRDownloader.initialize(context)
	val resultLiveData = MutableLiveData<String>()

	PRDownloader.download(imgUrl, dirPath, imgName)
		.build()
		.start(object : OnDownloadListener {
			override fun onDownloadComplete() {
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
	val iso24HrFullFormat = "yyyy-MM-dd HH:mm:ss.SSSZ"

	val sdf = SimpleDateFormat(iso24HrFullFormat, Locale.UK)
	var format = sdf.format(Date())

	format = format.substring(0, format.length - 2) + ':' + format.substring(format.length - 2)
	return format
}

fun currentTime(): String {
	val current = LocalDateTime.now()
	val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)

	return current.format(formatter).substring(0, 11)
}

fun generateNewToken(context: Context) {
	runBlocking {
		try {
			val response = ServiceBuilder.buildService(isTokenRequired = false).generateToken(
				TokenDataClass(SplashScreenFragment.REFRESH_TOKEN))
			SplashScreenFragment.ACCESS_TOKEN = response.body()?.access
			Datastore(context).saveAccessToken(SplashScreenFragment.ACCESS_TOKEN!!)
		} catch (e: Exception) {
			Log.d("REFRESH_TOKEN_EXPIRY", e.message.toString())
			Log.d("REFRESH_TOKEN_EXPIRY", SplashScreenFragment.REFRESH_TOKEN.toString())
			Log.d("REFRESH_TOKEN_EXPIRY", SplashScreenFragment.ACCESS_TOKEN.toString())
			Toast.makeText(context, "${e.message}\nPlease try again", Toast.LENGTH_SHORT).show()
		}
	}
}
