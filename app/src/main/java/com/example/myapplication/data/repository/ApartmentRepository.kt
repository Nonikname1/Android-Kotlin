package com.example.myapplication.data.repository

import com.example.myapplication.data.NetworkResult
import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.models.ApartmentDto
import com.example.myapplication.data.models.ApartmentListResponse
import com.example.myapplication.data.models.CreateApartmentRequest
import com.example.myapplication.data.models.StatsResponse
import com.example.myapplication.data.models.UpdateApartmentRequest
import com.example.myapplication.data.safeApiCall

class ApartmentRepository(private val api: ApiService) {

    suspend fun getApartments(
        page: Int,
        pageSize: Int = 20,
        status: String? = null,
        listingType: String? = null,
        rooms: Int? = null,
        minPrice: Long? = null,
        maxPrice: Long? = null,
        search: String? = null,
        sortBy: String? = null,
        sortOrder: String? = null
    ): NetworkResult<ApartmentListResponse> = safeApiCall {
        api.getApartments(
            status = status,
            listingType = listingType,
            rooms = rooms,
            minPrice = minPrice,
            maxPrice = maxPrice,
            search = search,
            sortBy = sortBy,
            sortOrder = sortOrder,
            page = page,
            pageSize = pageSize
        )
    }

    suspend fun getApartmentById(id: String): NetworkResult<ApartmentDto> = safeApiCall {
        api.getApartmentById(id)
    }

    suspend fun createApartment(request: CreateApartmentRequest): NetworkResult<ApartmentDto> =
        safeApiCall { api.createApartment(request) }

    suspend fun updateApartment(
        id: String,
        request: UpdateApartmentRequest
    ): NetworkResult<ApartmentDto> = safeApiCall { api.updateApartment(id, request) }

    suspend fun deleteApartment(id: String): NetworkResult<Unit> = safeApiCall {
        api.deleteApartment(id)
    }

    suspend fun getStats(): NetworkResult<StatsResponse> = safeApiCall {
        api.getStats()
    }
}
