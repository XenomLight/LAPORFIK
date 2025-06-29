package com.example.applaporfik.fragment.user

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.applaporfik.adapter.UserReportAdapter
import com.example.applaporfik.data.api.ApiService
import com.example.applaporfik.databinding.FragmentUserReportListBinding
import com.example.applaporfik.util.NetworkUtils
import com.example.applaporfik.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserReportListFragment : Fragment() {

    private var _binding: FragmentUserReportListBinding? = null
    private val binding get() = _binding!!

    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    private lateinit var reportAdapter: UserReportAdapter

    private var statusFilter: String? = null

    companion object {
        private const val ARG_STATUS = "status_filter"

        fun newInstance(status: String?): UserReportListFragment {
            val fragment = UserReportListFragment()
            val args = Bundle().apply {
                putString(ARG_STATUS, status)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusFilter = arguments?.getString(ARG_STATUS)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserReportListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiService.create()
        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        loadReports(statusFilter)
    }

    private fun setupRecyclerView() {
        reportAdapter = UserReportAdapter(emptyList())
        binding.recyclerViewUserReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reportAdapter
        }
    }

    private fun loadReports(status: String?) {
        val sessionInfo = sessionManager.getSessionInfo() ?: return

        binding.progressBarUserReports.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                    withContext(Dispatchers.Main) {
                        binding.progressBarUserReports.visibility = View.GONE
                        Toast.makeText(requireContext(), "No internet connection.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val response = apiService.getReports(
                    token = "Bearer ${sessionInfo.token}",
                    status = status
                )

                withContext(Dispatchers.Main) {
                    binding.progressBarUserReports.visibility = View.GONE
                    if (response.success && response.reports != null) {
                        reportAdapter.updateData(response.reports)
                    } else {
                        Toast.makeText(requireContext(), response.message ?: "Failed to load reports", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBarUserReports.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
