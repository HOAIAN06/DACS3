package com.fastdash.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PizzaHutRed = Color(0xFFC8102E)
private val LightGrey = Color(0xFFF4F4F4)
private val PrimaryBlack = Color(0xFF1C1C1C)
private val SurfaceWhite = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    fullName: String,
    email: String,
    phone: String,
    role: String,
    onBack: () -> Unit,
    onOpenOrders: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tài Khoản", fontWeight = FontWeight.ExtraBold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Profile
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceWhite
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = PizzaHutRed.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = PizzaHutRed)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(fullName, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlack)
                    Text(email, fontSize = 14.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Settings Group
            ProfileSectionTitle("CÀI ĐẶT CỦA TÔI")
            ProfileMenuItem(icon = Icons.Outlined.Assignment, title = "Lịch sử đơn hàng", onClick = onOpenOrders)
            ProfileMenuItem(icon = Icons.Outlined.LocationOn, title = "Địa chỉ đã lưu", onClick = { /* TODO */ })
            ProfileMenuItem(icon = Icons.Outlined.Payment, title = "Phương thức thanh toán", onClick = { /* TODO */ })

            Spacer(Modifier.height(24.dp))

            // Account Group
            ProfileSectionTitle("HỖ TRỢ")
            ProfileMenuItem(icon = Icons.Outlined.HelpOutline, title = "Trung tâm trợ giúp", onClick = { /* TODO */ })
            ProfileMenuItem(icon = Icons.Outlined.Info, title = "Điều khoản & Chính sách", onClick = { /* TODO */ })

            Spacer(Modifier.height(32.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PizzaHutRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Đăng xuất", color = PizzaHutRed, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = SurfaceWhite
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = PrimaryBlack)
            Spacer(Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = PrimaryBlack)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
