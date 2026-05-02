package com.example.myapplication.ui.analytics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.App
import com.example.myapplication.data.models.StatsResponse
import com.example.myapplication.databinding.FragmentAnalyticsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnalyticsViewModel by viewModels {
        AnalyticsViewModelFactory((requireActivity().application as App).apartmentRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPieChart()
        setupBarChart(isDistrict = false)
        setupBarChart(isDistrict = true)

        binding.retryButton.setOnClickListener { viewModel.loadStats() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.progressBar.isVisible = state is AnalyticsUiState.Loading
                binding.errorContainer.isVisible = state is AnalyticsUiState.Error
                binding.scrollContent.isVisible = state is AnalyticsUiState.Success
                when (state) {
                    is AnalyticsUiState.Success -> bindStats(state.stats)
                    is AnalyticsUiState.Error   -> binding.errorText.text = state.message
                    else -> {}
                }
            }
        }
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 38f
            transparentCircleRadius = 42f
            setHoleColor(Color.TRANSPARENT)
            setUsePercentValues(true)
            setEntryLabelTextSize(11f)
            setEntryLabelColor(Color.WHITE)
            legend.isEnabled = true
            legend.textSize = 12f
        }
    }

    private fun setupBarChart(isDistrict: Boolean) {
        val chart = if (isDistrict) binding.districtChart else binding.dynamicsChart
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setFitBars(true)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                labelRotationAngle = -35f
                textSize = 10f
            }
            axisLeft.apply {
                granularity = 1f
                axisMinimum = 0f
                textSize = 10f
            }
            axisRight.isEnabled = false
        }
    }

    private fun bindStats(stats: StatsResponse) {
        binding.totalApartmentsText.text = stats.totalApartments.toString()
        binding.totalViewsText.text = stats.totalViews.toString()

        bindPieChart(stats)
        bindDynamicsChart(stats)
        bindDistrictChart(stats)
    }

    private fun bindPieChart(stats: StatsResponse) {
        if (stats.byStatus.isEmpty()) return

        val statusLabels = mapOf(
            "available" to "Доступна",
            "reserved"  to "Бронь",
            "sold"      to "Продана",
            "cancelled" to "Отменена"
        )
        val statusColors = mapOf(
            "available" to Color.parseColor("#4CAF50"),
            "reserved"  to Color.parseColor("#FFA726"),
            "sold"      to Color.parseColor("#BDBDBD"),
            "cancelled" to Color.parseColor("#EF5350")
        )

        val grouped = stats.byStatus
            .groupBy { it.status }
            .mapValues { (_, list) -> list.sumOf { it.count }.toFloat() }
            .filter { it.value > 0 }

        val entries = grouped.map { (status, count) ->
            PieEntry(count, statusLabels[status] ?: status)
        }
        val colors = grouped.keys.map { statusColors[it] ?: Color.GRAY }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextColor = Color.WHITE
            valueTextSize = 11f
            valueFormatter = PercentFormatter(binding.pieChart)
            sliceSpace = 2f
        }

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
    }

    private fun bindDynamicsChart(stats: StatsResponse) {
        if (stats.salesDynamics.isEmpty()) return

        val grouped = stats.salesDynamics
            .groupBy { it.month }
            .mapValues { (_, list) -> list.sumOf { it.dealsCount }.toFloat() }
            .entries
            .sortedBy { it.key }
            .takeLast(12)

        val labels = grouped.map { it.key.takeLast(5) }
        val entries = grouped.mapIndexed { i, e -> BarEntry(i.toFloat(), e.value) }

        val dataSet = BarDataSet(entries, "Сделки").apply {
            color = Color.parseColor("#42A5F5")
            valueTextColor = Color.GRAY
            valueTextSize = 9f
        }

        binding.dynamicsChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            labelCount = labels.size.coerceAtMost(8)
        }
        binding.dynamicsChart.data = BarData(dataSet).apply { barWidth = 0.6f }
        binding.dynamicsChart.invalidate()
    }

    private fun bindDistrictChart(stats: StatsResponse) {
        if (stats.byDistrict.isEmpty()) return

        val top = stats.byDistrict
            .sortedByDescending { it.avgPrice }
            .take(8)

        val labels = top.map { (it.district ?: it.city ?: "—").take(12) }
        val entries = top.mapIndexed { i, d ->
            BarEntry(i.toFloat(), d.avgPrice / 1_000_000f)
        }

        val dataSet = BarDataSet(entries, "млн ₽").apply {
            color = Color.parseColor("#4CAF50")
            valueTextColor = Color.GRAY
            valueTextSize = 9f
        }

        binding.districtChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            labelCount = labels.size
        }
        binding.districtChart.data = BarData(dataSet).apply { barWidth = 0.6f }
        binding.districtChart.axisLeft.axisMaximum =
            (top.maxOf { it.avgPrice } / 1_000_000f) * 1.3f
        binding.districtChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
