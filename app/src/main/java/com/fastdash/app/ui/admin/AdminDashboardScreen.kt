package com.fastdash.app.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastdash.app.R
import com.fastdash.app.data.model.response.OrderResponse
import com.fastdash.app.data.repository.CategoryRepository
import com.fastdash.app.data.repository.OrderRepository
import com.fastdash.app.data.repository.ProductRepository
import com.fastdash.app.utils.CurrencyUtils

// Pizza Hut Style Admin Colors
private val PizzaHutRed = Color(0xFFC8102E)
private val PrimaryBlack = Color(0xFF1C1C1C)
private val LightBackground = Color(0xFFF8F8F8)
private val SurfaceWhite = Color.White
private val SuccessGreen = Color(0xFF27AE60)
private val WarningGold = Color(0xFFFFB81C)

private data class AdminDashboardStats(
    val totalOrders: Int = 0,
    val revenue: Double = 0.0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
    val products: Int = 0,
    val categories: Int = 0,
    val loading: Boolean = false,
    val errorMessage: String? = null
)

private enum class AdminModule(
    val title: String,
    val emoji: String,
    val ready: Boolean
) {
    Products("Sản phẩm", "🍕", true),
    Categories("Danh mục", "🗂️", true),
    Orders("Đơn hàng", "📦", true),
    Sizes("Kích thước", "📏", true),
    Toppings("Topping", "🧀", true),
    Users("Tài khoản", "👥", false),
    Branches("Chi nhánh", "🏪", false),
    Payments("Thanh toán", "💳", false),
    Statistics("Thống kê", "📊", true)
}

@Composable
fun AdminDashboardScreen(
    onOpenProducts: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenSizes: () -> Unit,
    onOpenToppings: () -> Unit,
    onOpenPlaceholder: (title: String, subtitle: String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val productRepository = remember { ProductRepository(context.applicationContext) }
    val categoryRepository = remember { CategoryRepository(context.applicationContext) }
    val orderRepository = remember { OrderRepository(context.applicationContext) }

    var stats by remember { mutableStateOf(AdminDashboardStats(loading = true)) }

    suspend fun loadDashboardStats() {
        val productsResponse = productRepository.getProducts()
        if (!productsResponse.isSuccessful) {
            val serverError = productsResponse.errorBody()?.string().orEmpty()
            throw IllegalStateException(
                "Không tải được danh sách sản phẩm (${productsResponse.code()})${if (serverError.isNotBlank()) ": $serverError" else ""}"
            )
        }

        val categoriesResponse = categoryRepository.getCategories()
        if (!categoriesResponse.isSuccessful) {
            val serverError = categoriesResponse.errorBody()?.string().orEmpty()
            throw IllegalStateException(
                "Không tải được danh mục (${categoriesResponse.code()})${if (serverError.isNotBlank()) ": $serverError" else ""}"
            )
        }

        val ordersResponse = orderRepository.getOrders()
        if (!ordersResponse.isSuccessful) {
            val serverError = ordersResponse.errorBody()?.string().orEmpty()
            throw IllegalStateException(
                "Không tải được đơn hàng (${ordersResponse.code()})${if (serverError.isNotBlank()) ": $serverError" else ""}"
            )
        }

        val products = productsResponse.body().orEmpty()
        val categories = categoriesResponse.body().orEmpty()
        val orders = ordersResponse.body().orEmpty()

        stats = AdminDashboardStats(
            totalOrders = orders.size,
            revenue = orders.sumOf { it.totalAmount },
            pendingOrders = orders.count { it.status.isPendingStatus() },
            completedOrders = orders.count { it.status.isCompletedStatus() },
            products = products.size,
            categories = categories.size,
            loading = false,
            errorMessage = null
        )
    }

    LaunchedEffect(Unit) {
        try {
            loadDashboardStats()
        } catch (e: Exception) {
            stats = stats.copy(loading = false, errorMessage = e.message)
        }
    }

    Scaffold(
        containerColor = LightBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Admin Header
            AdminHeader()

            if (stats.loading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PizzaHutRed)
                }
            } else {
                // Key Stats Grid
                AdminStatsGrid(stats)

                if (stats.errorMessage != null) {
                    Spacer(Modifier.height(12.dp))
                    DashboardInfoCard(
                        title = "Lỗi tải dữ liệu",
                        subtitle = stats.errorMessage!!,
                        accentColor = WarningGold
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Module Grid
                Text(
                    "QUẢN LÝ HỆ THỐNG",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                
                Spacer(Modifier.height(12.dp))

                ModuleGrid(
                    onModuleClick = { module ->
                        when (module) {
                            AdminModule.Products -> onOpenProducts()
                            AdminModule.Categories -> onOpenCategories()
                            AdminModule.Orders -> onOpenOrders()
                            AdminModule.Sizes -> onOpenSizes()
                            AdminModule.Toppings -> onOpenToppings()
                            else -> onOpenPlaceholder(module.title, "Chức năng đang phát triển")
                        }
                    }
                )

                Spacer(Modifier.height(32.dp))

                // Logout Action
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PizzaHutRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ĐĂNG XUẤT HỆ THỐNG", fontWeight = FontWeight.ExtraBold)
                }
                
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

private fun String.isPendingStatus(): Boolean {
    val normalized = trim().uppercase()
    return normalized == "PENDING" || normalized == "CONFIRMED" || normalized == "PREPARING" || normalized == "DELIVERING"
}

private fun String.isCompletedStatus(): Boolean {
    return trim().uppercase() == "COMPLETED"
}

@Composable
private fun AdminHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "TRANG QUẢN TRỊ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PizzaHutRed
                )
                Text(
                    "Hệ Thống FastDash",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryBlack
                )
            }
            Image(
                painter = painterResource(id = R.drawable.logo2),
                contentDescription = "Logo",
                modifier = Modifier.height(30.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun AdminStatsGrid(stats: AdminDashboardStats) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Revenue Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlack)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tổng Doanh Thu", color = Color.LightGray, fontSize = 14.sp)
                    Text(
                        CurrencyUtils.formatVnd(stats.revenue),
                        color = WarningGold,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = WarningGold,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Secondary Stats Row
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatSmallCard(
                modifier = Modifier.weight(1f),
                title = "Đơn Hàng",
                value = stats.totalOrders.toString(),
                icon = Icons.Default.ShoppingCart,
                color = PizzaHutRed
            )
            StatSmallCard(
                modifier = Modifier.weight(1f),
                title = "Sản Phẩm",
                value = stats.products.toString(),
                icon = Icons.Default.LocalPizza,
                color = SuccessGreen
            )
        }
    }
}

@Composable
private fun StatSmallCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlack)
            Text(title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun ModuleGrid(onModuleClick: (AdminModule) -> Unit) {
    val modules = AdminModule.values()
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        modules.toList().chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { module ->
                    ModuleItem(
                        modifier = Modifier.weight(1f),
                        module = module,
                        onClick = { if (module.ready) onModuleClick(module) }
                    )
                }
                // Fill empty slots if row has < 3 items
                repeat(3 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ModuleItem(modifier: Modifier, module: AdminModule, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        shadowElevation = 1.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (module.ready) PizzaHutRed.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(module.emoji, fontSize = 24.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                module.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (module.ready) PrimaryBlack else Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!module.ready) {
                Text("Sắp có", fontSize = 9.sp, color = PizzaHutRed)
            }
        }
    }
}

@Composable
private fun DashboardInfoCard(title: String, subtitle: String, accentColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Text(
                text = subtitle,
                color = PrimaryBlack,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

