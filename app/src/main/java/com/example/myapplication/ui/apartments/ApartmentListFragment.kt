package com.example.myapplication.ui.apartments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.data.paging.ApartmentFilters
import com.example.myapplication.databinding.FragmentApartmentListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ApartmentListFragment : Fragment() {

    private var _binding: FragmentApartmentListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ApartmentListViewModel by viewModels {
        ApartmentListViewModelFactory((requireActivity().application as App).apartmentRepository)
    }

    private lateinit var adapter: ApartmentPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApartmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupFilterButton()
        observeApartments()
        observeRefreshSignal()

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_list_to_create)
        }
    }

    private fun setupRecyclerView() {
        adapter = ApartmentPagingAdapter { apartment ->
            findNavController().navigate(
                R.id.action_list_to_detail,
                bundleOf(ApartmentDetailFragment.ARG_APARTMENT_ID to apartment.id)
            )
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter.withLoadStateFooter(
            ApartmentLoadStateAdapter { adapter.retry() }
        )

        adapter.addLoadStateListener { states ->
            val refresh = states.refresh
            binding.progressBar.isVisible = refresh is LoadState.Loading
            binding.recyclerView.isVisible =
                refresh is LoadState.NotLoading && adapter.itemCount > 0
            binding.emptyContainer.isVisible =
                refresh is LoadState.NotLoading && adapter.itemCount == 0
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener { text ->
            viewModel.setSearch(text?.toString().orEmpty())
        }
    }

    private fun setupFilterButton() {
        binding.toolbar.inflateMenu(R.menu.menu_apartment_list)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_filter) {
                showFilterSheet()
                true
            } else false
        }
    }

    private fun showFilterSheet() {
        val current = viewModel.currentFilters
        val sheet = FilterBottomSheet.newInstance(current)
        sheet.listener = object : FilterBottomSheet.OnFiltersAppliedListener {
            override fun onFiltersApplied(filters: ApartmentFilters) {
                viewModel.setFilters(filters)
                updateFilterIndicator(filters)
            }
        }
        sheet.show(parentFragmentManager, FilterBottomSheet.TAG)
    }

    private fun updateFilterIndicator(filters: ApartmentFilters) {
        val hasFilter = filters.status != null || filters.listingType != null ||
                filters.rooms != null || filters.sortBy != null
        val icon = binding.toolbar.menu.findItem(R.id.action_filter)
        icon?.setIcon(
            if (hasFilter) R.drawable.ic_filter_active else R.drawable.ic_filter
        )
    }

    private fun observeRefreshSignal() {
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("refreshList")
            ?.observe(viewLifecycleOwner) { needRefresh ->
                if (needRefresh == true) {
                    adapter.refresh()
                    findNavController().currentBackStackEntry
                        ?.savedStateHandle?.remove<Boolean>("refreshList")
                }
            }
    }

    private fun observeApartments() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.apartments.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
