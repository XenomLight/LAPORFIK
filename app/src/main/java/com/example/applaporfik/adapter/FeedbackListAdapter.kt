package com.example.applaporfik.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.applaporfik.databinding.ItemFeedbackListBinding
import com.example.applaporfik.model.FeedbackItem
import com.example.applaporfik.model.FeedbackStatus

class FeedbackListAdapter : ListAdapter<FeedbackItem, FeedbackListAdapter.FeedbackViewHolder>(FeedbackItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val binding = ItemFeedbackListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedbackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class FeedbackViewHolder(private val binding: ItemFeedbackListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FeedbackItem) {
            binding.textViewListItemTitle.text = item.title
            binding.textViewListItemDate.text = item.date
            
            // Set Status Chip text and background
            if (item.status == FeedbackStatus.FINISHED) {
                binding.chipListItemStatus.text = "Finished"
                binding.chipListItemStatus.setChipBackgroundColorResource(android.R.color.holo_green_light)
                binding.chipListItemStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
            } else {
                binding.chipListItemStatus.text = "Reported"
                binding.chipListItemStatus.setChipBackgroundColorResource(android.R.color.holo_orange_light)
                binding.chipListItemStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_orange_dark))
            }
        }
    }

    private class FeedbackItemDiffCallback : DiffUtil.ItemCallback<FeedbackItem>() {
        override fun areItemsTheSame(oldItem: FeedbackItem, newItem: FeedbackItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FeedbackItem, newItem: FeedbackItem): Boolean {
            return oldItem == newItem
        }
    }
} 