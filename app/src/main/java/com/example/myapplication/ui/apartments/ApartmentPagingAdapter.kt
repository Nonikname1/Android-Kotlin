package com.example.myapplication.ui.apartments

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.models.ApartmentDto
import com.example.myapplication.databinding.ItemApartmentBinding

class ApartmentPagingAdapter(
    private val onClick: (ApartmentDto) -> Unit
) : PagingDataAdapter<ApartmentDto, ApartmentPagingAdapter.ApartmentViewHolder>(DIFF) {

    inner class ApartmentViewHolder(private val binding: ItemApartmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ApartmentDto) {
            binding.addressText.text = item.fullAddress
            binding.detailsText.text = "${item.rooms} комн. · ${item.totalArea} м² · ${item.floor} эт."
            binding.priceText.text = formatPrice(item.price)

            val typeLabel = if (item.listingType == "sale") "Продажа" else "Аренда"
            val typeColor = if (item.listingType == "sale") 0xFF42A5F5.toInt() else 0xFFAB47BC.toInt()
            binding.listingTypeChip.text = typeLabel
            binding.listingTypeChip.chipBackgroundColor = ColorStateList.valueOf(typeColor)

            val (statusLabel, statusColor) = when (item.status) {
                "available" -> "Доступна" to 0xFF4CAF50.toInt()
                "reserved"  -> "Бронь"    to 0xFFFFA726.toInt()
                "sold"      -> "Продана"  to 0xFFBDBDBD.toInt()
                "cancelled" -> "Отменена" to 0xFFEF5350.toInt()
                else        -> item.status to 0xFF9E9E9E.toInt()
            }
            binding.statusChip.text = statusLabel
            binding.statusChip.chipBackgroundColor = ColorStateList.valueOf(statusColor)

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApartmentViewHolder {
        val binding = ItemApartmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ApartmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ApartmentViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    private fun formatPrice(price: Long): String = when {
        price >= 1_000_000 -> "%.1f млн ₽".format(price / 1_000_000.0)
        price >= 1_000     -> "${price / 1_000} тыс. ₽"
        else               -> "$price ₽"
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ApartmentDto>() {
            override fun areItemsTheSame(old: ApartmentDto, new: ApartmentDto) = old.id == new.id
            override fun areContentsTheSame(old: ApartmentDto, new: ApartmentDto) = old == new
        }
    }
}
