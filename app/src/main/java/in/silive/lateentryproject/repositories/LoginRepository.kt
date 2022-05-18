package `in`.silive.lateentryproject.repositories

import `in`.silive.lateentryproject.network.RetrofitClient

class LoginRepository {

	fun login(email: String, password: String) = RetrofitClient.getInstance().login(email, password)
}