package com.fastdash.app.ui.order

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastdash.app.utils.CurrencyUtils

private val PizzaHutRed = Color(0xFFC8102E)
private val BackgroundGrey = Color(0xFFF4F4F4)
private val SurfaceWhite = Color.White
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val DividerColor = Color(0xFFE5E7EB)
private val SuccessGreen = Color(0xFF16A34A)

data class OrderItemUiModel(
    val id: Long,
    val name: String,
    val sizeName: String,
    val toppings: List<String>,
    val quantity: Int,
    val unitPrice: Double,
    val note: String = ""
)

data class OrderDetailUiModel(
    val id: Long,
    val orderCode: String,
    val status: String,
    val createdAt: String,
    val receiverName: String,
    val receiverPhone: String,
    val deliveryAddress: String,
    val branchName: String,
    val branchAddress: String,
    val distanceKm: Double?,
    val paymentMethod: String,
    val paymentStatus: String,
    val subtotal: Double,
    val shippingFee: Double,
    val discountAmount: Double,
    val totalAmount: Double,
    val note: String,
    val items: List<OrderItemUiModel>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    order: OrderDetailUiModel,
    onBack: () -> Unit,
    onReorder: (OrderDetailUiModel) -> Unit,
    onCancelOrder: (OrderDetailUiModel) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng".normalizeVietnameseText(), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại".normalizeVietnameseText())
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = BackgroundGrey,
        bottomBar = {
            Surface(
                color = SurfaceWhite,
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (order.status.trim().uppercase() == "PENDING") {
                        OutlinedButton(
                            onClick = { onCancelOrder(order) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PizzaHutRed),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PizzaHutRed)
                        ) {
                            Text("Hủy đơn hàng".normalizeVietnameseText(), fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = { onReorder(order) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PizzaHutRed)
                    ) {
                        Text("Đặt lại đơn này".normalizeVietnameseText(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeaderCard(order)
            }
            item {
                DetailSection("Thông tin giao hàng".normalizeVietnameseText()) {
                    DetailLine(Icons.Outlined.Person, "Người nhận".normalizeVietnameseText(), order.receiverName.ifBlank { "Chưa có thông tin".normalizeVietnameseText() })
                    DetailLine(Icons.Outlined.Phone, "Số điện thoại".normalizeVietnameseText(), order.receiverPhone.ifBlank { "Chưa có thông tin".normalizeVietnameseText() })
                    DetailLine(Icons.Outlined.LocationOn, "Địa chỉ giao hàng".normalizeVietnameseText(), order.deliveryAddress.ifBlank { "Chưa có địa chỉ giao hàng".normalizeVietnameseText() })
                    DetailLine(Icons.Outlined.Storefront, "Cửa hàng phục vụ".normalizeVietnameseText(), order.branchName.ifBlank { "Chưa có thông tin".normalizeVietnameseText() })
                    if (order.branchAddress.isNotBlank()) {
                        DetailLine(Icons.Outlined.Storefront, "Địa chỉ cửa hàng".normalizeVietnameseText(), order.branchAddress)
                    }
                    order.distanceKm?.let {
                        DetailLine(Icons.Outlined.Tag, "Khoảng cách".normalizeVietnameseText(), String.format("%.2f km", it))
                    }
                }
            }
            item {
                DetailSection("Danh sách món".normalizeVietnameseText()) {
                    if (order.items.isEmpty()) {
                        Text("Đơn giao hàng".normalizeVietnameseText(), color = TextSecondary, fontSize = 14.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            order.items.forEachIndexed { index, item ->
                                OrderItemRow(item)
                                if (index < order.items.lastIndex) {
                                    HorizontalDivider(color = DividerColor)
                                }
                            }
                        }
                    }
                }
            }
            item {
                DetailSection("Thanh toán".normalizeVietnameseText()) {
                    DetailTextRow("Phương thức thanh toán".normalizeVietnameseText(), mapPaymentMethod(order.paymentMethod))
                    DetailTextRow("Trạng thái thanh toán".normalizeVietnameseText(), mapPaymentStatus(order.paymentStatus))
                }
            }
            item {
                DetailSection("Chi tiết thanh toán".normalizeVietnameseText()) {
                    DetailPriceRow("Tạm tính".normalizeVietnameseText(), order.subtotal)
                    DetailPriceRow("Phí giao hàng".normalizeVietnameseText(), order.shippingFee)
                    DetailPriceRow("Giảm giá".normalizeVietnameseText(), order.discountAmount)
                    HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
                    DetailPriceRow("Tổng cộng".normalizeVietnameseText(), order.totalAmount, highlight = true)
                }
            }
            if (order.note.isNotBlank()) {
                item {
                    DetailSection("Ghi chú".normalizeVietnameseText()) {
                        Text(
                            order.note,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCard(order: OrderDetailUiModel) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SurfaceWhite
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(order.orderCode.ifBlank { "Không có mã đơn".normalizeVietnameseText() }, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                    Text(formatOrderDate(order.createdAt), color = TextSecondary, fontSize = 13.sp)
                }
                Surface(
                    color = orderStatusColor(order.status).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        mapOrderStatus(order.status),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = orderStatusColor(order.status),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            DetailTextRow("Mã đơn".normalizeVietnameseText(), order.orderCode.ifBlank { "Không có mã đơn".normalizeVietnameseText() })
            DetailTextRow("Trạng thái".normalizeVietnameseText(), mapOrderStatus(order.status))
            DetailTextRow("Ngày đặt".normalizeVietnameseText(), formatOrderDate(order.createdAt))
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SurfaceWhite
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextPrimary)
            content()
        }
    }
}

@Composable
private fun DetailLine(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .background(PizzaHutRed.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = PizzaHutRed)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(value.normalizeVietnameseText(), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun DetailTextRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(
            value.normalizeVietnameseText(),
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DetailPriceRow(label: String, amount: Double, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(
            CurrencyUtils.formatVnd(amount),
            color = if (highlight) PizzaHutRed else TextPrimary,
            fontSize = if (highlight) 16.sp else 14.sp,
            fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.Medium
        )
    }
}

@Composable
private fun OrderItemRow(item: OrderItemUiModel) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.name.normalizeVietnameseText().ifBlank { "Món đã đặt".normalizeVietnameseText() }, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                if (item.sizeName.isNotBlank()) {
                    Text("Size: ${item.sizeName}".normalizeVietnameseText(), color = TextSecondary, fontSize = 13.sp)
                }
                if (item.toppings.isNotEmpty()) {
                    Text(
                        "Topping: ${item.toppings.joinToString(", ")}".normalizeVietnameseText(),
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                Text("Số lượng: ${item.quantity}".normalizeVietnameseText(), color = TextSecondary, fontSize = 13.sp)
                Text("Đơn giá: ${CurrencyUtils.formatVnd(item.unitPrice)}".normalizeVietnameseText(), color = TextSecondary, fontSize = 13.sp)
            }
            Text(
                CurrencyUtils.formatVnd(item.unitPrice * item.quantity),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        if (item.note.isNotBlank()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.StickyNote2, contentDescription = null, tint = TextSecondary)
                Text(item.note.normalizeVietnameseText(), color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
