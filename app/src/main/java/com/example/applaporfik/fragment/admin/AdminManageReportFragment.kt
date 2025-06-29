package com.example.applaporfik.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.applaporfik.adapter.AdminManageReportAdapter
import com.example.applaporfik.data.api.ApiService
import com.example.applaporfik.databinding.FragmentAdminManageReportBinding
import com.example.applaporfik.util.NetworkUtils
import com.example.applaporfik.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminManageReportFragment : Fragment() {

    private var _binding: FragmentAdminManageReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AdminManageReportAdapter
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminManageReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = ApiService.create()
        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        loadReports()
    }

    private fun setupRecyclerView() {
        adapter = AdminManageReportAdapter {
            // Reload reports when data changes (feedback/status update)
            loadReports()
        }
        binding.recyclerViewAdminManage.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewAdminManage.adapter = adapter
    }

    private fun loadReports() {
        val sessionInfo = sessionManager.getSessionInfo() ?: return
        binding.progressBarAdminManage.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                        binding.progressBarAdminManage.visibility = View.GONE
                    }
                    return@launch
                }

                val response = apiService.getReports(token = "Bearer ${sessionInfo.token}")
                withContext(Dispatchers.Main) {
                    binding.progressBarAdminManage.visibility = View.GONE
                    if (response.success && response.reports != null) {
                        adapter.submitList(response.reports)
                    } else {
                        Toast.makeText(context, response.message ?: "Failed to load reports", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBarAdminManage.visibility = View.GONE
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
