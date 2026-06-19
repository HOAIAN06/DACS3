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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPizza
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
private val FastDashBrandRed = Color(0xFFE31837)
private val FastDashBrandBlue = Color(0xFF1565C0)
private val LightGrey = Color(0xFFF7F7F7)
private val PrimaryBlack = Color(0xFF1F1F1F)
private val SurfaceWhite = Color.White
private val TextGrey = Color(0xFF777777)
private val UtilityTitleColor = Color(0xFF1F2937)
private val UtilitySubtitleColor = Color(0xFF6B7280)
private val UtilityIconBackground = Color(0xFFFFEEF2)

@Composable
fun HomeScreen(
    onOpenProduct: (ProductResponse) -> Unit = {},
    onCheckout: () -> Unit = {},
    onAddToCart: (ProductResponse) -> Unit = onOpenProduct,
    onOpenEditProfile: () -> Unit = {},
    orders: List<OrderHistoryUiModel> = emptyList(),
    onOpenOrder: (OrderHistoryUiModel) -> Unit = {},
    profileFullName: String = "Khách hàng FastDash",
    profileEmail: String = "customer@fastdash.com",
    profilePhone: String = "0123456789",
    profileRole: String = "USER",
    deliveryAddress: String = "",
    onLogout: () -> Unit = {},
    isLoggedIn: Boolean = true,
    onOrdersTabSelected: () -> Unit = {},
    onAccountTabSelected: () -> Unit = {},
    onAiClick: () -> Unit = {},
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
    LaunchedEffect(selectedTab, isLoggedIn) {
        if (!isLoggedIn) return@LaunchedEffect
        when (selectedTab) {
            BottomTab.Orders -> onOrdersTabSelected()
            BottomTab.Account -> onAccountTabSelected()
            else -> Unit
        }
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
                HomeHeader(
                    cartCount = cartCount,
                    onCartClick = onCheckout,
                    onProfileClick = { selectedTab = BottomTab.Account }
                )
            }
        },
        bottomBar = {
            Column {
                if (cartCount > 0 && (selectedTab == BottomTab.Home || selectedTab == BottomTab.Menu)) {
                    CartBar(count = cartCount, total = cartTotal, onClick = onCheckout)
                }
                FastDashBottomNavigation(
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it },
                    onAiClick = onAiClick
                )
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
                    hasFloatingCart = cartCount > 0,
                    onOpenAiAssistant = onAiClick,
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
                    onEditProfile = onOpenEditProfile,
                    onLogout = onLogout,
                    isLoggedIn = isLoggedIn
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
    hasFloatingCart: Boolean,
    onOpenAiAssistant: () -> Unit,
    onOpenProduct: (ProductResponse) -> Unit,
    onAddToCart: (ProductResponse) -> Unit,
    onSelectCategory: (CategoryResponse) -> Unit,
    onOpenMenu: () -> Unit,
    onOpenOrders: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = if (hasFloatingCart) 196.dp else 132.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { PromoBanner(banners = rememberPromoBanners(products), onPrimaryClick = { products.firstOrNull()?.let(onOpenProduct) ?: onOpenMenu() }) }
        item { RecommendedSection(products = products, onOpenProduct = onOpenProduct, onAddToCart = onAddToCart) }
        item { CategorySection(categories = categories, onOpenMenu = onOpenMenu, onSelectCategory = onSelectCategory) }
        item { HomeUtilitiesSection(onOpenOrders = onOpenOrders, onOpenAiAssistant = onOpenAiAssistant) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeHeader(
    cartCount: Int,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    androidx.compose.material3.TopAppBar(
        title = {
            FastDashLogo(modifier = Modifier.padding(start = 4.dp))
        },
        actions = {
            Row(
                modifier = Modifier.padding(end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderActionButton(
                    onClick = onProfileClick,
                    icon = Icons.Filled.Person,
                    contentDescription = "Tài khoản"
                )
                Surface(shape = CircleShape, color = SurfaceWhite, shadowElevation = 6.dp) {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(containerColor = FastDashBrandRed, contentColor = Color.White) {
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
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFFBFC))
    )
}

@Composable
private fun HeaderActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String
) {
    Surface(shape = CircleShape, color = SurfaceWhite, shadowElevation = 6.dp) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = PrimaryBlack,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun FastDashLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Biểu tượng F cách điệu với viền kép và gradient
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(FastDashBrandRed, Color(0xFFB71C1C))
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "F",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Chữ FastDash với kerning (giãn cách) rộng và phong cách thể thao/tốc độ
        Text(
            text = buildAnnotatedString {
                withStyle(style = androidx.compose.ui.text.SpanStyle(
                    color = PrimaryBlack,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )) {
                    append("FAST")
                }
                withStyle(style = androidx.compose.ui.text.SpanStyle(
                    color = FastDashBrandRed,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )) {
                    append("DASH")
                }
            },
            fontSize = 19.sp,
            maxLines = 1
        )
    }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clickable { onPrimaryClick() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = banner.drawableRes),
                    contentDescription = banner.contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
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
private fun RecommendedSection(
    products: List<ProductResponse>,
    onOpenProduct: (ProductResponse) -> Unit,
    onAddToCart: (ProductResponse) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StylizedSectionHeader(
            title = "Bạn sẽ thích",
        )
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
        SectionHeaderRow(title = "Thực đơn", actionText = "Xem thêm", onActionClick = onOpenMenu)
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
private fun HomeUtilitiesSection(
    onOpenOrders: () -> Unit,
    onOpenNearbyStores: () -> Unit = {},
    onOpenAiAssistant: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TodayOfferCard()
        SectionHeaderRow(title = "Tiện ích cho bạn")
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TrackingOrderCard(
                title = "Theo dõi đơn",
                subtitle = "Cập nhật trạng thái giao hàng",
                buttonText = "Xem ngay",
                onClick = onOpenOrders
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                UtilitySmallCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Storefront,
                    title = "Cửa hàng gần bạn",
                    subtitle = "Tìm FastDash gần nhất",
                    onClick = onOpenNearbyStores
                )
                UtilitySmallCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    title = "Trợ lý AI",
                    subtitle = "Gợi ý món phù hợp",
                    onClick = onOpenAiAssistant
                )
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
                Text(product.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack, maxLines = 2, overflow = TextOverflow.Ellipsis)
                product.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, fontSize = 12.sp, color = TextGrey, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(CurrencyUtils.formatVnd(product.basePrice), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FastDashRed)
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
        modifier = Modifier
            .width(136.dp)
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Ảnh hoặc Nền Gradient phủ toàn bộ Card
            if (category.imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(visual.colors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                AsyncImage(
                    model = ImageUtils.buildImageRequest(LocalContext.current, category.imageUrl),
                    contentDescription = category.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Lớp phủ Gradient đen mờ ở dưới để nổi bật chữ trắng
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 100f
                        )
                    )
            )

            // Chữ hiển thị đè lên trên ảnh ở góc dưới
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = category.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                category.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.deal),
            contentDescription = "Ưu đãi hôm nay",
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun TrackingOrderCard(
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(18.dp),
                color = UtilityIconBackground
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Assignment,
                        contentDescription = null,
                        tint = FastDashRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = UtilityTitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = UtilitySubtitleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FastDashRed),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = buttonText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun UtilitySmallCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = UtilityIconBackground
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = FastDashRed)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = UtilityTitleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = UtilitySubtitleColor,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StylizedSectionHeader(title: String, subtitle: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.LocalPizza,
            contentDescription = null,
            tint = PrimaryBlack,
            modifier = Modifier.size(28.dp)
        )
        
        Text(
            text = title.uppercase(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = PrimaryBlack,
            letterSpacing = 0.5.sp
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color.LightGray.copy(alpha = 0.6f))
        )
    }
}

@Composable
private fun SectionHeaderRow(title: String, actionText: String? = null, onActionClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlack)
        }
        if (actionText != null) {
            Text(actionText, modifier = Modifier.clickable { onActionClick() }, color = FastDashRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FastDashBottomNavigation(
    selectedTab: BottomTab,
    onSelectTab: (BottomTab) -> Unit,
    onAiClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = SurfaceWhite,
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BottomNavItem(
                    tab = BottomTab.Home,
                    isSelected = selectedTab == BottomTab.Home,
                    onClick = { onSelectTab(BottomTab.Home) }
                )
                BottomNavItem(
                    tab = BottomTab.Menu,
                    isSelected = selectedTab == BottomTab.Menu,
                    onClick = { onSelectTab(BottomTab.Menu) }
                )
                Spacer(modifier = Modifier.width(72.dp))
                BottomNavItem(
                    tab = BottomTab.Orders,
                    isSelected = selectedTab == BottomTab.Orders,
                    onClick = { onSelectTab(BottomTab.Orders) }
                )
                BottomNavItem(
                    tab = BottomTab.Account,
                    isSelected = selectedTab == BottomTab.Account,
                    onClick = { onSelectTab(BottomTab.Account) }
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-30).dp)
                .size(76.dp)
                .clickable { onAiClick() },
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .background(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                FastDashBrandRed,
                                Color(0xFF6200EE),
                                FastDashBrandBlue,
                                Color(0xFF03DAC6),
                                FastDashBrandRed
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(3.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Hiệu ứng phát sáng nhẹ bên trong
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(FastDashBrandRed.copy(alpha = 0.15f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.logo_ai),
                    contentDescription = "Trợ lý món ăn",
                    modifier = Modifier.size(38.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: BottomTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (tab) {
        BottomTab.Home -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
        BottomTab.Menu -> if (isSelected) Icons.Filled.LocalPizza else Icons.Outlined.LocalPizza
        BottomTab.Orders -> if (isSelected) Icons.AutoMirrored.Filled.Assignment else Icons.AutoMirrored.Outlined.Assignment
        BottomTab.Account -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
    }

    Column(
        modifier = Modifier
            .width(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) FastDashRed.copy(alpha = 0.12f) else Color.Transparent)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tab.label,
                tint = if (isSelected) FastDashRed else TextGrey
            )
        }
        Text(
            text = tab.label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) FastDashRed else TextGrey
        )
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
                Text("Giỏ hàng", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Text(CurrencyUtils.formatVnd(total), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

private data class PromoBannerUiModel(
    val drawableRes: Int,
    val contentDescription: String
)

private data class CategoryVisual(
    val icon: ImageVector,
    val colors: List<Color>
)

@Composable
private fun rememberPromoBanners(products: List<ProductResponse>): List<PromoBannerUiModel> {
    return remember(products) {
        listOf(
            PromoBannerUiModel(R.drawable.pannel1, "Khuyến mãi 1"),
            PromoBannerUiModel(R.drawable.pannel2, "Khuyến mãi 2"),
            PromoBannerUiModel(R.drawable.panel3, "Khuyến mãi 3"),
            PromoBannerUiModel(R.drawable.pannel4, "Khuyến mãi 4"),
            PromoBannerUiModel(R.drawable.pannel5, "Khuyến mãi 5")
        )
    }
}

private fun fallbackCategories(): List<CategoryResponse> = listOf(
    CategoryResponse(1L, "Pizza", "Đế giòn, phô mai kéo sợi", null),
    CategoryResponse(2L, "Gà rán", "Giòn rụm, đậm vị", null),
    CategoryResponse(3L, "Combo", "Tiết kiệm cho nhóm bạn", null),
    CategoryResponse(4L, "Nước uống", "Mát lạnh, dễ chọn món", null)
)

private fun categoryVisual(name: String): CategoryVisual {
    val normalized = name.lowercase()
    return when {
        normalized.contains("pizza") -> CategoryVisual(Icons.Filled.LocalPizza, listOf(Color(0xFFE53B36), Color(0xFF8E1F1B)))
        normalized.contains("gà") || normalized.contains("ga") || normalized.contains("fried") -> CategoryVisual(Icons.Outlined.RestaurantMenu, listOf(Color(0xFFF29B2E), Color(0xFFC15A10)))
        normalized.contains("combo") -> CategoryVisual(Icons.Outlined.ShoppingBag, listOf(Color(0xFFD6092F), Color(0xFF2A2A2A)))
        normalized.contains("nước") || normalized.contains("nuoc") || normalized.contains("drink") -> CategoryVisual(Icons.Outlined.Description, listOf(Color(0xFF2F8BCF), Color(0xFF1E4F7A)))
        else -> CategoryVisual(Icons.Outlined.RestaurantMenu, listOf(Color(0xFF3C3C3C), Color(0xFFD6092F)))
    }
}

private enum class BottomTab(val label: String) {
    Home("Trang chủ"),
    Menu("Thực đơn"),
    Orders("Đơn hàng"),
    Account("Tài khoản")
}
