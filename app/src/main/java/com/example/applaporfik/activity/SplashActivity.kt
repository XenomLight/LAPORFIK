package com.example.applaporfik.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.applaporfik.R
import com.example.applaporfik.util.SessionManager

class SplashActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sessionManager = SessionManager(this)

        try {
            // Show splash for 2 seconds then navigate based on login status
            Handler(Looper.getMainLooper()).postDelayed({
                navigateBasedOnLoginStatus()
            }, 2000) // 2000ms = 2 seconds
        } catch (e: Exception) {
            // If there's any error, go to login
            Handler(Looper.getMainLooper()).postDelayed({
                navigateToLogin()
            }, 1000)
        }
    }
    
    private fun navigateBasedOnLoginStatus() {
        if (sessionManager.isLoggedIn()) {
            // User is logged in, check role
            val userRole = sessionManager.getUserRole()
            if (userRole == "admin") {
                // Admin goes to admin dashboard
                val intent = Intent(this, AdminDashboardActivity::class.java)
                startActivity(intent)
            } else {
                // Regular user goes to main activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        } else {
            // User is not logged in, go to login
            navigateToLogin()
        }
        finish() // Close splash activity so user can't go back to it
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
} 