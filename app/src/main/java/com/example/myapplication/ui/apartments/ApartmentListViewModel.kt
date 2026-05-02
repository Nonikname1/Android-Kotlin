package com.example.myapplication.ui.apartments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.myapplication.data.models.ApartmentDto
import com.example.myapplication.data.paging.ApartmentFilters
import com.example.myapplication.data.paging.ApartmentPagingSource
import com.example.myapplication.data.repository.ApartmentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

class ApartmentListViewModel(private val repository: ApartmentRepository) : ViewModel() {

    private val _filters = MutableStateFlow(ApartmentFilters())

    @OptIn(ExperimentalCoroutinesApi::class)
    val apartments: Flow<PagingData<ApartmentDto>> = _filters
        .flatMapLatest { filters ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false,
                    prefetchDistance = 3
                ),
                pagingSourceFactory = { ApartmentPagingSource(repository, filters) }
            ).flow
        }
        .cachedIn(viewModelScope)

    val currentFilters get() = _filters.value

    fun setFilters(filters: ApartmentFilters) {
        _filters.value = filters
    }

    fun setSearch(query: String) {
        _filters.update { it.copy(search = query.takeIf { q -> q.isNotBlank() }) }
    }

    fun clearFilters() {
        _filters.value = ApartmentFilters()
    }
}

class ApartmentListViewModelFactory(
    private val repository: ApartmentRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ApartmentListViewModel(repository) as T
}
