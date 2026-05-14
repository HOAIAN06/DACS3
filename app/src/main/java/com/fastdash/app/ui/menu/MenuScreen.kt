package com.fastdash.app.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import coil.compose.AsyncImage
import com.fastdash.app.data.model.response.CategoryResponse
import com.fastdash.app.data.model.response.ProductResponse
import com.fastdash.app.utils.CurrencyUtils
import com.fastdash.app.utils.ImageUtils
import com.fastdash.app.viewmodel.HomeViewModel
import com.fastdash.app.viewmodel.HomeViewModelFactory

private val PizzaHutRed = Color(0xFFC8102E)
private val LightGrey = Color(0xFFF8F8F8)
private val PrimaryBlack = Color(0xFF1C1C1C)
private val SurfaceWhite = Color.White
private val TextGrey = Color(0xFF757575)

@Composable
fun MenuScreen(
    onOpenProduct: (ProductResponse) -> Unit,
    onAddToCart: (ProductResponse) -> Unit
) {
    val context = LocalContext.current
    val owner = context as? ViewModelStoreOwner
        ?: error("MenuScreen requires a ViewModelStoreOwner context")

    val viewModel: HomeViewModel = remember(owner) {
        ViewModelProvider(
            owner,
            HomeViewModelFactory(context.applicationContext)
        )[HomeViewModel::class.java]
    }

    val categories by viewModel.categories.observeAsState(emptyList())
    val products by viewModel.products.observeAsState(emptyList())
    val loading by viewModel.loading.observeAsState(false)
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
            viewModel.loadProducts(categories.first().id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGrey)
    ) {
        // Header with Search
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceWhite,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "THỰC ĐƠN",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = PrimaryBlack
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Bạn đang thèm gì?") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PizzaHutRed) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PizzaHutRed,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )
            }
        }

        // Category Selection
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategoryId == category.id
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedCategoryId = category.id
                        viewModel.loadProducts(category.id)
                    },
                    label = { Text(category.name.uppercase(), fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PizzaHutRed,
                        selectedLabelColor = Color.White,
                        containerColor = LightGrey,
                        labelColor = TextGrey
                    ),
                    border = null,
                    shape = RoundedCornerShape(99.dp)
                )
            }
        }

        // Product List
        val filteredProducts = products.filter { 
            it.name.contains(searchQuery, ignoreCase = true)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PizzaHutRed
                )
            } else if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có món nào trong danh mục này", color = TextGrey)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredProducts) { product ->
                        MenuProductCard(
                            product = product,
                            onClick = { onOpenProduct(product) },
                            onAddToCart = { onAddToCart(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuProductCard(
    product: ProductResponse,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageUtils.buildImageRequest(LocalContext.current, product.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlack,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.description ?: "",
                    fontSize = 12.sp,
                    color = TextGrey,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = CurrencyUtils.formatVnd(product.basePrice),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PizzaHutRed
                    )
                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .size(32.dp)
                            .background(PizzaHutRed, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
