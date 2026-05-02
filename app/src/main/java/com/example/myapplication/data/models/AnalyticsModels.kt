package com.example.myapplication.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatsResponse(
    @SerialName("byStatus") val byStatus: List<StatusStat>,
    @SerialName("byDistrict") val byDistrict: List<DistrictStat>,
    @SerialName("salesDynamics") val salesDynamics: List<SalesDynamicStat>,
    @SerialName("totalApartments") val totalApartments: Long,
    @SerialName("totalViews") val totalViews: Long
)

@Serializable
data class StatusStat(
    @SerialName("status") val status: String,
    @SerialName("listingType") val listingType: String,
    @SerialName("count") val count: Long,
    @SerialName("avgPrice") val avgPrice: Long,
    @SerialName("minPrice") val minPrice: Long,
    @SerialName("maxPrice") val maxPrice: Long
)

@Serializable
data class DistrictStat(
    @SerialName("district") val district: String?,
    @SerialName("city") val city: String?,
    @SerialName("total") val total: Long,
    @SerialName("avgPrice") val avgPrice: Long,
    @SerialName("avgPricePerSqm") val avgPricePerSqm: Long,
    @SerialName("available") val available: Long,
    @SerialName("sold") val sold: Long,
    @SerialName("reserved") val reserved: Long
)

@Serializable
data class SalesDynamicStat(
    @SerialName("month") val month: String,
    @SerialName("listingType") val listingType: String,
    @SerialName("dealsCount") val dealsCount: Long,
    @SerialName("totalRevenue") val totalRevenue: Long,
    @SerialName("avgPrice") val avgPrice: Long
)
