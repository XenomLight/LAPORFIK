package com.example.applaporfik.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.applaporfik.data.api.ApiService
import com.example.applaporfik.data.api.Report
import com.example.applaporfik.databinding.ItemAdminManageReportBinding
import com.example.applaporfik.util.NetworkUtils
import com.example.applaporfik.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.applaporfik.data.api.FeedbackRequest


class AdminManageReportAdapter(
    private val onItemClick: (reportId: Int) -> Unit
) : ListAdapter<Report, AdminManageReportAdapter.ViewHolder>(ReportDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminManageReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemAdminManageReportBinding,
        private val onItemClick: (reportId: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Report) {
            binding.textTitle.text = item.judul
            binding.textCategory.text = item.kategori.capitalize()
            binding.textDate.text = item.created_at.substring(0, 10)
            binding.textStatus.text = item.status.capitalize()

            val statusColor = when (item.status) {
                "pending" -> android.R.color.holo_orange_dark
                "in_progress" -> android.R.color.holo_blue_dark
                "resolved" -> android.R.color.holo_green_dark
                "rejected" -> android.R.color.holo_red_dark
                else -> android.R.color.darker_gray
            }
            binding.textStatus.setTextColor(itemView.context.getColor(statusColor))

            // Hide button if already resolved
            binding.buttonSendFeedback.visibility =
                if (item.status == "resolved" || item.status == "rejected") ViewGroup.GONE else ViewGroup.VISIBLE

            binding.buttonSendFeedback.setOnClickListener {
                val context = itemView.context
                val input = EditText(context)
                input.hint = "Enter feedback"

                AlertDialog.Builder(context)
                    .setTitle("Send Feedback")
                    .setView(input)
                    .setPositiveButton("Send") { _, _ ->
                        val feedback = input.text.toString().trim()
                        if (feedback.isNotEmpty()) {
                            sendFeedbackToServer(item.id, feedback)
                        } else {
                            Toast.makeText(context, "Feedback cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            itemView.setOnClickListener {
                onItemClick(item.id)
            }
        }

        private fun sendFeedbackToServer(reportId: Int, feedback: String) {
            val context = itemView.context
            val sessionManager = SessionManager(context)
            val sessionInfo = sessionManager.getSessionInfo()
            val apiService = ApiService.create()

            if (sessionInfo == null) {
                Toast.makeText(context, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                return
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (!NetworkUtils.isNetworkAvailable(context)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "No internet connection.", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    val response = apiService.sendFeedback(
                        reportId = reportId,
                        token = "Bearer ${sessionInfo.token}",
                        feedbackRequest = FeedbackRequest(feedback)
                    )


                    withContext(Dispatchers.Main) {
                        if (response.success) {
                            Toast.makeText(context, "Feedback sent successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, response.message ?: "Failed to send feedback", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private class ReportDiffCallback : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem == newItem
        }
    }
}
