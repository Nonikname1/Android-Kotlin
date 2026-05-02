package com.example.myapplication.data.api

import com.example.myapplication.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<TokenResponse>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): Response<Unit>

    // Apartments
    @GET("apartments")
    suspend fun getApartments(
        @Query("status") status: String? = null,
        @Query("listingType") listingType: String? = null,
        @Query("rooms") rooms: Int? = null,
        @Query("minPrice") minPrice: Long? = null,
        @Query("maxPrice") maxPrice: Long? = null,
        @Query("search") search: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sortOrder") sortOrder: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<ApartmentListResponse>

    @GET("apartments/{id}")
    suspend fun getApartmentById(@Path("id") id: String): Response<ApartmentDto>

    @POST("apartments")
    suspend fun createApartment(@Body request: CreateApartmentRequest): Response<ApartmentDto>

    @PUT("apartments/{id}")
    suspend fun updateApartment(
        @Path("id") id: String,
        @Body request: UpdateApartmentRequest
    ): Response<ApartmentDto>

    @DELETE("apartments/{id}")
    suspend fun deleteApartment(@Path("id") id: String): Response<Unit>

    // Analytics
    @GET("analytics/stats")
    suspend fun getStats(): Response<StatsResponse>
}
