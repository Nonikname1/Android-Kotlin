package com.example.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.NetworkResult
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val userName: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object LoggedOut : AuthUiState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = repository.login(email, password)) {
                is NetworkResult.Success ->
                    _uiState.value = AuthUiState.Success(result.data.user.fullName)
                is NetworkResult.Error ->
                    _uiState.value = AuthUiState.Error(result.message)
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun register(fullName: String, email: String, password: String, phone: String?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = repository.register(fullName, email, password, phone)) {
                is NetworkResult.Success ->
                    _uiState.value = AuthUiState.Success(result.data.user.fullName)
                is NetworkResult.Error ->
                    _uiState.value = AuthUiState.Error(result.message)
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.logout()
            _uiState.value = AuthUiState.LoggedOut
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AuthViewModel(repository) as T
}
