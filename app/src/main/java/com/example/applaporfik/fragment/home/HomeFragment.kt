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
import com.example.applaporfik.adapter.RecentFeedbackAdapter
import com.example.applaporfik.data.api.ApiService
import com.example.applaporfik.databinding.FragmentHomeBinding
import com.example.applaporfik.model.FeedbackItem
import com.example.applaporfik.model.FeedbackCategory
import com.example.applaporfik.model.FeedbackStatus
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
    
    private lateinit var recentFeedbackAdapter: RecentFeedbackAdapter
    private lateinit var sessionManager: SessionManager

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
        
        setupRecyclerView()
        setupUI()
        
        // Add a small delay to ensure UI is fully loaded before making network call
        view.postDelayed({
            loadRecentFeedback()
        }, 500) // 500ms delay
    }

    private fun setupRecyclerView() {
        recentFeedbackAdapter = RecentFeedbackAdapter()
        binding.recyclerViewRecentFeedback.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentFeedbackAdapter
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
    }



    private fun loadRecentFeedback() {
        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewRecentFeedback.visibility = View.GONE
        binding.textEmpty.visibility = View.GONE

        // Get recent feedback from API with timeout
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check network availability on main thread to avoid blocking
                val isNetworkAvailable = withContext(Dispatchers.Main) {
                    NetworkUtils.isNetworkAvailable(requireContext())
                }
                
                if (!isNetworkAvailable) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.textEmpty.visibility = View.VISIBLE
                        binding.textEmpty.text = "No internet connection"
                    }
                    return@launch
                }

                val apiService = ApiService.create()
                val response = apiService.getLatestReports(
                    token = "Bearer ${sessionManager.getSessionInfo()?.token ?: ""}",
                    limit = 3
                )

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    
                    if (response.success && response.reports != null && response.reports.isNotEmpty()) {
                        // Convert Report objects to FeedbackItem objects
                        val feedbackItems = response.reports.map { report ->
                            FeedbackItem(
                                id = report.id.toString(),
                                category = when (report.kategori.lowercase()) {
                                    "facility" -> FeedbackCategory.FACILITY
                                    "academic" -> FeedbackCategory.ACADEMIC
                                    else -> FeedbackCategory.FACILITY
                                },
                                title = report.judul,
                                date = report.created_at.substring(0, 10), // Get just the date part
                                description = report.rincian,
                                status = when (report.status.lowercase()) {
                                    "pending" -> FeedbackStatus.REPORTED
                                    "completed" -> FeedbackStatus.FINISHED
                                    else -> FeedbackStatus.REPORTED
                                }
                            )
                        }
                        
                        binding.textEmpty.visibility = View.GONE
                        binding.recyclerViewRecentFeedback.visibility = View.VISIBLE
                        recentFeedbackAdapter.submitList(feedbackItems)
                    } else {
                        binding.textEmpty.visibility = View.VISIBLE
                        binding.recyclerViewRecentFeedback.visibility = View.GONE
                        binding.textEmpty.text = "No Reports"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.textEmpty.visibility = View.VISIBLE
                    binding.textEmpty.text = " "
                    // Don't show error toast to avoid ANR, just log it
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