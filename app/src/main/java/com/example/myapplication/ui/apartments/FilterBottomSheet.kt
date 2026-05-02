package com.example.myapplication.ui.apartments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.myapplication.R
import com.example.myapplication.data.paging.ApartmentFilters
import com.example.myapplication.databinding.FragmentFilterBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterBottomSheet : BottomSheetDialogFragment() {

    interface OnFiltersAppliedListener {
        fun onFiltersApplied(filters: ApartmentFilters)
    }

    var listener: OnFiltersAppliedListener? = null

    private var _binding: FragmentFilterBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restoreCurrentFilters()
        updateSortOrderVisibility()

        binding.sortByChipGroup.setOnCheckedStateChangeListener { _, _ ->
            updateSortOrderVisibility()
        }

        binding.resetButton.setOnClickListener {
            binding.statusChipGroup.check(R.id.chipStatusAll)
            binding.listingTypeChipGroup.check(R.id.chipTypeAll)
            binding.roomsChipGroup.check(R.id.chipRoomsAll)
            binding.sortByChipGroup.check(R.id.chipSortDefault)
            binding.sortOrderChipGroup.check(R.id.chipSortAsc)
        }

        binding.applyButton.setOnClickListener {
            listener?.onFiltersApplied(buildFilters())
            dismiss()
        }
    }

    private fun updateSortOrderVisibility() {
        val sortActive = binding.sortByChipGroup.checkedChipId != R.id.chipSortDefault
        binding.sortOrderLabel.isVisible = sortActive
        binding.sortOrderChipGroup.isVisible = sortActive
    }

    private fun restoreCurrentFilters() {
        val status = arguments?.getString(ARG_STATUS)
        val listingType = arguments?.getString(ARG_LISTING_TYPE)
        val rooms = arguments?.getInt(ARG_ROOMS, 0) ?: 0
        val sortBy = arguments?.getString(ARG_SORT_BY)
        val sortOrder = arguments?.getString(ARG_SORT_ORDER) ?: "asc"

        binding.statusChipGroup.check(
            when (status) {
                "available" -> R.id.chipAvailable
                "reserved"  -> R.id.chipReserved
                "sold"      -> R.id.chipSold
                "cancelled" -> R.id.chipCancelled
                else        -> R.id.chipStatusAll
            }
        )
        binding.listingTypeChipGroup.check(
            when (listingType) {
                "sale" -> R.id.chipSale
                "rent" -> R.id.chipRent
                else   -> R.id.chipTypeAll
            }
        )
        binding.roomsChipGroup.check(
            when (rooms) {
                1 -> R.id.chipRooms1
                2 -> R.id.chipRooms2
                3 -> R.id.chipRooms3
                4 -> R.id.chipRooms4
                5 -> R.id.chipRooms5
                else -> R.id.chipRoomsAll
            }
        )
        binding.sortByChipGroup.check(
            when (sortBy) {
                "price"      -> R.id.chipSortPrice
                "totalArea"  -> R.id.chipSortArea
                "createdAt"  -> R.id.chipSortDate
                else         -> R.id.chipSortDefault
            }
        )
        binding.sortOrderChipGroup.check(
            if (sortOrder == "desc") R.id.chipSortDesc else R.id.chipSortAsc
        )
    }

    private fun buildFilters(): ApartmentFilters {
        val status = when (binding.statusChipGroup.checkedChipId) {
            R.id.chipAvailable -> "available"
            R.id.chipReserved  -> "reserved"
            R.id.chipSold      -> "sold"
            R.id.chipCancelled -> "cancelled"
            else               -> null
        }
        val listingType = when (binding.listingTypeChipGroup.checkedChipId) {
            R.id.chipSale -> "sale"
            R.id.chipRent -> "rent"
            else          -> null
        }
        val rooms = when (binding.roomsChipGroup.checkedChipId) {
            R.id.chipRooms1 -> 1
            R.id.chipRooms2 -> 2
            R.id.chipRooms3 -> 3
            R.id.chipRooms4 -> 4
            R.id.chipRooms5 -> 5
            else            -> null
        }
        val sortBy = when (binding.sortByChipGroup.checkedChipId) {
            R.id.chipSortPrice -> "price"
            R.id.chipSortArea  -> "totalArea"
            R.id.chipSortDate  -> "createdAt"
            else               -> null
        }
        val sortOrder = if (sortBy != null) {
            if (binding.sortOrderChipGroup.checkedChipId == R.id.chipSortDesc) "desc" else "asc"
        } else null

        return ApartmentFilters(
            status = status,
            listingType = listingType,
            rooms = rooms,
            sortBy = sortBy,
            sortOrder = sortOrder
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FilterBottomSheet"
        private const val ARG_STATUS = "status"
        private const val ARG_LISTING_TYPE = "listingType"
        private const val ARG_ROOMS = "rooms"
        private const val ARG_SORT_BY = "sortBy"
        private const val ARG_SORT_ORDER = "sortOrder"

        fun newInstance(filters: ApartmentFilters) = FilterBottomSheet().apply {
            arguments = Bundle().apply {
                filters.status?.let { putString(ARG_STATUS, it) }
                filters.listingType?.let { putString(ARG_LISTING_TYPE, it) }
                filters.rooms?.let { putInt(ARG_ROOMS, it) }
                filters.sortBy?.let { putString(ARG_SORT_BY, it) }
                filters.sortOrder?.let { putString(ARG_SORT_ORDER, it) }
            }
        }
    }
}
