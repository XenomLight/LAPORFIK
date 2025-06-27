package com.example.applaporfik.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.applaporfik.databinding.ItemRecentFeedbackBinding
import com.example.applaporfik.model.FeedbackItem
import com.example.applaporfik.model.FeedbackStatus

class RecentFeedbackAdapter : ListAdapter<FeedbackItem, RecentFeedbackAdapter.ViewHolder>(FeedbackDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentFeedbackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemRecentFeedbackBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: FeedbackItem) {
            binding.textTitle.text = item.title
            binding.textCategory.text = item.category.name
            binding.textDate.text = item.date
            
            // Set status text and color
            when (item.status) {
                FeedbackStatus.REPORTED -> {
                    binding.textStatus.text = "Reported"
                    binding.textStatus.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                }
                FeedbackStatus.FINISHED -> {
                    binding.textStatus.text = "Finished"
                    binding.textStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                }
            }
        }
    }

    private class FeedbackDiffCallback : DiffUtil.ItemCallback<FeedbackItem>() {
        override fun areItemsTheSame(oldItem: FeedbackItem, newItem: FeedbackItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FeedbackItem, newItem: FeedbackItem): Boolean {
            return oldItem == newItem
        }
    }
} 