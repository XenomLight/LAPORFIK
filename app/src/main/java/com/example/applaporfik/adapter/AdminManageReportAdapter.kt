package com.example.applaporfik.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.applaporfik.data.api.*
import com.example.applaporfik.databinding.ItemAdminManageReportBinding
import com.example.applaporfik.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminManageReportAdapter(
    private val onDataChanged: () -> Unit
) : ListAdapter<Report, AdminManageReportAdapter.ViewHolder>(ReportDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminManageReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onDataChanged)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemAdminManageReportBinding,
        private val onDataChanged: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Report) {
            binding.textTitle.text = item.judul
            binding.textCategory.text = item.kategori.capitalize()
            binding.textDate.text = item.created_at.substring(0, 10)
            binding.textDescription.text = item.rincian

            binding.textStatus.text = item.status.capitalize()
            val color = when (item.status) {
                "pending" -> android.R.color.holo_orange_dark
                "in_progress" -> android.R.color.holo_blue_dark
                "resolved" -> android.R.color.holo_green_dark
                "rejected" -> android.R.color.holo_red_dark
                else -> android.R.color.darker_gray
            }
            binding.textStatus.setTextColor(itemView.context.getColor(color))

            if (!item.feedback.isNullOrEmpty()) {
                binding.textFeedback.visibility = View.VISIBLE
                binding.textFeedback.text = "Feedback: ${item.feedback}"
            } else {
                binding.textFeedback.visibility = View.GONE
            }

            if (!item.images.isNullOrEmpty()) {
                binding.recyclerViewImages.visibility = View.VISIBLE
                binding.recyclerViewImages.layoutManager = LinearLayoutManager(
                    itemView.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                val imageAdapter = ImagePreviewAdapter(
                    isEditable = false,
                    onImageClick = { imageUrl ->
                        Toast.makeText(itemView.context, "Preview: $imageUrl", Toast.LENGTH_SHORT).show()
                    }
                )
                imageAdapter.setUrlImages(item.images ?: emptyList())
                binding.recyclerViewImages.adapter = imageAdapter
            } else {
                binding.recyclerViewImages.visibility = View.GONE
            }

            when (item.status) {
                "pending" -> {
                    binding.buttonSendFeedback.visibility = View.VISIBLE
                    binding.buttonSendFeedback.text = "MARK / REJECT"
                    binding.buttonSendFeedback.setOnClickListener {
                        showStatusChoiceDialog(item.id)
                    }
                }
                "in_progress" -> {
                    binding.buttonSendFeedback.visibility = View.VISIBLE
                    binding.buttonSendFeedback.text = "SEND FEEDBACK"
                    binding.buttonSendFeedback.setOnClickListener {
                        showFeedbackDialog(item.id, "resolved")
                    }
                }
                else -> {
                    binding.buttonSendFeedback.visibility = View.GONE
                }
            }
        }

        private fun showStatusChoiceDialog(reportId: Int) {
            val context = itemView.context
            val options = arrayOf("Mark In Progress", "Reject Feedback")

            AlertDialog.Builder(context)
                .setTitle("Choose Action")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> updateStatus(reportId, "in_progress")
                        1 -> showFeedbackDialog(reportId, "rejected")
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun showFeedbackDialog(reportId: Int, statusAfter: String) {
            val context = itemView.context
            val input = EditText(context)
            input.hint = if (statusAfter == "rejected") "Reason for rejection" else "Feedback"

            AlertDialog.Builder(context)
                .setTitle(if (statusAfter == "rejected") "Reject Feedback" else "Send Feedback")
                .setView(input)
                .setPositiveButton("Send") { _, _ ->
                    val feedback = input.text.toString().trim()
                    if (feedback.isNotEmpty()) {
                        sendFeedbackAndUpdateStatus(reportId, feedback, statusAfter)
                    } else {
                        Toast.makeText(context, "Input cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun sendFeedbackAndUpdateStatus(reportId: Int, feedback: String, statusAfter: String) {
            val context = itemView.context
            val sessionManager = SessionManager(context)
            val sessionInfo = sessionManager.getSessionInfo()
            val apiService = ApiService.create()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val responseFeedback = apiService.sendFeedback(
                        reportId,
                        "Bearer ${sessionInfo?.token}",
                        FeedbackRequest(feedback)
                    )
                    withContext(Dispatchers.Main) {
                        if (responseFeedback.success) {
                            updateStatusWithFeedback(reportId, statusAfter, feedback)
                        } else {
                            Toast.makeText(context, responseFeedback.message ?: "Failed to send feedback", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        private fun updateStatusWithFeedback(reportId: Int, status: String, feedback: String) {
            val context = itemView.context
            val sessionManager = SessionManager(context)
            val sessionInfo = sessionManager.getSessionInfo()
            val apiService = ApiService.create()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.updateReportStatus(
                        reportId,
                        "Bearer ${sessionInfo?.token}",
                        StatusRequest(status, feedback)
                    )
                    withContext(Dispatchers.Main) {
                        if (response.success) {
                            onDataChanged()
                        } else {
                            Toast.makeText(context, response.message ?: "Failed to update status", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        private fun updateStatus(reportId: Int, status: String) {
            val context = itemView.context
            val sessionManager = SessionManager(context)
            val sessionInfo = sessionManager.getSessionInfo()
            val apiService = ApiService.create()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.updateReportStatus(
                        reportId,
                        "Bearer ${sessionInfo?.token}",
                        StatusRequest(status)
                    )
                    withContext(Dispatchers.Main) {
                        if (response.success) {
                            onDataChanged()
                        } else {
                            Toast.makeText(context, response.message ?: "Failed to update status", Toast.LENGTH_SHORT).show()
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

    private class ReportDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem == newItem
        }
    }
}
