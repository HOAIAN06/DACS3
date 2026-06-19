package com.fastdash.app.ui.admin

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fastdash.app.data.model.response.AdminCustomerOrderResponse
import com.fastdash.app.data.model.response.AdminCustomerResponse
import com.fastdash.app.utils.CurrencyUtils
import com.fastdash.app.viewmodel.AdminCustomerViewModel
import com.fastdash.app.viewmodel.CustomerSegmentFilter

// Unified Admin Palette
private val AdminRed = Color(0xFFC8102E)
private val AdminBg = Color(0xFFF9FAFB)
private val AdminSurface = Color.White
private val AdminTextMain = Color(0xFF111827)
private val AdminTextMuted = Color(0xFF6B7280)
private val AdminBorder = Color(0xFFE5E7EB)
private val ColorSuccess = Color(0xFF10B981)
private val ColorPending = Color(0xFFF59E0B)
private val ColorDanger = Color(0xFFEF4444)
private val ColorInfo = Color(0xFF3B82F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCustomersScreen(
    viewModel: AdminCustomerViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onOpenOrderDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val visibleCustomers = remember(uiState.customers, uiState.selectedSegment) {
        uiState.customers.filter { customer ->
            when (uiState.selectedSegment) {
                CustomerSegmentFilter.ALL -> true
                CustomerSegmentFilter.ACTIVE_BUYER -> customer.segment.equals("ACTIVE", ignoreCase = true) || (customer.totalOrders > 0 && customer.status == 1)
                CustomerSegmentFilter.NEW -> customer.segment.equals("NEW", ignoreCase = true) || customer.totalOrders == 0
                CustomerSegmentFilter.BLOCKED -> customer.segment.equals("BLOCKED", ignoreCase = true) || customer.status == 0
            }
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.errorCode) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            if (uiState.errorCode == 401) onLogout()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Quản lý khách hàng", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text("${uiState.totalElements} khách hàng trong hệ thống", fontSize = 12.sp, color = AdminTextMuted, fontWeight = FontWeight.Medium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AdminTextMain)
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::refresh,
                        modifier = Modifier.background(AdminSurface, CircleShape).border(1.dp, AdminBorder, CircleShape)
                    ) {
                        Icon(Icons.Outlined.Refresh, null, tint = AdminTextMain, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBg)
            )
        },
        containerColor = AdminBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search and Segment Selection
            Surface(
                color = AdminBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(bottom = 12.dp, start = 20.dp, end = 20.dp)) {
                    OutlinedTextField(
                        value = uiState.keyword,
                        onValueChange = viewModel::onKeywordChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = { Text("Tìm tên, email, số điện thoại...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp), tint = AdminTextMuted) },
                        trailingIcon = {
                            if (uiState.keyword.isNotEmpty()) {
                                IconButton(onClick = viewModel::clearSearch) {
                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp), tint = AdminTextMuted)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = AdminBorder,
                            focusedBorderColor = AdminRed,
                            unfocusedContainerColor = AdminSurface,
                            focusedContainerColor = AdminSurface
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LazyColumn(
                            modifier = Modifier.height(40.dp).weight(1f),
                            // Using Row inside LazyColumn or just a Scrollable Row
                        ) { /* Not ideal, let's use a scrollable Row */ }
                        
                        Row(
                            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SegmentChip("Tất cả", uiState.selectedSegment == CustomerSegmentFilter.ALL) { viewModel.onSegmentChange(CustomerSegmentFilter.ALL) }
                            SegmentChip("Đã mua", uiState.selectedSegment == CustomerSegmentFilter.ACTIVE_BUYER) { viewModel.onSegmentChange(CustomerSegmentFilter.ACTIVE_BUYER) }
                            SegmentChip("Mới", uiState.selectedSegment == CustomerSegmentFilter.NEW) { viewModel.onSegmentChange(CustomerSegmentFilter.NEW) }
                            SegmentChip("Bị khóa", uiState.selectedSegment == CustomerSegmentFilter.BLOCKED) { viewModel.onSegmentChange(CustomerSegmentFilter.BLOCKED) }
                        }
                        
                        IconButton(
                            onClick = viewModel::toggleCreatedAtSort,
                            modifier = Modifier.size(40.dp).background(AdminSurface, RoundedCornerShape(12.dp)).border(1.dp, AdminBorder, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                if (uiState.sortDir == "desc") Icons.AutoMirrored.Filled.Sort else Icons.Default.SortByAlpha,
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = AdminRed
                            )
                        }
                    }
                }
            }

            if (uiState.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AdminRed, trackColor = AdminRed.copy(alpha = 0.1f))
            }

            CustomerInsightsRow(uiState.customers)

            // Customer List
            Box(modifier = Modifier.weight(1f)) {
                if (!uiState.loading && visibleCustomers.isEmpty()) {
                    EmptyStateView(onRefresh = viewModel::clearFilters)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(visibleCustomers, key = { _, item -> item.id }) { _, customer ->
                            CustomerListItem(
                                customer = customer,
                                onClick = { viewModel.openDetail(customer.id) },
                                onAction = {
                                    viewModel.openStatusConfirm(customer, if (customer.status == 1) 0 else 1)
                                }
                            )
                        }
                    }
                }
            }

            // Pagination Bottom Bar
            Surface(
                color = AdminSurface,
                border = BorderStroke(1.dp, AdminBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tổng cộng: ${uiState.totalElements}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminTextMuted
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(
                            onClick = { viewModel.onPageChange(uiState.page - 1) },
                            enabled = uiState.page > 0,
                            modifier = Modifier.size(36.dp).background(if (uiState.page > 0) AdminBg else Color.Transparent, CircleShape)
                        ) { Icon(Icons.Default.ChevronLeft, null, tint = if (uiState.page > 0) AdminTextMain else AdminTextMuted) }
                        
                        Text("${uiState.page + 1} / ${uiState.totalPages.coerceAtLeast(1)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdminTextMain)
                        
                        IconButton(
                            onClick = { viewModel.onPageChange(uiState.page + 1) },
                            enabled = uiState.page + 1 < uiState.totalPages,
                            modifier = Modifier.size(36.dp).background(if (uiState.page + 1 < uiState.totalPages) AdminBg else Color.Transparent, CircleShape)
                        ) { Icon(Icons.Default.ChevronRight, null, tint = if (uiState.page + 1 < uiState.totalPages) AdminTextMain else AdminTextMuted) }
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.detailOpen) {
        CustomerDetailBottomSheet(
            customer = uiState.selectedCustomer,
            isLoading = uiState.detailLoading,
            onDismiss = viewModel::closeDetail,
            onEdit = {
                uiState.selectedCustomer?.let {
                    viewModel.closeDetail()
                    viewModel.openEditCustomer(it)
                }
            },
            onViewOrders = {
                uiState.selectedCustomer?.let {
                    viewModel.closeDetail()
                    viewModel.openOrders(it)
                }
            },
            onToggleStatus = {
                uiState.selectedCustomer?.let {
                    viewModel.openStatusConfirm(it, if (it.status == 1) 0 else 1)
                }
            }
        )
    }

    if (uiState.editOpen) {
        EditCustomerDialog(
            fullName = uiState.editFullName,
            email = uiState.editEmail,
            phone = uiState.editPhone,
            address = uiState.editAddress,
            isLoading = uiState.editLoading,
            errorMessage = uiState.editErrorMessage,
            onFullNameChange = viewModel::onEditFullNameChange,
            onEmailChange = viewModel::onEditEmailChange,
            onPhoneChange = viewModel::onEditPhoneChange,
            onAddressChange = viewModel::onEditAddressChange,
            onDismiss = viewModel::closeEditCustomer,
            onSave = { viewModel.saveCustomerEdits(onUnauthorized = onLogout) }
        )
    }

    if (uiState.ordersOpen) {
        CustomerOrdersDialog(
            customer = uiState.ordersCustomer,
            orders = uiState.customerOrders,
            isLoading = uiState.ordersLoading,
            page = uiState.ordersPage,
            totalPages = uiState.ordersTotalPages,
            onDismiss = viewModel::closeOrders,
            onPrevious = { viewModel.onOrdersPageChange(uiState.ordersPage - 1) },
            onNext = { viewModel.onOrdersPageChange(uiState.ordersPage + 1) },
            onOpenOrderDetail = onOpenOrderDetail
        )
    }

    if (uiState.confirmOpen) {
        ConfirmStatusDialog(
            customer = uiState.statusTargetCustomer,
            nextStatus = uiState.nextStatus,
            loading = uiState.statusUpdating,
            onDismiss = viewModel::closeStatusConfirm,
            onConfirm = { viewModel.confirmStatusUpdate(onUnauthorized = onLogout) }
        )
    }
}

@Composable
private fun CustomerListItem(
    customer: AdminCustomerResponse,
    onClick: () -> Unit,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = AdminSurface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, AdminBorder),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar / Initial
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AdminRed.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    customer.fullName?.trim()?.firstOrNull()?.uppercase() ?: "K",
                    color = AdminRed,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        customer.fullName?.takeIf { it.isNotBlank() } ?: "Chưa đặt tên",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = AdminTextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (customer.roleName != "USER") {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = AdminRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                customer.roleName ?: "ADMIN",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = AdminRed
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Email, null, modifier = Modifier.size(14.dp), tint = AdminTextMuted)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        customer.email ?: "Không có email",
                        fontSize = 13.sp,
                        color = AdminTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Phone, null, modifier = Modifier.size(14.dp), tint = AdminTextMuted)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        customer.phone?.takeIf { it.isNotBlank() } ?: "--",
                        fontSize = 13.sp,
                        color = AdminTextMuted
                    )
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.height(100.dp)) {
                StatusBadge(customer.status)
                IconButton(
                    onClick = onAction,
                    modifier = Modifier.size(40.dp).background(AdminBg, CircleShape)
                ) {
                    Icon(
                        if (customer.status == 1) Icons.Default.LockOpen else Icons.Default.Lock,
                        null,
                        tint = if (customer.status == 1) ColorSuccess else ColorDanger,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (selected) AdminRed else AdminSurface,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) null else BorderStroke(1.dp, AdminBorder)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else AdminTextMuted
        )
    }
}

@Composable
private fun CustomerInsightsRow(customers: List<AdminCustomerResponse>) {
    val activeCount = customers.count { it.status == 1 }
    val buyerCount = customers.count { it.totalOrders > 0 }
    val newCount = customers.count { it.totalOrders == 0 }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        InsightCard("Đang hoạt động", activeCount.toString(), ColorSuccess, Icons.Outlined.CheckCircle, Modifier.weight(1f))
        InsightCard("Đã mua hàng", buyerCount.toString(), AdminRed, Icons.Outlined.ShoppingBag, Modifier.weight(1f))
        InsightCard("Khách hàng mới", newCount.toString(), ColorInfo, Icons.Outlined.PersonAdd, Modifier.weight(1f))
    }
}

@Composable
private fun InsightCard(label: String, value: String, accent: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = AdminSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AdminBorder),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(32.dp).background(accent.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(value, fontSize = 18.sp, color = AdminTextMain, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(label, fontSize = 10.sp, color = AdminTextMuted, fontWeight = FontWeight.Bold, lineHeight = 12.sp)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: Int) {
    val (label, color, bg) = when (status) {
        1 -> Triple("HOẠT ĐỘNG", ColorSuccess, ColorSuccess.copy(alpha = 0.1f))
        0 -> Triple("BỊ KHÓA", ColorDanger, ColorDanger.copy(alpha = 0.1f))
        else -> Triple("KHÁC", AdminTextMuted, AdminBorder)
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerDetailBottomSheet(
    customer: AdminCustomerResponse?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onViewOrders: () -> Unit,
    onToggleStatus: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AdminSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AdminBorder) },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(AdminRed.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        customer?.fullName?.trim()?.firstOrNull()?.uppercase() ?: "K",
                        color = AdminRed,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        customer?.fullName ?: "Chưa đặt tên",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = AdminTextMain
                    )
                    Text(
                        "Khách hàng ID: #${customer?.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AdminTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.weight(1f))
                StatusBadge(customer?.status ?: 1)
            }

            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AdminRed, strokeWidth = 3.dp)
                }
            } else if (customer != null) {
                Surface(
                    color = AdminBg,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, AdminBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        InfoItem(Icons.Outlined.Email, "Địa chỉ Email", customer.email ?: "Chưa có")
                        InfoItem(Icons.Outlined.Phone, "Số điện thoại", customer.phone ?: "Chưa có")
                        InfoItem(Icons.Outlined.Place, "Địa chỉ giao hàng", customer.address ?: "Chưa có")
                        InfoItem(Icons.Outlined.CalendarToday, "Ngày tham gia hệ thống", formatDateTime(customer.createdAt))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InsightCard("Tổng đơn hàng", customer.totalOrders.toString(), AdminRed, Icons.Outlined.Receipt, Modifier.weight(1f))
                    InsightCard("Đã hoàn tất", customer.completedOrders.toString(), ColorSuccess, Icons.Outlined.CheckCircle, Modifier.weight(1f))
                    InsightCard("Đã hủy đơn", customer.cancelledOrders.toString(), ColorPending, Icons.Outlined.Cancel, Modifier.weight(1f))
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onViewOrders,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminTextMain)
                    ) {
                        Icon(Icons.Outlined.ShoppingBag, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("XEM LỊCH SỬ ĐƠN HÀNG", fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, AdminBorder)
                        ) {
                            Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(20.dp), tint = AdminTextMain)
                            Spacer(Modifier.width(8.dp))
                            Text("CHỈNH SỬA", color = AdminTextMain, fontWeight = FontWeight.ExtraBold)
                        }
                        
                        Button(
                            onClick = onToggleStatus,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (customer.status == 1) ColorDanger.copy(alpha = 0.1f) else ColorSuccess.copy(alpha = 0.1f),
                                contentColor = if (customer.status == 1) ColorDanger else ColorSuccess
                            )
                        ) {
                            Icon(if (customer.status == 1) Icons.Default.Lock else Icons.Default.LockOpen, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (customer.status == 1) "KHÓA" else "MỞ KHÓA", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditCustomerDialog(
    fullName: String,
    email: String,
    phone: String,
    address: String,
    isLoading: Boolean,
    errorMessage: String?,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa thông tin khách hàng", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                errorMessage?.let {
                    Surface(color = ColorDanger.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(it, color = ColorDanger, fontSize = 12.sp, modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = { Text("Họ tên") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChange,
                    label = { Text("Địa chỉ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave, enabled = !isLoading, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = AdminRed)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("LƯU THAY ĐỔI", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("HỦY", color = AdminTextMuted, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = AdminSurface
    )
}

@Composable
private fun InfoItem(icon: ImageVector, label: String, value: String, valueColor: Color = AdminTextMain) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(44.dp).background(AdminBg, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = AdminRed)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 11.sp, color = AdminTextMuted, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = valueColor)
        }
    }
}

@Composable
private fun CustomerOrdersDialog(
    customer: AdminCustomerResponse?,
    orders: List<AdminCustomerOrderResponse>,
    isLoading: Boolean,
    page: Int,
    totalPages: Int,
    onDismiss: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenOrderDetail: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("ĐÓNG", color = AdminRed, fontWeight = FontWeight.Bold) } },
        title = {
            Column {
                Text("Lịch sử đơn hàng", fontWeight = FontWeight.Black)
                Text(customer?.fullName ?: "", fontSize = 13.sp, color = AdminTextMuted, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.widthIn(max = 450.dp)) {
                if (isLoading) {
                    Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AdminRed, strokeWidth = 3.dp)
                    }
                } else if (orders.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("Chưa có đơn hàng nào.", color = AdminTextMuted, fontWeight = FontWeight.Medium)
                    }
                } else {
                    LazyColumn(modifier = Modifier.height(350.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(orders) { _, order ->
                            OrderMiniCard(order, onOpenOrderDetail)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onPrevious, enabled = page > 0, modifier = Modifier.background(AdminBg, CircleShape)) {
                            Icon(Icons.Default.ChevronLeft, null, tint = if (page > 0) AdminRed else AdminTextMuted)
                        }
                        Text("${page + 1} / ${totalPages.coerceAtLeast(1)}", fontSize = 14.sp, fontWeight = FontWeight.Black)
                        IconButton(onClick = onNext, enabled = page + 1 < totalPages, modifier = Modifier.background(AdminBg, CircleShape)) {
                            Icon(Icons.Default.ChevronRight, null, tint = if (page + 1 < totalPages) AdminRed else AdminTextMuted)
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = AdminSurface
    )
}

@Composable
private fun OrderMiniCard(order: AdminCustomerOrderResponse, onOpenOrderDetail: (Long) -> Unit) {
    Surface(
        color = AdminSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onOpenOrderDetail(order.id) },
        border = BorderStroke(1.dp, AdminBorder),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    order.orderCode ?: "#${order.id}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = AdminTextMain
                )
                Text(
                    formatDateTime(order.createdAt),
                    fontSize = 11.sp,
                    color = AdminTextMuted,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OrderMiniBadge(order.orderStatus ?: "PENDING", ColorInfo)
                    OrderMiniBadge(mapPaymentStatus(order.paymentStatus), if (order.paymentStatus == "PAID") ColorSuccess else ColorPending)
                }
            }
            Text(
                CurrencyUtils.formatVnd(order.totalAmount),
                fontWeight = FontWeight.Black,
                color = AdminRed,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun OrderMiniBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            color = color,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun ConfirmStatusDialog(
    customer: AdminCustomerResponse?,
    nextStatus: Int?,
    loading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isUnlock = nextStatus == 1
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUnlock) "Mở khóa tài khoản?" else "Khóa tài khoản?", fontWeight = FontWeight.Black) },
        text = { Text("Bạn có chắc chắn muốn ${if (isUnlock) "mở khóa" else "khóa"} tài khoản của ${customer?.fullName ?: "khách hàng này"}?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = if (isUnlock) ColorSuccess else ColorDanger),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("XÁC NHẬN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !loading) { Text("HỦY", color = AdminTextMuted, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = AdminSurface
    )
}

private fun mapPaymentStatus(value: String?): String = when (value?.uppercase()) {
    "PAID" -> "Đã thanh toán"
    "UNPAID" -> "Chưa thanh toán"
    else -> value ?: "Khác"
}

@Composable
private fun EmptyStateView(onRefresh: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = AdminBg,
                shape = CircleShape,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Group,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = AdminBorder
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Không tìm thấy khách hàng",
                    fontWeight = FontWeight.Bold,
                    color = AdminTextMain,
                    fontSize = 18.sp
                )
                Text(
                    "Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm.",
                    color = AdminTextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(containerColor = AdminRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Làm mới danh sách", fontWeight = FontWeight.Bold)
            }
        }
    }
}
