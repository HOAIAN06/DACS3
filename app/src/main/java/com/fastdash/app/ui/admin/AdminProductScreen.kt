package com.fastdash.app.ui.admin

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fastdash.app.data.remote.api.AdminProductResponse
import com.fastdash.app.data.repository.AdminProductRepository
import com.fastdash.app.data.repository.ProductRepository
import com.fastdash.app.viewmodel.AdminProductViewModel
import com.fastdash.app.viewmodel.AdminProductUiState
import com.fastdash.app.utils.CurrencyUtils
import com.fastdash.app.utils.ImageUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.launch

// Unified Admin Palette
private val AdminRed = Color(0xFFC8102E)
private val AdminBg = Color(0xFFF9FAFB)
private val AdminSurface = Color.White
private val AdminTextMain = Color(0xFF111827)
private val AdminTextMuted = Color(0xFF6B7280)
private val AdminBorder = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductScreen(
    viewModel: AdminProductViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val repositoryProduct = remember { AdminProductRepository(context.applicationContext) }
    val publicProductRepository = remember { ProductRepository(context.applicationContext) }
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var products by remember { mutableStateOf<List<AdminProductResponse>>(emptyList()) }
    var productLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    fun loadProducts() {
        productLoading = true
        scope.launch {
            try {
                val response = repositoryProduct.getProducts()
                if (response.isSuccessful) products = response.body().orEmpty()
            } catch (e: Exception) { products = emptyList() } finally { productLoading = false }
        }
    }

    LaunchedEffect(Unit) { loadProducts() }
    
    val filteredProducts = products.filter { it.name.orEmpty().contains(searchQuery, ignoreCase = true) }

    if (uiState.showAddForm) {
        ProductWizardSheet(
            uiState = uiState,
            viewModel = viewModel,
            onClose = { viewModel.setShowAddForm(false) },
            onSuccess = { loadProducts() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý thực đơn", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = { IconButton(onClick = { loadProducts() }) { Icon(Icons.Outlined.Refresh, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBg)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.startAddForm() },
                containerColor = AdminRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("THÊM MÓN", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = AdminBg
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Stats & Search integrated
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProductMetricTile("Tổng số món", products.size.toString(), Modifier.weight(1f))
                    ProductMetricTile("Đang kinh doanh", products.count { it.status == 1 }.toString(), Modifier.weight(1f), Color(0xFF10B981))
                }
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tìm tên món ăn...") },
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
            }

            if (productLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AdminRed)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!productLoading && filteredProducts.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("Không tìm thấy món nào", color = AdminTextMuted, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                items(filteredProducts) { product ->
                    ProductManagementItem(
                        product = product,
                        onEdit = { viewModel.showEditForm(product) },
                        onToggleStatus = { viewModel.toggleProductStatus(product.id, product.status) { loadProducts() } },
                        onDelete = {
                            scope.launch {
                                if (repositoryProduct.deleteProduct(product.id).isSuccessful) {
                                    Toast.makeText(context, "Đã xóa món", Toast.LENGTH_SHORT).show()
                                    loadProducts()
                                }
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun ProductMetricTile(label: String, value: String, modifier: Modifier, color: Color = AdminRed) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, AdminBorder), color = AdminSurface) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminTextMuted)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
private fun ProductManagementItem(
    product: AdminProductResponse,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AdminSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(74.dp).clip(RoundedCornerShape(14.dp)).background(AdminBg)) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageUtils.buildImageRequest(LocalContext.current, product.imageUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Outlined.Image, null, modifier = Modifier.align(Alignment.Center).size(24.dp), tint = AdminBorder)
                }
                if (product.status == 0) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                        Text("ĐÃ ẨN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name.orEmpty(), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AdminTextMain, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(CurrencyUtils.formatVnd(product.basePrice), color = AdminRed, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text(if (product.isCustomizable == 1) "Có cấu hình" else "Đơn giản", fontSize = 11.sp, color = AdminTextMuted)
            }
            
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(20.dp), tint = AdminTextMain) }
                IconButton(onClick = onToggleStatus) { Icon(if (product.status == 1) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, null, modifier = Modifier.size(20.dp), tint = if (product.status == 1) Color(0xFF3B82F6) else AdminTextMuted) }
                IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(20.dp), tint = Color(0xFFEF4444).copy(alpha = 0.7f)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductWizardSheet(
    uiState: AdminProductUiState,
    viewModel: AdminProductViewModel,
    onClose: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onImageSelected(it, uriToMultipart(context, it)) }
    }

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = AdminSurface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp)) {
            // Wizard Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when(uiState.currentStep) {
                        1 -> "1. Thông tin cơ bản"
                        2 -> "2. Chọn danh mục"
                        3 -> "3. Thiết lập giá & cấu hình"
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = AdminRed
                )
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
            }

            // Step Indicator
            Row(modifier = Modifier.fillMaxWidth().height(4.dp).padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    Box(Modifier.weight(1f).fillMaxHeight().clip(CircleShape).background(if (uiState.currentStep > i) AdminRed else AdminBorder))
                }
            }

            Box(modifier = Modifier.weight(1f).padding(top = 16.dp)) {
                when (uiState.currentStep) {
                    1 -> WizardBasicStep(uiState, viewModel) { imageLauncher.launch("image/*") }
                    2 -> WizardCategoryStep(uiState, viewModel)
                    3 -> WizardConfigStep(uiState, viewModel)
                }
            }

            // Footer Actions
            Row(modifier = Modifier.padding(vertical = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uiState.currentStep > 1) {
                    OutlinedButton(
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AdminBorder)
                    ) { Text("QUAY LẠI", color = AdminTextMain) }
                }

                Button(
                    onClick = {
                        if (uiState.currentStep < 3) viewModel.nextStep()
                        else scope.launch {
                            val ok = if (uiState.editingProductId != null) viewModel.updateProductFixed() else viewModel.createProduct()
                            if (ok) { onSuccess(); onClose() }
                        }
                    },
                    enabled = if (uiState.currentStep < 3) uiState.canGoNext else uiState.canSubmit,
                    modifier = Modifier.weight(2f).height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminRed)
                ) {
                    if (uiState.loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    else Text(if (uiState.currentStep < 3) "TIẾP THEO" else "HOÀN TẤT & LƯU", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun WizardBasicStep(uiState: AdminProductUiState, viewModel: AdminProductViewModel, onPick: () -> Unit) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp)).background(AdminBg).clickable { onPick() }) {
            if (uiState.imagePreview.isNotEmpty()) {
                AsyncImage(
                    model = if (uiState.imagePreview.startsWith("content://")) uiState.imagePreview else ImageUtils.buildImageRequest(LocalContext.current, uiState.imagePreview),
                    contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.CloudUpload, null, tint = AdminRed, modifier = Modifier.size(40.dp))
                    Text("Tải ảnh món ăn", fontWeight = FontWeight.Bold, color = AdminRed, fontSize = 13.sp)
                }
            }
        }
        
        WizardField(uiState.name, "Tên món ăn", viewModel::onNameChanged)
        WizardField(uiState.description, "Mô tả món ăn", viewModel::onDescriptionChanged, singleLine = false, minLines = 3)
    }
}

@Composable
private fun WizardCategoryStep(uiState: AdminProductUiState, viewModel: AdminProductViewModel) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        uiState.categories.forEach { cat ->
            val sel = uiState.categoryIdInput == cat.id.toString()
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.onCategoryIdChanged(cat.id.toString()) },
                shape = RoundedCornerShape(16.dp),
                color = if (sel) AdminRed.copy(alpha = 0.05f) else AdminSurface,
                border = BorderStroke(1.dp, if (sel) AdminRed else AdminBorder)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = sel, onClick = { viewModel.onCategoryIdChanged(cat.id.toString()) }, colors = RadioButtonDefaults.colors(selectedColor = AdminRed))
                    Spacer(Modifier.width(12.dp))
                    Text(cat.name.orEmpty(), fontWeight = FontWeight.Bold, color = if (sel) AdminRed else AdminTextMain)
                }
            }
        }
    }
}

@Composable
private fun WizardConfigStep(uiState: AdminProductUiState, viewModel: AdminProductViewModel) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Surface(color = AdminBg, shape = RoundedCornerShape(16.dp)) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("PHÂN LOẠI THEO SIZE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Bật nếu món ăn có nhiều kích cỡ khác nhau", fontSize = 11.sp, color = AdminTextMuted)
                }
                Switch(checked = uiState.hasSizes, onCheckedChange = viewModel::setHasSizes, colors = SwitchDefaults.colors(checkedThumbColor = AdminRed, checkedTrackColor = AdminRed.copy(alpha = 0.2f)))
            }
        }

        if (uiState.hasSizes) {
            uiState.sizes.forEachIndexed { i, s ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = s.name, onValueChange = { viewModel.onSizeChanged(i, it, s.price) }, placeholder = { Text("Tên (S,M,L)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = s.price, onValueChange = { viewModel.onSizeChanged(i, s.name, it) }, placeholder = { Text("Giá thêm") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    IconButton(onClick = { viewModel.removeSizeInput(i) }) { Icon(Icons.Default.RemoveCircleOutline, null, tint = AdminTextMuted) }
                }
            }
            TextButton(onClick = viewModel::addSizeInput) { Text("+ Thêm lựa chọn size", fontWeight = FontWeight.Bold, color = AdminRed) }
        } else {
            WizardField(uiState.manualBasePriceInput, "Giá bán (VNĐ)", viewModel::onBasePriceChanged, keyboardType = KeyboardType.Number)
        }

        Text("TOPPING ÁP DỤNG", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
            uiState.allToppingsList.forEach { t ->
                val sel = uiState.selectedToppingIds.contains(t.id)
                FilterChip(
                    selected = sel, onClick = { viewModel.toggleToppingSelection(t.id) },
                    label = { Text(t.name.orEmpty()) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AdminRed, selectedLabelColor = Color.White)
                )
            }
        }
    }
}

@Composable
private fun WizardField(value: String, label: String, onValueChange: (String) -> Unit, singleLine: Boolean = true, minLines: Int = 1, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp), singleLine = singleLine, minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminRed, unfocusedBorderColor = AdminBorder)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(mainAxisSpacing: androidx.compose.ui.unit.Dp, crossAxisSpacing: androidx.compose.ui.unit.Dp, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing), verticalArrangement = Arrangement.spacedBy(crossAxisSpacing)) { content() }
}

private fun uriToMultipart(context: Context, uri: Uri): MultipartBody.Part {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    val body = bytes.toRequestBody((context.contentResolver.getType(uri) ?: "image/*").toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("image", "product.jpg", body)
}
