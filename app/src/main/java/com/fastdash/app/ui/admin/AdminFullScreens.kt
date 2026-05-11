package com.fastdash.app.ui.admin

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.fastdash.app.data.remote.api.BranchResponse
import com.fastdash.app.data.remote.api.AdminPaymentResponse
import com.fastdash.app.data.remote.api.CreateBranchRequest
import com.fastdash.app.data.remote.api.UserResponse
import com.fastdash.app.data.repository.AdminBranchRepository
import com.fastdash.app.data.repository.AdminPaymentRepository
import com.fastdash.app.data.repository.AdminUserRepository
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
fun AdminUsersScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { AdminUserRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var users by remember { mutableStateOf<List<UserResponse>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedUserId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        try {
            val response = repository.getUsers()
            users = response.body().orEmpty()
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            loading = false
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
            title = "Quản lý tài khoản",
            subtitle = "Xem, cập nhật trạng thái và phân quyền người dùng",
            onBack = onBack,
            onLogout = onLogout,
            accentColor = AdminRed
        )

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AdminRed)
            }
        } else {
            if (errorMessage != null) {
                AdminInfoCard(
                    title = "Lỗi",
                    subtitle = errorMessage!!,
                    accentColor = AdminWarning
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Tổng người dùng",
                    value = users.size.toString(),
                    accentColor = AdminBlue
                )
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Admin",
                    value = users.count { it.role == "ADMIN" }.toString(),
                    accentColor = AdminRed
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Customer",
                    value = users.count { it.role == "CUSTOMER" }.toString(),
                    accentColor = AdminSuccess
                )
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Active",
                    value = users.count { it.status == 1 }.toString(),
                    accentColor = AdminBlue
                )
            }

            users.forEach { user ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = AdminCard),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.fullName,
                                    color = AdminText,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = user.email,
                                    color = AdminMuted,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            StatusBadge(status = if (user.status == 1) "ACTIVE" else "BLOCKED")
                        }

                        Text(
                            text = "SĐT: ${user.phone} | Role: ${user.role}",
                            color = AdminMuted,
                            fontSize = 11.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { selectedUserId = user.id },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (user.status == 1) AdminWarning else AdminSuccess
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(if (user.status == 1) "Khóa" else "Mở khóa")
                            }
                            if (user.role != "ADMIN") {
                                TextButton(onClick = { }) {
                                    Text("Tân Admin", color = AdminRed)
                                }
                            }
                        }

                        if (selectedUserId == user.id) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            repository.updateUserStatus(user.id, if (user.status == 1) 0 else 1)
                                            Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                                            selectedUserId = null
                                            loading = true
                                            val response = repository.getUsers()
                                            users = response.body().orEmpty()
                                            loading = false
                                        } catch (e: Exception) {
                                            errorMessage = e.message
                                            loading = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AdminDanger),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Xác nhận ${if (user.status == 1) "khóa" else "mở khóa"}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBranchesScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { AdminBranchRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var branches by remember { mutableStateOf<List<BranchResponse>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForm by remember { mutableStateOf(false) }

    var formName by remember { mutableStateOf("") }
    var formAddress by remember { mutableStateOf("") }
    var formPhone by remember { mutableStateOf("") }
    var formOpenTime by remember { mutableStateOf("") }
    var formCloseTime by remember { mutableStateOf("") }

    fun loadBranches() {
        loading = true
        scope.launch {
            try {
                val response = repository.getBranches()
                branches = response.body().orEmpty()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadBranches()
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
            title = "Quản lý chi nhánh",
            subtitle = "Thêm, sửa, xóa chi nhánh",
            onBack = onBack,
            onLogout = onLogout,
            accentColor = AdminBlue
        )

        Button(
            onClick = { showForm = !showForm },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AdminSuccess),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(if (showForm) "Đóng form" else "Thêm chi nhánh mới", fontWeight = FontWeight.Bold)
        }

        if (showForm) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AdminCard),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = formName,
                        onValueChange = { formName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tên chi nhánh") }
                    )
                    OutlinedTextField(
                        value = formAddress,
                        onValueChange = { formAddress = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Địa chỉ") }
                    )
                    OutlinedTextField(
                        value = formPhone,
                        onValueChange = { formPhone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Số điện thoại") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = formOpenTime,
                        onValueChange = { formOpenTime = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Giờ mở (eg. 08:00)") }
                    )
                    OutlinedTextField(
                        value = formCloseTime,
                        onValueChange = { formCloseTime = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Giờ đóng (eg. 22:00)") }
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val request = CreateBranchRequest(
                                        name = formName,
                                        address = formAddress,
                                        phone = formPhone,
                                        openTime = formOpenTime.takeIf { it.isNotBlank() },
                                        closeTime = formCloseTime.takeIf { it.isNotBlank() }
                                    )
                                    repository.createBranch(request)
                                    Toast.makeText(context, "Thêm thành công", Toast.LENGTH_SHORT).show()
                                    formName = ""
                                    formAddress = ""
                                    formPhone = ""
                                    formOpenTime = ""
                                    formCloseTime = ""
                                    showForm = false
                                    loadBranches()
                                } catch (e: Exception) {
                                    errorMessage = e.message
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminSuccess),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Lưu chi nhánh")
                    }
                }
            }
        }

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AdminBlue)
            }
        } else {
            if (errorMessage != null) {
                AdminInfoCard(title = "Lỗi", subtitle = errorMessage!!, accentColor = AdminWarning)
            }

            AdminMetricCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Tổng chi nhánh",
                value = branches.size.toString(),
                accentColor = AdminBlue
            )

            if (branches.isEmpty()) {
                AdminInfoCard(
                    title = "Không có chi nhánh",
                    subtitle = "Thêm chi nhánh mới để bắt đầu",
                    accentColor = AdminMuted
                )
            } else {
                branches.forEach { branch ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AdminCard),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = branch.name,
                                    color = AdminText,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                                StatusBadge(status = if (branch.status == 1) "ACTIVE" else "CLOSED")
                            }
                            Text(text = branch.address, color = AdminMuted, fontSize = 12.sp)
                            Text(text = "SĐT: ${branch.phone}", color = AdminMuted, fontSize = 11.sp)
                            if (!branch.openTime.isNullOrBlank() && !branch.closeTime.isNullOrBlank()) {
                                Text(
                                    text = "Giờ: ${branch.openTime} - ${branch.closeTime}",
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

@Composable
fun AdminPaymentsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { AdminPaymentRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var payments by remember { mutableStateOf<List<AdminPaymentResponse>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedPaymentId by remember { mutableStateOf<Long?>(null) }
    var newStatus by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        loading = true
        try {
            val response = repository.getPayments()
            payments = response.body().orEmpty()
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            loading = false
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
            title = "Quản lý thanh toán",
            subtitle = "Xem và cập nhật trạng thái thanh toán",
            onBack = onBack,
            onLogout = onLogout,
            accentColor = AdminRed
        )

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AdminRed)
            }
        } else {
            if (errorMessage != null) {
                AdminInfoCard(title = "Lỗi", subtitle = errorMessage!!, accentColor = AdminWarning)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Tổng thanh toán",
                    value = payments.size.toString(),
                    accentColor = AdminBlue
                )
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Đã trả",
                    value = payments.count { it.status == "PAID" }.toString(),
                    accentColor = AdminSuccess
                )
            }

            if (payments.isEmpty()) {
                AdminInfoCard(
                    title = "Không có thanh toán",
                    subtitle = "Chưa có giao dịch thanh toán nào",
                    accentColor = AdminMuted
                )
            } else {
                payments.forEach { payment ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AdminCard),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Đơn #${payment.orderId}",
                                        color = AdminText,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = CurrencyUtils.formatVnd(payment.amount),
                                        color = AdminRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                StatusBadge(status = payment.status)
                            }
                            Text(
                                text = "Phương thức: ${payment.method}",
                                color = AdminMuted,
                                fontSize = 11.sp
                            )
                            if (!payment.transactionCode.isNullOrBlank()) {
                                Text(
                                    text = "Mã giao dịch: ${payment.transactionCode}",
                                    color = AdminMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "Ngày: ${payment.createdAt}",
                                color = AdminMuted,
                                fontSize = 10.sp
                            )

                            if (selectedPaymentId == payment.id) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = newStatus,
                                        onValueChange = { newStatus = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        label = { Text("Status") },
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    repository.updatePaymentStatus(payment.id, newStatus)
                                                    Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                                                    selectedPaymentId = null
                                                    newStatus = ""
                                                    loading = true
                                                    val response = repository.getPayments()
                                                    payments = response.body().orEmpty()
                                                    loading = false
                                                } catch (e: Exception) {
                                                    errorMessage = e.message
                                                    loading = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AdminSuccess),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("OK")
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { selectedPaymentId = payment.id },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminBlue),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Cập nhật trạng thái")
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
        "ACTIVE", "COMPLETED", "PAID" -> AdminSuccess
        "PENDING", "UNPAID", "BLOCKED" -> AdminWarning
        "DELIVERING" -> AdminBlue
        "CANCELLED", "FAILED" -> AdminDanger
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








