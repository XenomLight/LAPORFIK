package com.example.applaporfik.fragment.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.applaporfik.activity.LoginActivity
import com.example.applaporfik.adapter.UserReportAdapter
import com.example.applaporfik.data.api.ApiService
import com.example.applaporfik.databinding.FragmentHomeBinding
import com.example.applaporfik.util.NetworkUtils
import com.example.applaporfik.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.applaporfik.R


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var reportAdapter: UserReportAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        apiService = ApiService.create()
        
        setupRecyclerView()
        setupUI()
        
        // Add a small delay to ensure UI is fully loaded before making network call
        view.postDelayed({
            loadRecentReports()
        }, 500) // 500ms delay
    }

    private fun setupRecyclerView() {
        reportAdapter = UserReportAdapter(emptyList())
        binding.recyclerViewRecentFeedback.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reportAdapter
        }
    }

    private fun setupUI() {
        if (sessionManager.isLoggedIn()) {
            // User is logged in - show cards
            binding.cardLogin.visibility = View.GONE
            binding.cardLapor.visibility = View.VISIBLE
            binding.cardMyReports.visibility = View.VISIBLE

            binding.btnLapor.setOnClickListener {
                val navController = androidx.navigation.fragment.NavHostFragment.findNavController(
                    requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)!!
                )
                navController.navigate(R.id.formFragment)

            }

            binding.btnMyReports.setOnClickListener {
                val navController = androidx.navigation.fragment.NavHostFragment.findNavController(
                    requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)!!
                )
                navController.navigate(R.id.userReportPagerFragment)
            }

        } else {
            // User not logged in
            binding.cardLogin.visibility = View.VISIBLE
            binding.cardLapor.visibility = View.GONE
            binding.cardMyReports.visibility = View.GONE

            binding.btnLogin.setOnClickListener {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
        }

        // Setup refresh button for both guests and logged-in users
        binding.btnRefresh.setOnClickListener {
            loadRecentReports()
        }
    }

    private fun loadRecentReports() {
        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewRecentFeedback.visibility = View.GONE
        binding.textEmpty.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.textEmpty.visibility = View.VISIBLE
                        binding.textEmpty.text = "No internet connection"
                    }
                    return@launch
                }

                val response = apiService.getReportsPublic(public = "true", limit = 5)

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    if (response.success && response.reports != null) {
                        if (response.reports.isNotEmpty()) {
                            binding.textEmpty.visibility = View.GONE
                            binding.recyclerViewRecentFeedback.visibility = View.VISIBLE
                            reportAdapter.updateData(response.reports)
                        } else {
                            binding.textEmpty.visibility = View.VISIBLE
                            binding.recyclerViewRecentFeedback.visibility = View.GONE
                            binding.textEmpty.text = "No Reports"
                        }
                    } else {
                        binding.textEmpty.visibility = View.VISIBLE
                        binding.recyclerViewRecentFeedback.visibility = View.GONE
                        binding.textEmpty.text = response.message ?: "Failed to load reports"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.textEmpty.visibility = View.VISIBLE
                    binding.textEmpty.text = "Unable to load reports"
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Update UI when returning from login
        setupUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 