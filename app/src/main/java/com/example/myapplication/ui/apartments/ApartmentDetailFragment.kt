package com.example.myapplication.ui.apartments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.data.models.ApartmentDto
import com.example.myapplication.databinding.FragmentApartmentDetailBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ApartmentDetailFragment : Fragment() {

    private var _binding: FragmentApartmentDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ApartmentDetailViewModel by viewModels {
        ApartmentDetailViewModelFactory((requireActivity().application as App).apartmentRepository)
    }

    private lateinit var photoAdapter: PhotoAdapter
    private var apartmentId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApartmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apartmentId = arguments?.getString(ARG_APARTMENT_ID)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        setupPhotosRecyclerView()
        observeUiState()
        observeErrors()
        observeStatusChanged()

        binding.retryButton.setOnClickListener {
            apartmentId?.let { viewModel.loadApartment(it) }
        }

        apartmentId?.let { viewModel.loadApartment(it) }
    }

    private fun setupPhotosRecyclerView() {
        photoAdapter = PhotoAdapter()
        binding.photosRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.progressBar.isVisible = state is DetailUiState.Loading
                binding.errorContainer.isVisible = state is DetailUiState.Error
                binding.scrollView.isVisible = state is DetailUiState.Success

                when (state) {
                    is DetailUiState.Error   -> binding.errorText.text = state.message
                    is DetailUiState.Success -> bindApartment(state.apartment)
                    else -> {}
                }
            }
        }
    }

    private fun observeErrors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorEvent.collectLatest { message ->
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun observeStatusChanged() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.statusChangedEvent.collectLatest {
                // уведомляем список квартир что нужно обновиться
                findNavController().previousBackStackEntry
                    ?.savedStateHandle?.set("refreshList", true)
            }
        }
    }

    private fun bindApartment(apt: ApartmentDto) {
        binding.toolbar.title = apt.fullAddress.take(30)

        // Photos
        if (apt.photos.isNotEmpty()) {
            binding.photosRecyclerView.isVisible = true
            photoAdapter.submitList(apt.photos.sortedWith(compareByDescending { it.isMain }))
        }

        // Chips
        val typeLabel = if (apt.listingType == "sale") "Продажа" else "Аренда"
        val typeColor = if (apt.listingType == "sale") 0xFF42A5F5.toInt() else 0xFFAB47BC.toInt()
        binding.listingTypeChip.text = typeLabel
        binding.listingTypeChip.chipBackgroundColor = ColorStateList.valueOf(typeColor)

        val (statusLabel, statusColor) = when (apt.status) {
            "available" -> "Доступна" to 0xFF4CAF50.toInt()
            "reserved"  -> "Бронь"    to 0xFFFFA726.toInt()
            "sold"      -> "Продана"  to 0xFFBDBDBD.toInt()
            "cancelled" -> "Отменена" to 0xFFEF5350.toInt()
            else        -> apt.status to 0xFF9E9E9E.toInt()
        }
        binding.statusChip.text = statusLabel
        binding.statusChip.chipBackgroundColor = ColorStateList.valueOf(statusColor)

        // Buttons
        binding.changeStatusButton.setOnClickListener { showStatusDialog(apt) }
        binding.editButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_detail_to_edit,
                bundleOf(CreateApartmentFragment.ARG_APARTMENT_ID to apt.id)
            )
        }

        // Address & details
        binding.addressText.text = apt.fullAddress
        val details = buildString {
            append("${apt.rooms} комн. · ${apt.totalArea} м²")
            apt.livingArea?.let { append(" · жил. ${it} м²") }
            append(" · ${apt.floor} эт.")
            apt.totalFloors?.let { append("/$it") }
        }
        binding.detailsText.text = details

        // Price
        binding.priceText.text = formatPrice(apt.price)
        apt.pricePerSqm?.let { ppsm ->
            binding.priceText.text = "${formatPrice(apt.price)} (${formatPrice(ppsm)}/м²)"
        }

        // Description
        if (!apt.description.isNullOrBlank()) {
            binding.descriptionLabel.isVisible = true
            binding.descriptionText.isVisible = true
            binding.descriptionText.text = apt.description
        }

        // Meta
        binding.viewsText.text = "Просмотры: ${apt.viewsCount}"
        binding.datesText.text = "Добавлено: ${apt.createdAt.take(10)}"
    }

    private fun showStatusDialog(apt: ApartmentDto) {
        val dialog = StatusChangeDialog.newInstance(apt.status)
        dialog.listener = object : StatusChangeDialog.OnStatusChangedListener {
            override fun onStatusChanged(newStatus: String, comment: String?) {
                viewModel.changeStatus(apt.id, newStatus, comment)
            }
        }
        dialog.show(parentFragmentManager, StatusChangeDialog.TAG)
    }

    private fun formatPrice(price: Long): String = when {
        price >= 1_000_000 -> "%.1f млн ₽".format(price / 1_000_000.0)
        price >= 1_000     -> "${price / 1_000} тыс. ₽"
        else               -> "$price ₽"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_APARTMENT_ID = "apartmentId"
    }
}
