package com.fastdash.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastdash.app.data.model.response.AdminDashboardSummaryResponse
import com.fastdash.app.data.repository.AdminDashboardRepository
import com.fastdash.app.utils.CurrencyUtils

private val PizzaHutRed = Color(0xFFC8102E)
private val DeepCrust = Color(0xFF24130F)
private val CharcoalSauce = Color(0xFF3A1E18)
private val PrimaryBlack = Color(0xFF1C1C1C)
private val LightBackground = Color(0xFFF6EFE7)
private val SurfaceWhite = Color.White
private val SuccessGreen = Color(0xFF27AE60)
private val WarningGold = Color(0xFFFFB81C)
private val AdminBlue = Color(0xFF0078AE)
private val WarmCream = Color(0xFFFFF6EE)

private enum class AdminModule(val title: String, val ready: Boolean) {
    Products("San pham", true),
    Categories("Danh muc", true),
    Orders("Don hang", true),
    Sizes("Kich thuoc", true),
    Toppings("Topping", true),
    Users("Tai khoan", true),
    Branches("Chi nhanh", true),
    Payments("Thanh toan", true)
}

@Composable
fun AdminDashboardScreen(
    onOpenProducts: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenSizes: () -> Unit,
    onOpenToppings: () -> Unit,
    onOpenUsers: () -> Unit,
    onOpenBranches: () -> Unit,
    onOpenPayments: () -> Unit,
    onOpenPlaceholder: (title: String, subtitle: String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { AdminDashboardRepository(context.applicationContext) }

    var summary by remember { mutableStateOf(AdminDashboardSummaryResponse()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        errorMessage = null
        try {
            val response = repository.getSummary()
            if (response.isSuccessful) {
                summary = response.body() ?: AdminDashboardSummaryResponse()
            } else {
                errorMessage = "Khong tai duoc tong quan: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .verticalScroll(rememberScrollState())
    ) {
        AdminHeader()

        if (loading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PizzaHutRed)
            }
        } else {
            AdminStatsGrid(summary)

            errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                DashboardInfoCard("Loi tai du lieu", it, WarningGold)
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "DIEU HUONG NHANH",
                modifier = Modifier.padding(horizontal = 20.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalSauce.copy(alpha = 0.72f)
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
                        AdminModule.Users -> onOpenUsers()
                        AdminModule.Branches -> onOpenBranches()
                        AdminModule.Payments -> onOpenPayments()
                        else -> onOpenPlaceholder(module.title, "Module admin san sang ket noi backend")
                    }
                }
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PizzaHutRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("DANG XUAT HE THONG", fontWeight = FontWeight.ExtraBold)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun AdminHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DeepCrust, CharcoalSauce, PizzaHutRed)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.14f)
            ) {
                Text(
                    "FASTDASH ADMIN",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmCream
                )
            }
            Text("Godfather Console", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(
                "Theo doi doanh thu, van hanh va dieu huong mon an tren cung mot man hinh.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = WarmCream.copy(alpha = 0.88f)
            )
        }
    }
}

@Composable
private fun AdminStatsGrid(stats: AdminDashboardSummaryResponse) {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(DeepCrust, CharcoalSauce, PizzaHutRed)
                        )
                    )
            ) {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(999.dp), color = Color.White.copy(alpha = 0.12f)) {
                            Text(
                                "TONG DOANH THU",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color = WarmCream,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            CurrencyUtils.formatVnd(stats.totalRevenue),
                            color = WarningGold,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Tap trung vao don dang cho va module ban chay de day toc do xu ly.",
                            color = WarmCream.copy(alpha = 0.82f),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = WarningGold,
                            modifier = Modifier.height(44.dp)
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatSmallCard(Modifier.weight(1f), "Don Hang", stats.totalOrders.toString(), Icons.Default.ShoppingCart, PizzaHutRed)
            StatSmallCard(Modifier.weight(1f), "San Pham", stats.totalProducts.toString(), Icons.Default.LocalPizza, SuccessGreen)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatSmallCard(Modifier.weight(1f), "Nguoi Dung", stats.totalUsers.toString(), Icons.Default.People, WarningGold)
            StatSmallCard(Modifier.weight(1f), "Dang Cho", stats.pendingOrders.toString(), Icons.Default.Payments, AdminBlue)
        }
    }
}

@Composable
private fun StatSmallCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(SurfaceWhite, WarmCream)
                    )
                )
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.height(22.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlack)
            Text(title.uppercase(), fontSize = 11.sp, color = CharcoalSauce.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ModuleGrid(onModuleClick: (AdminModule) -> Unit) {
    val modules = AdminModule.entries
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        modules.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { module ->
                    val accent = when (module) {
                        AdminModule.Products -> PizzaHutRed
                        AdminModule.Categories -> WarningGold
                        AdminModule.Orders -> DeepCrust
                        AdminModule.Sizes -> SuccessGreen
                        AdminModule.Toppings -> AdminBlue
                        AdminModule.Users -> CharcoalSauce
                        AdminModule.Branches -> Color(0xFFCC5C2E)
                        AdminModule.Payments -> Color(0xFF8F3DFF)
                    }
                    val icon = when (module) {
                        AdminModule.Products -> Icons.Default.LocalPizza
                        AdminModule.Categories -> Icons.Default.Storefront
                        AdminModule.Orders -> Icons.Default.ShoppingCart
                        AdminModule.Sizes -> Icons.Default.TrendingUp
                        AdminModule.Toppings -> Icons.Default.Payments
                        AdminModule.Users -> Icons.Default.People
                        AdminModule.Branches -> Icons.Default.Storefront
                        AdminModule.Payments -> Icons.Default.Payments
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { onModuleClick(module) },
                        shape = RoundedCornerShape(22.dp),
                        color = SurfaceWhite,
                        shadowElevation = 0.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.White, accent.copy(alpha = 0.08f))
                                    )
                                )
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(accent.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = accent)
                            }
                            Spacer(Modifier.height(10.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    module.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = PrimaryBlack,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Mo", fontSize = 11.sp, color = accent, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = accent, modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DashboardInfoCard(title: String, subtitle: String, accentColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.08f), SurfaceWhite)
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(shape = RoundedCornerShape(10.dp), color = accentColor.copy(alpha = 0.1f)) {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Text(text = subtitle, color = PrimaryBlack, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}
