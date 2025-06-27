package com.example.applaporfik.fragment.profil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.applaporfik.R
import com.example.applaporfik.activity.LoginActivity
import com.example.applaporfik.data.api.ApiService
import com.example.applaporfik.databinding.FragmentProfilBinding
import com.example.applaporfik.databinding.FragmentProfileLoggedInBinding
import com.example.applaporfik.databinding.FragmentProfileNotLoginBinding
import com.example.applaporfik.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    
    private var _notLoginBinding: FragmentProfileNotLoginBinding? = null
    private var _loggedInBinding: FragmentProfileLoggedInBinding? = null
    
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        apiService = ApiService.create()
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        checkLoginStatusAndShowLayout()
    }

    override fun onResume() {
        super.onResume()
        // Check if login status has changed and refresh if needed
        val isCurrentlyLoggedIn = _loggedInBinding != null
        val shouldBeLoggedIn = sessionManager.isLoggedIn()
        
        if (isCurrentlyLoggedIn != shouldBeLoggedIn) {
            // Clear current layout
            _notLoginBinding?.root?.let { binding.root.removeView(it) }
            _loggedInBinding?.root?.let { binding.root.removeView(it) }
            _notLoginBinding = null
            _loggedInBinding = null
            
            // Show appropriate layout
            checkLoginStatusAndShowLayout()
        }
    }

    private fun checkLoginStatusAndShowLayout() {
        if (sessionManager.isLoggedIn()) {
            // User is logged in - show logged in layout
            showLoggedInLayout()
            loadUserProfile()
        } else {
            // User is not logged in - show not logged in layout
            showNotLoggedInLayout()
        }
    }

    private fun showNotLoggedInLayout() {
        // Inflate the not logged in layout
        _notLoginBinding = FragmentProfileNotLoginBinding.inflate(layoutInflater, binding.root as ViewGroup, true)
        
        // Set up login button
        _notLoginBinding?.btnLogin?.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
        
        // Set up register button (optional - you can implement registration later)
        _notLoginBinding?.btnRegister?.setOnClickListener {
            Toast.makeText(context, "Registration feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoggedInLayout() {
        // Inflate the logged in layout
        _loggedInBinding = FragmentProfileLoggedInBinding.inflate(layoutInflater, binding.root as ViewGroup, true)
        
        // Set up logout button
        _loggedInBinding?.btnLogout?.setOnClickListener {
            logoutUser()
        }
        
        // Set up other profile options
        _loggedInBinding?.btnEditProfile?.setOnClickListener {
            Toast.makeText(context, "Edit profile feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        _loggedInBinding?.btnChangePassword?.setOnClickListener {
            Toast.makeText(context, "Change password feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserProfile() {
        val sessionInfo = sessionManager.getSessionInfo()
        if (sessionInfo == null) {
            Toast.makeText(context, "Session information not available", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show loading state
        _loggedInBinding?.tvUserName?.text = "Loading..."
        _loggedInBinding?.tvUserRole?.text = "Loading..."
        _loggedInBinding?.tvUserEmail?.text = "Loading..."
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getUserProfile("Bearer ${sessionInfo.token}", sessionInfo.nim)
                
                withContext(Dispatchers.Main) {
                    if (response.success && response.user != null) {
                        // Update the UI with user data from API
                        _loggedInBinding?.tvUserName?.text = response.user.nama
                        _loggedInBinding?.tvUserRole?.text = response.user.role.replaceFirstChar { it.uppercase() }
                        _loggedInBinding?.tvUserEmail?.text = response.user.gmail ?: "${response.user.nim}@student.upn.ac.id"
                        _loggedInBinding?.tvUserNim?.text = response.user.nim
                        _loggedInBinding?.tvUserJurusan?.text = response.user.jurusan
                        
                        // Load profile image
                        loadProfileImage(response.user.profile_url)
                    } else {
                        // Fallback to session data
                        _loggedInBinding?.tvUserName?.text = sessionInfo.nim
                        _loggedInBinding?.tvUserRole?.text = sessionInfo.userRole.replaceFirstChar { it.uppercase() }
                        _loggedInBinding?.tvUserEmail?.text = "${sessionInfo.nim}@student.upn.ac.id"
                        
                        // Load default profile image
                        loadProfileImage(null)
                        
                        Toast.makeText(context, "Failed to load profile data", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Fallback to session data
                    _loggedInBinding?.tvUserName?.text = sessionInfo.nim
                    _loggedInBinding?.tvUserRole?.text = sessionInfo.userRole.replaceFirstChar { it.uppercase() }
                    _loggedInBinding?.tvUserEmail?.text = "${sessionInfo.nim}@student.upn.ac.id"
                    
                    // Load default profile image
                    loadProfileImage(null)
                    
                    Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadProfileImage(profileUrl: String?) {
        val imageView = _loggedInBinding?.ivUserProfile
        if (imageView != null) {
            if (profileUrl != null && profileUrl.isNotEmpty()) {
                // Load custom profile image
                Glide.with(this)
                    .load(profileUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(imageView)
            } else {
                // Load default profile image
                Glide.with(this)
                    .load(R.drawable.ic_profile)
                    .circleCrop()
                    .into(imageView)
            }
        }
    }

    private fun logoutUser() {
        // Clear user session
        sessionManager.clearSession()
        
        // Show not logged in layout
        _loggedInBinding?.root?.let { binding.root.removeView(it) }
        _loggedInBinding = null
        
        showNotLoggedInLayout()
        
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _notLoginBinding = null
        _loggedInBinding = null
    }
} 