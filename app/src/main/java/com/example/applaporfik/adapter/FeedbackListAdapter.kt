package com.example.applaporfik.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.applaporfik.databinding.ItemFeedbackListBinding
import com.example.applaporfik.data.api.Report
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.applaporfik.data.api.FeedbackRequest

class FeedbackListAdapter : ListAdapter<Report, FeedbackListAdapter.FeedbackViewHolder>(FeedbackItemDiffCallback()) {

    private val reports = mutableListOf<Report>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val binding = ItemFeedbackListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedbackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    fun setReports(newReports: List<Report>) {
        reports.clear()
        reports.addAll(newReports)
        submitList(reports.toList())
    }

    fun addReports(newReports: List<Report>) {
        reports.addAll(newReports)
        submitList(reports.toList())
    }

    fun clearReports() {
        reports.clear()
        submitList(emptyList())
    }

    class FeedbackViewHolder(private val binding: ItemFeedbackListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Report) {
            binding.textViewListItemTitle.text = item.judul
            binding.textViewListItemDate.text = formatDate(item.created_at)
            
            // Set Status Chip text and background
            when (item.status.lowercase()) {
                "completed", "finished" -> {
                    binding.chipListItemStatus.text = "Completed"
                    binding.chipListItemStatus.setChipBackgroundColorResource(android.R.color.holo_green_light)
                    binding.chipListItemStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
                }
                "in_progress", "processing" -> {
                    binding.chipListItemStatus.text = "In Progress"
                    binding.chipListItemStatus.setChipBackgroundColorResource(android.R.color.holo_blue_light)
                    binding.chipListItemStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_blue_dark))
                }
                else -> {
                    binding.chipListItemStatus.text = "Pending"
                    binding.chipListItemStatus.setChipBackgroundColorResource(android.R.color.holo_orange_light)
                    binding.chipListItemStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_orange_dark))
                }
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date ?: return dateString)
            } catch (e: Exception) {
                dateString
            }
        }
    }

    private class FeedbackItemDiffCallback : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem == newItem
        }
    }
} 