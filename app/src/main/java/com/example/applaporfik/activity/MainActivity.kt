// FIX: navController.navigateUp() does not accept NavOptions.
// Replace with the correct call without parameters.

package com.example.applaporfik.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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

        // Hide the status bar for full immersive mode
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(android.view.WindowInsets.Type.statusBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setSupportActionBar(binding.toolbar)
        sessionManager = SessionManager(this)

        try {
            if (!sessionManager.isLoggedIn()) {
                navigateToLogin()
                return
            }

            if (sessionManager.getUserRole() == "admin") {
                navigateToAdminDashboard()
                return
            }

            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment
            if (navHostFragment != null) {
                navController = navHostFragment.navController

                navController.addOnDestinationChangedListener { _, destination, _ ->
                    if (destination.id == R.id.formFragment) {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        supportActionBar?.title = "Fill the form"
                    } else {
                        supportActionBar?.setDisplayHomeAsUpEnabled(false)
                        val userNim = sessionManager.getNim() ?: "User"
                        supportActionBar?.title = "Welcome, $userNim"
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
        Toast.makeText(this, "Please use logout to exit", Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        navController.navigateUp() // FIXED: removed NavOptions parameter, not supported
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val currentDestination = navController.currentDestination
        if (currentDestination != null && currentDestination.id == R.id.formFragment) {
            menu?.clear()
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
