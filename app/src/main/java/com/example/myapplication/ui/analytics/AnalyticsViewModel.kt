package com.example.myapplication.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.NetworkResult
import com.example.myapplication.data.models.StatsResponse
import com.example.myapplication.data.repository.ApartmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success(val stats: StatsResponse) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}

class AnalyticsViewModel(private val repository: ApartmentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = AnalyticsUiState.Loading
            when (val result = repository.getStats()) {
                is NetworkResult.Success -> _uiState.value = AnalyticsUiState.Success(result.data)
                is NetworkResult.Error   -> _uiState.value = AnalyticsUiState.Error(result.message)
                NetworkResult.Loading    -> {}
            }
        }
    }
}

class AnalyticsViewModelFactory(
    private val repository: ApartmentRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AnalyticsViewModel(repository) as T
}
