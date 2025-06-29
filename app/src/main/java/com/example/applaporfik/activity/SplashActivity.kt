package com.example.applaporfik.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.applaporfik.R
import com.example.applaporfik.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sessionManager = SessionManager(this)

        // Use a coroutine to do the work on a background thread.
        // This prevents blocking the main UI thread and causing an ANR.
        lifecycleScope.launch {
            // Non-blocking delay for 2 seconds
            delay(2000)

            // Determine which activity to navigate to on a background thread
            val destination = getDestinationActivity()

            // Switch back to the main thread to perform the UI navigation
            withContext(Dispatchers.Main) {
                startActivity(Intent(this@SplashActivity, destination))
                finish() // Close splash activity so user can't go back to it
            }
        }
    }

    /**
     * Checks the user's login status and role on a background thread.
     * This is a 'suspend' function, meaning it can be paused and resumed,
     * which is perfect for background work without blocking the UI.
     *
     * @return The class of the Activity to navigate to.
     */
    private suspend fun getDestinationActivity(): Class<*> {
        // Perform the file read (SharedPreferences) on the IO dispatcher (background thread)
        return withContext(Dispatchers.IO) {
            if (sessionManager.isLoggedIn()) {
                val userRole = sessionManager.getUserRole()
                if (userRole == "admin") {
                    AdminDashboardActivity::class.java
                } else {
                    MainActivity::class.java
                }
            } else {
                // Always go to MainActivity for guest access
                MainActivity::class.java
            }
        }
    }
}
