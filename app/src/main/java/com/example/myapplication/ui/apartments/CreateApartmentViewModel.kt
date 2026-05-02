package com.example.myapplication.ui.apartments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.NetworkResult
import com.example.myapplication.data.models.ApartmentDto
import com.example.myapplication.data.models.CreateApartmentRequest
import com.example.myapplication.data.models.UpdateApartmentRequest
import com.example.myapplication.data.repository.ApartmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CreateUiState {
    object Idle : CreateUiState()
    object LoadingApartment : CreateUiState()
    data class ApartmentLoaded(val apartment: ApartmentDto) : CreateUiState()
    object Saving : CreateUiState()
    object SaveSuccess : CreateUiState()
    data class Error(val message: String) : CreateUiState()
}

class CreateApartmentViewModel(private val repository: ApartmentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateUiState>(CreateUiState.Idle)
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    private var editingId: String? = null
    val isEditMode get() = editingId != null

    fun initialize(apartmentId: String?) {
        if (!apartmentId.isNullOrBlank()) {
            editingId = apartmentId
            viewModelScope.launch {
                _uiState.value = CreateUiState.LoadingApartment
                when (val result = repository.getApartmentById(apartmentId)) {
                    is NetworkResult.Success -> _uiState.value = CreateUiState.ApartmentLoaded(result.data)
                    is NetworkResult.Error   -> _uiState.value = CreateUiState.Error(result.message)
                    NetworkResult.Loading    -> {}
                }
            }
        }
    }

    fun submit(
        fullAddress: String,
        floor: Int,
        totalFloors: Int?,
        rooms: Int,
        totalArea: Double,
        price: Long,
        listingType: String,
        description: String?
    ) {
        val id = editingId
        viewModelScope.launch {
            _uiState.value = CreateUiState.Saving
            val result = if (id == null) {
                repository.createApartment(
                    CreateApartmentRequest(
                        fullAddress = fullAddress,
                        floor = floor,
                        totalFloors = totalFloors,
                        rooms = rooms,
                        totalArea = totalArea,
                        price = price,
                        listingType = listingType,
                        description = description?.takeIf { it.isNotBlank() }
                    )
                )
            } else {
                repository.updateApartment(
                    id,
                    UpdateApartmentRequest(
                        fullAddress = fullAddress,
                        floor = floor,
                        totalFloors = totalFloors,
                        rooms = rooms,
                        totalArea = totalArea,
                        price = price,
                        listingType = listingType,
                        description = description?.takeIf { it.isNotBlank() }
                    )
                )
            }
            when (result) {
                is NetworkResult.Success -> _uiState.value = CreateUiState.SaveSuccess
                is NetworkResult.Error   -> {
                    val code = result.code ?: 0
                    _uiState.value = CreateUiState.Error("HTTP $code: ${result.message}")
                }
                NetworkResult.Loading    -> {}
            }
        }
    }
}

class CreateApartmentViewModelFactory(
    private val repository: ApartmentRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CreateApartmentViewModel(repository) as T
}
