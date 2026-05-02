package com.example.myapplication.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApartmentDto(
    @SerialName("id") val id: String,
    @SerialName("buildingId") val buildingId: String? = null,
    @SerialName("agentId") val agentId: String,
    @SerialName("fullAddress") val fullAddress: String,
    @SerialName("apartmentNumber") val apartmentNumber: String? = null,
    @SerialName("floor") val floor: Int,
    @SerialName("totalFloors") val totalFloors: Int? = null,
    @SerialName("rooms") val rooms: Int,
    @SerialName("totalArea") val totalArea: Double,
    @SerialName("livingArea") val livingArea: Double? = null,
    @SerialName("kitchenArea") val kitchenArea: Double? = null,
    @SerialName("price") val price: Long,
    @SerialName("pricePerSqm") val pricePerSqm: Long? = null,
    @SerialName("listingType") val listingType: String,
    @SerialName("status") val status: String,
    @SerialName("description") val description: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("viewsCount") val viewsCount: Int,
    @SerialName("photos") val photos: List<PhotoDto> = emptyList(),
    @SerialName("createdAt") val createdAt: String,
    @SerialName("updatedAt") val updatedAt: String,
    @SerialName("soldAt") val soldAt: String? = null
)

@Serializable
data class PhotoDto(
    @SerialName("id") val id: String,
    @SerialName("url") val url: String,
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerialName("sortOrder") val sortOrder: Int,
    @SerialName("isMain") val isMain: Boolean
)

@Serializable
data class ApartmentListResponse(
    @SerialName("items") val items: List<ApartmentDto>,
    @SerialName("total") val total: Int,
    @SerialName("page") val page: Int,
    @SerialName("pageSize") val pageSize: Int,
    @SerialName("totalPages") val totalPages: Int
)

@Serializable
data class CreateApartmentRequest(
    @SerialName("buildingId") val buildingId: String? = null,
    @SerialName("fullAddress") val fullAddress: String,
    @SerialName("apartmentNumber") val apartmentNumber: String? = null,
    @SerialName("floor") val floor: Int,
    @SerialName("totalFloors") val totalFloors: Int? = null,
    @SerialName("rooms") val rooms: Int,
    @SerialName("totalArea") val totalArea: Double,
    @SerialName("livingArea") val livingArea: Double? = null,
    @SerialName("kitchenArea") val kitchenArea: Double? = null,
    @SerialName("price") val price: Long,
    @SerialName("listingType") val listingType: String,
    @SerialName("status") val status: String = "available",
    @SerialName("description") val description: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("photoUrls") val photoUrls: List<String>? = null
)

@Serializable
data class UpdateApartmentRequest(
    @SerialName("fullAddress") val fullAddress: String? = null,
    @SerialName("apartmentNumber") val apartmentNumber: String? = null,
    @SerialName("floor") val floor: Int? = null,
    @SerialName("totalFloors") val totalFloors: Int? = null,
    @SerialName("rooms") val rooms: Int? = null,
    @SerialName("totalArea") val totalArea: Double? = null,
    @SerialName("livingArea") val livingArea: Double? = null,
    @SerialName("kitchenArea") val kitchenArea: Double? = null,
    @SerialName("price") val price: Long? = null,
    @SerialName("listingType") val listingType: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("photoUrls") val photoUrls: List<String>? = null,
    @SerialName("statusComment") val statusComment: String? = null
)
