package com.fastdash.app.ui.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import coil.compose.AsyncImage
import com.fastdash.app.R
import com.fastdash.app.data.model.response.CategoryResponse
import com.fastdash.app.data.model.response.ProductResponse
import com.fastdash.app.ui.menu.MenuScreen
import com.fastdash.app.ui.order.OrderHistoryScreen
import com.fastdash.app.ui.order.OrderHistoryUiModel
import com.fastdash.app.ui.profile.ProfileScreen
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
fun HomeScreen(
    onOpenProduct: (ProductResponse) -> Unit = {},
    onCheckout: () -> Unit = {},
    onAddToCart: (ProductResponse) -> Unit = onOpenProduct,
    orders: List<OrderHistoryUiModel> = emptyList(),
    onOpenOrder: (OrderHistoryUiModel) -> Unit = {},
    profileFullName: String = "Khách hàng FastDash",
    profileEmail: String = "customer@fastdash.com",
    profilePhone: String = "0123456789",
    profileRole: String = "USER",
    onLogout: () -> Unit = {},
    cartCount: Int = 0,
    cartTotal: Double = 0.0
) {
    val context = LocalContext.current
    val owner = context as? ViewModelStoreOwner
        ?: error("HomeScreen requires a ViewModelStoreOwner context")

    val viewModel: HomeViewModel = remember(owner) {
        ViewModelProvider(
            owner,
            HomeViewModelFactory(context.applicationContext)
        )[HomeViewModel::class.java]
    }

    val categories by viewModel.categories.observeAsState(emptyList())
    val products by viewModel.products.observeAsState(emptyList())
    val loading by viewModel.loading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()

    var selectedTab by remember { mutableStateOf(BottomTab.Home) }

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            if (selectedTab == BottomTab.Home) {
                HomeTopBar(cartCount = cartCount, onCartClick = onCheckout)
            }
        },
        bottomBar = {
            Column {
                if (cartCount > 0 && (selectedTab == BottomTab.Home || selectedTab == BottomTab.Menu)) {
                    MiniCartIndicator(
                        count = cartCount,
                        total = cartTotal,
                        onClick = onCheckout
                    )
                }
                PizzaHutBottomNavigation(
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it }
                )
            }
        },
        containerColor = LightGrey
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (loading && selectedTab == BottomTab.Home) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = PizzaHutRed
                )
            }
            when (selectedTab) {
                BottomTab.Home -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item { PromotionPager() }
                        item { WelcomeAddressSection() }
                        item {
                            SectionHeaderRow(title = "Bạn sẽ thích")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(products.take(5)) { product ->
                                    RecommendedProductCard(
                                        product = product,
                                        onClick = { onOpenProduct(product) },
                                        onAddToCart = { onAddToCart(product) }
                                    )
                                }
                            }
                        }
                        item {
                            SectionHeaderRow(
                                title = "Menu",
                                actionText = "Xem thêm",
                                onActionClick = { selectedTab = BottomTab.Menu }
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(categories) { category ->
                                    MenuCategoryCard(
                                        category = category,
                                        onClick = {
                                            viewModel.selectCategory(category.id)
                                            selectedTab = BottomTab.Menu
                                        }
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                            ActionMenuItem(
                                icon = Icons.Outlined.ShoppingBag,
                                title = "Theo dõi đơn hàng",
                                subtitle = "Dễ dàng theo dõi trạng thái đơn hàng của bạn",
                                onClick = { selectedTab = BottomTab.Orders }
                            )
                            ActionMenuItem(
                                icon = Icons.Outlined.LocationOn,
                                title = "Cửa hàng của chúng tôi",
                                onClick = {}
                            )
                        }
                        item {
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Kết nối với FastDash",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(12.dp))
                            ActionMenuItem(
                                icon = Icons.Outlined.Phone,
                                title = "Cần trợ giúp?",
                                subtitle = "Gọi 1900 1822",
                                onClick = {}
                            )
                            ActionMenuItem(
                                icon = Icons.Outlined.Description,
                                title = "Điều khoản và điều kiện",
                                onClick = {}
                            )
                        }
                    }
                }

                BottomTab.Menu -> {
                    MenuScreen(
                        onOpenProduct = onOpenProduct,
                        onAddToCart = onAddToCart
                    )
                }

                BottomTab.Orders -> {
                    OrderHistoryScreen(
                        orders = orders,
                        onBack = { selectedTab = BottomTab.Home },
                        onOpenOrder = onOpenOrder
                    )
                }

                BottomTab.Account -> {
                    ProfileScreen(
                        fullName = profileFullName,
                        email = profileEmail,
                        phone = profilePhone,
                        role = profileRole,
                        onBack = { selectedTab = BottomTab.Home },
                        onOpenOrders = { selectedTab = BottomTab.Orders },
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(cartCount: Int, onCartClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.logo2),
                contentDescription = "FastDash Logo",
                modifier = Modifier
                    .height(45.dp)
                    .padding(vertical = 4.dp),
                contentScale = ContentScale.Fit
            )
        },
        actions = {
            BadgedBox(
                badge = {
                    if (cartCount > 0) {
                        Badge(containerColor = PizzaHutRed) {
                            Text(cartCount.toString(), color = Color.White)
                        }
                    }
                },
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Icon(
                    Icons.Filled.ShoppingCart,
                    contentDescription = "Cart",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onCartClick() }
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SurfaceWhite)
    )
}

@Composable
private fun PromotionPager() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PizzaHutRed)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(color = Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        "Combo",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp),
                        fontSize = 10.sp
                    )
                }
                Text("Đại sứ ăn ngon", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                Text("Chỉ từ", color = Color.White, fontSize = 12.sp)
                Text("249.000 VNĐ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text("Pizza combo", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == 0) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (index == 0) Color.White else Color.White.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
private fun WelcomeAddressSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Chào mừng bạn trở lại", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Vui lòng chọn giao hàng hoặc mua mang về để đặt hàng", fontSize = 13.sp, color = TextGrey)

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = PizzaHutRed.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Moped, contentDescription = null, tint = PizzaHutRed, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Giao hàng tới", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "170 Võ Nguyên Giáp, An Hải, Sơn Trà, Đà Nẵng",
                        fontSize = 12.sp,
                        color = TextGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGrey)
            }
        }
    }
}

@Composable
private fun RecommendedProductCard(
    product: ProductResponse,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageUtils.buildImageRequest(LocalContext.current, product.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("Món nổi bật", fontSize = 10.sp, color = TextGrey)
                Spacer(Modifier.height(8.dp))
                Text("Chỉ từ", fontSize = 11.sp, color = TextGrey)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        CurrencyUtils.formatVnd(product.basePrice),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlack
                    )
                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .size(28.dp)
                            .background(PizzaHutRed, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuCategoryCard(category: CategoryResponse, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                            )
                        )
                )
                Text(
                    category.name.uppercase(),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun ActionMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = PizzaHutRed, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 12.sp, color = TextGrey)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGrey)
        }
    }
}

@Composable
private fun SectionHeaderRow(title: String, actionText: String? = null, onActionClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (actionText != null) {
            Text(
                actionText,
                modifier = Modifier.clickable { onActionClick() },
                color = PizzaHutRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PizzaHutBottomNavigation(selectedTab: BottomTab, onSelectTab: (BottomTab) -> Unit) {
    NavigationBar(containerColor = SurfaceWhite, tonalElevation = 8.dp) {
        BottomTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectTab(tab) },
                icon = {
                    val icon = when (tab) {
                        BottomTab.Home -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                        BottomTab.Menu -> if (isSelected) Icons.Filled.LocalPizza else Icons.Outlined.LocalPizza
                        BottomTab.Orders -> if (isSelected) Icons.AutoMirrored.Filled.Assignment else Icons.AutoMirrored.Outlined.Assignment
                        BottomTab.Account -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
                    }
                    Icon(icon, contentDescription = tab.label)
                },
                label = {
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PizzaHutRed,
                    selectedTextColor = PizzaHutRed,
                    unselectedIconColor = TextGrey,
                    unselectedTextColor = TextGrey,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun MiniCartIndicator(count: Int, total: Double, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        color = PizzaHutRed,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(count.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text("Xem giỏ hàng", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Text(CurrencyUtils.formatVnd(total), color = Color.White, fontWeight = FontWeight.ExtraBold)
        }
    }
}

private enum class BottomTab(val label: String) {
    Home("Trang chủ"),
    Menu("Menu"),
    Orders("Đơn hàng"),
    Account("Tài khoản")
}
