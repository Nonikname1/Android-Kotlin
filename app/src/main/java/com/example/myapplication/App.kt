package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.local.TokenManager
import com.example.myapplication.data.repository.ApartmentRepository
import com.example.myapplication.data.repository.AuthRepository

class App : Application() {

    lateinit var tokenManager: TokenManager
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var apartmentRepository: ApartmentRepository
        private set

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(this)
        val api = RetrofitClient.create(tokenManager)
        authRepository = AuthRepository(api, tokenManager)
        apartmentRepository = ApartmentRepository(api)
    }
}
