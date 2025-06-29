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
    private val isEditable: Boolean = false, // true = user upload, false = admin preview
    private val onRemoveImage: ((Int) -> Unit)? = null, // for user upload
    private val onImageClick: ((String) -> Unit)? = null // for admin preview
) : RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder>() {

    private val uriImages = mutableListOf<Uri>()      // for user upload
    private val urlImages = mutableListOf<String>()   // for admin preview

    /** For user upload */
    fun addImage(uri: Uri) {
        if (uriImages.size < 3) {
            uriImages.add(uri)
            notifyItemInserted(uriImages.size - 1)
        }
    }

    fun removeImage(position: Int) {
        if (position in uriImages.indices) {
            uriImages.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getImages(): List<Uri> = uriImages.toList()

    fun clearImages() {
        uriImages.clear()
        notifyDataSetChanged()
    }

    /** For admin preview */
    fun setUrlImages(urls: List<String>) {
        urlImages.clear()
        urlImages.addAll(urls)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int =
        if (isEditable) uriImages.size else urlImages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_preview, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if (isEditable) {
            holder.bindUri(uriImages[position], position)
        } else {
            holder.bindUrl(urlImages[position])
        }
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivImagePreview)
        private val removeButton: ImageButton = itemView.findViewById(R.id.btnRemoveImage)

        fun bindUri(uri: Uri, position: Int) {
            Glide.with(itemView.context)
                .load(uri)
                .centerCrop()
                .into(imageView)

            removeButton.visibility = View.VISIBLE
            removeButton.setOnClickListener {
                onRemoveImage?.invoke(position)
            }

            // Optional: preview image on click (user upload)
            imageView.setOnClickListener {
                // If you want user preview on click, implement here
            }
        }

        fun bindUrl(url: String) {
            Glide.with(itemView.context)
                .load(url)
                .centerCrop()
                .into(imageView)

            removeButton.visibility = View.GONE

            imageView.setOnClickListener {
                onImageClick?.invoke(url)
            }
        }
    }
}
