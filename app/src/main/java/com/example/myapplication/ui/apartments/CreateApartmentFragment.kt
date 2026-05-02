package com.example.myapplication.ui.apartments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentCreateApartmentBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateApartmentFragment : Fragment() {

    private var _binding: FragmentCreateApartmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateApartmentViewModel by viewModels {
        CreateApartmentViewModelFactory((requireActivity().application as App).apartmentRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateApartmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apartmentId = arguments?.getString(ARG_APARTMENT_ID)?.takeIf { it.isNotBlank() }

        viewModel.initialize(apartmentId)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.listingTypeToggle.check(R.id.btnSale)

        observeUiState()

        binding.saveButton.setOnClickListener { submitForm() }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                val isBusy = state is CreateUiState.LoadingApartment || state is CreateUiState.Saving
                binding.progressBar.isVisible = isBusy
                binding.saveButton.isEnabled = !isBusy

                when (state) {
                    is CreateUiState.ApartmentLoaded -> prefillForm(state.apartment)
                    is CreateUiState.SaveSuccess     -> findNavController().navigateUp()
                    is CreateUiState.Error           -> {
                        val msg = state.message.take(200)
                        Snackbar.make(binding.root, msg, Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK") {}
                            .show()
                        android.util.Log.e("CreateApartment", "Error: ${state.message}")
                    }
                    else -> {}
                }

                binding.toolbar.title = if (viewModel.isEditMode)
                    getString(R.string.edit_apartment_title)
                else
                    getString(R.string.create_apartment_title)
            }
        }
    }

    private fun prefillForm(apt: com.example.myapplication.data.models.ApartmentDto) {
        binding.addressInput.setText(apt.fullAddress)
        binding.floorInput.setText(apt.floor.toString())
        apt.totalFloors?.let { binding.totalFloorsInput.setText(it.toString()) }
        binding.roomsInput.setText(apt.rooms.toString())
        binding.areaInput.setText(apt.totalArea.toString())
        binding.priceInput.setText(apt.price.toString())
        binding.descriptionInput.setText(apt.description.orEmpty())
        binding.listingTypeToggle.check(
            if (apt.listingType == "rent") R.id.btnRent else R.id.btnSale
        )
    }

    private fun submitForm() {
        val address = binding.addressInput.text?.toString().orEmpty().trim()
        val floorStr = binding.floorInput.text?.toString().orEmpty()
        val totalFloorsStr = binding.totalFloorsInput.text?.toString().orEmpty()
        val roomsStr = binding.roomsInput.text?.toString().orEmpty()
        val areaStr = binding.areaInput.text?.toString().orEmpty()
        val priceStr = binding.priceInput.text?.toString().orEmpty()
        val description = binding.descriptionInput.text?.toString().orEmpty()

        if (address.isBlank() || floorStr.isBlank() || roomsStr.isBlank() ||
            areaStr.isBlank() || priceStr.isBlank()
        ) {
            Snackbar.make(binding.root, getString(R.string.error_fill_all_fields), Snackbar.LENGTH_SHORT).show()
            return
        }

        val floor = floorStr.toIntOrNull()
        val rooms = roomsStr.toIntOrNull()
        val area = areaStr.replace(',', '.').toDoubleOrNull()
        val price = priceStr.toLongOrNull()

        if (floor == null || rooms == null || area == null || price == null) {
            Snackbar.make(binding.root, getString(R.string.error_invalid_number), Snackbar.LENGTH_SHORT).show()
            return
        }

        val totalFloors = totalFloorsStr.toIntOrNull()
        val listingType = if (binding.listingTypeToggle.checkedButtonId == R.id.btnRent) "rent" else "sale"

        viewModel.submit(
            fullAddress = address,
            floor = floor,
            totalFloors = totalFloors,
            rooms = rooms,
            totalArea = area,
            price = price,
            listingType = listingType,
            description = description
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_APARTMENT_ID = "apartmentId"
    }
}
