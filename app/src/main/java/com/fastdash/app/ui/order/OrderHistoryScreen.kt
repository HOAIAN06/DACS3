package com.fastdash.app.ui.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastdash.app.utils.CurrencyUtils

private val PizzaHutRed = Color(0xFFC8102E)
private val LightGrey = Color(0xFFF4F4F4)
private val SurfaceWhite = Color.White
private val PrimaryBlack = Color(0xFF1C1C1C)

data class OrderHistoryUiModel(
    val id: Long,
    val orderCode: String,
    val createdAt: String,
    val itemCount: Int,
    val totalAmount: Double,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    orders: List<OrderHistoryUiModel>,
    onBack: () -> Unit,
    onOpenOrder: (OrderHistoryUiModel) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử đơn hàng", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = LightGrey
    ) { innerPadding ->
        if (orders.isEmpty()) {
            EmptyOrdersState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderCard(order = order, onClick = { onOpenOrder(order) })
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderHistoryUiModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderCode,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = PrimaryBlack
                )
                Surface(
                    color = getStatusColor(order.status).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = order.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = getStatusColor(order.status),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(text = order.createdAt, fontSize = 14.sp, color = Color.Gray)
            Text(text = "${order.itemCount} món", fontSize = 14.sp, color = Color.Gray)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = CurrencyUtils.formatVnd(order.totalAmount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = PizzaHutRed
                )
                Text("Xem chi tiết >", color = PizzaHutRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyOrdersState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bạn chưa có đơn hàng nào", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Thực hiện đơn hàng đầu tiên ngay thôi!", color = Color.Gray, fontSize = 14.sp)
    }
}

private fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "preparing", "processing", "pending", "confirmed" -> Color(0xFFF2994A)
        "delivered", "completed" -> Color(0xFF27AE60)
        "cancelled" -> Color(0xFFEB5757)
        else -> Color.Gray
    }
}
