package com.example.applaporfik.fragment.user

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.applaporfik.data.api.ApiService
import com.example.applaporfik.databinding.FragmentUserReportDetailBinding
import com.example.applaporfik.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserReportDetailFragment : Fragment() {

    private var _binding: FragmentUserReportDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager

    private var reportId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiService.create()
        sessionManager = SessionManager(requireContext())

        reportId = arguments?.getInt("report_id", -1) ?: -1

        if (reportId != -1) {
            loadReportDetail(reportId)
        } else {
            Toast.makeText(requireContext(), "Invalid report ID", Toast.LENGTH_SHORT).show()
        }

        binding.btnMarkFinished.setOnClickListener {
            markReportAsResolved(reportId)
        }
    }

    private fun loadReportDetail(id: Int) {
        val sessionInfo = sessionManager.getSessionInfo() ?: return

        binding.progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getReportById(
                    token = "Bearer ${sessionInfo.token}",
                    reportId = id
                )

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    if (response.success && response.report != null) {
                        val report = response.report
                        binding.textTitle.text = report.judul
                        binding.textDescription.text = report.rincian
                        binding.textDate.text = report.created_at.substring(0, 10)
                        binding.textStatus.text = report.status.capitalize()
                        binding.textFeedback.text = report.feedback ?: "No feedback yet."

                        if (!report.images.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(report.images[0])
                                .into(binding.imageReport)
                        }

                        if (report.status == "resolved") {
                            binding.btnMarkFinished.visibility = View.GONE
                        } else {
                            binding.btnMarkFinished.visibility = View.VISIBLE
                        }
                    } else {
                        Toast.makeText(requireContext(), response.message ?: "Failed to load report", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun markReportAsResolved(id: Int) {
        val sessionInfo = sessionManager.getSessionInfo() ?: return

        binding.btnMarkFinished.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.markReportAsResolved(
                    reportId = id,
                    token = "Bearer ${sessionInfo.token}"
                )

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        Toast.makeText(requireContext(), "Marked as resolved.", Toast.LENGTH_SHORT).show()
                        loadReportDetail(id)
                    } else {
                        Toast.makeText(requireContext(), response.message ?: "Failed to update.", Toast.LENGTH_SHORT).show()
                    }
                    binding.btnMarkFinished.isEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnMarkFinished.isEnabled = true
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
