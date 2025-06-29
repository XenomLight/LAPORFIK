package com.example.applaporfik.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider // ✅ IMPORT PENTING AGAR TIDAK MERAH
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.applaporfik.adapter.AdminManageReportAdapter
import com.example.applaporfik.data.api.ApiService
import com.example.applaporfik.databinding.FragmentAdminManageReportListBinding
import com.example.applaporfik.util.NetworkUtils
import com.example.applaporfik.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminManageReportListFragment : Fragment() {

    private var _binding: FragmentAdminManageReportListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AdminManageReportAdapter
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    private lateinit var adminReportViewModel: AdminReportViewModel

    private var statusFilter: String? = null

    companion object {
        private const val ARG_STATUS = "arg_status"

        fun newInstance(status: String): AdminManageReportListFragment {
            val fragment = AdminManageReportListFragment()
            val args = Bundle()
            args.putString(ARG_STATUS, status)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusFilter = arguments?.getString(ARG_STATUS)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminManageReportListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ INI AGAR ViewModel tidak merah
        adminReportViewModel = ViewModelProvider(requireActivity())[AdminReportViewModel::class.java]

        adminReportViewModel.shouldReload.observe(viewLifecycleOwner) { shouldReload ->
            if (shouldReload == true) {
                loadReports()
                adminReportViewModel.resetReload()
            }
        }

        apiService = ApiService.create()
        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        loadReports()
    }

    private fun setupRecyclerView() {
        adapter = AdminManageReportAdapter {
            // dipanggil saat feedback/status diubah
            loadReports()
        }
        binding.recyclerViewAdminManageList.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewAdminManageList.adapter = adapter
    }

    private fun loadReports() {
        val sessionInfo = sessionManager.getSessionInfo() ?: return
        binding.progressBarAdminManageList.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                        binding.progressBarAdminManageList.visibility = View.GONE
                    }
                    return@launch
                }

                val response = apiService.getReports(
                    token = "Bearer ${sessionInfo.token}",
                    status = statusFilter
                )
                withContext(Dispatchers.Main) {
                    binding.progressBarAdminManageList.visibility = View.GONE
                    if (response.success && response.reports != null) {
                        adapter.submitList(response.reports)
                    } else {
                        Toast.makeText(
                            context,
                            response.message ?: "Failed to load reports",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBarAdminManageList.visibility = View.GONE
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
