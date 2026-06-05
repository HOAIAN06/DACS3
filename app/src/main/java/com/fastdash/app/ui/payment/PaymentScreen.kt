package com.fastdash.app.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastdash.app.utils.CurrencyUtils

private val FastDashRed = Color(0xFFC8102E)
private val SurfaceWhite = Color.White
private val BackgroundGrey = Color(0xFFF8F8F8)
private val TextGrey = Color(0xFF6B7280)
private val SuccessGreen = Color(0xFF1B8B4B)
private val WarningAmber = Color(0xFFD9822B)
private val ErrorRed = Color(0xFFC23030)

enum class PaymentScreenState {
    OPENING,
    CHECKING,
    PENDING,
    PAID,
    FAILED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    amount: Double,
    orderCode: String?,
    state: PaymentScreenState,
    message: String? = null,
    isPrimaryLoading: Boolean = false,
    isSecondaryLoading: Boolean = false,
    onBack: () -> Unit,
    onOpenPayment: () -> Unit,
    onCheckAgain: () -> Unit,
    onOpenOrderDetail: () -> Unit
) {
    val title = when (state) {
        PaymentScreenState.OPENING -> "Đang mở cổng thanh toán VNPAY..."
        PaymentScreenState.CHECKING -> "Đang kiểm tra trạng thái thanh toán..."
        PaymentScreenState.PENDING -> "Thanh toán chưa hoàn tất"
        PaymentScreenState.PAID -> "Thanh toán thành công"
        PaymentScreenState.FAILED -> "Thanh toán thất bại"
    }
    val description = when (state) {
        PaymentScreenState.OPENING -> "Đang chuyển sang VNPAY."
        PaymentScreenState.CHECKING -> "Đang xác nhận từ hệ thống đơn hàng."
        PaymentScreenState.PENDING -> "Đơn hàng vẫn đang chờ thanh toán."
        PaymentScreenState.PAID -> "Đơn hàng đang chờ cửa hàng xác nhận."
        PaymentScreenState.FAILED -> "Giao dịch chưa thành công."
    }
    val accentColor = when (state) {
        PaymentScreenState.OPENING, PaymentScreenState.CHECKING -> FastDashRed
        PaymentScreenState.PENDING -> WarningAmber
        PaymentScreenState.PAID -> SuccessGreen
        PaymentScreenState.FAILED -> ErrorRed
    }
    val icon = when (state) {
        PaymentScreenState.OPENING -> Icons.Default.OpenInBrowser
        PaymentScreenState.CHECKING -> Icons.Default.Sync
        PaymentScreenState.PENDING -> Icons.Default.HourglassEmpty
        PaymentScreenState.PAID -> Icons.Default.CheckCircle
        PaymentScreenState.FAILED -> Icons.Default.Warning
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán VNPAY", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = BackgroundGrey
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceWhite,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PaymentStatusIcon(icon = icon, color = accentColor, loading = state == PaymentScreenState.OPENING || state == PaymentScreenState.CHECKING)
                    Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, textAlign = TextAlign.Center)
                    Text(description, color = TextGrey, fontSize = 14.sp, textAlign = TextAlign.Center)
                    orderCode?.takeIf { it.isNotBlank() }?.let {
                        Text("Mã đơn: $it", fontWeight = FontWeight.SemiBold, color = Color.Black)
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accentColor.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Tổng thanh toán", fontSize = 12.sp, color = TextGrey, fontWeight = FontWeight.Bold)
                            Text(
                                CurrencyUtils.formatVnd(amount),
                                fontSize = 30.sp,
                                color = accentColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    message?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = TextGrey, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (state) {
                    PaymentScreenState.OPENING -> {
                        LoadingButton(text = "Mở VNPAY", loading = isPrimaryLoading, onClick = onOpenPayment)
                    }
                    PaymentScreenState.CHECKING -> {
                        LoadingButton(text = "Kiểm tra lại", loading = isPrimaryLoading, onClick = onCheckAgain)
                        OutlinedActionButton(text = "Mở lại VNPAY", loading = isSecondaryLoading, onClick = onOpenPayment)
                    }
                    PaymentScreenState.PENDING -> {
                        LoadingButton(text = "Kiểm tra lại", loading = isPrimaryLoading, onClick = onCheckAgain)
                        OutlinedActionButton(text = "Thanh toán lại", loading = isSecondaryLoading, onClick = onOpenPayment)
                    }
                    PaymentScreenState.PAID -> {
                        Button(
                            onClick = onOpenOrderDetail,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Text("XEM CHI TIẾT ĐƠN HÀNG", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    PaymentScreenState.FAILED -> {
                        LoadingButton(text = "Thử thanh toán lại", loading = isSecondaryLoading, onClick = onOpenPayment)
                        OutlinedActionButton(text = "Kiểm tra lại", loading = isPrimaryLoading, onClick = onCheckAgain)
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusIcon(icon: ImageVector, color: Color, loading: Boolean) {
    Box(contentAlignment = Alignment.Center) {
        if (loading) {
            CircularProgressIndicator(color = color, strokeWidth = 3.dp)
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun LoadingButton(text: String, loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = FastDashRed)
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.height(18.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun OutlinedActionButton(text: String, loading: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.height(18.dp), color = FastDashRed, strokeWidth = 2.dp)
            }
            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
