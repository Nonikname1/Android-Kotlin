package com.example.myapplication.ui.apartments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemLoadStateBinding

class ApartmentLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<ApartmentLoadStateAdapter.LoadStateViewHolder>() {

    inner class LoadStateViewHolder(private val binding: ItemLoadStateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(state: LoadState) {
            binding.progressBar.isVisible = state is LoadState.Loading
            binding.errorText.isVisible = state is LoadState.Error
            binding.retryButton.isVisible = state is LoadState.Error
            if (state is LoadState.Error) {
                binding.errorText.text = state.error.localizedMessage
            }
            binding.retryButton.setOnClickListener { retry() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val binding = ItemLoadStateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LoadStateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
}
