package com.example.applaporfik.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applaporfik.databinding.ActivityLoginBinding
import com.example.applaporfik.data.api.ApiService
import com.example.applaporfik.data.api.LoginRequest
import com.example.applaporfik.util.SessionManager
import com.example.applaporfik.util.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = ApiService.create()
        sessionManager = SessionManager(this)

        setupUI()
        setupLoginButton()
        setupAdditionalButtons()
    }

    private fun setupUI() {
        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            // User is already logged in, go to main activity
            navigateToMainActivity()
            return
        }

        // Restore remember me state if available
        binding.cbRememberMe.isChecked = sessionManager.isRememberMeEnabled()
        
        // Setup password visibility toggle
        setupPasswordVisibilityToggle()
    }

    private fun setupPasswordVisibilityToggle() {
        var isPasswordVisible = false
        
        binding.eyeIcon.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            
            if (isPasswordVisible) {
                // Show password
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT
                binding.eyeIcon.setImageResource(com.example.applaporfik.R.drawable.ic_eye_open)
            } else {
                // Hide password
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.eyeIcon.setImageResource(com.example.applaporfik.R.drawable.ic_eye_closed)
            }
            
            // Move cursor to end of text
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            val nim = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val rememberMe = binding.cbRememberMe.isChecked

            if (nim.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(nim, password, rememberMe)
        }
    }

    private fun setupAdditionalButtons() {
        // Forgot Password
        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Register
        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin(nim: String, password: String, rememberMe: Boolean) {
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Logging in..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // First check network connectivity
                if (!NetworkUtils.isNetworkAvailable(this@LoginActivity)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "No internet connection available", Toast.LENGTH_LONG).show()
                        resetLoginButton()
                    }
                    return@launch
                }

                val loginRequest = LoginRequest(nim, password)
                val response = apiService.login(loginRequest)
                
                withContext(Dispatchers.Main) {
                    if (response.success) {
                        // Save session using SessionManager
                        val sessionDuration = if (rememberMe) {
                            // 30 days for remember me
                            30L * 24L * 60L * 60L * 1000L
                        } else {
                            // 24 hours for regular session
                            24L * 60L * 60L * 1000L
                        }
                        
                        sessionManager.saveSession(
                            token = response.token ?: "",
                            nim = nim, // Use NIM for session
                            userRole = response.role ?: "user",
                            rememberMe = rememberMe,
                            sessionDuration = sessionDuration
                        )

                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    } else {
                        Toast.makeText(this@LoginActivity, response.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMessage = NetworkUtils.getConnectionErrorMessage(e)
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                    resetLoginButton()
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        // Check user role and navigate accordingly
        val userRole = sessionManager.getUserRole()
        
        when (userRole) {
            "admin" -> {
                // Navigate to admin dashboard
                val intent = Intent(this, AdminDashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            else -> {
                // Navigate to user dashboard (MainActivity)
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun resetLoginButton() {
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = "Login"
    }
} 