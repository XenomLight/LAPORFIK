package com.example.applaporfik.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.applaporfik.R
import com.example.applaporfik.databinding.ActivityAdminDashboardBinding
import com.example.applaporfik.util.SessionManager

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var navController: NavController
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        try {
            // Check if user is logged in and is admin
            if (!sessionManager.isLoggedIn()) {
                navigateToLogin()
                return
            }

            if (sessionManager.getUserRole() != "admin") {
                navigateToMain()
                return
            }

            setupUI()
            setupNavigation()
            setupBottomNavigation()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun setupNavigation() {
        // Set up navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_admin) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setupUI() {
        // Set admin title
        binding.tvAdminTitle.text = "Admin Dashboard"
        
        // Set up logout button
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun setupBottomNavigation() {
        binding.btnOverview.setOnClickListener {
            navController.navigate(R.id.overviewFragment)
            updateButtonStates(true, false, false)
        }

        binding.btnManageReports.setOnClickListener {
            navController.navigate(R.id.manageFeedbackFragment)
            updateButtonStates(false, true, false)
        }

        binding.btnReportsList.setOnClickListener {
            navController.navigate(R.id.feedbackListFragment)
            updateButtonStates(false, false, true)
        }

        // Set initial state (Overview is selected by default)
        updateButtonStates(true, false, false)
    }

    private fun updateButtonStates(overviewSelected: Boolean, manageSelected: Boolean, listSelected: Boolean) {
        binding.btnOverview.isSelected = overviewSelected
        binding.btnManageReports.isSelected = manageSelected
        binding.btnReportsList.isSelected = listSelected
        
        // Update button colors based on selection
        val selectedColor = resources.getColor(android.R.color.holo_blue_light, null)
        val defaultColor = resources.getColor(android.R.color.transparent, null)
        val selectedTextColor = resources.getColor(android.R.color.white, null)
        val defaultTextColor = resources.getColor(android.R.color.black, null)

        binding.btnOverview.setBackgroundColor(if (overviewSelected) selectedColor else defaultColor)
        binding.btnOverview.setTextColor(if (overviewSelected) selectedTextColor else defaultTextColor)

        binding.btnManageReports.setBackgroundColor(if (manageSelected) selectedColor else defaultColor)
        binding.btnManageReports.setTextColor(if (manageSelected) selectedTextColor else defaultTextColor)

        binding.btnReportsList.setBackgroundColor(if (listSelected) selectedColor else defaultColor)
        binding.btnReportsList.setTextColor(if (listSelected) selectedTextColor else defaultTextColor)
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

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Prevent going back to login screen
        Toast.makeText(this, "Please use logout to exit", Toast.LENGTH_SHORT).show()
    }
} 