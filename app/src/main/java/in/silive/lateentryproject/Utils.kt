package `in`.silive.lateentryproject

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity

class Utils {
	private val baseUrl = "https://lateentry.azurewebsites.net"

	fun download(activity: FragmentActivity?, imgUrl: String, imgName: String): Long {
		val completeImgUrl = Uri.parse(baseUrl + imgUrl)
		val request = DownloadManager.Request(completeImgUrl)
		Log.e("dddd", "Downloading")
		request.apply {
			setTitle("Title")
			setDescription("Downloading...")
			setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
			setMimeType("image/jpg")
			setDestinationInExternalFilesDir(activity, Environment.DIRECTORY_DOWNLOADS,
											 "Cached Images/$imgName.jpg")
			setAllowedOverRoaming(true)
			setAllowedOverMetered(true)
		}

		val downloadManager =
			activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

		return downloadManager.enqueue(request)
	}

	fun showKeyboard(view: View, activity: FragmentActivity?) {
		val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
	}

	fun hideKeyboard(view: View, activity: FragmentActivity?) {
		val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(view.windowToken, 0)
	}

	fun checkInternetAtStartup(applicationContext: Context): Boolean {
		var result = false
		val connectivityManager = applicationContext.getSystemService(Context
																		  .CONNECTIVITY_SERVICE)
				as ConnectivityManager?
		connectivityManager?.let {
			it.getNetworkCapabilities(connectivityManager.activeNetwork)?.apply {
				result = when{
					hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
					hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
					else -> false
				}
			}
		}
		return result
	}
}