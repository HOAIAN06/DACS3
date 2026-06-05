package com.fastdash.app.ui.admin

import androidx.compose.foundation.gestures.detectTapGestures
import android.location.Address
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fastdash.app.data.model.response.BranchResponse
import com.fastdash.app.data.remote.api.AdminPaymentResponse
import com.fastdash.app.data.remote.api.UserResponse
import com.fastdash.app.data.repository.AdminPaymentRepository
import com.fastdash.app.data.repository.AdminUserRepository
import com.fastdash.app.utils.CurrencyUtils
import com.fastdash.app.viewmodel.AdminBranchUiState
import com.fastdash.app.viewmodel.AdminBranchViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// Unified Admin Palette
private val AdminRed = Color(0xFFC8102E)
private val AdminBg = Color(0xFFF9FAFB)
private val AdminSurface = Color.White
private val AdminTextMain = Color(0xFF111827)
private val AdminTextMuted = Color(0xFF6B7280)
private val AdminBorder = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(onBack: () -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { AdminUserRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var users by remember { mutableStateOf<List<UserResponse>>(emptyList()) }

    fun loadUsers() {
        loading = true
        scope.launch {
            try {
                val resp = repository.getUsers()
                if (resp.isSuccessful) users = resp.body().orEmpty()
            } catch (_: Exception) { } finally { loading = false }
        }
    }

    LaunchedEffect(Unit) { loadUsers() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quáº£n lÃ½ ngÆ°á»i dÃ¹ng", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBg)
            )
        },
        containerColor = AdminBg
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AdminRed)

            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(users) { user ->
                    UserManagementItem(user) {
                        scope.launch {
                            val ok = repository.updateUserStatus(user.id, if (user.status == 1) 0 else 1).isSuccessful
                            if (ok) { 
                                Toast.makeText(context, "Cáº­p nháº­t thÃ nh cÃ´ng", Toast.LENGTH_SHORT).show()
                                loadUsers() 
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserManagementItem(user: UserResponse, onToggleStatus: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AdminSurface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, AdminBorder)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(AdminRed.copy(alpha = 0.05f), CircleShape), contentAlignment = Alignment.Center) {
                Text(user.fullName.take(1).uppercase(), color = AdminRed, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AdminTextMain)
                Text(user.email, fontSize = 12.sp, color = AdminTextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Role: ${user.role}", fontSize = 11.sp, color = AdminRed, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(if (user.status == 1) "ACTIVE" else "BLOCKED")
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onToggleStatus, contentPadding = PaddingValues(0.dp)) {
                    Text(if (user.status == 1) "KhÃ³a" else "Má»Ÿ khÃ³a", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (user.status == 1) Color(0xFFEF4444) else Color(0xFF10B981))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBranchesScreen(
    viewModel: AdminBranchViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val branchPendingDelete = uiState.branches.firstOrNull { it.id == uiState.deletingBranchId }

    if (uiState.showForm) {
        AdminBranchFormScreen(
            uiState = uiState,
            onDismiss = viewModel::closeForm,
            onNameChanged = viewModel::onNameChanged,
            onAddressChanged = viewModel::onAddressChanged,
            onPhoneChanged = viewModel::onPhoneChanged,
            onOpenTimeChanged = viewModel::onOpenTimeChanged,
            onCloseTimeChanged = viewModel::onCloseTimeChanged,
            onLatitudeChanged = viewModel::onLatitudeChanged,
            onLongitudeChanged = viewModel::onLongitudeChanged,
            onActiveChanged = viewModel::onActiveChanged,
            onCoordinatesPicked = viewModel::setCoordinates,
            onSubmit = viewModel::submitForm
        )
        return
    }

    if (branchPendingDelete != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            shape = RoundedCornerShape(24.dp),
            containerColor = AdminSurface,
            title = { Text("Xóa chi nhánh", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn chắc chắn muốn xóa ${branchPendingDelete.name}? Thao tác này không thể hoàn tác.") },
            confirmButton = {
                Button(
                    onClick = viewModel::deleteBranch,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Xóa vĩnh viễn", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) { Text("Hủy", color = AdminTextMuted) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Quản lý chi nhánh", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text("${uiState.branches.size} chi nhánh hiện có", fontSize = 12.sp, color = AdminTextMuted, fontWeight = FontWeight.Medium)
                    }
                },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AdminTextMain) 
                    } 
                },
                actions = {
                    IconButton(
                        onClick = viewModel::loadBranches,
                        modifier = Modifier.background(AdminSurface, CircleShape).border(1.dp, AdminBorder, CircleShape)
                    ) {
                        Icon(Icons.Outlined.Refresh, null, tint = AdminTextMain, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBg)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::showAddForm,
                containerColor = AdminRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(12.dp))
                Text("THÊM CHI NHÁNH MỚI", fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
            }
        },
        containerColor = AdminBg
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AdminRed, trackColor = AdminRed.copy(alpha = 0.1f))
            }
            uiState.message?.let { message ->
                Surface(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = (if (uiState.isError) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, if (uiState.isError) Color(0xFFFEE2E2) else Color(0xFFDCFCE7))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (uiState.isError) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircle,
                            null,
                            tint = if (uiState.isError) Color(0xFFEF4444) else Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(message, fontSize = 13.sp, color = AdminTextMain, modifier = Modifier.weight(1f))
                        IconButton(onClick = viewModel::dismissMessage, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = AdminTextMuted)
                        }
                    }
                }
            }
            
            if (!uiState.loading && uiState.branches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Storefront, null, modifier = Modifier.size(64.dp), tint = AdminTextMuted.copy(alpha = 0.3f))
                        Spacer(Modifier.height(16.dp))
                        Text("Chưa có chi nhánh nào", fontWeight = FontWeight.Bold, color = AdminTextMuted)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.branches, key = { it.id }) { branch ->
                    BranchManagementItem(
                        branch = branch,
                        isToggling = uiState.togglingBranchId == branch.id,
                        onEdit = { viewModel.showEditForm(branch) },
                        onToggle = { viewModel.toggleBranchStatus(branch) },
                        onDelete = { viewModel.requestDelete(branch.id) }
                    )
                }
            }
        }
    }
}


@Composable
private fun BranchManagementItem(
    branch: BranchResponse,
    isToggling: Boolean,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AdminSurface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, AdminBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        branch.name,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = AdminTextMain,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Place, null, tint = AdminRed, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(branch.address, fontSize = 13.sp, color = AdminTextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                StatusBadge(if (branch.status == 1) "ACTIVE" else "INACTIVE")
            }

            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().background(AdminBg, RoundedCornerShape(12.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Phone, null, tint = AdminTextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(branch.phone, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminTextMain)
                }
                if (!branch.openTime.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, null, tint = AdminTextMuted, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("${branch.openTime.toDisplayTime()} - ${branch.closeTime.toDisplayTime()}", fontSize = 12.sp, color = AdminTextMain)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = AdminTextMain),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sửa", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onToggle,
                    modifier = Modifier.weight(1.2f),
                    enabled = !isToggling,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (branch.status == 1) AdminRed.copy(alpha = 0.08f) else Color(0xFF10B981).copy(alpha = 0.08f),
                        contentColor = if (branch.status == 1) AdminRed else Color(0xFF10B981)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (isToggling) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AdminRed)
                    } else {
                        Icon(
                            if (branch.status == 1) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (branch.status == 1) "Tạm ngưng" else "Mở lại", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(44.dp).background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Outlined.DeleteOutline, null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminBranchFormScreen(
    uiState: AdminBranchUiState,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onOpenTimeChanged: (String) -> Unit,
    onCloseTimeChanged: (String) -> Unit,
    onLatitudeChanged: (String) -> Unit,
    onLongitudeChanged: (String) -> Unit,
    onActiveChanged: (Boolean) -> Unit,
    onCoordinatesPicked: (Double, Double) -> Unit,
    onSubmit: () -> Unit
) {
    var showMapPicker by remember { mutableStateOf(false) }

    if (showMapPicker) {
        BranchLocationPickerDialog(
            initialLatitude = uiState.latitudeInput.toDoubleOrNull(),
            initialLongitude = uiState.longitudeInput.toDoubleOrNull(),
            onDismiss = { showMapPicker = false },
            onConfirm = { latitude, longitude ->
                onCoordinatesPicked(latitude, longitude)
                showMapPicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (uiState.editingBranchId == null) "Thêm chi nhánh" else "Cập nhật chi nhánh",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AdminTextMain)
                    }
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (uiState.formMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (uiState.formError) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, if (uiState.formError) Color(0xFFFEE2E2) else Color(0xFFDCFCE7))
                ) {
                    Text(
                        text = uiState.formMessage,
                        modifier = Modifier.padding(16.dp),
                        color = if (uiState.formError) Color(0xFFEF4444) else Color(0xFF10B981),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = AdminSurface,
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Thông tin cơ bản", fontWeight = FontWeight.Bold, color = AdminTextMain, fontSize = 14.sp)
                    FormTextField(value = uiState.nameInput, onValueChange = onNameChanged, label = "Tên chi nhánh", icon = Icons.Outlined.Storefront)
                    FormTextField(value = uiState.addressInput, onValueChange = onAddressChanged, label = "Địa chỉ", icon = Icons.Outlined.Place, singleLine = false)
                    FormTextField(value = uiState.phoneInput, onValueChange = onPhoneChanged, label = "Số điện thoại", icon = Icons.Outlined.Phone, keyboardType = KeyboardType.Phone)
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = AdminSurface,
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Thời gian hoạt động", fontWeight = FontWeight.Bold, color = AdminTextMain, fontSize = 14.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BranchTimeField(value = uiState.openTimeInput, label = "Mở cửa", onValueChange = onOpenTimeChanged, modifier = Modifier.weight(1f))
                        BranchTimeField(value = uiState.closeTimeInput, label = "Đóng cửa", onValueChange = onCloseTimeChanged, modifier = Modifier.weight(1f))
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = AdminSurface,
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Vị trí GPS", fontWeight = FontWeight.Bold, color = AdminTextMain, fontSize = 14.sp)
                        Button(
                            onClick = { showMapPicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminRed.copy(alpha = 0.08f), contentColor = AdminRed),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Outlined.Map, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Bản đồ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AdminBg, RoundedCornerShape(16.dp))
                            .border(1.dp, AdminBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        if (uiState.latitudeInput.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.MyLocation, null, tint = AdminRed, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "${uiState.latitudeInput}, ${uiState.longitudeInput}",
                                    fontSize = 14.sp,
                                    color = AdminTextMain,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Text("Chưa xác định tọa độ", fontSize = 13.sp, color = AdminTextMuted)
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = AdminSurface,
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Trạng thái", fontWeight = FontWeight.Bold, color = AdminTextMain, fontSize = 14.sp)
                        Text(if (uiState.isActiveInput) "Đang hoạt động" else "Tạm ngưng", fontSize = 12.sp, color = AdminTextMuted)
                    }
                    Switch(
                        checked = uiState.isActiveInput,
                        onCheckedChange = onActiveChanged,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AdminRed)
                    )
                }
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.formLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AdminRed)
            ) {
                if (uiState.formLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 3.dp)
                } else {
                    Text("LƯU THÔNG TIN CHI NHÁNH", fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                }
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}


@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(20.dp), tint = AdminTextMuted) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AdminRed,
            unfocusedBorderColor = AdminBorder,
            focusedLabelColor = AdminRed
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BranchTimeField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val displayValue = remember(value) { value.toDisplayTime() }

    if (showPicker) {
        val initialHour = value.substringBefore(':').toIntOrNull() ?: 8
        val initialMinute = value.substringAfter(':').substringBefore(':').toIntOrNull() ?: 0
        val pickerState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)
        
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(String.format(Locale.US, "%02d:%02d:00", pickerState.hour, pickerState.minute))
                    showPicker = false
                }) { Text("Xác nhận", fontWeight = FontWeight.Bold, color = AdminRed) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Hủy", color = AdminTextMuted) }
            },
            title = { Text("Chọn giờ $label", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = pickerState) },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(20.dp), tint = AdminTextMuted) },
            trailingIcon = {
                if (value.isNotBlank()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = AdminTextMuted)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AdminRed,
                unfocusedBorderColor = AdminBorder,
                focusedLabelColor = AdminRed,
                cursorColor = Color.Transparent
            )
        )
        // Overlay to catch clicks reliably
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 8.dp) // Avoid clicking the label area if needed
                .clickable { showPicker = true }
        )
    }
}


private fun String?.toDisplayTime(): String {
    val trimmed = this?.trim() ?: return ""
    if (trimmed.isBlank()) return ""
    val parts = trimmed.substringBefore('.').split(":")
    if (parts.size < 2) return trimmed
    val hour = parts[0].toIntOrNull() ?: return trimmed
    val minute = parts[1].toIntOrNull() ?: return trimmed
    return String.format(Locale.US, "%02d:%02d", hour, minute)
}

private fun String.toApiTime(): String {
    val trimmed = trim()
    if (trimmed.isBlank()) return ""
    val parts = trimmed.substringBefore('.').split(":")
    if (parts.size < 2) return trimmed
    val hour = parts[0].toIntOrNull() ?: return trimmed
    val minute = parts[1].toIntOrNull() ?: return trimmed
    val second = parts.getOrNull(2)?.toIntOrNull() ?: 0
    return String.format(Locale.US, "%02d:%02d:%02d", hour, minute, second)
}

@Composable
private fun BranchLocationPickerDialog(
    initialLatitude: Double?,
    initialLongitude: Double?,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val startPoint = remember(initialLatitude, initialLongitude) {
        LatLng(initialLatitude ?: 16.0471, initialLongitude ?: 108.2068)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPoint, 15f)
    }
    var selectedPoint by remember(startPoint) { mutableStateOf(startPoint) }
    var resolvedAddress by remember { mutableStateOf("") }
    var resolvingAddress by remember { mutableStateOf(false) }

    suspend fun resolve(point: LatLng) {
        resolvingAddress = true
        resolvedAddress = reverseGeocodeBranch(context, point.latitude, point.longitude)
        resolvingAddress = false
    }

    LaunchedEffect(startPoint.latitude, startPoint.longitude) {
        resolve(startPoint)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = AdminSurface) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Chọn vị trí chi nhánh", modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = false),
                        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, compassEnabled = true),
                        onMapClick = { point ->
                            selectedPoint = point
                            scope.launch { resolve(point) }
                        }
                    ) {
                        Marker(state = MarkerState(position = selectedPoint), title = "Chi nhánh")
                    }
                    if (resolvingAddress) {
                        Surface(modifier = Modifier.align(Alignment.TopCenter).padding(12.dp), shape = RoundedCornerShape(999.dp), color = Color.White.copy(alpha = 0.96f)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Text("Đang lấy địa chỉ...", fontSize = 12.sp)
                            }
                        }
                    }
                }
                Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(resolvedAddress.ifBlank { "Chưa lấy được địa chỉ gần đúng" }, fontSize = 13.sp, color = AdminTextMain)
                    Text(String.format(Locale.US, "%.6f, %.6f", selectedPoint.latitude, selectedPoint.longitude), fontSize = 12.sp, color = AdminTextMuted)
                }
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Hủy") }
                    Button(onClick = { onConfirm(selectedPoint.latitude, selectedPoint.longitude) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = AdminRed)) { Text("Xác nhận") }
                }
            }
        }
    }
}

private suspend fun reverseGeocodeBranch(
    context: android.content.Context,
    latitude: Double,
    longitude: Double
): String = withContext(Dispatchers.IO) {
    try {
        val geocoder = Geocoder(context, Locale("vi", "VN"))
        val address = runCatching {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latitude, longitude, 1)
        }.getOrNull()?.firstOrNull()
        address?.toBranchReadableAddress().orEmpty().ifBlank {
            String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
        }
    } catch (_: Exception) {
        String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
    }
}

private fun Address.toBranchReadableAddress(): String {
    val parts = listOf(subThoroughfare, thoroughfare, subLocality, locality, adminArea)
        .mapNotNull { it?.trim() }
        .filter { it.isNotBlank() }
        .distinct()
    return if (parts.isNotEmpty()) parts.joinToString(", ") else getAddressLine(0).orEmpty()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPaymentsScreen(onBack: () -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { AdminPaymentRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var payments by remember { mutableStateOf<List<AdminPaymentResponse>>(emptyList()) }

    fun load() {
        loading = true
        scope.launch {
            try {
                val resp = repository.getPayments()
                if (resp.isSuccessful) payments = resp.body().orEmpty()
            } catch (_: Exception) {} finally { loading = false }
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quáº£n lÃ½ thanh toÃ¡n", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBg)
            )
        },
        containerColor = AdminBg
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AdminRed)
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(payments) { payment ->
                    PaymentManagementItem(payment)
                }
            }
        }
    }
}

@Composable
private fun PaymentManagementItem(payment: AdminPaymentResponse) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AdminSurface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, AdminBorder)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("ÄÆ¡n hÃ ng #${payment.orderId}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = AdminTextMain)
                    Text(payment.createdAt ?: "--", fontSize = 11.sp, color = AdminTextMuted)
                }
                StatusBadge(payment.status)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(32.dp).background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Payments, null, tint = AdminTextMain, modifier = Modifier.size(16.dp))
                    }
                    Text(payment.method ?: "COD", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Text(CurrencyUtils.formatVnd(payment.amount), color = AdminRed, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
            if (!payment.transactionCode.isNullOrBlank()) {
                Text("MÃ£ giao dá»‹ch: ${payment.transactionCode}", fontSize = 11.sp, color = AdminTextMuted, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(AdminBg).padding(4.dp))
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String?) {
    val normalized = status?.trim()?.uppercase() ?: "UNKNOWN"
    val (color, bgColor) = when (normalized) {
        "ACTIVE", "PAID", "COMPLETED", "SUCCESS" -> Color(0xFF10B981) to Color(0xFFD1FAE5)
        "PENDING", "UNPAID", "WAITING" -> Color(0xFFF59E0B) to Color(0xFFFEF3C7)
        "BLOCKED", "FAILED", "CANCELLED", "CLOSED", "INACTIVE" -> Color(0xFFEF4444) to Color(0xFFFEE2E2)
        else -> AdminTextMuted to AdminBg
    }
    Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = normalized,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}


