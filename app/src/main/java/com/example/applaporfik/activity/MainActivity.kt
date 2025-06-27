package com.example.applaporfik.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.applaporfik.R
import com.example.applaporfik.databinding.ActivityMainBinding
import com.example.applaporfik.util.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        try {
            // Check if user is logged in
            if (!sessionManager.isLoggedIn()) {
                navigateToLogin()
                return
            }

            // Check if user is admin (shouldn't be here if admin)
            if (sessionManager.getUserRole() == "admin") {
                navigateToAdminDashboard()
                return
            }

            // Set up navigation
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment
            if (navHostFragment != null) {
                navController = navHostFragment.navController
                
                // Set up custom bottom navigation
                setupBottomNavigation()
                setupUserUI()
            } else {
                Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun setupUserUI() {
        // Set user title
        val userNim = sessionManager.getNim() ?: "User"
        binding.tvUserTitle.text = "Welcome, $userNim"
        
        // Set up logout button
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun setupBottomNavigation() {
        binding.btnHome.setOnClickListener {
            navController.navigate(R.id.navigation_home)
            updateButtonStates(true, false)
        }

        binding.btnProfile.setOnClickListener {
            navController.navigate(R.id.navigation_profile)
            updateButtonStates(false, true)
        }

        // Set initial state (Home is selected by default)
        updateButtonStates(true, false)
    }

    private fun updateButtonStates(homeSelected: Boolean, profileSelected: Boolean) {
        binding.btnHome.isSelected = homeSelected
        binding.btnProfile.isSelected = profileSelected
        
        // Update button colors based on selection
        if (homeSelected) {
            binding.btnHome.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light, null))
            binding.btnHome.setTextColor(resources.getColor(android.R.color.white, null))
        } else {
            binding.btnHome.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
            binding.btnHome.setTextColor(resources.getColor(android.R.color.black, null))
        }

        if (profileSelected) {
            binding.btnProfile.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light, null))
            binding.btnProfile.setTextColor(resources.getColor(android.R.color.white, null))
        } else {
            binding.btnProfile.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
            binding.btnProfile.setTextColor(resources.getColor(android.R.color.black, null))
        }
    }

    private fun logout() {
        sessionManager.clearSession()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToAdminDashboard() {
        val intent = Intent(this, AdminDashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Prevent going back to login screen
        Toast.makeText(this, "Please use logout to exit", Toast.LENGTH_SHORT).show()
    }
}