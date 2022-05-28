package `in`.silive.lateentryproject.ui.activities

import `in`.silive.lateentryproject.models.Datastore
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class SplashScreen: AppCompatActivity()  {
    lateinit var datastore: Datastore
    companion object{
        var login by Delegates.notNull<Boolean>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        datastore = Datastore(this)
        lifecycleScope.launch {
            login = datastore.isLogin()
        }
        val intent =Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}