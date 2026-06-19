package com.fastdash.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.model.response.AdminDashboardSummaryResponse
import com.fastdash.app.data.model.response.RevenueReportResponse
import com.fastdash.app.data.repository.AdminDashboardRepository
import com.fastdash.app.ui.admin.friendlyErrorMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RevenueRange(
    val label: String
) {
    DAY("Ngày"),
    WEEK("Tuần"),
    MONTH("Tháng")
}

data class RevenuePoint(
    val label: String,
    val detailLabel: String = label,
    val revenue: Long,
    val orderCount: Int
)

enum class RevenueComparisonState {
    UP,
    DOWN,
    FLAT,
    NO_PREVIOUS
}

data class RevenueAnalyticsUiModel(
    val range: RevenueRange = RevenueRange.WEEK,
    val totalRevenue: Long = 0L,
    val totalOrders: Long = 0L,
    val totalCompletedOrders: Long = 0L,
    val paidCompletedOrders: Long = 0L,
    val cancelledOrders: Long = 0L,
    val averageOrderValue: Long = 0L,
    val trendPercent: Float = 0f,
    val trendLabel: String = "Chưa có dữ liệu kỳ trước",
    val comparisonState: RevenueComparisonState = RevenueComparisonState.NO_PREVIOUS,
    val completionRate: Float = 0f,
    val cancelRate: Float = 0f,
    val peakLabel: String = "--",
    val peakRevenue: Long = 0L,
    val peakOrderCount: Int = 0,
    val chartSubtitle: String = "7 ngay gan nhat",
    val points: List<RevenuePoint> = emptyList(),
    val from: String = "",
    val to: String = ""
)

data class AdminDashboardUiState(
    val summary: AdminDashboardSummaryResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRevenueLoading: Boolean = false,
    val revenueErrorMessage: String? = null,
    val revenueAnalytics: RevenueAnalyticsUiModel = RevenueAnalyticsUiModel()
)

class AdminDashboardViewModel(
    private val repository: AdminDashboardRepository
) : ViewModel() {
    private companion object {
        const val TAG = "AdminDashboard"
    }

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()
    private var revenueJob: Job? = null

    init {
        loadSummary()
        loadRevenueAnalytics()
    }

    fun loadSummary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = repository.getSummary()
                Log.d(TAG, "loadSummary responseCode=${response.code()}")
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        summary = response.body(),
                        isLoading = false,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = friendlyErrorMessage(response, "Khong the tai thong ke")
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadSummary exception=${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Loi ket noi may chu"
                )
            }
        }
    }

    fun refresh() {
        loadSummary()
        loadRevenueAnalytics(force = true)
    }

    fun loadRevenueAnalytics(
        range: RevenueRange = _uiState.value.revenueAnalytics.range,
        from: String? = null,
        to: String? = null,
        force: Boolean = false
    ) {
        val currentState = _uiState.value.revenueAnalytics
        if (!force && currentState.range == range && currentState.points.isNotEmpty() && from == null && to == null) {
            return
        }

        revenueJob?.cancel()
        revenueJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRevenueLoading = true,
                    revenueErrorMessage = null,
                    revenueAnalytics = it.revenueAnalytics.copy(range = range)
                )
            }
            try {
                val response = repository.getRevenueReport(
                    period = range.name,
                    from = from,
                    to = to
                )
                Log.d(TAG, "loadRevenueAnalytics responseCode=${response.code()}")
                if (response.isSuccessful) {
                    val body = response.body() ?: RevenueReportResponse(period = range.name)
                    Log.d(TAG, "loadRevenueAnalytics body=$body")
                    _uiState.update {
                        it.copy(
                            isRevenueLoading = false,
                            revenueErrorMessage = null,
                            revenueAnalytics = body.toUiModel(range)
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isRevenueLoading = false,
                            revenueErrorMessage = friendlyErrorMessage(response, "Khong the tai bao cao doanh thu")
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadRevenueAnalytics exception=${e.message}", e)
                _uiState.update {
                    it.copy(
                        isRevenueLoading = false,
                        revenueErrorMessage = e.message ?: "Khong the tai bao cao doanh thu"
                    )
                }
            }
        }
    }

    private fun RevenueReportResponse.toUiModel(range: RevenueRange): RevenueAnalyticsUiModel {
        val comparisonState = when {
            growthRate > 0f -> RevenueComparisonState.UP
            growthRate < 0f -> RevenueComparisonState.DOWN
            growthRate == 0f && (totalRevenue > 0L || chartData.isNotEmpty()) -> RevenueComparisonState.FLAT
            else -> RevenueComparisonState.NO_PREVIOUS
        }

        return RevenueAnalyticsUiModel(
            range = range,
            totalRevenue = totalRevenue,
            totalOrders = totalOrders,
            totalCompletedOrders = completedOrders,
            paidCompletedOrders = paidCompletedOrders,
            cancelledOrders = cancelledOrders,
            averageOrderValue = averageOrderValue,
            trendPercent = growthRate,
            trendLabel = buildTrendLabel(growthRate, comparisonState),
            comparisonState = comparisonState,
            completionRate = completionRate,
            cancelRate = cancelRate,
            peakLabel = peakLabel ?: "--",
            peakRevenue = peakRevenue,
            peakOrderCount = peakOrderCount.toInt(),
            chartSubtitle = chartSubtitle(range),
            points = chartData.map {
                RevenuePoint(
                    label = it.label,
                    detailLabel = it.label,
                    revenue = it.revenue,
                    orderCount = it.orderCount.toInt()
                )
            },
            from = from.orEmpty(),
            to = to.orEmpty()
        )
    }

    private fun chartSubtitle(range: RevenueRange): String = when (range) {
        RevenueRange.DAY -> "Theo khung giờ trong ngày"
        RevenueRange.WEEK -> "7 ngày gần nhất"
        RevenueRange.MONTH -> "Theo ngày trong tháng"
    }

    private fun buildTrendLabel(growthRate: Float, state: RevenueComparisonState): String = when (state) {
        RevenueComparisonState.UP -> "Tăng ${String.format(java.util.Locale.US, "%.1f", growthRate)}% so với kỳ trước"
        RevenueComparisonState.DOWN -> "Giảm ${String.format(java.util.Locale.US, "%.1f", kotlin.math.abs(growthRate))}% so với kỳ trước"
        RevenueComparisonState.FLAT -> "Ổn định so với kỳ trước"
        RevenueComparisonState.NO_PREVIOUS -> "Chưa có dữ liệu kỳ trước"
    }
}
