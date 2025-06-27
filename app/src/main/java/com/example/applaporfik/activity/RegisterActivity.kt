package com.example.applaporfik.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applaporfik.databinding.ActivityRegisterBinding
import com.example.applaporfik.data.api.ApiService
import com.example.applaporfik.data.api.RegisterRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = ApiService.create()

        setupUI()
        setupRegisterButton()
    }

    private fun setupUI() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRegisterButton() {
        binding.btnRegister.setOnClickListener {
            val nama = binding.etNama.text.toString().trim()
            val nim = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInputs(nama, nim, password, confirmPassword)) {
                performRegister(nama, nim, password)
            }
        }
    }

    private fun validateInputs(nama: String, nim: String, password: String, confirmPassword: String): Boolean {
        if (nama.isEmpty()) {
            Toast.makeText(this, "Nama is required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (nim.isEmpty()) {
            Toast.makeText(this, "NIM is required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (nim.length < 3) {
            Toast.makeText(this, "NIM must be at least 3 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun performRegister(nama: String, nim: String, password: String) {
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Registering..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val registerRequest = RegisterRequest(nama, nim, password)
                val response = apiService.register(registerRequest)
                
                withContext(Dispatchers.Main) {
                    if (response.success) {
                        Toast.makeText(this@RegisterActivity, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
                        
                        // Return to MainActivity
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, response.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                        resetRegisterButton()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                    resetRegisterButton()
                }
            }
        }
    }

    private fun resetRegisterButton() {
        binding.btnRegister.isEnabled = true
        binding.btnRegister.text = "Register"
    }
} 