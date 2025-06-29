package com.example.applaporfik.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.applaporfik.databinding.FragmentOverviewBinding
import com.example.applaporfik.data.api.ApiService
import com.example.applaporfik.data.api.Report
import com.example.applaporfik.data.api.ReportStats
import com.example.applaporfik.util.SessionManager
import com.example.applaporfik.util.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OverviewFragment : Fragment() {

    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    private var latestReports: List<Report> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        apiService = ApiService.create()
        sessionManager = SessionManager(requireContext())
        
        // Check if user is still logged in
        if (!sessionManager.isLoggedIn()) {
            // Navigate to login activity
            val intent = android.content.Intent(requireContext(), com.example.applaporfik.activity.LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }
        
        setupUI()
        loadData()
    }

    private fun setupUI() {
        // Set admin name (first name only) from local session
        val sessionInfo = sessionManager.getSessionInfo()
        if (sessionInfo != null) {
            val firstName = sessionInfo.userName.split(" ").firstOrNull() ?: "Admin"
            binding.textViewAdminName.text = firstName
        } else {
            binding.textViewAdminName.text = "Admin"
        }
        
        // Setup click listeners
        binding.buttonManageReport.setOnClickListener {
            findNavController().navigate(com.example.applaporfik.R.id.adminManageReportPagerFragment)
        }
        
        binding.buttonViewAllReport.setOnClickListener {
            findNavController().navigate(com.example.applaporfik.R.id.feedbackListFragment)
        }
        
        // Setup notification item clicks
        binding.judulNotifikasi1.setOnClickListener {
            if (latestReports.isNotEmpty()) {
                navigateToReportDetail(latestReports[0].id)
            }
        }
        
        binding.judulNotifikasi2.setOnClickListener {
            if (latestReports.size > 1) {
                navigateToReportDetail(latestReports[1].id)
            }
        }
    }

    private fun loadData() {
        loadLatestReports()
        loadReportStats()
    }

    private fun loadLatestReports() {
        // Check if user is still logged in
        if (!sessionManager.isLoggedIn()) {
            val intent = android.content.Intent(requireContext(), com.example.applaporfik.activity.LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        // Check if token needs refresh
        if (sessionManager.needsTokenRefresh()) {
            refreshTokenAndLoadData()
            return
        }

        val sessionInfo = sessionManager.getSessionInfo()
        if (sessionInfo == null) {
            // Session info is null, redirect to login
            val intent = android.content.Intent(requireContext(), com.example.applaporfik.activity.LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val response = apiService.getLatestReports("Bearer ${sessionInfo.token}")
                
                withContext(Dispatchers.Main) {
                    if (response.success && response.reports != null) {
                        latestReports = response.reports
                        updateLatestReportsUI()
                    } else {
                        Toast.makeText(context, response.message ?: "Failed to load reports", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun refreshTokenAndLoadData() {
        // For now, redirect to login to get fresh token
        // In a production app, you might want to implement token refresh API
        val intent = android.content.Intent(requireContext(), com.example.applaporfik.activity.LoginActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun loadReportStats() {
        // Check if user is still logged in
        if (!sessionManager.isLoggedIn()) {
            val intent = android.content.Intent(requireContext(), com.example.applaporfik.activity.LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        // Check if token needs refresh
        if (sessionManager.needsTokenRefresh()) {
            refreshTokenAndLoadData()
            return
        }

        val sessionInfo = sessionManager.getSessionInfo()
        if (sessionInfo == null) {
            // Session info is null, redirect to login
            val intent = android.content.Intent(requireContext(), com.example.applaporfik.activity.LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                    return@launch
                }

                val response = apiService.getReportStats("Bearer ${sessionInfo.token}")
                
                withContext(Dispatchers.Main) {
                    if (response.success && response.stats != null) {
                        updateStatsUI(response.stats)
                    }
                }
            } catch (e: Exception) {
                // Handle error silently for stats
            }
        }
    }

    private fun updateLatestReportsUI() {
        if (latestReports.isNotEmpty()) {
            binding.judulNotifikasi1.text = latestReports[0].judul
            binding.judulNotifikasi1.visibility = View.VISIBLE
            binding.numberNotification1.visibility = View.VISIBLE
        } else {
            binding.judulNotifikasi1.visibility = View.GONE
            binding.numberNotification1.visibility = View.GONE
        }

        if (latestReports.size > 1) {
            binding.judulNotifikasi2.text = latestReports[1].judul
            binding.judulNotifikasi2.visibility = View.VISIBLE
            binding.numberNotification2.visibility = View.VISIBLE
        } else {
            binding.judulNotifikasi2.visibility = View.GONE
            binding.numberNotification2.visibility = View.GONE
        }
    }

    private fun updateStatsUI(stats: ReportStats) {
        binding.textViewReportCountToday.text = stats.today.toString()
        binding.textViewReportCountWeek.text = stats.this_week.toString()
        binding.textViewReportCountAllTime.text = stats.all_time.toString()
        
        // Update notification badge
        if (stats.unread_count > 0) {
            binding.notificationBadge.text = stats.unread_count.toString()
            binding.notificationBadge.visibility = View.VISIBLE
        } else {
            binding.notificationBadge.visibility = View.GONE
        }
    }

    private fun navigateToReportDetail(reportId: Int) {
        // TODO: Navigate to report detail fragment
        Toast.makeText(context, "Report detail feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 