package com.fastdash.app.ui.admin

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fastdash.app.R
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

private val PizzaHutRed = Color(0xFFC8102E)
private val DarkRed = Color(0xFF8B0000)
private val PrimaryBlack = Color(0xFF1C1C1C)
private val LightBackground = Color(0xFFF6EFE7)
private val SurfaceWhite = Color.White
private val AdminMuted = Color(0xFF7A7A7A)
private val AdminBlue = Color(0xFF0078AE)
private val AdminCream = Color(0xFFFFF6EE)

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
                if (response.isSuccessful) {
                    products = response.body().orEmpty()
                } else {
                    products = emptyList()
                }
            } catch (e: Exception) {
                products = emptyList()
            } finally {
                productLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadProducts() }
    LaunchedEffect(uiState.showAddForm) {
        if (uiState.showAddForm) {
            viewModel.loadFormData()
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    val filteredProducts = products.filter { it.name.orEmpty().contains(searchQuery, ignoreCase = true) }

    if (uiState.showAddForm) {
        AddProductWizard(
            uiState = uiState,
            viewModel = viewModel,
            onClose = { viewModel.setShowAddForm(false) },
            onSuccess = { loadProducts() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("PRODUCT COMMAND", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = AdminCream.copy(alpha = 0.8f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo2),
                                contentDescription = "FastDash",
                                modifier = Modifier.height(28.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("PRODUCTS", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = AdminCream)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(
                    Brush.horizontalGradient(
                        colors = listOf(PrimaryBlack, DarkRed, PizzaHutRed)
                    )
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.startAddForm() },
                containerColor = PizzaHutRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("THÊM MÓN", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = LightBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Metrics and Search Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SurfaceWhite, AdminCream)
                        )
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "TỔNG MÓN",
                        value = products.size.toString(),
                        accentColor = PizzaHutRed
                    )
                    AdminMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "ĐANG BÁN",
                        value = products.count { it.status == 1 }.toString(),
                        accentColor = AdminBlue
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tìm kiếm tên món ăn...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PizzaHutRed) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PizzaHutRed,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )
            }

            if (productLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PizzaHutRed)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!productLoading && filteredProducts.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("Không tìm thấy sản phẩm nào", color = AdminMuted)
                        }
                    }
                }

                items(filteredProducts) { product ->
                    AdminProductItem(
                        repository = repositoryProduct,
                        publicRepository = publicProductRepository,
                        product = product,
                        onEdit = { viewModel.showEditForm(product) },
                        onToggleStatus = {
                            viewModel.toggleProductStatus(product.id, product.status) {
                                loadProducts()
                            }
                        },
                        onDelete = {
                            scope.launch {
                                try {
                                    val response = repositoryProduct.deleteProduct(product.id)
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Đã xóa món ăn", Toast.LENGTH_SHORT).show()
                                        loadProducts()
                                    } else {
                                        Toast.makeText(context, "Lỗi xóa món ăn: ${response.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProductWizard(
    uiState: AdminProductUiState,
    viewModel: AdminProductViewModel,
    onClose: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val part = uriToMultipart(context, it)
            viewModel.onImageSelected(it, part)
        }
    }

    BackHandler {
        if (uiState.currentStep > 1) viewModel.previousStep()
        else onClose()
    }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = LightBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp)
        ) {
            // Wizard Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when(uiState.currentStep) {
                        1 -> "1. THÔNG TIN CƠ BẢN"
                        2 -> "2. DANH MỤC"
                        3 -> "3. CẤU HÌNH"
                        else -> ""
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = PizzaHutRed
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            // Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(if (uiState.currentStep > index) PizzaHutRed else Color.LightGray)
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).padding(20.dp)) {
                when (uiState.currentStep) {
                    1 -> StepBasicInfo(uiState, viewModel, onPickImage = { imagePicker.launch("image/*") })
                    2 -> StepCategory(uiState, viewModel)
                    3 -> StepConfiguration(uiState, viewModel)
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.currentStep > 1) {
                    OutlinedButton(
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PizzaHutRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PizzaHutRed)
                    ) {
                        Text("QUAY LẠI")
                    }
                }

                Button(
                    onClick = {
                        if (uiState.currentStep == 1 && uiState.categories.isEmpty()) {
                            viewModel.setMessage("Vui lòng chờ danh mục tải. Nếu lỗi thì kiểm tra kết nối mạng.", isError = true)
                        } else if (uiState.currentStep < 3) {
                            viewModel.nextStep()
                        } else {
                            scope.launch {
                                        val isEditing = uiState.editingProductId != null
                                        val success = if (isEditing) {
                                            viewModel.updateProductFixed()
                                        } else {
                                            viewModel.createProduct()
                                        }
                                        if (success) {
                                    onSuccess()
                                    onClose()
                                }
                            }
                        }
                    },
                    enabled = if (uiState.currentStep < 3) uiState.canGoNext else uiState.canSubmit,
                    modifier = Modifier.weight(2f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PizzaHutRed)
                ) {
                    if (uiState.loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                                val isEditing = uiState.editingProductId != null
                                val buttonText = if (uiState.currentStep < 3) "TIẾP THEO" else {
                                    if (isEditing) "CẬP NHẬT & LƯU" else "HOÀN TẤT & LƯU"
                                }
                        Text(
                                    buttonText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            if (uiState.message != null) {
                Text(
                    text = uiState.message,
                    color = if (uiState.isError) Color.Red else PizzaHutRed,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StepBasicInfo(uiState: AdminProductUiState, viewModel: AdminProductViewModel, onPickImage: () -> Unit) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .clickable { onPickImage() },
            contentAlignment = Alignment.Center
        ) {
            if (uiState.imagePreview.isNotEmpty()) {
                AsyncImage(
                    model = if (uiState.imagePreview.startsWith("content://")) {
                        uiState.imagePreview
                    } else {
                        ImageUtils.buildImageRequest(LocalContext.current, uiState.imagePreview)
                    },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.CloudUpload, contentDescription = null, modifier = Modifier.size(48.dp), tint = PizzaHutRed)
                    Spacer(Modifier.height(8.dp))
                    Text("Tải ảnh món ăn", fontWeight = FontWeight.Bold, color = PizzaHutRed)
                }
            }
        }

        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChanged,
            label = { Text("Tên món ăn") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChanged,
            label = { Text("Mô tả món ăn") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 3
        )

        Text(
            text = "Giá bán và cấu hình size sẽ được thiết lập ở bước 3.",
            fontSize = 12.sp,
            color = AdminMuted
        )
    }
}

@Composable
private fun StepCategory(uiState: AdminProductUiState, viewModel: AdminProductViewModel) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Chọn danh mục cho món ăn này:", fontWeight = FontWeight.Bold, color = AdminMuted)
        
        if (uiState.categories.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PizzaHutRed)
                    Spacer(Modifier.height(8.dp))
                    Text("Đang tải danh mục...", fontSize = 12.sp, color = AdminMuted)
                }
            }
        }

        uiState.categories.forEach { category ->
            val isSelected = uiState.categoryIdInput == category.id.toString()
            val categoryName = category.name?.takeIf { it.isNotBlank() } ?: "(Không tên)"
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onCategoryIdChanged(category.id.toString()) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) PizzaHutRed.copy(alpha = 0.1f) else Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PizzaHutRed else Color.LightGray
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { viewModel.onCategoryIdChanged(category.id.toString()) },
                        colors = RadioButtonDefaults.colors(selectedColor = PizzaHutRed)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(categoryName, fontWeight = FontWeight.Bold, color = if (isSelected) PizzaHutRed else PrimaryBlack)
                        if (!category.description.isNullOrBlank()) {
                            Text(category.description, fontSize = 12.sp, color = AdminMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepConfiguration(uiState: AdminProductUiState, viewModel: AdminProductViewModel) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Sizes
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("MÓN CÓ NHIỀU SIZE", fontWeight = FontWeight.Bold, color = PrimaryBlack, fontSize = 13.sp)
                        Text(
                            if (uiState.hasSizes) "Đang dùng giá theo từng size" else "Đang dùng một giá bán chung",
                            fontSize = 12.sp,
                            color = AdminMuted
                        )
                    }
                    Switch(
                        checked = uiState.hasSizes,
                        onCheckedChange = { viewModel.setHasSizes(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PizzaHutRed,
                            checkedTrackColor = PizzaHutRed.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Text("KÍCH THƯỚC (SIZE)", fontWeight = FontWeight.Black, color = PrimaryBlack)

            if (uiState.hasSizes) {
                Text(
                    "Nhập từng size và giá bán tương ứng. Giá nhỏ nhất sẽ dùng làm giá cơ sở.",
                    fontSize = 12.sp,
                    color = AdminMuted
                )
                uiState.sizes.forEachIndexed { index, sizeInput ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = sizeInput.name,
                            onValueChange = { viewModel.onSizeChanged(index, it, sizeInput.price) },
                            placeholder = { Text("Size (S/M/L)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = sizeInput.price,
                            onValueChange = { viewModel.onSizeChanged(index, sizeInput.name, it) },
                            placeholder = { Text("Giá bán") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            prefix = { Text("₫") }
                        )
                        IconButton(onClick = { viewModel.removeSizeInput(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                TextButton(onClick = { viewModel.addSizeInput() }, contentPadding = PaddingValues(0.dp)) {
                    Text("+ Thêm lựa chọn size khác", color = PizzaHutRed, fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedTextField(
                    value = uiState.manualBasePriceInput,
                    onValueChange = viewModel::onBasePriceChanged,
                    label = { Text("Giá bán (VNĐ)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text("₫ ", fontWeight = FontWeight.Bold) }
                )
                Text("Món này không dùng size. Giá bán sẽ lấy từ ô trên.", fontSize = 12.sp, color = AdminMuted)
            }
        }

        Divider()

        // Toppings
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("TOPPING ÁP DỤNG", fontWeight = FontWeight.Black, color = PrimaryBlack)
            
            if (uiState.allToppingsList.isEmpty()) {
                Text("Chưa có topping nào được tạo.", fontSize = 12.sp, color = AdminMuted)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.allToppingsList.forEach { topping ->
                        val isSelected = uiState.selectedToppingIds.contains(topping.id)
                        val toppingName = topping.name?.takeIf { it.isNotBlank() } ?: "(Không tên)"
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.toggleToppingSelection(topping.id) },
                            label = { Text(toppingName) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PizzaHutRed,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing),
        verticalArrangement = Arrangement.spacedBy(crossAxisSpacing)
    ) {
        content()
    }
}

@Composable
private fun AdminProductItem(
    repository: AdminProductRepository,
    publicRepository: ProductRepository,
    product: AdminProductResponse,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    var resolvedImageUrl by remember(product.id) { mutableStateOf(product.imageUrl) }

    LaunchedEffect(product.id, product.imageUrl) {
        if (resolvedImageUrl.isNullOrBlank()) {
            val adminDetailUrl = runCatching {
                val detailResponse = repository.getProductDetail(product.id)
                if (detailResponse.isSuccessful) detailResponse.body()?.imageUrl else null
            }.getOrNull()

            val publicDetailUrl = if (adminDetailUrl.isNullOrBlank()) {
                runCatching {
                    val detailResponse = publicRepository.getProductById(product.id)
                    if (detailResponse.isSuccessful) detailResponse.body()?.imageUrl else null
                }.getOrNull()
            } else adminDetailUrl

            if (!publicDetailUrl.isNullOrBlank()) resolvedImageUrl = publicDetailUrl
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                if (!resolvedImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageUtils.buildImageRequest(LocalContext.current, resolvedImageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0F0F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.CloudUpload, contentDescription = null, tint = AdminMuted, modifier = Modifier.size(22.dp))
                    }
                }
                if (product.status == 0) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ẨN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name.orEmpty(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PrimaryBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = CurrencyUtils.formatVnd(product.basePrice),
                    color = PizzaHutRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(12.dp), tint = AdminMuted)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (product.isCustomizable == 1) "Có cấu hình riêng" else "Không có cấu hình",
                        fontSize = 11.sp,
                        color = AdminMuted
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PizzaHutRed)
                }
                IconButton(onClick = onToggleStatus, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (product.status == 1) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Status",
                        tint = if (product.status == 1) AdminBlue else AdminMuted
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.LightGray)
                }
            }
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = PrimaryBlack)
        }
    }
}

private fun uriToMultipart(
    context: Context,
    uri: Uri
): MultipartBody.Part {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    val mimeType = context.contentResolver.getType(uri) ?: "image/*"
    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    val fileExtension = when (mimeType.lowercase()) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/jpeg", "image/jpg" -> "jpg"
        else -> "jpg"
    }
    val fileName = "product_image.$fileExtension"
    return MultipartBody.Part.createFormData(name = "image", filename = fileName, body = requestBody)
}
