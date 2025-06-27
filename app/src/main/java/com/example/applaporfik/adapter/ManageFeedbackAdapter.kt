package com.example.applaporfik.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.applaporfik.databinding.ItemManageFeedbackBinding
import com.example.applaporfik.model.FeedbackItem

class ManageFeedbackAdapter : RecyclerView.Adapter<ManageFeedbackAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemManageFeedbackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // TODO: Bind data
    }

    override fun getItemCount(): Int = 0 // TODO: Return actual item count

    class ViewHolder(val binding: ItemManageFeedbackBinding) : RecyclerView.ViewHolder(binding.root)
} 