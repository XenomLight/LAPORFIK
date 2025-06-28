package com.example.applaporfik.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.example.applaporfik.databinding.FragmentFeedbackListBinding
import com.example.applaporfik.adapter.FeedbackListAdapter
import com.example.applaporfik.data.api.ApiService
import com.example.applaporfik.data.api.Report
import com.example.applaporfik.util.SessionManager
import com.example.applaporfik.util.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedbackListFragment : Fragment() {

    private var _binding: FragmentFeedbackListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: FeedbackListAdapter
    
    private var currentPage = 1
    private var isLoading = false
    private var hasMoreData = true
    
    // Filter and sort options
    private var selectedStatus: String? = null
    private var selectedCategory: String? = null
    private var sortBy = "created_at"
    private var sortOrder = "desc"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        apiService = ApiService.create()
        sessionManager = SessionManager(requireContext())
        
        setupRecyclerView()
        setupFilterChips()
        setupSortSpinner()
        loadReports()
    }

    private fun setupRecyclerView() {
        adapter = FeedbackListAdapter()
        binding.recyclerViewFeedbackList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@FeedbackListFragment.adapter
        }
        
        // Add scroll listener for pagination
        binding.recyclerViewFeedbackList.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                
                if (!isLoading && hasMoreData) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                        firstVisibleItemPosition >= 0) {
                        loadMoreReports()
                    }
                }
            }
        })
    }

    private fun setupFilterChips() {
        // Status filter chips
        binding.chipStatusAll.setOnClickListener { 
            selectedStatus = null
            resetAndLoadReports()
        }
        binding.chipStatusReported.setOnClickListener { 
            selectedStatus = "pending"
            resetAndLoadReports()
        }
        binding.chipStatusFinished.setOnClickListener { 
            selectedStatus = "completed"
            resetAndLoadReports()
        }
        
        // Category filter chips
        binding.chipCategoryAll.setOnClickListener { 
            selectedCategory = null
            resetAndLoadReports()
        }
        binding.chipCategoryFacility.setOnClickListener { 
            selectedCategory = "facility"
            resetAndLoadReports()
        }
        binding.chipCategoryAcademic.setOnClickListener { 
            selectedCategory = "academic"
            resetAndLoadReports()
        }
    }

    private fun setupSortSpinner() {
        // Use the toggle group for date sorting
        binding.toggleGroupDateSort.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    binding.buttonDateDesc.id -> {
                        sortBy = "created_at"
                        sortOrder = "desc"
                    }
                    binding.buttonDateAsc.id -> {
                        sortBy = "created_at"
                        sortOrder = "asc"
                    }
                }
                resetAndLoadReports()
            }
        }
    }

    private fun resetAndLoadReports() {
        currentPage = 1
        hasMoreData = true
        adapter.clearReports()
        loadReports()
    }

    private fun loadReports() {
        if (isLoading) return
        
        isLoading = true
        showLoading(true)
        
        val sessionInfo = sessionManager.getSessionInfo()
        if (sessionInfo == null) {
            Toast.makeText(context, "Session not available", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                        showLoading(false)
                    }
                    return@launch
                }

                val response = apiService.getReports(
                    token = "Bearer ${sessionInfo.token}",
                    page = currentPage,
                    limit = 10,
                    status = selectedStatus,
                    kategori = selectedCategory,
                    sort = sortBy,
                    order = sortOrder
                )
                
                withContext(Dispatchers.Main) {
                    if (response.success && response.reports != null) {
                        if (currentPage == 1) {
                            adapter.setReports(response.reports)
                        } else {
                            adapter.addReports(response.reports)
                        }
                        
                        hasMoreData = response.reports.isNotEmpty()
                        showLoading(false)
                    } else {
                        Toast.makeText(context, response.message ?: "Failed to load reports", Toast.LENGTH_SHORT).show()
                        showLoading(false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                }
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadMoreReports() {
        currentPage++
        loadReports()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBarFeedbackList.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        resetAndLoadReports()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 