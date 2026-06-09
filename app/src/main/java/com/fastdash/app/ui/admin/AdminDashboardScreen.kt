package com.fastdash.app.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastdash.app.data.model.response.AdminDashboardSummaryResponse
import com.fastdash.app.viewmodel.AdminDashboardViewModel
import com.fastdash.app.viewmodel.RevenueComparisonState
import com.fastdash.app.viewmodel.RevenueAnalyticsUiModel
import com.fastdash.app.viewmodel.RevenuePoint
import com.fastdash.app.viewmodel.RevenueRange
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

// Professional Admin Palette
private val AdminPrimary = Color(0xFFC8102E)
private val AdminBg = Color(0xFFF9FAFB)
private val AdminSurface = Color.White
private val AdminTextPrimary = Color(0xFF111827)
private val AdminTextSecondary = Color(0xFF6B7280)
private val AdminBorder = Color(0xFFE5E7EB)

// Functional Colors
private val ColorPending = Color(0xFFF59E0B)
private val ColorSuccess = Color(0xFF10B981)
private val ColorInfo = Color(0xFF3B82F6)
private val ColorSecondary = Color(0xFF6366F1)
private val ColorDanger = Color(0xFFEF4444)

@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onOpenProducts: () -> Unit,
    onOpenOrders: (filter: String?) -> Unit,
    onOpenRevenue: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenToppings: () -> Unit,
    onOpenCustomers: () -> Unit,
    onOpenBranches: () -> Unit,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.loadSummary() }
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.summary
    
    val todayDate = remember {
        try {
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM", Locale("vi", "VN")))
        } catch (e: Exception) {
            "Hôm nay"
        }
    }

    Scaffold(
        containerColor = AdminBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                DashboardHeader(
                    dateLabel = todayDate,
                    isRefreshing = uiState.isLoading,
                    onRefresh = viewModel::refresh,
                    onLogout = onLogout
                )
            }

            item {
                MainRevenueCard(
                    revenue = summary?.todayRevenue ?: summary?.totalRevenue ?: 0L,
                    completedCount = summary?.completedOrders ?: 0L,
                    isLoading = uiState.isLoading,
                    onDetailClick = onOpenRevenue
                )
            }

            item {
                SectionHeader("Cần xử lý ngay")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionTile("Chờ duyệt", summary?.pendingOrders ?: 0L, Icons.Outlined.NotificationsActive, ColorPending, Modifier.weight(1f), { onOpenOrders("PENDING_CONFIRMATION") })
                    ActionTile("Chuẩn bị", summary?.preparingOrders ?: 0L, Icons.Outlined.SoupKitchen, ColorSecondary, Modifier.weight(1f), { onOpenOrders("PREPARING") })
                    ActionTile("Đang giao", summary?.deliveringOrders ?: 0L, Icons.Outlined.LocalShipping, ColorInfo, Modifier.weight(1f), { onOpenOrders("DELIVERING") })
                }
            }

            item {
                SectionHeader("Thống kê hệ thống")
                StatsGrid(summary)
            }

            item {
                SectionHeader("Quản lý hệ thống")
                ManagementShortcuts(
                    onProd = onOpenProducts,
                    onCat = onOpenCategories,
                    onTop = onOpenToppings,
                    onCust = onOpenCustomers,
                    onOrders = { onOpenOrders(null) },
                    onBr = onOpenBranches
                )
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("FastDash Admin Console • v1.9", style = MaterialTheme.typography.labelSmall, color = AdminTextSecondary.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(dateLabel: String, isRefreshing: Boolean, onRefresh: () -> Unit, onLogout: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("Xin chào, Admin", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = AdminTextPrimary)
            Text(dateLabel, style = MaterialTheme.typography.bodyMedium, color = AdminTextSecondary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(modifier = Modifier.size(44.dp).clickable { if (!isRefreshing) onRefresh() }, shape = CircleShape, color = AdminSurface, border = BorderStroke(1.dp, AdminBorder)) {
                Box(contentAlignment = Alignment.Center) {
                    if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AdminPrimary)
                    else Icon(Icons.Default.Refresh, null, tint = AdminTextPrimary, modifier = Modifier.size(20.dp))
                }
            }
            ProfileAvatarMenu(onLogout)
        }
    }
}

@Composable
private fun MainRevenueCard(revenue: Long, completedCount: Long, isLoading: Boolean, onDetailClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(24.dp), color = Color(0xFF111827)
    ) {
        Column(modifier = Modifier.clickable { onDetailClick() }.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Doanh thu hôm nay", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (isLoading) Text("...", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    else Text(com.fastdash.app.utils.CurrencyUtils.formatVnd(revenue.toDouble()), color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                }
                Box(modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Outlined.TrendingUp, null, tint = ColorSuccess, modifier = Modifier.size(24.dp))
                }
            }
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.05f)) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Đã hoàn thành $completedCount đơn", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Báo cáo", color = ColorSuccess, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = ColorSuccess, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionTile(label: String, count: Long, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = AdminSurface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, AdminBorder.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    count.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = AdminTextPrimary
                )
                Text(
                    label.uppercase(),
                    color = AdminTextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(summary: AdminDashboardSummaryResponse?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatItem("Đơn hàng", (summary?.totalOrders ?: 0).toString(), Icons.Outlined.Receipt, AdminPrimary, Modifier.weight(1f))
            StatItem("Khách hàng", (summary?.totalUsers ?: 0).toString(), Icons.Outlined.PeopleAlt, ColorInfo, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatItem("Sản phẩm", (summary?.totalProducts ?: 0).toString(), Icons.Outlined.Inventory2, ColorSuccess, Modifier.weight(1f))
            StatItem("Hủy bỏ", (summary?.cancelledOrders ?: 0).toString(), Icons.Outlined.Cancel, AdminTextSecondary, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = AdminSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AdminBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(
                    value,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = AdminTextPrimary,
                    lineHeight = 18.sp
                )
                Text(
                    label,
                    fontSize = 11.sp,
                    color = AdminTextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ManagementShortcuts(
    onProd: () -> Unit,
    onCat: () -> Unit,
    onTop: () -> Unit,
    onCust: () -> Unit,
    onOrders: () -> Unit,
    onBr: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShortcutCard("Thực đơn", "Món & giá", Icons.Outlined.Fastfood, AdminPrimary, Modifier.weight(1f), onProd)
            ShortcutCard("Danh mục", "Phân loại", Icons.Outlined.Category, ColorInfo, Modifier.weight(1f), onCat)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShortcutCard("Topping", "Thêm món", Icons.Outlined.BubbleChart, ColorSecondary, Modifier.weight(1f), onTop)
            ShortcutCard("Người dùng", "Tài khoản", Icons.Outlined.Group, ColorSuccess, Modifier.weight(1f), onCust)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShortcutCard("Đơn hàng", "Theo dõi", Icons.Outlined.ReceiptLong, ColorPending, Modifier.weight(1f), onOrders)
            ShortcutCard("Chi nhánh", "Vị trí", Icons.Outlined.Storefront, ColorSecondary, Modifier.weight(1f), onBr)
        }
    }
}

@Composable
private fun ShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = AdminSurface,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, AdminBorder.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(
                    title,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = AdminTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = AdminTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ProfileAvatarMenu(onLogout: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(modifier = Modifier.size(44.dp).clickable { expanded = true }, shape = CircleShape, color = AdminPrimary) {
            Box(contentAlignment = Alignment.Center) { Text("A", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp) }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(AdminSurface).width(180.dp), shape = RoundedCornerShape(16.dp)) {
            DropdownMenuItem(text = { Text("Đăng xuất", fontWeight = FontWeight.Bold, color = AdminPrimary) }, onClick = { expanded = false; onLogout() }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = AdminPrimary) })
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(bottom = 4.dp),
        fontSize = 16.sp,
        fontWeight = FontWeight.Black,
        color = AdminTextPrimary,
        letterSpacing = (-0.4).sp
    )
}

@Composable
fun SectionLabel(text: String) {
    Text(text = text.uppercase(), modifier = Modifier.padding(bottom = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = AdminTextSecondary)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRevenueScreen(
    viewModel: AdminDashboardViewModel,
    onBack: () -> Unit,
    onOpenCompletedOrders: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadSummary()
        viewModel.loadRevenueAnalytics(force = true)
    }
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.summary
    val analytics = uiState.revenueAnalytics
    var selectedPoint by remember(analytics.range, analytics.points) {
        mutableStateOf(analytics.points.maxByOrNull { it.revenue } ?: analytics.points.firstOrNull())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Báo cáo doanh thu", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBg)
            )
        },
        containerColor = AdminBg
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            RevenueRangeSelector(analytics.range) { viewModel.loadRevenueAnalytics(range = it, force = true) }
            RevenueHeroCard(analytics, uiState.isRevenueLoading)
            RevenueChartCard(analytics, selectedPoint, uiState.isRevenueLoading) { selectedPoint = it }

            SectionLabel("Hiệu suất vận hành")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PerformanceMetric("AOV (TB/Đơn)", formatCurrencyVnd(analytics.averageOrderValue), ColorInfo, Modifier.weight(1f))
                PerformanceMetric("Tỷ lệ hoàn tất", formatPercent(analytics.completionRate), ColorSuccess, Modifier.weight(1f))
            }

            SectionLabel("Phân tích chi tiết")
            Surface(color = AdminSurface, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, AdminBorder)) {
                Column(modifier = Modifier.padding(4.dp)) {
                    DetailRowItem("Tổng số đơn", (summary?.totalOrders ?: 0).toString(), Icons.Outlined.Receipt)
                    HorizontalDivider(color = AdminBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                    DetailRowItem("Đơn hoàn tất", analytics.totalCompletedOrders.toString(), Icons.Outlined.CheckCircle)
                    HorizontalDivider(color = AdminBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                    DetailRowItem("Đỉnh doanh thu", formatCurrencyVnd(analytics.peakRevenue), Icons.Outlined.Bolt)
                    HorizontalDivider(color = AdminBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                    DetailRowItem("Đơn đã hủy", (summary?.cancelledOrders ?: 0).toString(), Icons.Outlined.Cancel)
                }
            }

            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onOpenCompletedOrders,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary)
                ) {
                    Text("Lịch sử đơn", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RevenueRangeSelector(selectedRange: RevenueRange, onRangeSelected: (RevenueRange) -> Unit) {
    Surface(color = AdminSurface, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, AdminBorder)) {
        Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RevenueRange.entries.forEach { range ->
                val sel = selectedRange == range
                Surface(modifier = Modifier.weight(1f).clickable { onRangeSelected(range) }, color = if (sel) AdminPrimary else Color.Transparent, shape = RoundedCornerShape(12.dp)) {
                    Text(text = range.label, modifier = Modifier.padding(vertical = 9.dp), textAlign = TextAlign.Center, color = if (sel) Color.White else AdminTextSecondary, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun RevenueHeroCard(analytics: RevenueAnalyticsUiModel, isLoading: Boolean) {
    val trendColor = when (analytics.comparisonState) {
        RevenueComparisonState.UP -> ColorSuccess
        RevenueComparisonState.DOWN -> ColorDanger
        RevenueComparisonState.FLAT -> ColorInfo
        else -> Color.White.copy(alpha = 0.6f)
    }
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
        color = Color(0xFF111827),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    val periodLabel = when(analytics.range) {
                        RevenueRange.DAY -> "ngày hôm nay"
                        RevenueRange.WEEK -> "tuần này"
                        RevenueRange.MONTH -> "tháng này"
                    }
                    Text(
                        "Tổng doanh thu $periodLabel",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    if (isLoading) {
                        Text("...", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    } else {
                        Text(
                            formatCurrencyVnd(analytics.totalRevenue),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }
                if (analytics.comparisonState != RevenueComparisonState.NO_PREVIOUS) {
                    Surface(shape = RoundedCornerShape(12.dp), color = trendColor.copy(alpha = 0.15f)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                if (analytics.trendPercent >= 0) Icons.AutoMirrored.Outlined.TrendingUp else Icons.AutoMirrored.Outlined.TrendingDown,
                                null,
                                tint = trendColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(formatPercent(analytics.trendPercent), color = trendColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            Text(
                text = analytics.trendLabel,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RevenueChartCard(analytics: RevenueAnalyticsUiModel, selectedPoint: RevenuePoint?, isLoading: Boolean, onPointSelected: (RevenuePoint) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AdminSurface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, AdminBorder.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Biểu đồ doanh thu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = AdminTextPrimary)
                    Text(analytics.chartSubtitle, style = MaterialTheme.typography.bodySmall, color = AdminTextSecondary, fontWeight = FontWeight.Medium)
                }
                if (!isLoading && analytics.points.isNotEmpty()) {
                    Surface(color = AdminBg, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            if (analytics.peakRevenue > 0L) "Đỉnh: ${formatCompactMoney(analytics.peakRevenue)}" else "${analytics.totalCompletedOrders} đơn",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminPrimary
                        )
                    }
                }
            }
            
            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { 
                    CircularProgressIndicator(color = AdminPrimary, strokeWidth = 3.dp) 
                }
            } else if (analytics.points.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { 
                    Text("Chưa có dữ liệu", color = AdminTextSecondary, fontWeight = FontWeight.Medium) 
                }
            } else {
                RevenueChart(points = analytics.points, color = AdminPrimary, onPointSelected = onPointSelected)
            }
            
            if (!isLoading && selectedPoint != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AdminBg,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, AdminBorder)
                ) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(selectedPoint.detailLabel, fontWeight = FontWeight.Black, fontSize = 13.sp, color = AdminTextPrimary)
                            Text("${selectedPoint.orderCount} đơn hàng thành công", fontSize = 11.sp, color = AdminTextSecondary, fontWeight = FontWeight.Medium)
                        }
                        Text(formatCurrencyVnd(selectedPoint.revenue), fontWeight = FontWeight.Black, color = AdminPrimary, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RevenueChart(points: List<RevenuePoint>, color: Color, onPointSelected: (RevenuePoint) -> Unit) {
    val maxVal = (points.maxOfOrNull { it.revenue } ?: 0L).coerceAtLeast(1L)
    val yGuides = listOf(maxVal, maxVal / 2, 0L)
    var touchX by remember { mutableStateOf<Float?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.height(180.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
                yGuides.forEach { Text(text = formatCompactMoney(it), color = AdminTextSecondary.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
            }
            Box(modifier = Modifier.weight(1f).height(180.dp)
                .pointerInput(points) {
                    detectTapGestures(
                        onPress = { offset -> touchX = offset.x; try { awaitRelease() } finally { touchX = null } },
                        onTap = { offset ->
                            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                            val index = (offset.x / stepX).roundToInt().coerceIn(0, points.lastIndex)
                            onPointSelected(points[index])
                        }
                    )
                }
                .pointerInput(points) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> touchX = offset.x },
                        onDragEnd = { touchX = null },
                        onDragCancel = { touchX = null },
                        onHorizontalDrag = { change, _ ->
                            touchX = change.position.x
                            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                            val index = (change.position.x / stepX).roundToInt().coerceIn(0, points.lastIndex)
                            onPointSelected(points[index])
                        }
                    )
                }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val topPadding = 12.dp.toPx()
                    val bottomPadding = 4.dp.toPx()
                    val usableHeight = height - topPadding - bottomPadding
                    val stepX = if (points.size > 1) width / (points.size - 1) else 0f

                    yGuides.forEachIndexed { i, _ ->
                        val y = topPadding + (usableHeight / (yGuides.size - 1)) * i
                        drawLine(color = AdminBorder.copy(alpha = 0.3f), start = Offset(0f, y), end = Offset(width, y), strokeWidth = 1.dp.toPx())
                    }

                    if (points.size > 1) {
                        val linePath = Path()
                        val areaPath = Path()
                        val conX = stepX * 0.35f
                        val firstY = topPadding + usableHeight - (points[0].revenue.toFloat() / maxVal) * usableHeight
                        linePath.moveTo(0f, firstY)
                        areaPath.moveTo(0f, height)
                        areaPath.lineTo(0f, firstY)

                        for (i in 1 until points.size) {
                            val prevX = stepX * (i - 1)
                            val prevY = topPadding + usableHeight - (points[i - 1].revenue.toFloat() / maxVal) * usableHeight
                            val currX = stepX * i
                            val currY = topPadding + usableHeight - (points[i].revenue.toFloat() / maxVal) * usableHeight
                            linePath.cubicTo(prevX + conX, prevY, currX - conX, currY, currX, currY)
                            areaPath.cubicTo(prevX + conX, prevY, currX - conX, currY, currX, currY)
                        }
                        areaPath.lineTo(width, height)
                        areaPath.close()

                        drawPath(path = areaPath, brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.2f), Color.Transparent), startY = topPadding, endY = height), style = Fill)
                        drawPath(path = linePath, color = color, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))

                        touchX?.let { x ->
                            val index = (x.coerceIn(0f, width) / stepX).roundToInt().coerceIn(0, points.lastIndex)
                            val centerX = index * stepX
                            val centerY = topPadding + usableHeight - (points[index].revenue.toFloat() / maxVal) * usableHeight
                            drawLine(color = color.copy(alpha = 0.4f), start = Offset(centerX, topPadding), end = Offset(centerX, height), strokeWidth = 1.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                            drawCircle(color = color, radius = 6.dp.toPx(), center = Offset(centerX, centerY))
                            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(centerX, centerY))
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.padding(start = 44.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val step = if (points.size > 8) points.size / 4 else 1
            points.forEachIndexed { i, p -> if (i % step == 0 || i == points.lastIndex) Text(p.label, color = AdminTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun PerformanceMetric(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = AdminSurface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, AdminBorder.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.08f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Analytics, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(value, fontWeight = FontWeight.Black, fontSize = 17.sp, color = AdminTextPrimary)
                Text(label, fontSize = 11.sp, color = AdminTextSecondary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DetailRowItem(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AdminBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = AdminPrimary, modifier = Modifier.size(18.dp))
            }
            Text(label, color = AdminTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Text(value, fontWeight = FontWeight.Black, color = AdminTextPrimary, fontSize = 15.sp)
    }
}

private fun formatCurrencyVnd(value: Long): String = com.fastdash.app.utils.CurrencyUtils.formatVnd(value.toDouble())
private fun formatPercent(value: Float): String = if (value % 1f == 0f) "${value.toInt()}%" else String.format(Locale.US, "%.1f%%", value)
private fun formatCompactMoney(value: Long): String = when {
    value >= 1_000_000_000L -> String.format(Locale.US, "%.1fB", value / 1_000_000_000f)
    value >= 1_000_000L -> String.format(Locale.US, "%.1fM", value / 1_000_000f)
    value >= 1_000L -> String.format(Locale.US, "%.0fK", value / 1_000f)
    else -> value.toString()
}
