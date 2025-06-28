package com.example.applaporfik.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.applaporfik.R

class ImagePreviewAdapter(
    private val onRemoveImage: (Int) -> Unit
) : RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder>() {

    private val images = mutableListOf<Uri>()

    fun addImage(uri: Uri) {
        if (images.size < 3) {
            images.add(uri)
            notifyItemInserted(images.size - 1)
        }
    }

    fun removeImage(position: Int) {
        if (position in 0 until images.size) {
            images.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getImages(): List<Uri> = images.toList()

    fun clearImages() {
        images.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_preview, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position], position)
    }

    override fun getItemCount(): Int = images.size

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivImagePreview)
        private val removeButton: ImageButton = itemView.findViewById(R.id.btnRemoveImage)

        fun bind(uri: Uri, position: Int) {
            Glide.with(itemView.context)
                .load(uri)
                .centerCrop()
                .into(imageView)

            removeButton.setOnClickListener {
                onRemoveImage(position)
            }
        }
    }
} 