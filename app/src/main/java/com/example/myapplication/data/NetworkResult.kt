package com.example.myapplication.data

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}

suspend fun <T> safeApiCall(call: suspend () -> retrofit2.Response<T>): NetworkResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error("Empty response body", response.code())
            }
        } else {
            val errorBody = response.errorBody()?.string()
            NetworkResult.Error(errorBody ?: "Unknown error", response.code())
        }
    } catch (e: Exception) {
        NetworkResult.Error(e.message ?: "Network error")
    }
}
