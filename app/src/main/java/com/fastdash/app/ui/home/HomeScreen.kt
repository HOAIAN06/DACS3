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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay

private val FastDashRed = Color(0xFFD6092F)
private val FastDashRedDark = Color(0xFF9B0622)
private val LightGrey = Color(0xFFF7F7F7)
private val PrimaryBlack = Color(0xFF1F1F1F)
private val SurfaceWhite = Color.White
private val TextGrey = Color(0xFF777777)

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
    deliveryAddress: String = "",
    onLogout: () -> Unit = {},
    cartCount: Int = 0,
    cartTotal: Double = 0.0
) {
    val context = LocalContext.current
    val owner = context as? ViewModelStoreOwner ?: error("HomeScreen requires a ViewModelStoreOwner context")
    val viewModel: HomeViewModel = remember(owner) {
        ViewModelProvider(owner, HomeViewModelFactory(context.applicationContext))[HomeViewModel::class.java]
    }
    val categories by viewModel.categories.observeAsState(emptyList())
    val products by viewModel.products.observeAsState(emptyList())
    val loading by viewModel.loading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    var selectedTab by remember { mutableStateOf(BottomTab.Home) }

    LaunchedEffect(Unit) { viewModel.loadHomeData() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            if (selectedTab == BottomTab.Home) {
                HomeHeader(cartCount = cartCount, profileFullName = profileFullName, onCartClick = onCheckout)
            }
        },
        bottomBar = {
            Column {
                if (cartCount > 0 && (selectedTab == BottomTab.Home || selectedTab == BottomTab.Menu)) {
                    CartBar(count = cartCount, total = cartTotal, onClick = onCheckout)
                }
                FastDashBottomNavigation(selectedTab = selectedTab, onSelectTab = { selectedTab = it })
            }
        },
        containerColor = LightGrey,
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            if (loading && selectedTab == BottomTab.Home) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    color = FastDashRed,
                    trackColor = FastDashRed.copy(alpha = 0.16f)
                )
            }
            when (selectedTab) {
                BottomTab.Home -> HomeContent(
                    products = products,
                    categories = categories,
                    deliveryAddress = deliveryAddress,
                    onOpenProduct = onOpenProduct,
                    onAddToCart = onAddToCart,
                    onSelectCategory = {
                        viewModel.selectCategory(it.id)
                        selectedTab = BottomTab.Menu
                    },
                    onOpenMenu = { selectedTab = BottomTab.Menu },
                    onOpenOrders = { selectedTab = BottomTab.Orders }
                )
                BottomTab.Menu -> MenuScreen(onOpenProduct = onOpenProduct, onAddToCart = onAddToCart, cartCount = cartCount, onCartClick = onCheckout)
                BottomTab.Orders -> OrderHistoryScreen(orders = orders, onBack = { selectedTab = BottomTab.Home }, onOpenOrder = onOpenOrder)
                BottomTab.Account -> ProfileScreen(
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

@Composable
private fun HomeContent(
    products: List<ProductResponse>,
    categories: List<CategoryResponse>,
    deliveryAddress: String,
    onOpenProduct: (ProductResponse) -> Unit,
    onAddToCart: (ProductResponse) -> Unit,
    onSelectCategory: (CategoryResponse) -> Unit,
    onOpenMenu: () -> Unit,
    onOpenOrders: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { PromoBanner(banners = rememberPromoBanners(products), onPrimaryClick = { products.firstOrNull()?.let(onOpenProduct) ?: onOpenMenu() }) }
        item { DeliveryAddressCard(address = deliveryAddress, onClick = onOpenMenu) }
        item { RecommendedSection(products = products, onOpenProduct = onOpenProduct, onAddToCart = onAddToCart) }
        item { CategorySection(categories = categories, onOpenMenu = onOpenMenu, onSelectCategory = onSelectCategory) }
        item { QuickActionSection(onOpenOrders = onOpenOrders) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeHeader(cartCount: Int, profileFullName: String, onCartClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo2),
                    contentDescription = "FastDash Logo",
                    modifier = Modifier.height(52.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(text = "Chào mừng bạn trở lại", fontSize = 14.sp, color = TextGrey)
                    Text(
                        text = if (profileFullName.isNotBlank()) "Hôm nay bạn muốn ăn gì?" else "FastDash luôn sẵn sàng phục vụ",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlack,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            Surface(modifier = Modifier.padding(end = 16.dp), shape = CircleShape, color = SurfaceWhite, shadowElevation = 6.dp) {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(containerColor = FastDashRed, contentColor = Color.White) {
                                Text(text = cartCount.coerceAtMost(99).toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Giỏ hàng", tint = PrimaryBlack, modifier = Modifier.size(24.dp))
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = LightGrey)
    )
}

@Composable
private fun PromoBanner(banners: List<PromoBannerUiModel>, onPrimaryClick: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { banners.size })
    LaunchedEffect(banners.size) {
        if (banners.size > 1) {
            while (true) {
                delay(3200)
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % banners.size)
            }
        }
    }

    Column(modifier = Modifier.padding(top = 8.dp)) {
        HorizontalPager(state = pagerState, contentPadding = PaddingValues(horizontal = 16.dp), pageSpacing = 12.dp) { page ->
            val banner = banners[page]
            Card(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(Brush.linearGradient(colors = listOf(banner.startColor, banner.endColor)))
                        .padding(20.dp)
                ) {
                    Box(Modifier.align(Alignment.TopEnd).size(140.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)))
                    Box(Modifier.align(Alignment.BottomEnd).size(160.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.08f)))
                    Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.14f)) {
                                Text(banner.eyebrow, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(banner.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 28.sp)
                            Text(banner.subtitle, color = Color.White.copy(alpha = 0.88f), fontSize = 14.sp, lineHeight = 20.sp)
                            Text(banner.price, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            Button(
                                onClick = onPrimaryClick,
                                modifier = Modifier.height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = banner.endColor),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp)
                            ) { Text("Đặt ngay", fontWeight = FontWeight.Bold) }
                        }
                        Box(modifier = Modifier.weight(0.9f), contentAlignment = Alignment.Center) {
                            if (banner.imageUrl.isNullOrBlank()) {
                                Icon(banner.fallbackIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(100.dp))
                            } else {
                                AsyncImage(
                                    model = ImageUtils.buildImageRequest(LocalContext.current, banner.imageUrl),
                                    contentDescription = banner.title,
                                    modifier = Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(22.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.Center) {
            repeat(banners.size) { index ->
                Box(
                    modifier = Modifier.padding(horizontal = 4.dp)
                        .size(width = if (pagerState.currentPage == index) 22.dp else 8.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(if (pagerState.currentPage == index) FastDashRed else FastDashRed.copy(alpha = 0.18f))
                )
            }
        }
    }
}

@Composable
private fun DeliveryAddressCard(address: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(16.dp), color = FastDashRed.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = FastDashRed, modifier = Modifier.size(24.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Giao hàng tới", fontSize = 13.sp, color = TextGrey, fontWeight = FontWeight.Medium)
                Text(
                    text = address.ifBlank { "Vui lòng chọn giao hàng để đặt món" },
                    fontSize = 15.sp,
                    color = PrimaryBlack,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGrey)
        }
    }
}

@Composable
private fun RecommendedSection(
    products: List<ProductResponse>,
    onOpenProduct: (ProductResponse) -> Unit,
    onAddToCart: (ProductResponse) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeaderRow(title = "Bạn sẽ thích", subtitle = "Chọn nhanh những món bán chạy hôm nay")
        if (products.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
                Text("Thực đơn đang được cập nhật. Vui lòng quay lại sau ít phút.", modifier = Modifier.padding(20.dp), color = TextGrey, fontSize = 14.sp)
            }
        } else {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(products.take(6), key = { it.id }) { product ->
                    RecommendedProductCard(product = product, onClick = { onOpenProduct(product) }, onAddToCart = { onAddToCart(product) })
                }
            }
        }
    }
}

@Composable
private fun CategorySection(categories: List<CategoryResponse>, onOpenMenu: () -> Unit, onSelectCategory: (CategoryResponse) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeaderRow(title = "Thực đơn", subtitle = "Pizza, gà rán, combo và nước uống hấp dẫn", actionText = "Xem thêm", onActionClick = onOpenMenu)
        val items = if (categories.isEmpty()) fallbackCategories() else categories
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(items, key = { it.id }) { category ->
                CategoryCard(category = category, visual = categoryVisual(category.name), onClick = {
                    if (categories.isEmpty()) onOpenMenu() else onSelectCategory(category)
                })
            }
        }
    }
}

@Composable
private fun QuickActionSection(onOpenOrders: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TodayOfferCard()
        SectionHeaderRow(title = "Dịch vụ nhanh", subtitle = "Tiện ích cần thiết trong vài chạm")
        val actions = listOf(
            QuickActionUiModel(Icons.Outlined.ShoppingBag, "Theo dõi đơn hàng", "Cập nhật trạng thái", onOpenOrders),
            QuickActionUiModel(Icons.Outlined.Storefront, "Cửa hàng gần bạn", "Tìm chi nhánh gần nhất", {}),
            QuickActionUiModel(Icons.AutoMirrored.Outlined.HelpOutline, "Cần trợ giúp?", "Liên hệ hỗ trợ", {}),
            QuickActionUiModel(Icons.Outlined.Description, "Điều khoản", "Chính sách dịch vụ", {})
        )
        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            actions.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowItems.forEach { action ->
                        QuickActionGridItem(action = action, modifier = Modifier.weight(1f))
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendedProductCard(product: ProductResponse, onClick: () -> Unit, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier.width(300.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(width = 116.dp, height = 116.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFFF2F2F2)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNullOrBlank()) {
                    Icon(categoryVisual(product.categoryName).icon, contentDescription = null, tint = FastDashRed, modifier = Modifier.size(42.dp))
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
                Surface(color = FastDashRed.copy(alpha = 0.1f), shape = RoundedCornerShape(50)) {
                    Text("Món nổi bật", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = FastDashRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(product.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    text = product.description?.takeIf { it.isNotBlank() } ?: "Đậm vị, nóng hổi và sẵn sàng giao nhanh tới bạn.",
                    fontSize = 12.sp,
                    color = TextGrey,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Giá chỉ từ", fontSize = 11.sp, color = TextGrey)
                        Text(CurrencyUtils.formatVnd(product.basePrice), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FastDashRed)
                    }
                    Surface(modifier = Modifier.size(42.dp).clickable { onAddToCart() }, shape = CircleShape, color = FastDashRed, shadowElevation = 4.dp) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Add, contentDescription = "Thêm vào giỏ", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(category: CategoryResponse, visual: CategoryVisual, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(148.dp).height(168.dp).clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(visual.colors)).padding(14.dp)) {
            Box(Modifier.align(Alignment.TopEnd).size(68.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)))
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.18f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(visual.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(category.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(category.description?.takeIf { it.isNotBlank() } ?: visual.subtitle, color = Color.White.copy(alpha = 0.88f), fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun TodayOfferCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(FastDashRed, FastDashRedDark)))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Ưu đãi hôm nay", color = Color.White.copy(alpha = 0.88f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Combo pizza + nước giảm đến 30%", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 22.sp)
            }
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.16f)) {
                Text(
                    text = "Xem ngay",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuickActionGridItem(action: QuickActionUiModel, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { action.onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(14.dp),
                color = FastDashRed.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(action.icon, contentDescription = null, tint = FastDashRed)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(action.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(action.subtitle, fontSize = 12.sp, color = TextGrey, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun QuickActionItem(action: QuickActionUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { action.onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(14.dp), color = FastDashRed.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) { Icon(action.icon, contentDescription = null, tint = FastDashRed) }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(action.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack)
                Text(action.subtitle, fontSize = 12.sp, color = TextGrey, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGrey)
        }
    }
}

@Composable
private fun SectionHeaderRow(title: String, subtitle: String, actionText: String? = null, onActionClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlack)
            Text(subtitle, fontSize = 13.sp, color = TextGrey)
        }
        if (actionText != null) {
            Text(actionText, modifier = Modifier.clickable { onActionClick() }, color = FastDashRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FastDashBottomNavigation(selectedTab: BottomTab, onSelectTab: (BottomTab) -> Unit) {
    NavigationBar(containerColor = SurfaceWhite, tonalElevation = 10.dp, modifier = Modifier.navigationBarsPadding()) {
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
                    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (isSelected) FastDashRed.copy(alpha = 0.12f) else Color.Transparent).padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Icon(icon, contentDescription = tab.label)
                    }
                },
                label = { Text(tab.label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FastDashRed,
                    selectedTextColor = FastDashRed,
                    unselectedIconColor = TextGrey,
                    unselectedTextColor = TextGrey,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun CartBar(count: Int, total: Double, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { onClick() },
        color = PrimaryBlack,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 7.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = FastDashRed, modifier = Modifier.size(30.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text(count.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Column {
                    Text("Xem giỏ hàng", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Kiểm tra món đã chọn trước khi thanh toán", color = Color.White.copy(alpha = 0.72f), fontSize = 11.sp)
                }
            }
            Text(CurrencyUtils.formatVnd(total), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

private data class PromoBannerUiModel(
    val eyebrow: String,
    val title: String,
    val subtitle: String,
    val price: String,
    val imageUrl: String?,
    val fallbackIcon: ImageVector,
    val startColor: Color,
    val endColor: Color
)

private data class QuickActionUiModel(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

private data class CategoryVisual(
    val icon: ImageVector,
    val subtitle: String,
    val colors: List<Color>
)

@Composable
private fun rememberPromoBanners(products: List<ProductResponse>): List<PromoBannerUiModel> {
    val productImages = products.mapNotNull { it.imageUrl.takeIf { url -> !url.isNullOrBlank() } }
    return remember(products) {
        listOf(
            PromoBannerUiModel("Ưu đãi hôm nay", "Combo ngon mỗi ngày", "Pizza, gà rán, nước uống", "Chỉ từ 79.000đ", productImages.getOrNull(0), Icons.Outlined.RestaurantMenu, FastDashRed, FastDashRedDark),
            PromoBannerUiModel("Giao nhanh 24/7", "Bữa tối trọn vị", "Combo gia đình nóng hổi, giao tận nơi trong ít phút", "Chỉ từ 249.000đ", productImages.getOrNull(1), Icons.Filled.LocalPizza, Color(0xFF1F1F1F), Color(0xFF3A3A3A)),
            PromoBannerUiModel("Đặt nhanh", "Thêm món, thêm vui", "Pizza đế giòn, gà rán vàng ruộm và nước uống mát lạnh", "Chỉ từ 129.000đ", productImages.getOrNull(2), Icons.Outlined.ShoppingBag, Color(0xFFE24A2A), Color(0xFFD6092F))
        )
    }
}

private fun fallbackCategories(): List<CategoryResponse> = listOf(
    CategoryResponse(1L, "Pizza", "Đế giòn, phô mai kéo sợi"),
    CategoryResponse(2L, "Gà rán", "Giòn rụm, đậm vị"),
    CategoryResponse(3L, "Combo", "Tiết kiệm cho nhóm bạn"),
    CategoryResponse(4L, "Nước uống", "Mát lạnh, dễ chọn món")
)

private fun categoryVisual(name: String): CategoryVisual {
    val normalized = name.lowercase()
    return when {
        normalized.contains("pizza") -> CategoryVisual(Icons.Filled.LocalPizza, "Đế giòn, topping đầy đặn", listOf(Color(0xFFE53B36), Color(0xFF8E1F1B)))
        normalized.contains("gà") || normalized.contains("ga") || normalized.contains("fried") -> CategoryVisual(Icons.Outlined.RestaurantMenu, "Giòn rụm, nóng hổi mỗi phần", listOf(Color(0xFFF29B2E), Color(0xFFC15A10)))
        normalized.contains("combo") -> CategoryVisual(Icons.Outlined.ShoppingBag, "Tiện lợi cho nhóm bạn và gia đình", listOf(Color(0xFFD6092F), Color(0xFF2A2A2A)))
        normalized.contains("nước") || normalized.contains("nuoc") || normalized.contains("drink") -> CategoryVisual(Icons.Outlined.Description, "Nước uống mát lạnh dễ kết hợp", listOf(Color(0xFF2F8BCF), Color(0xFF1E4F7A)))
        else -> CategoryVisual(Icons.Outlined.RestaurantMenu, "Món ngon sẵn sàng giao nhanh", listOf(Color(0xFF3C3C3C), Color(0xFFD6092F)))
    }
}

private enum class BottomTab(val label: String) {
    Home("Trang chủ"),
    Menu("Thực đơn"),
    Orders("Đơn hàng"),
    Account("Tài khoản")
}





