package com.example.myapplication.data.api

import com.example.myapplication.BuildConfig
import com.example.myapplication.data.local.TokenManager
import com.example.myapplication.data.models.TokenResponse
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(private val tokenManager: TokenManager) : Authenticator {

    private val json = Json { ignoreUnknownKeys = true }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val refreshToken = runBlocking { tokenManager.refreshToken.firstOrNull() }
            ?: return null

        val body = """{"refreshToken":"$refreshToken"}"""
            .toRequestBody("application/json".toMediaType())

        val refreshRequest = Request.Builder()
            .url("${BuildConfig.BASE_URL}auth/refresh")
            .post(body)
            .build()

        val refreshResponse = try {
            OkHttpClient().newCall(refreshRequest).execute()
        } catch (e: Exception) {
            return null
        }

        if (!refreshResponse.isSuccessful) {
            runBlocking { tokenManager.clearAll() }
            return null
        }

        val responseBody = refreshResponse.body?.string() ?: return null
        val tokens = try {
            json.decodeFromString<TokenResponse>(responseBody)
        } catch (e: Exception) {
            return null
        }

        runBlocking { tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken) }

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${tokens.accessToken}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) { count++; prior = prior.priorResponse }
        return count
    }
}
