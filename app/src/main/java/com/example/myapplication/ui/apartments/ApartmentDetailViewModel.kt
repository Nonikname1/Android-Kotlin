package com.example.myapplication.ui.apartments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.NetworkResult
import com.example.myapplication.data.models.ApartmentDto
import com.example.myapplication.data.models.UpdateApartmentRequest
import com.example.myapplication.data.repository.ApartmentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val apartment: ApartmentDto) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

class ApartmentDetailViewModel(private val repository: ApartmentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    private val _statusChangedEvent = MutableSharedFlow<Unit>()
    val statusChangedEvent: SharedFlow<Unit> = _statusChangedEvent.asSharedFlow()

    fun loadApartment(id: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            when (val result = repository.getApartmentById(id)) {
                is NetworkResult.Success -> _uiState.value = DetailUiState.Success(result.data)
                is NetworkResult.Error   -> _uiState.value = DetailUiState.Error(result.message)
                NetworkResult.Loading    -> {}
            }
        }
    }

    fun changeStatus(id: String, newStatus: String, comment: String?) {
        viewModelScope.launch {
            val request = UpdateApartmentRequest(
                status = newStatus,
                statusComment = comment?.takeIf { it.isNotBlank() }
            )
            when (val result = repository.updateApartment(id, request)) {
                is NetworkResult.Success -> {
                    _uiState.value = DetailUiState.Success(result.data)
                    _statusChangedEvent.emit(Unit)
                }
                is NetworkResult.Error   -> _errorEvent.emit(result.message)
                NetworkResult.Loading    -> {}
            }
        }
    }
}

class ApartmentDetailViewModelFactory(
    private val repository: ApartmentRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ApartmentDetailViewModel(repository) as T
}
