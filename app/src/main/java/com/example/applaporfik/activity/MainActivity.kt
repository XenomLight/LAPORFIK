package com.example.applaporfik.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
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

        // Hide status bar for immersive experience
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(android.view.WindowInsets.Type.statusBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setSupportActionBar(binding.toolbar)
        sessionManager = SessionManager(this)

        // Start the app lifecycle service to detect app removal
        startService(Intent(this, com.example.applaporfik.AppLifecycleService::class.java))

        try {
            // Check if user is admin and redirect if needed
            if (sessionManager.isLoggedIn() && sessionManager.getUserRole() == "admin") {
                navigateToAdminDashboard()
                return
            }

            // Allow both logged-in users and guests to access MainActivity
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment
            if (navHostFragment != null) {
                navController = navHostFragment.navController

                navController.addOnDestinationChangedListener { _, destination, _ ->
                    when (destination.id) {
                        R.id.formFragment -> {
                            // Check if user is logged in before allowing form access
                            if (!sessionManager.isLoggedIn()) {
                                Toast.makeText(this, "Please login to submit a report", Toast.LENGTH_SHORT).show()
                                navigateToLogin()
                                return@addOnDestinationChangedListener
                            }
                            supportActionBar?.setDisplayHomeAsUpEnabled(true)
                            supportActionBar?.title = "Fill the form"
                        }
                        R.id.userReportPagerFragment -> {
                            // Check if user is logged in before allowing report access
                            if (!sessionManager.isLoggedIn()) {
                                Toast.makeText(this, "Please login to view your reports", Toast.LENGTH_SHORT).show()
                                navigateToLogin()
                                return@addOnDestinationChangedListener
                            }
                            supportActionBar?.setDisplayHomeAsUpEnabled(true)
                            supportActionBar?.title = "Report Detail"
                        }
                        else -> {
                            supportActionBar?.setDisplayHomeAsUpEnabled(false)
                            // Use local session credentials for immediate display
                            val sessionInfo = sessionManager.getSessionInfo()
                            if (sessionInfo != null) {
                                val firstName = sessionInfo.userName.split(" ").firstOrNull() ?: "User"
                                supportActionBar?.title = "Welcome, $firstName!"
                            } else {
                                supportActionBar?.title = "Welcome to LaporFIK!"
                            }
                        }
                    }
                }

                setupBottomNavigation()
            } else {
                Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun setupBottomNavigation() {
        binding.btnHome.setOnClickListener {
            navController.navigate(R.id.navigation_home, null, getNavAnimation())
            updateButtonStates(true, false)
        }

        binding.btnProfile.setOnClickListener {
            navController.navigate(R.id.navigation_profile, null, getNavAnimation())
            updateButtonStates(false, true)
        }

        updateButtonStates(true, false)
    }

    private fun getNavAnimation(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(android.R.anim.fade_in)
            .setExitAnim(android.R.anim.fade_out)
            .setPopEnterAnim(android.R.anim.fade_in)
            .setPopExitAnim(android.R.anim.fade_out)
            .build()
    }

    private fun updateButtonStates(homeSelected: Boolean, profileSelected: Boolean) {
        binding.btnHome.isSelected = homeSelected
        binding.btnProfile.isSelected = profileSelected

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
        sessionManager.clearAllData()
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
        if (sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please use logout to exit", Toast.LENGTH_SHORT).show()
        } else {
            // For guest users, allow normal back button behavior
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Only show menu for logged-in users
        if (sessionManager.isLoggedIn()) {
            menuInflater.inflate(R.menu.menu_main, menu)
            val currentDestination = navController.currentDestination
            if (currentDestination != null && currentDestination.id == R.id.formFragment) {
                menu?.clear()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
