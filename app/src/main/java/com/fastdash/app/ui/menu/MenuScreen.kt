package com.fastdash.app.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

private val FastDashRed = Color(0xFFD6092F)
private val FastDashRedDark = Color(0xFF9B0622)
private val BackgroundGrey = Color(0xFFF7F7F7)
private val PrimaryBlack = Color(0xFF1F1F1F)
private val SurfaceWhite = Color.White
private val TextGrey = Color(0xFF777777)
private val DividerGrey = Color(0xFFEEEEEE)

@Composable
fun MenuScreen(
    onOpenProduct: (ProductResponse) -> Unit,
    onAddToCart: (ProductResponse) -> Unit,
    cartCount: Int = 0,
    onCartClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val owner = context as? ViewModelStoreOwner ?: error("MenuScreen requires a ViewModelStoreOwner context")
    val viewModel: HomeViewModel = remember(owner) {
        ViewModelProvider(owner, HomeViewModelFactory(context.applicationContext))[HomeViewModel::class.java]
    }

    val categories by viewModel.categories.observeAsState(emptyList())
    val products by viewModel.products.observeAsState(emptyList())
    val loading by viewModel.loading.observeAsState(false)
    val selectedCategoryId by viewModel.selectedCategoryId.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadHomeData() }
    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            viewModel.selectCategory(categories.first().id)
        }
    }

    val filteredProducts = products.filter { product ->
        val query = searchQuery.trim()
        query.isBlank() || product.name.contains(query, true) || product.description.orEmpty().contains(query, true)
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGrey)) {
        MenuHeader(cartCount = cartCount, onCartClick = onCartClick)
        MenuSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }, onClear = { searchQuery = "" })
        CategoryChipRow(categories = categories, selectedCategoryId = selectedCategoryId, onSelectCategory = { viewModel.selectCategory(it.id) })
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                loading -> MenuLoadingState()
                errorMessage != null && filteredProducts.isEmpty() -> MenuErrorState(message = "Không thể tải thực đơn lúc này", onRetry = {
                    viewModel.clearError()
                    viewModel.loadProducts(selectedCategoryId)
                })
                filteredProducts.isEmpty() -> MenuEmptyState(isSearching = searchQuery.isNotBlank())
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 148.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        MenuProductCard(product = product, onClick = { onOpenProduct(product) }, onAddToCart = { onAddToCart(product) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuHeader(cartCount: Int, onCartClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = BackgroundGrey) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Thực đơn", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlack)
                Text("Chọn món ngon hôm nay", fontSize = 14.sp, color = TextGrey)
            }
            Surface(shape = CircleShape, color = SurfaceWhite, shadowElevation = 6.dp) {
                BadgedBox(badge = {
                    if (cartCount > 0) {
                        Badge(containerColor = FastDashRed, contentColor = Color.White) {
                            Text(cartCount.coerceAtMost(99).toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }) {
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Giỏ hàng", tint = PrimaryBlack)
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuSearchBar(query: String, onQueryChange: (String) -> Unit, onClear: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), color = BackgroundGrey) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Bạn đang thèm gì?", color = TextGrey) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = FastDashRed) },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Filled.Close, contentDescription = "Xóa tìm kiếm", tint = TextGrey)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FastDashRed,
                unfocusedBorderColor = DividerGrey,
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                cursorColor = FastDashRed
            )
        )
    }
}

@Composable
private fun CategoryChipRow(
    categories: List<CategoryResponse>,
    selectedCategoryId: Long?,
    onSelectCategory: (CategoryResponse) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categories, key = { it.id }) { category ->
            val selected = selectedCategoryId == category.id
            Surface(
                modifier = Modifier.clickable { onSelectCategory(category) },
                color = if (selected) FastDashRed else Color(0xFFF0F0F0),
                shape = RoundedCornerShape(50),
                shadowElevation = if (selected) 4.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(categoryIcon(category.name), contentDescription = null, tint = if (selected) Color.White else PrimaryBlack, modifier = Modifier.size(16.dp))
                    Text(
                        text = categoryLabel(category.name),
                        color = if (selected) Color.White else PrimaryBlack,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuProductCard(product: ProductResponse, onClick: () -> Unit, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(width = 102.dp, height = 102.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF2F2F2)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNullOrBlank()) {
                    Icon(categoryIcon(product.categoryName), contentDescription = null, tint = FastDashRed, modifier = Modifier.size(40.dp))
                } else {
                    AsyncImage(
                        model = ImageUtils.buildImageRequest(LocalContext.current, product.imageUrl),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(product.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    product.description?.takeIf { it.isNotBlank() } ?: "Món ngon nóng hổi, giao nhanh đến bạn.",
                    fontSize = 12.sp,
                    color = TextGrey,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(CurrencyUtils.formatVnd(product.basePrice), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FastDashRed)
                    Surface(modifier = Modifier.size(40.dp).clickable { onAddToCart() }, shape = CircleShape, color = FastDashRed, shadowElevation = 4.dp) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Add, contentDescription = "Thêm nhanh", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuLoadingState() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 148.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(4) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(102.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFECECEC)))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.fillMaxWidth(0.55f).height(18.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFECECEC)))
                        Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF1F1F1)))
                        Box(Modifier.fillMaxWidth(0.7f).height(14.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF1F1F1)))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(88.dp).height(18.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFECECEC)))
                            Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFECECEC)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuEmptyState(isSearching: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.Fastfood, contentDescription = null, tint = FastDashRed, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(12.dp))
        Text("Không tìm thấy món phù hợp", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack)
        Spacer(Modifier.height(6.dp))
        Text(
            if (isSearching) "Thử tìm kiếm bằng từ khóa khác" else "Danh mục này hiện chưa có món hiển thị",
            color = TextGrey,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun MenuErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Sync, contentDescription = null, tint = FastDashRed, modifier = Modifier.size(54.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack)
        Spacer(Modifier.height(6.dp))
        Text("Vui lòng thử lại sau ít phút hoặc tải lại danh sách món.", color = TextGrey, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = FastDashRed, contentColor = Color.White), shape = RoundedCornerShape(14.dp)) {
            Text("Thử lại", fontWeight = FontWeight.Bold)
        }
    }
}

private fun categoryLabel(name: String): String {
    val normalized = name.lowercase()
    return when {
        normalized.contains("pizza") -> "Pizza"
        normalized.contains("gà") || normalized.contains("ga") || normalized.contains("chicken") -> "Gà rán"
        normalized.contains("combo") -> "Combo"
        normalized.contains("nước") || normalized.contains("nuoc") || normalized.contains("drink") -> "Nước uống"
        else -> name
    }
}

private fun categoryIcon(name: String): ImageVector {
    val normalized = name.lowercase()
    return when {
        normalized.contains("pizza") -> Icons.Outlined.LocalPizza
        normalized.contains("gà") || normalized.contains("ga") || normalized.contains("chicken") -> Icons.Outlined.SetMeal
        normalized.contains("combo") -> Icons.Outlined.RestaurantMenu
        normalized.contains("nước") || normalized.contains("nuoc") || normalized.contains("drink") -> Icons.Outlined.LocalDrink
        else -> Icons.Outlined.Fastfood
    }
}
