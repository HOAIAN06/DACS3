package com.fastdash.app.ui.admin

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastdash.app.R
import com.fastdash.app.data.model.response.CategoryResponse
import com.fastdash.app.data.model.response.OrderResponse
import com.fastdash.app.data.repository.CategoryRepository
import com.fastdash.app.data.repository.OrderRepository
import com.fastdash.app.data.repository.AdminOrderStatusRepository
import com.fastdash.app.utils.CurrencyUtils
import kotlinx.coroutines.launch

private val AdminRed = Color(0xFFE31837)
private val AdminBlue = Color(0xFF0078AE)
private val AdminBackground = Color(0xFFF8F5F2)
private val AdminCard = Color.White
private val AdminText = Color(0xFF202124)
private val AdminMuted = Color(0xFF7A7A7A)
private val AdminSuccess = Color(0xFF1E8E3E)
private val AdminWarning = Color(0xFFF29900)
private val AdminDanger = Color(0xFFB3261E)

@Composable
fun AdminComingSoonScreen(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBackground)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AdminTopBar(
            title = title,
            subtitle = subtitle,
            onBack = onBack,
            onLogout = onLogout,
            accentColor = AdminWarning
        )

        AdminInfoCard(
            title = "Chức năng đang chờ triển khai",
            subtitle = "Màn này được khóa để không lẫn với luồng khách hàng. Khi có API/model cho module này, mình sẽ nối CRUD thật vào đây.",
            accentColor = AdminWarning
        )

        AdminInfoCard(
            title = "Cần có để làm thật",
            subtitle = "1) API backend CRUD\n2) Request/response model\n3) Quyền admin ở server\n4) Validation và xử lý lỗi",
            accentColor = AdminBlue
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = AdminCard),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Danh sách chức năng sẽ làm sau",
                    color = AdminText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                val plannedItems = when (title.lowercase()) {
                    "quản lý tài khoản" -> listOf(
                        "Xem danh sách người dùng",
                        "Thêm / sửa / khóa tài khoản",
                        "Phân quyền Admin, Customer, Staff",
                        "Tìm kiếm theo tên, email, số điện thoại"
                    )
                    "quản lý chi nhánh" -> listOf(
                        "Thêm chi nhánh",
                        "Sửa tên, địa chỉ, số điện thoại",
                        "Giờ mở / đóng cửa",
                        "Bật / tắt trạng thái chi nhánh"
                    )
                    "quản lý size" -> listOf(
                        "Thêm size cho từng sản phẩm",
                        "Sửa giá theo size",
                        "Bật / tắt size",
                        "Không cho trùng size trong cùng một món"
                    )
                    "quản lý topping" -> listOf(
                        "Thêm topping",
                        "Sửa tên và giá topping",
                        "Upload ảnh topping",
                        "Bật / tắt topping"
                    )
                    "quản lý thanh toán" -> listOf(
                        "Xem COD / ONLINE",
                        "Xem trạng thái UNPAID / PAID / FAILED",
                        "Xem mã giao dịch",
                        "Cập nhật trạng thái thanh toán"
                    )
                    else -> listOf(
                        "CRUD đầy đủ sẽ nối sau",
                        "Cần backend trước khi làm tiếp",
                        "Cần thêm model request/response",
                        "Sẽ giữ admin shell riêng"
                    )
                }

                plannedItems.forEach { item ->
                    Text(
                        text = "• $item",
                        color = AdminMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminCategoriesScreen(
    viewModel: com.fastdash.app.viewmodel.AdminCategoryViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val visibleCategories = remember(uiState.categories, uiState.searchQuery) {
        val q = uiState.searchQuery.trim()
        if (q.isBlank()) uiState.categories else uiState.categories.filter {
            it.name.contains(q, ignoreCase = true) || (it.description?.contains(q, ignoreCase = true) == true)
        }
    }

    if (uiState.showAddForm) {
        AdminCategoryFormDialog(
            viewModel = viewModel,
            uiState = uiState,
            isEditing = uiState.editingId != null,
            onClose = { viewModel.closeForm() }
        )
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBackground)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AdminTopBar(
            title = "Quản lý danh mục",
            subtitle = "Xem, thêm, sửa, xóa danh mục món ăn",
            onBack = onBack,
            onLogout = onLogout,
            accentColor = AdminRed
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                singleLine = true,
                placeholder = { Text("Tìm danh mục") }
            )
            Button(
                onClick = { viewModel.showAddForm() },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AdminRed)
            ) {
                Text("+ Thêm", fontWeight = FontWeight.Bold)
            }
        }

        if (uiState.loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AdminRed)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Tổng danh mục",
                    value = uiState.categories.size.toString(),
                    accentColor = AdminRed
                )
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Hiển thị",
                    value = visibleCategories.size.toString(),
                    accentColor = AdminBlue
                )
            }

            if (uiState.isError && uiState.message != null) {
                AdminInfoCard(
                    title = "Lỗi tải dữ liệu",
                    subtitle = uiState.message!!,
                    accentColor = AdminWarning
                )
            }

            if (visibleCategories.isEmpty()) {
                AdminInfoCard(
                    title = "Không có kết quả",
                    subtitle = "Không tìm thấy danh mục phù hợp.",
                    accentColor = AdminMuted
                )
            } else {
                visibleCategories.forEach { category ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AdminCard),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = category.name.orEmpty().ifBlank { "(Không tên)" },
                                        color = AdminText,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = category.description ?: "Chưa có mô tả",
                                        color = AdminMuted,
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = (if (category.status == 1) AdminSuccess else AdminMuted).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = if (category.status == 1) "Hiển thị" else "Ẩn",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        color = if (category.status == 1) AdminSuccess else AdminMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.showEditForm(category) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminBlue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Sửa", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            viewModel.toggleCategoryStatus(category.id, category.status)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (category.status == 1) AdminWarning else AdminSuccess
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(if (category.status == 1) "Ẩn" else "Hiện", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            viewModel.deleteCategory(category.id)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminDanger),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Xóa", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCategoryFormDialog(
    viewModel: com.fastdash.app.viewmodel.AdminCategoryViewModel,
    uiState: com.fastdash.app.viewmodel.AdminCategoryUiState,
    isEditing: Boolean,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = !uiState.formLoading) { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {}
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCard)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEditing) "Sửa danh mục" else "Thêm danh mục",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AdminText,
                    fontWeight = FontWeight.ExtraBold
                )

                OutlinedTextField(
                    value = uiState.nameInput,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text("Tên danh mục *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.formLoading
                )

                OutlinedTextField(
                    value = uiState.descriptionInput,
                    onValueChange = viewModel::onDescriptionChanged,
                    label = { Text("Mô tả") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    enabled = !uiState.formLoading
                )

                if (uiState.formMessage != null) {
                    Surface(
                        color = if (uiState.formError) AdminDanger.copy(alpha = 0.1f) else AdminSuccess.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.formMessage!!,
                            modifier = Modifier.padding(12.dp),
                            color = if (uiState.formError) AdminDanger else AdminSuccess,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.formLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = AdminMuted)
                    ) {
                        Text("Hủy")
                    }

                    Button(
                        onClick = {
                            if (isEditing) viewModel.updateCategory() else viewModel.createCategory()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.nameInput.isNotBlank() && !uiState.formLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = AdminBlue)
                    ) {
                        Text(if (uiState.formLoading) "Đang xử lý..." else (if (isEditing) "Cập nhật" else "Thêm"))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrdersScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { OrderRepository(context.applicationContext) }
    val statusRepository = remember { com.fastdash.app.data.repository.AdminOrderStatusRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var orders by remember { mutableStateOf<List<OrderResponse>>(emptyList()) }
    var selectedStatus by remember { mutableStateOf("ALL") }
    var expandedOrderId by remember { mutableStateOf<Long?>(null) }
    var updatingOrderId by remember { mutableStateOf<Long?>(null) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    suspend fun loadOrders() {
        val response = repository.getOrders()
        if (response.isSuccessful) {
            orders = response.body().orEmpty()
            errorMessage = null
        } else {
            val serverError = response.errorBody()?.string().orEmpty()
            throw IllegalStateException(
                "Không tải được đơn hàng (${response.code()})${if (serverError.isNotBlank()) ": $serverError" else ""}"
            )
        }
    }

    LaunchedEffect(Unit) {
        loading = true
        errorMessage = null
        try {
            loadOrders()
        } catch (e: Exception) {
            errorMessage = e.message ?: "Không tải được đơn hàng"
        } finally {
            loading = false
        }
    }

    val statusOptions = remember(orders) {
        listOf("ALL") + orders.map { it.status.trim().uppercase() }.distinct().sorted()
    }

    val visibleOrders = remember(orders, selectedStatus) {
        if (selectedStatus == "ALL") orders else orders.filter { it.status.trim().uppercase() == selectedStatus }
    }

    val revenue = remember(orders) { orders.sumOf { it.totalAmount } }
    val pendingOrders = remember(orders) { orders.count { it.status.isPendingStatus() } }
    val completedOrders = remember(orders) { orders.count { it.status.isCompletedStatus() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBackground)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AdminTopBar(
            title = "Quản lý đơn hàng",
            subtitle = "Xem danh sách đơn và chi tiết món. Cập nhật trạng thái sẽ nối sau.",
            onBack = onBack,
            onLogout = onLogout,
            accentColor = AdminBlue
        )

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AdminBlue)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Tổng đơn",
                    value = orders.size.toString(),
                    accentColor = AdminBlue
                )
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Doanh thu",
                    value = CurrencyUtils.formatVnd(revenue),
                    accentColor = AdminRed
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Chờ xử lý",
                    value = pendingOrders.toString(),
                    accentColor = AdminWarning
                )
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Hoàn thành",
                    value = completedOrders.toString(),
                    accentColor = AdminSuccess
                )
            }

            if (errorMessage != null) {
                AdminInfoCard(
                    title = "Lỗi tải dữ liệu",
                    subtitle = errorMessage!!,
                    accentColor = AdminWarning
                )
            }

            AdminInfoCard(
                title = "Thông tin đang có",
                subtitle = "Màn này hiển thị được mã đơn, trạng thái, tổng tiền, phí ship và các món trong đơn. Dữ liệu payment/status update chi tiết sẽ làm sau.",
                accentColor = AdminBlue
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Lọc trạng thái",
                    color = AdminText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    statusOptions.forEach { status ->
                        val selected = selectedStatus == status
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) AdminBlue else AdminCard,
                            modifier = Modifier.clickable { selectedStatus = status }
                        ) {
                            Text(
                                text = status,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (selected) Color.White else AdminText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (visibleOrders.isEmpty()) {
                AdminInfoCard(
                    title = "Không có đơn phù hợp",
                    subtitle = "Chưa có đơn hàng ở trạng thái này.",
                    accentColor = AdminMuted
                )
            } else {
                visibleOrders.forEach { order ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AdminCard),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = order.code,
                                        color = AdminText,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = order.createdAt,
                                        color = AdminMuted,
                                        fontSize = 12.sp
                                    )
                                }
                                StatusBadge(status = order.status)
                            }

                            Text(
                                text = "Tổng tiền: ${CurrencyUtils.formatVnd(order.totalAmount)} · Phí ship: ${CurrencyUtils.formatVnd(order.shippingFee)}",
                                color = AdminText,
                                fontSize = 13.sp
                            )

                            Text(
                                text = order.deliveryAddress ?: "Đơn nhận tại cửa hàng / chưa có địa chỉ giao",
                                color = AdminMuted,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { expandedOrderId = if (expandedOrderId == order.id) null else order.id },
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminBlue),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(if (expandedOrderId == order.id) "Ẩn chi tiết" else "Xem chi tiết")
                                }
                                Button(
                                    onClick = { updatingOrderId = order.id; showStatusDropdown = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminWarning),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Cập nhật trạng thái")
                                }
                            }

                            if (updatingOrderId == order.id && showStatusDropdown) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = AdminCard,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        listOf("PENDING", "CONFIRMED", "PREPARING", "DELIVERING", "COMPLETED", "CANCELLED")
                                            .forEach { status ->
                                            Text(
                                                text = status,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        scope.launch {
                                                            try {
                                                                val updateResponse = statusRepository.updateOrderStatus(order.id, status)
                                                                if (!updateResponse.isSuccessful) {
                                                                    val serverError = updateResponse.errorBody()?.string().orEmpty()
                                                                    throw IllegalStateException(
                                                                        "Cập nhật trạng thái thất bại (${updateResponse.code()})${if (serverError.isNotBlank()) ": $serverError" else ""}"
                                                                    )
                                                                }
                                                                Toast.makeText(
                                                                    context,
                                                                    "Cập nhật trạng thái thành công",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                updatingOrderId = null
                                                                showStatusDropdown = false
                                                                loading = true
                                                                loadOrders()
                                                                loading = false
                                                            } catch (e: Exception) {
                                                                errorMessage = e.message
                                                                loading = false
                                                            }
                                                        }
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                color = AdminText,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }

                            if (expandedOrderId == order.id) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Món trong đơn",
                                        color = AdminText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    order.items.orEmpty().forEach { item ->
                                        Surface(
                                            color = AdminBackground,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = item.productName,
                                                    color = AdminText,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "Số lượng: ${item.quantity} · Thành tiền: ${CurrencyUtils.formatVnd(item.price)}",
                                                    color = AdminMuted,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = "Size / topping / ghi chú: sẽ hiển thị sau khi backend trả về đủ dữ liệu",
                                                    color = AdminMuted,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AdminInfoCard(
                title = "Thanh toán & trạng thái",
                subtitle = "Mục payment, mã giao dịch và cập nhật trạng thái đơn sẽ nối tiếp sau khi có API admin tương ứng.",
                accentColor = AdminWarning
            )
        }
    }
}

@Composable
private fun AdminTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    accentColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AdminCard),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = AdminText,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = subtitle,
                        color = AdminMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.logo2),
                    contentDescription = null,
                    modifier = Modifier.height(24.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onBack) { Text("Quay lại") }
                TextButton(onClick = onLogout) { Text("Đăng xuất") }
            }
        }
    }
}

@Composable
private fun AdminInfoCard(title: String, subtitle: String, accentColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AdminCard),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Text(
                text = subtitle,
                color = AdminText,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun AdminMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AdminCard),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.1f)
            ) {
                Text(
                    title,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Text(
                text = value,
                color = AdminText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val normalized = status.trim().uppercase()
    val color = when (normalized) {
        "COMPLETED" -> AdminSuccess
        "CANCELLED" -> AdminDanger
        "DELIVERING" -> AdminBlue
        "CONFIRMED", "PREPARING", "PENDING" -> AdminWarning
        else -> AdminMuted
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = normalized,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun String.isPendingStatus(): Boolean {
    val normalized = trim().uppercase()
    return normalized == "PENDING" || normalized == "CONFIRMED" || normalized == "PREPARING" || normalized == "DELIVERING"
}

private fun String.isCompletedStatus(): Boolean {
     return trim().uppercase() == "COMPLETED"
}

@Composable
fun AdminSizesScreen(
    viewModel: com.fastdash.app.viewmodel.AdminSizeViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    if (uiState.showAddForm) {
        AdminSizeFormDialog(
            viewModel = viewModel,
            uiState = uiState,
            isEditing = uiState.editingId != null,
            onClose = { viewModel.closeForm() }
        )
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBackground)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AdminTopBar(
            title = "Quản lý size",
            subtitle = "Quản lý kích cỡ (S, M, L) cho từng sản phẩm",
            onBack = onBack,
            onLogout = onLogout,
            accentColor = AdminBlue
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.productIdInput,
                onValueChange = viewModel::onProductIdChanged,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                singleLine = true,
                placeholder = { Text("Mã sản phẩm") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Button(
                onClick = {
                    val productId = uiState.productIdInput.toLongOrNull()
                    if (productId != null && productId > 0) {
                        viewModel.loadSizesByProduct(productId)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AdminBlue)
            ) {
                Text("Tìm")
            }
        }

        if (uiState.selectedProductId > 0) {
            Button(
                onClick = { viewModel.showAddForm() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AdminSuccess)
            ) {
                Text("+ Thêm size")
            }
        }

        if (uiState.loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AdminBlue)
            }
        } else if (uiState.selectedProductId > 0) {
            AdminMetricCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Tổng size",
                value = uiState.sizes.size.toString(),
                accentColor = AdminBlue
            )

            if (uiState.isError && uiState.message != null) {
                AdminInfoCard(
                    title = "Lỗi",
                    subtitle = uiState.message!!,
                    accentColor = AdminWarning
                )
            }

            if (uiState.sizes.isEmpty()) {
                AdminInfoCard(
                    title = "Chưa có size",
                    subtitle = "Bấm nút 'Thêm size' để thêm size mới cho sản phẩm này.",
                    accentColor = AdminMuted
                )
            } else {
                uiState.sizes.forEach { size ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AdminCard),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = size.sizeName,
                                        color = AdminText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = CurrencyUtils.formatVnd(size.price.toDouble()),
                                        color = AdminRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = (if (size.status == 1) AdminSuccess else AdminMuted).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = if (size.status == 1) "Bật" else "Tắt",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        color = if (size.status == 1) AdminSuccess else AdminMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.showEditForm(size) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Sửa", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        scope.launch { viewModel.toggleSizeStatus(size.id, size.status) }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (size.status == 1) AdminWarning else AdminSuccess
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(if (size.status == 1) "Tắt" else "Bật", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        scope.launch { viewModel.deleteSize(size.id) }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminDanger),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Xóa", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSizeFormDialog(
    viewModel: com.fastdash.app.viewmodel.AdminSizeViewModel,
    uiState: com.fastdash.app.viewmodel.AdminSizeUiState,
    isEditing: Boolean,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = !uiState.formLoading) { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {}
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCard)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEditing) "Sửa size" else "Thêm size",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AdminText,
                    fontWeight = FontWeight.ExtraBold
                )

                OutlinedTextField(
                    value = uiState.sizeNameInput,
                    onValueChange = viewModel::onSizeNameChanged,
                    label = { Text("Tên size (S, M, L) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.formLoading
                )

                OutlinedTextField(
                    value = uiState.priceInput,
                    onValueChange = viewModel::onPriceChanged,
                    label = { Text("Giá thêm (nếu có) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = !uiState.formLoading
                )

                if (uiState.formMessage != null) {
                    Surface(
                        color = if (uiState.formError) AdminDanger.copy(alpha = 0.1f) else AdminSuccess.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.formMessage!!,
                            modifier = Modifier.padding(12.dp),
                            color = if (uiState.formError) AdminDanger else AdminSuccess,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.formLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = AdminMuted)
                    ) {
                        Text("Hủy")
                    }

                    Button(
                        onClick = {
                            if (isEditing) viewModel.updateSize() else viewModel.createSize()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.sizeNameInput.isNotBlank() && uiState.priceInput.isNotBlank() && !uiState.formLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = AdminBlue)
                    ) {
                        Text(if (uiState.formLoading) "Đang xử lý..." else (if (isEditing) "Cập nhật" else "Thêm"))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminToppingsScreen(
    viewModel: com.fastdash.app.viewmodel.AdminToppingViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val visibleToppings = remember(uiState.toppings, uiState.searchQuery) {
        val q = uiState.searchQuery.trim()
        if (q.isBlank()) uiState.toppings else uiState.toppings.filter {
            it.name.contains(q, ignoreCase = true)
        }
    }

    if (uiState.showAddForm) {
        AdminToppingFormDialog(
            viewModel = viewModel,
            uiState = uiState,
            isEditing = uiState.editingId != null,
            onClose = { viewModel.closeForm() }
        )
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBackground)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AdminTopBar(
            title = "Quản lý topping",
            subtitle = "Thêm, sửa, xóa topping và quản lý trạng thái",
            onBack = onBack,
            onLogout = onLogout,
            accentColor = AdminWarning
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                singleLine = true,
                placeholder = { Text("Tìm topping") }
            )
            Button(
                onClick = { viewModel.showAddForm() },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AdminWarning)
            ) {
                Text("+ Thêm", fontWeight = FontWeight.Bold)
            }
        }

        if (uiState.loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AdminWarning)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Tổng topping",
                    value = uiState.toppings.size.toString(),
                    accentColor = AdminWarning
                )
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Hiển thị",
                    value = visibleToppings.size.toString(),
                    accentColor = AdminSuccess
                )
            }

            if (uiState.isError && uiState.message != null) {
                AdminInfoCard(
                    title = "Lỗi tải dữ liệu",
                    subtitle = uiState.message!!,
                    accentColor = AdminWarning
                )
            }

            if (visibleToppings.isEmpty()) {
                AdminInfoCard(
                    title = "Không có topping",
                    subtitle = "Bấm nút 'Thêm' để thêm topping mới.",
                    accentColor = AdminMuted
                )
            } else {
                visibleToppings.forEach { topping ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AdminCard),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = topping.name.orEmpty().ifBlank { "(Không tên)" },
                                        color = AdminText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = CurrencyUtils.formatVnd(topping.price),
                                        color = AdminRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = (if (topping.status == 1) AdminSuccess else AdminMuted).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = if (topping.status == 1) "Bật" else "Tắt",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        color = if (topping.status == 1) AdminSuccess else AdminMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.showEditForm(topping) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Sửa", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        scope.launch { viewModel.toggleToppingStatus(topping.id, topping.status) }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (topping.status == 1) AdminWarning else AdminSuccess
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(if (topping.status == 1) "Tắt" else "Bật", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        scope.launch { viewModel.deleteTopping(topping.id) }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminDanger),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Xóa", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminToppingFormDialog(
    viewModel: com.fastdash.app.viewmodel.AdminToppingViewModel,
    uiState: com.fastdash.app.viewmodel.AdminToppingUiState,
    isEditing: Boolean,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = !uiState.formLoading) { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {}
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCard)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEditing) "Sửa topping" else "Thêm topping",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AdminText,
                    fontWeight = FontWeight.ExtraBold
                )

                OutlinedTextField(
                    value = uiState.nameInput,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text("Tên topping *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.formLoading
                )

                OutlinedTextField(
                    value = uiState.priceInput,
                    onValueChange = viewModel::onPriceChanged,
                    label = { Text("Giá topping *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = !uiState.formLoading
                )

                OutlinedTextField(
                    value = uiState.imageUrlInput,
                    onValueChange = viewModel::onImageUrlChanged,
                    label = { Text("URL ảnh topping (tuỳ chọn)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.formLoading
                )

                if (uiState.formMessage != null) {
                    Surface(
                        color = if (uiState.formError) AdminDanger.copy(alpha = 0.1f) else AdminSuccess.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.formMessage!!,
                            modifier = Modifier.padding(12.dp),
                            color = if (uiState.formError) AdminDanger else AdminSuccess,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.formLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = AdminMuted)
                    ) {
                        Text("Hủy")
                    }

                    Button(
                        onClick = {
                            if (isEditing) viewModel.updateTopping() else viewModel.createTopping()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.nameInput.isNotBlank() && uiState.priceInput.isNotBlank() && !uiState.formLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = AdminWarning)
                    ) {
                        Text(if (uiState.formLoading) "Đang xử lý..." else (if (isEditing) "Cập nhật" else "Thêm"))
                    }
                }
            }
        }
    }
}








