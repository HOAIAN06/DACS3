package com.fastdash.app.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastdash.app.data.model.response.CartItemResponse
import com.fastdash.app.utils.CurrencyUtils

private val PizzaHutRed = Color(0xFFC8102E)
private val LightGrey = Color(0xFFF4F4F4)
private val PrimaryBlack = Color(0xFF1C1C1C)
private val SurfaceWhite = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItemResponse> = emptyList(),
    subtotal: Double = 0.0,
    shippingFee: Double = 15000.0,
    onBack: () -> Unit = {},
    onRemoveItem: (Long) -> Unit = {},
    onCheckout: () -> Unit = {}
) {
    val safeSubtotal = subtotal.coerceAtLeast(0.0)
    val safeShippingFee = if (cartItems.isEmpty()) 0.0 else shippingFee.coerceAtLeast(0.0)
    val total = safeSubtotal + safeShippingFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giỏ Hàng", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = SurfaceWhite
                ) {
                    Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tổng thanh toán", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                CurrencyUtils.formatVnd(total),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = PizzaHutRed
                            )
                        }
                        Button(
                            onClick = onCheckout,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PizzaHutRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ĐẶT HÀNG NGAY", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        },
        containerColor = LightGrey
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            EmptyCartContent(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cartItems, key = { it.id }) { item ->
                    CartItemCard(
                        item = item,
                        onRemoveItem = { onRemoveItem(item.id) }
                    )
                }

                item {
                    OrderSummaryCard(
                        subtotal = safeSubtotal,
                        shippingFee = safeShippingFee
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItemResponse,
    onRemoveItem: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Icon Placeholder
            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(8.dp),
                color = LightGrey
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🍕", fontSize = 28.sp)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PrimaryBlack
                )
                Text(
                    "${item.sizeName ?: "Tiêu chuẩn"} x ${item.quantity}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                if (item.toppings.isNotEmpty()) {
                    Text(
                        item.toppings.joinToString(", ") { it.name.orEmpty() }.ifBlank { "Topping" },
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    CurrencyUtils.formatVnd(item.totalPrice),
                    fontWeight = FontWeight.Bold,
                    color = PizzaHutRed,
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onRemoveItem) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = Color.Gray)
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(
    subtotal: Double,
    shippingFee: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Chi tiết đơn hàng", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            SummaryRow("Tạm tính", CurrencyUtils.formatVnd(subtotal))
            SummaryRow("Phí giao hàng", CurrencyUtils.formatVnd(shippingFee))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LightGrey)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tổng cộng", fontWeight = FontWeight.Bold)
                Text(CurrencyUtils.formatVnd(subtotal + shippingFee), fontWeight = FontWeight.ExtraBold, color = PizzaHutRed)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun EmptyCartContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🛒", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("Giỏ hàng của bạn đang trống", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Hãy lấp đầy nó với những món ăn nóng hổi!", color = Color.Gray, fontSize = 14.sp)
    }
}
