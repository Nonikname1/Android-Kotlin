package com.example.myapplication.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("fullName") val fullName: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("phone") val phone: String? = null
)

@Serializable
data class LoginRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refreshToken") val refreshToken: String
)

@Serializable
data class AuthResponse(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("user") val user: UserDto
)

@Serializable
data class TokenResponse(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String
)

@Serializable
data class UserDto(
    @SerialName("id") val id: String,
    @SerialName("fullName") val fullName: String,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String? = null,
    @SerialName("avatarUrl") val avatarUrl: String? = null
)
