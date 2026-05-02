package com.example.myapplication.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.myapplication.data.models.ApartmentDto
import com.example.myapplication.data.repository.ApartmentRepository
import com.example.myapplication.data.NetworkResult

data class ApartmentFilters(
    val status: String? = null,
    val listingType: String? = null,
    val rooms: Int? = null,
    val minPrice: Long? = null,
    val maxPrice: Long? = null,
    val search: String? = null,
    val sortBy: String? = null,
    val sortOrder: String? = null
)

class ApartmentPagingSource(
    private val repository: ApartmentRepository,
    private val filters: ApartmentFilters = ApartmentFilters()
) : PagingSource<Int, ApartmentDto>() {

    override fun getRefreshKey(state: PagingState<Int, ApartmentDto>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ApartmentDto> {
        val page = params.key ?: 1
        return when (val result = repository.getApartments(
            page = page,
            pageSize = params.loadSize,
            status = filters.status,
            listingType = filters.listingType,
            rooms = filters.rooms,
            minPrice = filters.minPrice,
            maxPrice = filters.maxPrice,
            search = filters.search,
            sortBy = filters.sortBy,
            sortOrder = filters.sortOrder
        )) {
            is NetworkResult.Success -> {
                val response = result.data
                LoadResult.Page(
                    data = response.items,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (page >= response.totalPages) null else page + 1
                )
            }
            is NetworkResult.Error -> LoadResult.Error(Exception(result.message))
            NetworkResult.Loading -> LoadResult.Error(Exception("Unexpected loading state"))
        }
    }
}
