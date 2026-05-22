package com.fastdash.app.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FastDashRed = Color(0xFFD6092F)
private val FastDashRedDark = Color(0xFF9B0622)
private val LightGrey = Color(0xFFF7F7F7)
private val PrimaryBlack = Color(0xFF1F1F1F)
private val SurfaceWhite = Color.White
private val TextGrey = Color(0xFF777777)
private val DividerGrey = Color(0xFFEEEEEE)

@Composable
fun ProfileScreen(
    fullName: String,
    email: String,
    phone: String,
    role: String,
    onBack: () -> Unit,
    onOpenOrders: () -> Unit,
    onLogout: () -> Unit,
    isLoggedIn: Boolean = true,
    onLogin: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val displayName = fullName.takeIf { it.isNotBlank() && it != "User" } ?: "Khách hàng FastDash"
    val displayEmail = email.ifBlank { "Chưa cập nhật email" }
    val displayPhone = phone.ifBlank { "Chưa cập nhật số điện thoại" }
    val displayRole = when (role.uppercase()) {
        "ADMIN" -> "Quản trị viên"
        "USER", "" -> "Thành viên FastDash"
        else -> role.lowercase().replaceFirstChar { it.uppercase() }
    }
    val shouldShowGuestState = !isLoggedIn || (fullName == "User" && email.isBlank() && phone.isBlank())

    Box(modifier = Modifier.fillMaxSize().background(LightGrey)) {
        if (shouldShowGuestState) {
            GuestAccountState(onLogin = onLogin)
        } else {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                AccountHeaderCard(name = displayName, email = displayEmail, phone = displayPhone, role = displayRole)
                Spacer(Modifier.height(16.dp))
                AccountSection(title = "Đơn hàng của tôi") {
                    AccountMenuItem(Icons.AutoMirrored.Outlined.Assignment, "Lịch sử đơn hàng", "Xem lại các đơn đã đặt", onOpenOrders)
                    AccountMenuItem(Icons.Outlined.Settings, "Theo dõi đơn hàng", "Cập nhật trạng thái giao hàng mới nhất", onOpenOrders, showDivider = false)
                }
                AccountSection(title = "Cài đặt của tôi") {
                    AccountMenuItem(Icons.Outlined.LocationOn, "Địa chỉ đã lưu", "Quản lý địa điểm nhận hàng", {})
                    AccountMenuItem(Icons.Outlined.CreditCard, "Phương thức thanh toán", "Lựa chọn cách thanh toán phù hợp", {}, showDivider = false)
                }
                AccountSection(title = "Hỗ trợ") {
                    AccountMenuItem(Icons.Outlined.SupportAgent, "Trung tâm trợ giúp", "Liên hệ hỗ trợ và câu hỏi thường gặp", {})
                    AccountMenuItem(Icons.Outlined.Info, "Điều khoản & Chính sách", "Thông tin sử dụng và quyền riêng tư", {}, showDivider = false)
                }
                AccountSection(title = "Thông tin tài khoản") {
                    AccountMenuItem(Icons.Outlined.Phone, "Số điện thoại", displayPhone, {}, enabled = false)
                    AccountMenuItem(Icons.Outlined.Badge, "Vai trò tài khoản", displayRole, {}, enabled = false, showDivider = false)
                }
                LogoutButton(onClick = { showLogoutDialog = true })
                Spacer(Modifier.height(32.dp))
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                confirmButton = {
                    Button(onClick = { showLogoutDialog = false; onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = FastDashRed, contentColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                        Text("Đăng xuất", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showLogoutDialog = false }, border = BorderStroke(1.dp, DividerGrey), shape = RoundedCornerShape(14.dp)) {
                        Text("Hủy", color = PrimaryBlack)
                    }
                },
                title = { Text("Đăng xuất?", fontWeight = FontWeight.Bold, color = PrimaryBlack) },
                text = { Text("Bạn có chắc muốn đăng xuất khỏi FastDash không?", color = TextGrey) },
                containerColor = SurfaceWhite,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
private fun AccountHeaderCard(name: String, email: String, phone: String, role: String) {
    Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(FastDashRed, FastDashRedDark))).padding(horizontal = 16.dp, vertical = 20.dp)) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(78.dp), shape = CircleShape, color = FastDashRed.copy(alpha = 0.12f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = FastDashRed, modifier = Modifier.size(38.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlack, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(email, fontSize = 14.sp, color = TextGrey, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(phone, fontSize = 13.sp, color = TextGrey, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Surface(shape = RoundedCornerShape(50), color = FastDashRed.copy(alpha = 0.08f)) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Badge, contentDescription = null, tint = FastDashRed, modifier = Modifier.size(16.dp))
                        Text(role, color = FastDashRedDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                OutlinedButton(onClick = {}, enabled = false, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, DividerGrey)) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = TextGrey, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Chỉnh sửa", color = TextGrey)
                }
            }
        }
    }
}

@Composable
private fun AccountSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack)
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun AccountMenuItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, enabled: Boolean = true, showDivider: Boolean = true) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { onClick() }.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(14.dp), color = FastDashRed.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = if (enabled) FastDashRed else TextGrey) }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlack)
                Text(subtitle, fontSize = 12.sp, color = TextGrey, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (enabled) Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextGrey)
        }
        if (showDivider) {
            Spacer(Modifier.fillMaxWidth().height(1.dp).background(DividerGrey).padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(52.dp),
        border = BorderStroke(1.dp, FastDashRed.copy(alpha = 0.28f)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = FastDashRed.copy(alpha = 0.06f))
    ) {
        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = FastDashRed)
        Spacer(Modifier.width(10.dp))
        Text("Đăng xuất", color = FastDashRed, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GuestAccountState(onLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(modifier = Modifier.size(88.dp), shape = CircleShape, color = FastDashRed.copy(alpha = 0.12f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = FastDashRed, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Bạn chưa đăng nhập", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlack)
        Spacer(Modifier.height(8.dp))
        Text("Đăng nhập để quản lý đơn hàng, địa chỉ và thông tin tài khoản của bạn.", color = TextGrey, fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onLogin, colors = ButtonDefaults.buttonColors(containerColor = FastDashRed, contentColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Text("Đăng nhập ngay", fontWeight = FontWeight.Bold)
        }
    }
}


