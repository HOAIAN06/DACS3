package com.fastdash.app.ui.admin

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastdash.app.data.model.response.AdminOrderDetailResponse
import com.fastdash.app.data.model.response.AdminOrderSummaryResponse
import com.fastdash.app.data.model.response.OrderItemResponse
import com.fastdash.app.viewmodel.AdminOrderDetailViewModel
import com.fastdash.app.viewmodel.AdminOrdersUiState
import com.fastdash.app.viewmodel.AdminOrdersViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import java.time.LocalDate
import kotlin.math.roundToLong

// Unified Admin Palette
private val AdminRed = Color(0xFFC8102E)
private val AdminBg = Color(0xFFF9FAFB)
private val AdminSurface = Color.White
private val AdminTextMain = Color(0xFF111827)
private val AdminTextMuted = Color(0xFF6B7280)
private val AdminBorder = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    viewModel: AdminOrdersViewModel,
    initialStatus: String?,
    onBack: () -> Unit,
    onOpenOrder: (Long) -> Unit,
    onUnauthorized: () -> Unit
) {
    LaunchedEffect(initialStatus) { viewModel.applyInitialStatus(initialStatus) }

    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingStatusUpdate by remember { mutableStateOf<Pair<Long, OrderAction>?>(null) }

    LaunchedEffect(listState, uiState.orders.size, uiState.hasMore, uiState.isLoadingMore, uiState.isLoading) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filterNotNull()
            .distinctUntilChanged()
            .collectLatest { index ->
                if (!uiState.isLoading && !uiState.isLoadingMore && uiState.hasMore && index >= uiState.orders.lastIndex - 2) {
                    viewModel.loadMore()
                }
            }
    }

    if (pendingStatusUpdate != null) {
        val pending = pendingStatusUpdate!!
        ConfirmStatusDialog(
            actionLabel = pending.second.label,
            targetStatusLabel = orderStatusLabel(pending.second.targetStatus),
            onDismiss = { pendingStatusUpdate = null },
            onConfirm = {
                pendingStatusUpdate = null
                viewModel.updateOrderStatus(pending.first, pending.second.targetStatus)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Quản lý đơn hàng", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        if (uiState.totalElements > 0) {
                            Text("${uiState.totalElements} đơn hàng được tìm thấy", fontSize = 12.sp, color = AdminTextMuted, fontWeight = FontWeight.Medium)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AdminTextMain) }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::refresh,
                        modifier = Modifier.background(AdminSurface, CircleShape).border(1.dp, AdminBorder, CircleShape)
                    ) { 
                        Icon(Icons.Outlined.Refresh, null, tint = AdminTextMain, modifier = Modifier.size(20.dp)) 
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = viewModel::openFilterSheet,
                        modifier = Modifier.background(AdminSurface, CircleShape).border(1.dp, AdminBorder, CircleShape)
                    ) {
                        BadgedBox(badge = { if (hasActiveAdvancedFilters(uiState)) Badge(containerColor = AdminRed) }) {
                            Icon(Icons.Outlined.FilterAlt, null, tint = if (hasActiveAdvancedFilters(uiState)) AdminRed else AdminTextMain, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBg)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AdminBg
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OrderQuickSearch(uiState.keyword, viewModel::onKeywordChange, viewModel::clearSearch)
            
            OrderGroupTabs(uiState.selectedGroup, viewModel::onGroupSelected)

            if (uiState.isLoading && uiState.orders.isEmpty()) {
                OrderSkeletonList()
            } else if (uiState.errorMessage != null && uiState.orders.isEmpty()) {
                AdminOrdersErrorView(
                    message = uiState.errorMessage!!,
                    onRetry = viewModel::refresh
                )
            } else if (!uiState.isLoading && uiState.orders.isEmpty()) {
                OrderEmptyView(uiState, viewModel::clearFilters)
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.orders, key = { it.id }) { order ->
                        OrderListItem(
                            order = order,
                            onClick = { onOpenOrder(order.id) },
                            onQuickAction = { action -> pendingStatusUpdate = order.id to action }
                        )
                    }
                    if (uiState.isLoadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AdminRed, strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.isFilterSheetOpen) {
        OrdersFilterBottomSheet(
            selectedStatus = uiState.selectedStatus,
            fromDate = uiState.fromDate,
            toDate = uiState.toDate,
            sort = uiState.sort,
            onDismiss = viewModel::closeFilterSheet,
            onClear = { viewModel.clearFilters(clearKeyword = false) },
            onApply = viewModel::applyFilters
        )
    }
}

@Composable
private fun OrderQuickSearch(keyword: String, onValueChange: (String) -> Unit, onClear: () -> Unit) {
    OutlinedTextField(
        value = keyword,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        placeholder = { Text("Mã đơn, tên, số điện thoại khách...", fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(20.dp), tint = AdminTextMuted) },
        trailingIcon = { if (keyword.isNotBlank()) IconButton(onClick = onClear) { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp), tint = AdminTextMuted) } },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = AdminBorder,
            focusedBorderColor = AdminRed,
            unfocusedContainerColor = AdminSurface,
            focusedContainerColor = AdminSurface
        )
    )
}

@Composable
private fun OrderGroupTabs(selectedGroup: OrderGroup, onGroupSelected: (OrderGroup) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(OrderGroup.entries) { group ->
            val isSelected = selectedGroup == group
            Surface(
                modifier = Modifier.clickable { onGroupSelected(group) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) AdminRed else AdminSurface,
                border = if (isSelected) null else BorderStroke(1.dp, AdminBorder)
            ) {
                Text(
                    text = orderGroupLabel(group),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    color = if (isSelected) Color.White else AdminTextMuted,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OrderListItem(order: AdminOrderSummaryResponse, onClick: () -> Unit, onQuickAction: (OrderAction) -> Unit) {
    val status = order.orderStatus ?: order.status
    val quickAction = quickActionForList(status)

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = AdminSurface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, AdminBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        order.orderCode,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = AdminTextMain,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        formatDateTime(order.createdAt),
                        fontSize = 12.sp,
                        color = AdminTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
                StatusBadge(status)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Column(modifier = Modifier.fillMaxWidth().background(AdminBg, RoundedCornerShape(12.dp)).padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, null, tint = AdminTextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        order.receiverName ?: order.customerName ?: "Ẩn danh",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AdminTextMain
                    )
                }
                if (!order.receiverPhone.isNullOrBlank() || !order.customerEmail.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ContactSupport, null, tint = AdminTextMuted, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(order.receiverPhone ?: order.customerEmail ?: "--", fontSize = 12.sp, color = AdminTextMuted)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Tổng thanh toán", fontSize = 11.sp, color = AdminTextMuted, fontWeight = FontWeight.Bold)
                    Text(formatVnd(order.totalAmount), fontWeight = FontWeight.Black, fontSize = 18.sp, color = AdminRed)
                }
                
                if (quickAction != null) {
                    Button(
                        onClick = { onQuickAction(quickAction) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminRed),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(quickAction.label, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                } else {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = AdminBorder, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String?) {
    val color = statusContentColor(status)
    val bgColor = statusContainerColor(status)
    
    Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = orderStatusLabel(status).uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderDetailScreen(
    orderId: Long,
    viewModel: AdminOrderDetailViewModel,
    onBack: () -> Unit,
    onOrderUpdated: () -> Unit,
    onUnauthorized: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingAction by remember { mutableStateOf<OrderAction?>(null) }

    LaunchedEffect(orderId) { viewModel.loadOrder(orderId) }

    val order = uiState.order
    val actions = allowedNextActions((order?.orderStatus ?: order?.status).orEmpty())

    if (pendingAction != null && order != null) {
        ConfirmStatusDialog(
            actionLabel = pendingAction!!.label,
            targetStatusLabel = orderStatusLabel(pendingAction!!.targetStatus),
            onDismiss = { pendingAction = null },
            onConfirm = {
                val action = pendingAction ?: return@ConfirmStatusDialog
                pendingAction = null
                viewModel.updateStatus(order.id, action.targetStatus, onSuccess = onOrderUpdated)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AdminTextMain) } },
                actions = { 
                    IconButton(
                        onClick = { viewModel.loadOrder(orderId) },
                        modifier = Modifier.background(AdminSurface, CircleShape).border(1.dp, AdminBorder, CircleShape)
                    ) { Icon(Icons.Outlined.Refresh, null, tint = AdminTextMain, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBg)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (order != null && actions.isNotEmpty()) {
                Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 16.dp, color = AdminSurface, border = BorderStroke(1.dp, AdminBorder)) {
                    Row(modifier = Modifier.navigationBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        actions.forEach { action ->
                            Button(
                                onClick = { pendingAction = action },
                                modifier = Modifier.weight(1f).height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (action.isDestructive) Color(0xFFEF4444) else AdminRed)
                            ) {
                                Text(action.label.uppercase(), fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                            }
                        }
                    }
                }
            }
        },
        containerColor = AdminBg
    ) { padding ->
        if (uiState.isLoading && order == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AdminRed) }
        } else if (order != null) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                item { OrderDetailSummaryCard(order) }
                item { DetailInfoSection("Thông tin người nhận", listOf(
                    Icons.Outlined.Person to ("Họ tên" to (order.receiverName ?: "N/A")),
                    Icons.Outlined.Phone to ("Số điện thoại" to (order.receiverPhone ?: "N/A")),
                    Icons.Outlined.Place to ("Địa chỉ giao hàng" to (order.deliveryAddress ?: "N/A"))
                )) }
                item { DetailInfoSection("Hình thức thanh toán", listOf(
                    Icons.Outlined.Payments to ("Phương thức" to paymentMethodLabel(order.paymentMethod)),
                    Icons.Outlined.Info to ("Trạng thái" to paymentStatusLabel(order.paymentStatus))
                )) }
                item { OrderItemsCard(order.items) }
                item { OrderFinalCalculationCard(order) }
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun OrderDetailSummaryCard(order: AdminOrderDetailResponse) {
    Surface(color = AdminSurface, shape = RoundedCornerShape(28.dp), border = BorderStroke(1.dp, AdminBorder), shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(order.orderCode, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = AdminTextMain)
                StatusBadge(order.orderStatus ?: order.status)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule, null, tint = AdminTextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(formatDateTime(order.createdAt), fontSize = 13.sp, color = AdminTextMuted, fontWeight = FontWeight.Medium)
            }
            HorizontalDivider(color = AdminBorder)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("TỔNG GIÁ TRỊ ĐƠN", fontWeight = FontWeight.Black, color = AdminTextMuted, fontSize = 12.sp, letterSpacing = 1.sp)
                Text(formatVnd(order.totalAmount), color = AdminRed, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun DetailInfoSection(title: String, rows: List<Pair<ImageVector, Pair<String, String>>>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Black, color = AdminTextMuted, letterSpacing = 1.sp)
        Surface(color = AdminSurface, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, AdminBorder)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                rows.forEach { (icon, data) ->
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.size(40.dp).background(AdminBg, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(icon, null, modifier = Modifier.size(20.dp), tint = AdminRed)
                        }
                        Column {
                            Text(data.first, fontSize = 11.sp, color = AdminTextMuted, fontWeight = FontWeight.Bold)
                            Text(data.second, fontSize = 15.sp, fontWeight = FontWeight.Black, color = AdminTextMain)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItemsCard(items: List<OrderItemResponse>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Danh sách món ăn".uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Black, color = AdminTextMuted, letterSpacing = 1.sp)
        Surface(color = AdminSurface, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, AdminBorder)) {
            Column(modifier = Modifier.padding(20.dp)) {
                items.forEachIndexed { idx, item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${item.productName}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = AdminTextMain)
                            Text("Số lượng: ${item.quantity}", fontSize = 13.sp, color = AdminTextMuted, fontWeight = FontWeight.Medium)
                            if (item.sizeName != null) Text("Kích cỡ: ${item.sizeName}", fontSize = 12.sp, color = AdminTextMuted)
                            if (item.toppings.isNotEmpty()) Text("Toppings: ${item.toppings.joinToString(", ")}", fontSize = 12.sp, color = AdminTextMuted)
                        }
                        Text(formatVnd(item.unitPrice * item.quantity), fontWeight = FontWeight.Black, fontSize = 15.sp, color = AdminTextMain)
                    }
                    if (idx < items.lastIndex) HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = AdminBorder.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun OrderFinalCalculationCard(order: AdminOrderDetailResponse) {
    Surface(color = AdminSurface, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, AdminBorder)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tạm tính", color = AdminTextMuted, fontWeight = FontWeight.Bold)
                Text(formatVnd(order.subtotal), fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Phí giao hàng", color = AdminTextMuted, fontWeight = FontWeight.Bold)
                Text(formatVnd(order.shippingFee), fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = AdminBorder)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("TỔNG THANH TOÁN", fontWeight = FontWeight.Black, color = AdminTextMain, letterSpacing = 0.5.sp)
                Text(formatVnd(order.totalAmount), fontWeight = FontWeight.Black, color = AdminRed, fontSize = 22.sp)
            }
        }
    }
}

@Composable
private fun OrderSkeletonList() {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(4) {
            Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(24.dp)).background(AdminBorder.copy(alpha = 0.4f)))
        }
    }
}

@Composable
private fun OrderEmptyView(uiState: AdminOrdersUiState, onClear: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(100.dp).background(AdminBg, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.ReceiptLong, null, modifier = Modifier.size(48.dp), tint = AdminBorder)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Không có đơn hàng nào", fontWeight = FontWeight.Black, color = AdminTextMain, fontSize = 18.sp)
                Text("Hãy thử thay đổi bộ lọc hoặc tìm kiếm.", fontSize = 14.sp, color = AdminTextMuted, textAlign = TextAlign.Center)
            }
            Button(
                onClick = onClear,
                colors = ButtonDefaults.buttonColors(containerColor = AdminRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("XÓA BỘ LỌC", fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
private fun AdminOrdersErrorView(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Outlined.ErrorOutline, null, modifier = Modifier.size(64.dp), tint = Color(0xFFEF4444))
            Text("Không thể tải đơn hàng", fontWeight = FontWeight.Black, color = AdminTextMain, fontSize = 18.sp)
            Text(message, fontSize = 14.sp, color = AdminTextMuted, textAlign = TextAlign.Center)
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = AdminRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("THỬ LẠI", fontWeight = FontWeight.Bold)
            }
        }
    }
}
private fun hasActiveAdvancedFilters(uiState: AdminOrdersUiState): Boolean {
    return uiState.selectedStatus != null || uiState.fromDate != null || uiState.toDate != null
}

@Composable
private fun ConfirmStatusDialog(actionLabel: String, targetStatusLabel: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = AdminRed), shape = RoundedCornerShape(12.dp)) { Text("XÁC NHẬN", fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("HỦY", color = AdminTextMuted, fontWeight = FontWeight.Bold) } },
        title = { Text(actionLabel, fontWeight = FontWeight.Black) },
        text = { Text("Chuyển trạng thái đơn hàng sang: $targetStatusLabel?", fontSize = 15.sp) },
        shape = RoundedCornerShape(28.dp),
        containerColor = AdminSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrdersFilterBottomSheet(
    selectedStatus: String?, fromDate: String?, toDate: String?, sort: String,
    onDismiss: () -> Unit, onClear: () -> Unit, onApply: (String?, String?, String?, String) -> Unit
) {
    val context = LocalContext.current
    var tStatus by remember { mutableStateOf(selectedStatus) }
    var tFrom by remember { mutableStateOf(fromDate) }
    var tTo by remember { mutableStateOf(toDate) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AdminSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AdminBorder) },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(modifier = Modifier.navigationBarsPadding().padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text("Bộ lọc nâng cao", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Trạng thái đơn hàng", fontWeight = FontWeight.Bold, color = AdminTextMain)
                FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                    adminOrderStatusFilters.forEach { filter ->
                        val sel = tStatus == filter.value
                        FilterChip(
                            selected = sel,
                            onClick = { tStatus = filter.value },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AdminRed, selectedLabelColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Khoảng thời gian", fontWeight = FontWeight.Bold, color = AdminTextMain)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(modifier = Modifier.weight(1f).height(48.dp), onClick = { 
                        DatePickerDialog(context, { _, y, m, d -> tFrom = formatApiDate(LocalDate.of(y, m+1, d)) }, LocalDate.now().year, LocalDate.now().monthValue-1, LocalDate.now().dayOfMonth).show()
                    }, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, AdminBorder)) {
                        Text(if (tFrom != null) formatFilterDate(tFrom) else "Từ ngày", fontSize = 13.sp, color = if (tFrom != null) AdminRed else AdminTextMuted)
                    }
                    OutlinedButton(modifier = Modifier.weight(1f).height(48.dp), onClick = {
                        DatePickerDialog(context, { _, y, m, d -> tTo = formatApiDate(LocalDate.of(y, m+1, d)) }, LocalDate.now().year, LocalDate.now().monthValue-1, LocalDate.now().dayOfMonth).show()
                    }, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, AdminBorder)) {
                        Text(if (tTo != null) formatFilterDate(tTo) else "Đến ngày", fontSize = 13.sp, color = if (tTo != null) AdminRed else AdminTextMuted)
                    }
                }
            }

            Button(
                onClick = { onApply(tStatus, tFrom, tTo, sort); onDismiss() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AdminRed)
            ) {
                Text("ÁP DỤNG BỘ LỌC", fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
            }
            TextButton(onClick = { onClear(); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                Text("Xóa tất cả bộ lọc", color = AdminTextMuted, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(mainAxisSpacing: androidx.compose.ui.unit.Dp, crossAxisSpacing: androidx.compose.ui.unit.Dp, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing), verticalArrangement = Arrangement.spacedBy(crossAxisSpacing)) { content() }
}
