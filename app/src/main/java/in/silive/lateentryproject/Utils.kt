package `in`.silive.lateentryproject

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity


class Utils {
	private val imageUri = Uri.parse("https://media.geeksforgeeks.org/wp-content/uploads/" +
											 "20210224040124/JSBinCollaborativeJavaScriptDebugging6-300x160.png")

	fun download(activity: FragmentActivity?): Long {
		val request = DownloadManager.Request(imageUri)

		request.apply {
			setTitle("Title")
			setDescription("Downloading...")
			setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
			setMimeType("image/jpg")
			setDestinationInExternalFilesDir(activity, Environment.DIRECTORY_DOWNLOADS,
											 "Cached Images/img.jpg")
			setAllowedOverRoaming(true)
			setAllowedOverMetered(true)
		}

		val downloadManager =
			activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

		return downloadManager.enqueue(request)
	}

	fun showKeyboard(view: View, activity : FragmentActivity?) {
		val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
	}

	fun hideKeyboard(view: View, activity : FragmentActivity?) {
		val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(view.windowToken, 0)
	}


}