package com.fastdash.app.ui.admin

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.fastdash.app.data.remote.api.UpdateProductRequest
import com.fastdash.app.data.repository.AdminProductRepository
import com.fastdash.app.viewmodel.AdminProductViewModel
import com.fastdash.app.utils.CurrencyUtils
import com.fastdash.app.utils.ImageUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.launch

private val PizzaHutRed = Color(0xFFC8102E)
private val PrimaryBlack = Color(0xFF1C1C1C)
private val LightBackground = Color(0xFFF8F8F8)
private val SurfaceWhite = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductScreen(
    viewModel: AdminProductViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val repositoryProduct = remember { AdminProductRepository(context.applicationContext) }
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var products by remember { mutableStateOf<List<AdminProductResponse>>(emptyList()) }
    var productLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var editingProductId by remember { mutableStateOf<Long?>(null) }
    
    // Edit Form State
    var editFormCategoryId by remember { mutableStateOf("") }
    var editFormName by remember { mutableStateOf("") }
    var editFormDescription by remember { mutableStateOf("") }
    var editFormPrice by remember { mutableStateOf("") }
    var editFormImageUrl by remember { mutableStateOf("") }

    fun loadProducts() {
        productLoading = true
        scope.launch {
            try {
                val response = repositoryProduct.getProducts()
                products = response.body().orEmpty()
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                productLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadProducts() }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val part = uriToMultipart(context, it)
            viewModel.uploadImage(part)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.logo2),
                        contentDescription = "FastDash Logo",
                        modifier = Modifier.height(32.dp),
                        contentScale = ContentScale.Fit
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = LightBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // STEP 3-7: Add Product Form
            AddProductFullForm(
                uiState = uiState,
                viewModel = viewModel,
                onPickImage = { imagePicker.launch("image/*") },
                onSubmit = {
                    viewModel.createProduct()
                    loadProducts()
                }
            )

            // Search & List
            Divider()
            Text("DANH SÁCH MÓN ĂN HIỆN TẠI", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tìm tên món...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            if (productLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PizzaHutRed)
            } else {
                val filteredProducts = products.filter { it.name.contains(searchQuery, ignoreCase = true) }
                filteredProducts.forEach { product ->
                    AdminProductItem(
                        product = product,
                        onDelete = {
                            scope.launch {
                                repositoryProduct.deleteProduct(product.id)
                                loadProducts()
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddProductFullForm(
    uiState: com.fastdash.app.viewmodel.AdminProductUiState,
    viewModel: AdminProductViewModel,
    onPickImage: () -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("THÊM MÓN MỚI", fontWeight = FontWeight.Black, fontSize = 18.sp, color = PizzaHutRed)

            // 4. Basic Info
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
                label = { Text("Mô tả") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2
            )

            OutlinedTextField(
                value = uiState.basePriceInput,
                onValueChange = viewModel::onBasePriceChanged,
                label = { Text("Giá cơ bản (VNĐ)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            // Image
            Button(
                onClick = onPickImage,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = LightBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.CloudUpload, contentDescription = null, tint = PizzaHutRed)
                Spacer(Modifier.width(8.dp))
                Text(if (uiState.imageUrl.isEmpty()) "Chọn ảnh món ăn" else "Đã chọn ảnh", color = PrimaryBlack)
            }

            if (uiState.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageUtils.formatImageUrl(uiState.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Divider()

            // 5. Category Selection
            Text("CHỌN DANH MỤC", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.categories.forEach { cat ->
                    FilterChip(
                        selected = uiState.categoryIdInput == cat.id.toString(),
                        onClick = { viewModel.onCategoryIdChanged(cat.id.toString()) },
                        label = { Text(cat.name) }
                    )
                }
            }

            Divider()

            // 6. Sizes Management
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("KÍCH THƯỚC & GIÁ THÊM", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                TextButton(onClick = { viewModel.addSizeInput() }) {
                    Text("+ Thêm size", color = PizzaHutRed)
                }
            }

            uiState.sizes.forEachIndexed { index, sizeInput ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = sizeInput.name,
                        onValueChange = { viewModel.onSizeChanged(index, it, sizeInput.price) },
                        label = { Text("Tên (S/M/L)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = sizeInput.price,
                        onValueChange = { viewModel.onSizeChanged(index, sizeInput.name, it) },
                        label = { Text("Giá thêm") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp)
                    )
                    IconButton(onClick = { viewModel.removeSizeInput(index) }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red)
                    }
                }
            }

            Divider()

            // 7. Toppings Selection
            Text("CHỌN TOPPING ÁP DỤNG", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.allToppings.forEach { topping ->
                    val isSelected = uiState.selectedToppingIds.contains(topping.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleToppingSelection(topping.id) },
                        label = { Text(topping.name) },
                        leadingIcon = {
                            if (isSelected) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 8. Save Button
            Button(
                onClick = onSubmit,
                enabled = uiState.canSubmit,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PizzaHutRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("LƯU MÓN ĂN & CẤU HÌNH", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            if (uiState.message != null) {
                Text(
                    text = uiState.message!!,
                    color = if (uiState.isError) Color.Red else PizzaHutRed,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AdminProductItem(
    product: AdminProductResponse,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageUtils.formatImageUrl(product.imageUrl),
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(CurrencyUtils.formatVnd(product.basePrice), color = PizzaHutRed, fontSize = 13.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color.Red)
            }
        }
    }
}

private fun uriToMultipart(
    context: Context,
    uri: Uri
): MultipartBody.Part {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(name = "file", filename = "product_image.jpg", body = requestBody)
}
