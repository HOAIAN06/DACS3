package com.fastdash.app.ui.admin

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.fastdash.app.data.model.response.CategoryResponse
import com.fastdash.app.utils.CurrencyUtils
import com.fastdash.app.utils.ImageUtils
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// Unified Admin Palette
private val AdminRed = Color(0xFFC8102E)
private val AdminBg = Color(0xFFF9FAFB)
private val AdminSurface = Color.White
private val AdminTextMain = Color(0xFF111827)
private val AdminTextMuted = Color(0xFF6B7280)
private val AdminBorder = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
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
            it.name?.contains(q, ignoreCase = true) == true
        }
    }

    if (uiState.showAddForm) {
        CategoryFormDialog(viewModel, uiState) { viewModel.closeForm() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý danh mục", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = { IconButton(onClick = { /* Refresh logic if any */ }) { Icon(Icons.Outlined.Refresh, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBg)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddForm() },
                containerColor = AdminRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("THÊM DANH MỤC", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = AdminBg
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                placeholder = { Text("Tìm kiếm danh mục...") },
                leadingIcon = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(20.dp), tint = AdminTextMuted) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AdminRed,
                    unfocusedBorderColor = AdminBorder,
                    unfocusedContainerColor = AdminSurface,
                    focusedContainerColor = AdminSurface
                ),
                singleLine = true
            )

            if (uiState.loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AdminRed)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(visibleCategories) { cat ->
                    CategoryManagementItem(
                        cat = cat,
                        onEdit = { viewModel.showEditForm(cat) },
                        onToggle = { viewModel.toggleCategoryStatus(cat.id, cat.status) },
                        onDelete = { viewModel.deleteCategory(cat.id) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun CategoryManagementItem(
    cat: com.fastdash.app.data.remote.api.AdminCategoryResponse,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AdminSurface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, AdminBorder)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(AdminBg)) {
                if (!cat.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageUtils.buildImageRequest(LocalContext.current, cat.imageUrl),
                        contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cat.name.orEmpty(), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AdminTextMain)
                Text(cat.description ?: "Không có mô tả", fontSize = 12.sp, color = AdminTextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(20.dp)) }
            IconButton(onClick = onToggle) { Icon(if (cat.status == 1) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, null, modifier = Modifier.size(20.dp), tint = if (cat.status == 1) Color(0xFF3B82F6) else AdminTextMuted) }
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(20.dp), tint = Color(0xFFEF4444).copy(alpha = 0.7f)) }
        }
    }
}

@Composable
private fun CategoryFormDialog(
    viewModel: com.fastdash.app.viewmodel.AdminCategoryViewModel,
    uiState: com.fastdash.app.viewmodel.AdminCategoryUiState,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onImageSelected(it, uriToMultipart(context, it)) }
    }

    Dialog(onDismissRequest = onClose) {
        Surface(shape = RoundedCornerShape(28.dp), color = AdminSurface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(if (uiState.editingId != null) "Cập nhật danh mục" else "Thêm danh mục mới", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                
                Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(20.dp)).background(AdminBg).clickable { imagePicker.launch("image/*") }) {
                    if (uiState.imagePreview.isNotBlank()) {
                        AsyncImage(model = if (uiState.imagePreview.startsWith("content://")) uiState.imagePreview else ImageUtils.buildImageRequest(context, uiState.imagePreview), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.CloudUpload, null, tint = AdminRed, modifier = Modifier.size(32.dp))
                            Text("Tải ảnh danh mục", fontWeight = FontWeight.Bold, color = AdminRed, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(value = uiState.nameInput, onValueChange = viewModel::onNameChanged, label = { Text("Tên danh mục") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                OutlinedTextField(value = uiState.descriptionInput, onValueChange = viewModel::onDescriptionChanged, label = { Text("Mô tả") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(14.dp)) { Text("HỦY") }
                    Button(onClick = { if (uiState.editingId != null) viewModel.updateCategory() else viewModel.createCategory() }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = AdminRed)) {
                        if (uiState.formLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        else Text("LƯU", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminToppingsScreen(
    viewModel: com.fastdash.app.viewmodel.AdminToppingViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val visibleToppings = remember(uiState.toppings, uiState.searchQuery) {
        val q = uiState.searchQuery.trim()
        if (q.isBlank()) uiState.toppings else uiState.toppings.filter { it.name?.contains(q, ignoreCase = true) == true }
    }

    if (uiState.showAddForm) {
        ToppingFormDialog(viewModel, uiState) { viewModel.closeForm() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Topping", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBg)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddForm() },
                containerColor = AdminRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("THÊM TOPPING", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = AdminBg
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                placeholder = { Text("Tìm kiếm topping...") },
                leadingIcon = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(20.dp), tint = AdminTextMuted) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminRed, unfocusedBorderColor = AdminBorder, unfocusedContainerColor = AdminSurface, focusedContainerColor = AdminSurface),
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(visibleToppings) { topping ->
                    ToppingManagementItem(
                        topping = topping,
                        onEdit = { viewModel.showEditForm(topping) },
                        onToggle = { viewModel.toggleToppingStatus(topping.id, topping.status) },
                        onDelete = { viewModel.deleteTopping(topping.id) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun ToppingManagementItem(
    topping: com.fastdash.app.data.remote.api.AdminToppingResponse,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AdminSurface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, AdminBorder)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)).background(AdminBg)) {
                if (!topping.imageUrl.isNullOrBlank()) {
                    AsyncImage(model = ImageUtils.buildImageRequest(LocalContext.current, topping.imageUrl), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Outlined.AddCircleOutline, null, modifier = Modifier.align(Alignment.Center).size(20.dp), tint = AdminTextMuted)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(topping.name.orEmpty(), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AdminTextMain)
                Text(CurrencyUtils.formatVnd(topping.price), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = AdminRed)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onToggle) { Icon(if (topping.status == 1) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, null, modifier = Modifier.size(18.dp), tint = if (topping.status == 1) Color(0xFF3B82F6) else AdminTextMuted) }
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(18.dp), tint = Color(0xFFEF4444).copy(alpha = 0.7f)) }
        }
    }
}

@Composable
private fun ToppingFormDialog(
    viewModel: com.fastdash.app.viewmodel.AdminToppingViewModel,
    uiState: com.fastdash.app.viewmodel.AdminToppingUiState,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onImageSelected(it, uriToMultipart(context, it)) }
    }

    Dialog(onDismissRequest = onClose) {
        Surface(shape = RoundedCornerShape(28.dp), color = AdminSurface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(if (uiState.editingId != null) "Sửa topping" else "Thêm topping mới", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                
                Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(20.dp)).background(AdminBg).clickable { imagePicker.launch("image/*") }) {
                    if (uiState.imagePreview.isNotBlank()) {
                        AsyncImage(model = if (uiState.imagePreview.startsWith("content://")) uiState.imagePreview else ImageUtils.buildImageRequest(context, uiState.imagePreview), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.CloudUpload, null, tint = AdminRed, modifier = Modifier.size(32.dp))
                            Text("Chọn ảnh topping", fontWeight = FontWeight.Bold, color = AdminRed, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(value = uiState.nameInput, onValueChange = viewModel::onNameChanged, label = { Text("Tên topping") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                OutlinedTextField(value = uiState.priceInput, onValueChange = viewModel::onPriceChanged, label = { Text("Giá topping") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(14.dp)) { Text("HỦY") }
                    Button(onClick = { if (uiState.editingId != null) viewModel.updateTopping() else viewModel.createTopping() }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = AdminRed)) {
                        if (uiState.formLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        else Text("LƯU", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Admin Shell/Coming Soon Placeholders
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminComingSoonScreen(title: String, subtitle: String, onBack: () -> Unit, onLogout: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(title, fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) },
        containerColor = AdminBg
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Outlined.Handyman, null, modifier = Modifier.size(80.dp), tint = AdminRed.copy(alpha = 0.5f))
            Spacer(Modifier.height(24.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(subtitle, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = AdminTextMuted, fontSize = 15.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        SizeFormDialog(viewModel, uiState) { viewModel.closeForm() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Kích thước", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBg)
            )
        },
        containerColor = AdminBg
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Product ID Search
            Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = uiState.productIdInput,
                    onValueChange = viewModel::onProductIdChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nhập ID Sản phẩm") },
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminRed, unfocusedBorderColor = AdminBorder, unfocusedContainerColor = AdminSurface, focusedContainerColor = AdminSurface)
                )
                Button(
                    onClick = {
                        val pid = uiState.productIdInput.toLongOrNull()
                        if (pid != null) viewModel.loadSizesByProduct(pid)
                    },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminRed)
                ) { Text("Tìm") }
            }

            if (uiState.loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AdminRed)

            if (uiState.selectedProductId > 0L) {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    item {
                        Button(onClick = { viewModel.showAddForm() }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("THÊM SIZE MỚI", fontWeight = FontWeight.Bold)
                        }
                    }
                    items(uiState.sizes) { size ->
                        SizeManagementItem(
                            size = size,
                            onEdit = { viewModel.showEditForm(size) },
                            onToggle = { scope.launch { viewModel.toggleSizeStatus(size.id, size.status) } },
                            onDelete = { scope.launch { viewModel.deleteSize(size.id) } }
                        )
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Vui lòng nhập ID sản phẩm để xem danh sách size", color = AdminTextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun SizeManagementItem(
    size: com.fastdash.app.data.remote.api.AdminSizeResponse,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AdminSurface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, AdminBorder)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(size.sizeName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AdminTextMain)
                Text("Giá thêm: ${CurrencyUtils.formatVnd(size.price.toDouble())}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = AdminRed)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onToggle) { Icon(if (size.status == 1) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, null, modifier = Modifier.size(18.dp), tint = if (size.status == 1) Color(0xFF3B82F6) else AdminTextMuted) }
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(18.dp), tint = Color(0xFFEF4444).copy(alpha = 0.7f)) }
        }
    }
}

@Composable
private fun SizeFormDialog(
    viewModel: com.fastdash.app.viewmodel.AdminSizeViewModel,
    uiState: com.fastdash.app.viewmodel.AdminSizeUiState,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        Surface(shape = RoundedCornerShape(28.dp), color = AdminSurface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(if (uiState.editingId != null) "Sửa kích thước" else "Thêm kích thước mới", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                OutlinedTextField(value = uiState.sizeNameInput, onValueChange = viewModel::onSizeNameChanged, label = { Text("Tên size (S, M, L)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                OutlinedTextField(value = uiState.priceInput, onValueChange = viewModel::onPriceChanged, label = { Text("Giá thêm") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(14.dp)) { Text("HỦY") }
                    Button(onClick = { if (uiState.editingId != null) viewModel.updateSize() else viewModel.createSize() }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = AdminRed)) {
                        if (uiState.formLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp)) else Text("LƯU", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun uriToMultipart(context: Context, uri: Uri): MultipartBody.Part {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    val mimeType = context.contentResolver.getType(uri) ?: "image/*"
    val fileName = context.resolveFileName(uri)
    val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("image", fileName, body)
}
private fun Context.resolveFileName(uri: Uri): String {
    val contentName = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
        ?.trim()
        .orEmpty()
    if (contentName.isNotBlank()) return contentName
    val lastPath = uri.lastPathSegment?.substringAfterLast('/')?.trim().orEmpty()
    if (lastPath.contains('.')) return lastPath
    val extension = when (contentResolver.getType(uri)) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/jpeg", "image/jpg" -> "jpg"
        else -> "jpg"
    }
    return "image.$extension"
}

