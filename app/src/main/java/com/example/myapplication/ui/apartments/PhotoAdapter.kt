package com.example.myapplication.ui.apartments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.myapplication.R
import com.example.myapplication.data.models.PhotoDto
import com.example.myapplication.databinding.ItemPhotoBinding

class PhotoAdapter : ListAdapter<PhotoDto, PhotoAdapter.PhotoViewHolder>(DIFF) {

    inner class PhotoViewHolder(private val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: PhotoDto) {
            binding.photoImage.load(photo.thumbnailUrl ?: photo.url) {
                placeholder(R.drawable.ic_photo_placeholder)
                error(R.drawable.ic_photo_placeholder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PhotoDto>() {
            override fun areItemsTheSame(old: PhotoDto, new: PhotoDto) = old.id == new.id
            override fun areContentsTheSame(old: PhotoDto, new: PhotoDto) = old == new
        }
    }
}
