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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import java.util.concurrent.TimeUnit

class Utils {

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

		return current.format(formatter).substring(0, 11)
	}

	fun compareTimeInHrs(t1: String, t2: String): Long {
		val ISO_8601_24H_FULL_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
		val sdf = SimpleDateFormat(ISO_8601_24H_FULL_FORMAT, Locale.UK)
		val time1 = sdf.parse(t1)
		val time2 = sdf.parse(t2)

		val diff = time2!!.time - time1!!.time
		return TimeUnit.MILLISECONDS.toHours(diff)
	}

//	suspend fun generateToken(token: String, refToken: String, context: Context)
//	{
//		Log.w("GENERATE TOKEN access", "NEW ACCESS TOKEN : $token")
//		ServiceBuilder.buildService().generateToken(TokenDataClass(refresh = refToken))
//			.enqueue(object : Callback<TokenDataClass?> {
//				override fun onResponse(call: Call<TokenDataClass?>, response: Response<TokenDataClass?>) {
//
//					if(response.isSuccessful) {
//						val newToken = response.body()?.access!!
//						GlobalScope.launch{
//							Datastore(context).saveAccessToken(newToken)
//							SplashScreenFragment.ACCESS_TOKEN = newToken
//							Log.w("NEW TOKEN", "NEW ACCESS TOKEN : "+Datastore(context).getAccessToken())
//
//						}
//
//					}
//				}
//
//				override fun onFailure(call: Call<TokenDataClass?>, t: Throwable) {
//					Toast.makeText(context, " Failed: ${t.message}", Toast.LENGTH_SHORT).show()
//				}
//			})
//	}

	fun generateNewToken(context: Context) {
		runBlocking {
			try {
				val response = ServiceBuilder.buildService().generateToken(TokenDataClass(
					SplashScreenFragment.REFRESH_TOKEN, SplashScreenFragment.ACCESS_TOKEN))

				SplashScreenFragment.ACCESS_TOKEN = response.body()?.access
				Datastore(context).saveAccessToken(SplashScreenFragment.ACCESS_TOKEN!!)
				Log.e("dddd", "${SplashScreenFragment.ACCESS_TOKEN}")
			} catch (e: Exception) {
				Log.e("dddd", e.message.toString())
			}
		}
	}
}