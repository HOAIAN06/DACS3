package com.fastdash.app.ui.cart

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fastdash.app.data.model.response.CartItemResponse
import com.fastdash.app.ui.order.normalizeVietnameseText
import com.fastdash.app.utils.CurrencyUtils
import com.fastdash.app.utils.ImageUtils

private val FastDashRed = Color(0xFFD6092F)
private val BackgroundGrey = Color(0xFFF7F7F7)
private val PrimaryBlack = Color(0xFF1F1F1F)
private val SurfaceWhite = Color.White
private val TextGrey = Color(0xFF777777)
private val DividerColor = Color(0xFFEEEEEE)
private val SoftGrey = Color(0xFFFAFAFA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItemResponse> = emptyList(),
    subtotal: Double = 0.0,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onBack: () -> Unit = {},
    onUpdateQuantity: (itemId: Long, quantity: Int) -> Unit = { _, _ -> },
    onRemoveItem: (Long) -> Unit = {},
    onCheckout: () -> Unit = {},
    onRetry: () -> Unit = {},
    onBrowseMenu: () -> Unit = onBack
) {
    val safeSubtotal = subtotal.coerceAtLeast(0.0)
    val total = safeSubtotal
    var pendingDeleteItem by remember { mutableStateOf<CartItemResponse?>(null) }

    Scaffold(
        topBar = {
            CartHeader(itemCount = cartItems.sumOf { it.quantity }, onBack = onBack)
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                CheckoutBottomBar(total = total, enabled = cartItems.isNotEmpty(), onCheckout = onCheckout)
            }
        },
        containerColor = BackgroundGrey
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> CartLoadingState()
                errorMessage != null && cartItems.isEmpty() -> CartErrorState(onRetry = onRetry)
                cartItems.isEmpty() -> EmptyCartState(onBrowseMenu = onBrowseMenu)
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(cartItems, key = { it.id }) { item ->
                            CartItemCard(
                                item = item,
                                onIncrease = { onUpdateQuantity(item.id, item.quantity + 1) },
                                onDecrease = { if (item.quantity > 1) onUpdateQuantity(item.id, item.quantity - 1) },
                                onRemoveItem = { pendingDeleteItem = item }
                            )
                        }
                        item { OrderSummaryCard(subtotal = safeSubtotal) }
                    }
                }
            }
        }
    }

    pendingDeleteItem?.let { item ->
        DeleteCartItemDialog(
            productName = item.resolvedProductName.normalizeVietnameseText(),
            onDismiss = { pendingDeleteItem = null },
            onConfirm = {
                onRemoveItem(item.id)
                pendingDeleteItem = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartHeader(itemCount: Int, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text("Giỏ hàng", fontWeight = FontWeight.ExtraBold, color = PrimaryBlack)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = PrimaryBlack)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
    )
}

@Composable
private fun CartItemCard(
    item: CartItemResponse,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemoveItem: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                CartItemImage(imageUrl = item.productImageUrl, contentDescription = item.resolvedProductName)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.resolvedProductName.normalizeVietnameseText(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    item.resolvedSizeName?.normalizeVietnameseText()?.takeIf { it.isNotBlank() }?.let {
                        Text("Size $it", fontSize = 12.sp, color = TextGrey)
                    }
                    if (item.toppings.isNotEmpty()) {
                        Text(
                            "Topping: ${item.toppings.joinToString(", ") { topping -> topping.name.normalizeVietnameseText() }}",
                            fontSize = 12.sp,
                            color = TextGrey,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    item.note?.normalizeVietnameseText()?.takeIf { it.isNotBlank() }?.let {
                        Text("Ghi chú: $it", fontSize = 12.sp, color = TextGrey, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                IconButton(onClick = onRemoveItem, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa món", tint = FastDashRed.copy(alpha = 0.7f))
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                CartQuantitySelector(quantity = item.quantity, onDecrease = onDecrease, onIncrease = onIncrease, canDecrease = item.quantity > 1)
                Text(CurrencyUtils.formatVnd(item.totalPrice), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = FastDashRed)
            }
        }
    }
}

@Composable
private fun CartItemImage(imageUrl: String?, contentDescription: String) {
    Box(
        modifier = Modifier
            .size(width = 92.dp, height = 92.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF3F3F3)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNullOrBlank()) {
            Icon(Icons.Outlined.LocalPizza, contentDescription = null, tint = FastDashRed, modifier = Modifier.size(38.dp))
        } else {
            AsyncImage(
                model = ImageUtils.buildImageRequest(LocalContext.current, imageUrl),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun CartQuantitySelector(quantity: Int, onDecrease: () -> Unit, onIncrease: () -> Unit, canDecrease: Boolean) {
    Surface(shape = RoundedCornerShape(50), color = SoftGrey, border = BorderStroke(1.dp, DividerColor)) {
        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrease, enabled = canDecrease, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Giảm số lượng", tint = if (canDecrease) PrimaryBlack else TextGrey)
            }
            Text(quantity.toString(), modifier = Modifier.padding(horizontal = 10.dp), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack)
            IconButton(onClick = onIncrease, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Tăng số lượng", tint = FastDashRed)
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(subtotal: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null, tint = FastDashRed)
                Text("Chi tiết đơn hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlack)
            }
            SummaryRow("Tạm tính", CurrencyUtils.formatVnd(subtotal), highlight = true)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, highlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = if (highlight) PrimaryBlack else TextGrey, fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal)
        Text(value, fontSize = if (highlight) 18.sp else 14.sp, fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.Medium, color = if (highlight) FastDashRed else PrimaryBlack)
    }
}

@Composable
private fun CheckoutBottomBar(total: Double, enabled: Boolean, onCheckout: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = SurfaceWhite, shadowElevation = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp).navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Tổng thanh toán", fontSize = 12.sp, color = TextGrey)
                Text(CurrencyUtils.formatVnd(total), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = FastDashRed)
            }
            Button(
                onClick = onCheckout,
                enabled = enabled,
                modifier = Modifier.height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FastDashRed, contentColor = Color.White, disabledContainerColor = DividerColor, disabledContentColor = TextGrey),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Text("Thanh toán", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun EmptyCartState(onBrowseMenu: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(modifier = Modifier.size(88.dp), shape = CircleShape, color = FastDashRed.copy(alpha = 0.1f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.LocalPizza, contentDescription = null, tint = FastDashRed, modifier = Modifier.size(42.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Giỏ hàng đang trống", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlack)
        Spacer(Modifier.height(8.dp))
        Text("Thêm món để bắt đầu", color = TextGrey, fontSize = 14.sp)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onBrowseMenu, colors = ButtonDefaults.buttonColors(containerColor = FastDashRed, contentColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Text("Xem thực đơn", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CartLoadingState() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(3) {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(92.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFFEDEDED)))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.fillMaxWidth(0.55f).height(18.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEDEDED)))
                            Box(Modifier.fillMaxWidth(0.35f).height(14.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF3F3F3)))
                            Box(Modifier.fillMaxWidth(0.7f).height(14.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF3F3F3)))
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Box(Modifier.width(110.dp).height(40.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFEDEDED)))
                        Box(Modifier.width(90.dp).height(34.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEDEDED)))
                    }
                }
            }
        }
    }
}

@Composable
private fun CartErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Không thể tải giỏ hàng", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlack)
        Spacer(Modifier.height(8.dp))
        Text("Vui lòng thử lại", fontSize = 14.sp, color = TextGrey)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = FastDashRed, contentColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Text("Thử lại", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DeleteCartItemDialog(productName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xóa món khỏi giỏ?", fontWeight = FontWeight.Bold, color = PrimaryBlack) },
        text = { Text("Bạn có chắc muốn xóa món này không?", color = TextGrey) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = FastDashRed, contentColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                Text("Xóa", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, border = BorderStroke(1.dp, DividerColor), shape = RoundedCornerShape(14.dp)) {
                Text("Hủy", color = PrimaryBlack)
            }
        },
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp)
    )
}



