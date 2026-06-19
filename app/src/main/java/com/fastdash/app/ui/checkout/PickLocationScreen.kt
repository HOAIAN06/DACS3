package com.fastdash.app.ui.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val FastDashRed = Color(0xFFC8102E)
private val ScreenBg = Color(0xFFF8F9FA)
private val CardBorder = Color(0xFFE9ECEF)
private val MutedText = Color(0xFF6C757D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickLocationScreen(
    currentLocation: PickedLocation?,
    selectedLocation: PickedLocation?,
    loadingCurrentLocation: Boolean,
    onBack: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onConfirm: (PickedLocation) -> Unit
) {
    val activeLocation = selectedLocation ?: currentLocation

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn vị trí giao hàng", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = ScreenBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFEFF3F6),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = FastDashRed,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Chọn vị trí trên bản đồ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Chọn vị trí giao hàng rồi xác nhận để tiếp tục.",
                            color = MutedText,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = FastDashRed, modifier = Modifier.size(16.dp))
                        Text("Vị trí này sẽ được dùng để tính phí giao hàng.", color = MutedText, fontSize = 12.sp)
                    }
                    HorizontalDivider(color = CardBorder)
                    Text("Vị trí đang chọn", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        activeLocation?.address ?: "Chưa có vị trí giao hàng",
                        color = if (activeLocation == null) MutedText else Color.Black,
                        fontSize = 14.sp
                    )
                    if (activeLocation != null) {
                        Text(
                            "Tọa độ: ${String.format(Locale.US, "%.4f", activeLocation.latitude)}, ${String.format(Locale.US, "%.4f", activeLocation.longitude)}",
                            color = MutedText,
                            fontSize = 12.sp
                        )
                    }
                    if (currentLocation != null) {
                        Text(
                            "Vị trí hiện tại: ${currentLocation.address}",
                            color = MutedText,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { onConfirm(currentLocation) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onUseCurrentLocation,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (loadingCurrentLocation) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Lấy vị trí hiện tại")
                    }
                }
                Button(
                    onClick = {
                        activeLocation?.let(onConfirm)
                    },
                    enabled = activeLocation != null,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = FastDashRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Xác nhận vị trí")
                }
            }
        }
    }
}
