package com.example.myapplication.data.repository

import com.example.myapplication.data.NetworkResult
import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.local.TokenManager
import com.example.myapplication.data.models.AuthResponse
import com.example.myapplication.data.models.LoginRequest
import com.example.myapplication.data.models.RefreshTokenRequest
import com.example.myapplication.data.models.RegisterRequest
import com.example.myapplication.data.safeApiCall
import kotlinx.coroutines.flow.firstOrNull

class AuthRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phone: String?
    ): NetworkResult<AuthResponse> {
        val result = safeApiCall {
            api.register(RegisterRequest(fullName, email, password, phone))
        }
        if (result is NetworkResult.Success) {
            saveSession(result.data)
        }
        return result
    }

    suspend fun login(email: String, password: String): NetworkResult<AuthResponse> {
        val result = safeApiCall { api.login(LoginRequest(email, password)) }
        if (result is NetworkResult.Success) {
            saveSession(result.data)
        }
        return result
    }

    suspend fun logout() {
        val refreshToken = tokenManager.refreshToken.firstOrNull()
        if (!refreshToken.isNullOrBlank()) {
            safeApiCall { api.logout(RefreshTokenRequest(refreshToken)) }
        }
        tokenManager.clearAll()
    }

    suspend fun isLoggedIn(): Boolean {
        return !tokenManager.accessToken.firstOrNull().isNullOrBlank()
    }

    private suspend fun saveSession(response: AuthResponse) {
        tokenManager.saveTokens(response.accessToken, response.refreshToken)
        tokenManager.saveUserInfo(
            id = response.user.id,
            name = response.user.fullName,
            email = response.user.email
        )
    }
}
