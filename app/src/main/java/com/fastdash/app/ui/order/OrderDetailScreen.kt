package com.fastdash.app.ui.order

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tag
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
import java.util.Locale

private val FastDashRed = Color(0xFFE31837)
private val BackgroundGrey = Color(0xFFF7F7F7)
private val SurfaceWhite = Color.White
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val DividerColor = Color(0xFFE5E7EB)
private val SuccessGreen = Color(0xFF27AE60)

data class OrderItemReviewUiModel(
    val isReviewed: Boolean = false,
    val rating: Int? = null,
    val comment: String = ""
)

data class OrderItemUiModel(
    val id: Long,
    val productId: Long,
    val name: String,
    val productImageUrl: String = "",
    val sizeName: String,
    val toppings: List<String>,
    val quantity: Int,
    val unitPrice: Double,
    val note: String = "",
    val review: OrderItemReviewUiModel? = null
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
    val paymentUrl: String,
    val note: String,
    val items: List<OrderItemUiModel>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    order: OrderDetailUiModel,
    isReviewSectionLoading: Boolean,
    reviewSectionError: String?,
    onRetryReviewSection: () -> Unit,
    onBack: () -> Unit,
    onReorder: (OrderDetailUiModel) -> Unit,
    onCancelOrder: (OrderDetailUiModel) -> Unit,
    onRetryPayment: (OrderDetailUiModel) -> Unit,
    onReview: (OrderItemUiModel) -> Unit
) {
    val showRetryPayment = shouldShowRetryPayment(order)
    val showReviewAction = order.status.trim().uppercase() == "COMPLETED"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = BackgroundGrey,
        bottomBar = {
            Surface(color = SurfaceWhite, shadowElevation = 10.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (showRetryPayment) {
                        Button(
                            onClick = { onRetryPayment(order) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FastDashRed)
                        ) {
                            Text(retryPaymentLabel(order), fontWeight = FontWeight.Bold)
                        }
                    }
                    if (order.status.trim().uppercase() in setOf("PENDING", "PENDING_PAYMENT")) {
                        OutlinedButton(
                            onClick = { onCancelOrder(order) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, FastDashRed),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FastDashRed)
                        ) {
                            Text("Hủy đơn hàng", fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = { onReorder(order) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (showRetryPayment) TextPrimary else FastDashRed)
                    ) {
                        Text("Đặt lại đơn này", fontWeight = FontWeight.Bold)
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
            item { HeaderCard(order) }
            item {
                DetailSection("Thông tin giao hàng") {
                    DetailLine(Icons.Outlined.Person, "Người nhận", order.receiverName.ifBlank { "Chưa có thông tin" })
                    DetailLine(Icons.Outlined.Phone, "Số điện thoại", order.receiverPhone.ifBlank { "Chưa có thông tin" })
                    DetailLine(Icons.Outlined.LocationOn, "Địa chỉ giao hàng", order.deliveryAddress.ifBlank { "Chưa có địa chỉ giao hàng" })
                    DetailLine(Icons.Outlined.Storefront, "Cửa hàng phục vụ", order.branchName.ifBlank { "Chưa có thông tin" })
                    if (order.branchAddress.isNotBlank()) {
                        DetailLine(Icons.Outlined.Storefront, "Địa chỉ cửa hàng", order.branchAddress)
                    }
                    order.distanceKm?.let {
                        DetailLine(Icons.Outlined.Tag, "Khoảng cách", String.format(Locale.getDefault(), "%.2f km", it))
                    }
                }
            }
            item {
                DetailSection("Danh sách món") {
                    if (showReviewAction) {
                        when {
                            isReviewSectionLoading -> {
                                Text(
                                    text = "Đang tải trạng thái đánh giá...",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }

                            !reviewSectionError.isNullOrBlank() -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = reviewSectionError,
                                        color = Color(0xFFDC2626),
                                        fontSize = 13.sp
                                    )
                                    OutlinedButton(
                                        onClick = onRetryReviewSection,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, FastDashRed),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FastDashRed)
                                    ) {
                                        Text("Thử lại", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            else -> {
                                Text(
                                    text = "Bạn có thể viết đánh giá riêng cho từng món trong đơn này.",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    if (order.items.isEmpty()) {
                        Text("Đơn giao hàng", color = TextSecondary, fontSize = 14.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            order.items.forEachIndexed { index, item ->
                                OrderItemRow(
                                    item = item,
                                    showReviewAction = showReviewAction,
                                    onReview = onReview
                                )
                                if (index < order.items.lastIndex) {
                                    HorizontalDivider(color = DividerColor)
                                }
                            }
                        }
                    }
                }
            }
            item {
                DetailSection("Thanh toán") {
                    DetailTextRow("Phương thức thanh toán", mapPaymentMethod(order.paymentMethod))
                    DetailTextRow("Trạng thái thanh toán", mapPaymentStatus(order.paymentStatus))
                }
            }
            item {
                DetailSection("Chi tiết thanh toán") {
                    DetailPriceRow("Tạm tính", order.subtotal)
                    DetailPriceRow("Phí giao hàng", order.shippingFee)
                    DetailPriceRow("Giảm giá", order.discountAmount)
                    HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
                    DetailPriceRow("Tổng cộng", order.totalAmount, highlight = true)
                }
            }
            if (order.note.isNotBlank()) {
                item {
                    DetailSection("Ghi chú") {
                        Text(order.note, color = TextPrimary, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

private fun shouldShowRetryPayment(order: OrderDetailUiModel): Boolean {
    val paymentMethod = order.paymentMethod.trim().uppercase()
    val paymentStatus = order.paymentStatus.trim().uppercase()
    val orderStatus = order.status.trim().uppercase()
    if (paymentMethod != "VNPAY") return false
    return paymentStatus in setOf("PENDING", "UNPAID", "FAILED") || orderStatus in setOf("PENDING_PAYMENT", "PAYMENT_FAILED")
}

private fun retryPaymentLabel(order: OrderDetailUiModel): String {
    val paymentStatus = order.paymentStatus.trim().uppercase()
    val orderStatus = order.status.trim().uppercase()
    return if (paymentStatus == "FAILED" || orderStatus == "PAYMENT_FAILED") "Thanh toán lại" else "Tiếp tục thanh toán"
}

@Composable
private fun HeaderCard(order: OrderDetailUiModel) {
    Surface(shape = RoundedCornerShape(18.dp), color = SurfaceWhite) {
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
                    Text(order.orderCode.ifBlank { "Không có mã đơn" }, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                    Text(formatOrderDate(order.createdAt), color = TextSecondary, fontSize = 13.sp)
                }
                Surface(color = orderStatusColor(order.status).copy(alpha = 0.12f), shape = RoundedCornerShape(999.dp)) {
                    Text(
                        mapOrderStatus(order.status),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = orderStatusColor(order.status),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            DetailTextRow("Mã đơn", order.orderCode.ifBlank { "Không có mã đơn" })
            DetailTextRow("Trạng thái", mapOrderStatus(order.status))
            DetailTextRow("Ngày đặt", formatOrderDate(order.createdAt))
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = SurfaceWhite) {
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
                .background(FastDashRed.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = FastDashRed)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
        Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
            color = if (highlight) FastDashRed else TextPrimary,
            fontSize = if (highlight) 16.sp else 14.sp,
            fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.Medium
        )
    }
}

@Composable
private fun OrderItemRow(
    item: OrderItemUiModel,
    showReviewAction: Boolean,
    onReview: (OrderItemUiModel) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.name.ifBlank { "Món đã đặt" }, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                if (item.sizeName.isNotBlank()) {
                    Text("Size: ${item.sizeName}", color = TextSecondary, fontSize = 13.sp)
                }
                if (item.toppings.isNotEmpty()) {
                    Text("Topping: ${item.toppings.joinToString(", ")}", color = TextSecondary, fontSize = 13.sp)
                }
                Text("Số lượng: ${item.quantity}", color = TextSecondary, fontSize = 13.sp)
                Text("Đơn giá: ${CurrencyUtils.formatVnd(item.unitPrice)}", color = TextSecondary, fontSize = 13.sp)
            }
            Text(CurrencyUtils.formatVnd(item.unitPrice * item.quantity), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        if (item.note.isNotBlank()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.StickyNote2, contentDescription = null, tint = TextSecondary)
                Text(item.note, color = TextSecondary, fontSize = 12.sp)
            }
        }
        if (showReviewAction) {
            ReviewActionRow(item = item, onReview = onReview)
        }
    }
}

@Composable
private fun ReviewActionRow(item: OrderItemUiModel, onReview: (OrderItemUiModel) -> Unit) {
    val review = item.review
    if (review?.isReviewed == true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGrey, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(color = SuccessGreen.copy(alpha = 0.12f), shape = RoundedCornerShape(999.dp)) {
                Text(
                    text = "Đã đánh giá",
                    color = SuccessGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            review.rating?.let { rating ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Bạn đã đánh giá $rating/5 sao", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF4A622), modifier = Modifier.size(16.dp))
                }
            }
            if (review.comment.isNotBlank()) {
                Text(review.comment, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    } else {
        Button(
            onClick = { onReview(item) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FastDashRed)
        ) {
            Text("Viết đánh giá", fontWeight = FontWeight.Bold)
        }
    }
}
