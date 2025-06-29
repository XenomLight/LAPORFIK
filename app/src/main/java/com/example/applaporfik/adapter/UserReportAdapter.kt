package com.example.applaporfik.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.applaporfik.data.api.Report
import com.example.applaporfik.databinding.ItemUserReportBinding

class UserReportAdapter(
    private var reports: List<Report>
) : RecyclerView.Adapter<UserReportAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemUserReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(report: Report) {
            binding.textTitle.text = report.judul
            binding.textDate.text = report.created_at.substring(0, 10)
            binding.textStatus.text = report.status.capitalize()
            binding.textDescription.text = report.rincian.ifEmpty { "-" }

            val feedbackText = if (!report.feedback.isNullOrEmpty()) {
                "Feedback: ${report.feedback}"
            } else {
                "No feedback."
            }
            binding.textFeedback.text = feedbackText

            val statusColor = when (report.status.lowercase()) {
                "pending" -> android.R.color.holo_orange_dark
                "in_progress" -> android.R.color.holo_blue_dark
                "resolved" -> android.R.color.holo_green_dark
                "rejected" -> android.R.color.holo_red_dark
                else -> android.R.color.darker_gray
            }
            binding.textStatus.setTextColor(itemView.context.getColor(statusColor))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = reports.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    fun updateData(newReports: List<Report>) {
        reports = newReports
        notifyDataSetChanged()
    }
}
