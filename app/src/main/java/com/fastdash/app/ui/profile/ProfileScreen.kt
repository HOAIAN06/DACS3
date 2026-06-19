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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastdash.app.ui.auth.FastDashCanvas
import com.fastdash.app.ui.auth.FastDashInk
import com.fastdash.app.ui.auth.FastDashLine
import com.fastdash.app.ui.auth.FastDashMuted
import com.fastdash.app.ui.auth.FastDashOrange
import com.fastdash.app.ui.auth.FastDashRed
import com.fastdash.app.ui.auth.FastDashRedDark
import com.fastdash.app.ui.auth.FastDashSurface
import com.fastdash.app.ui.auth.FastDashSurfaceAlt
import com.fastdash.app.ui.auth.FastDashWhite

@Composable
fun ProfileScreen(
    fullName: String,
    email: String,
    phone: String,
    role: String,
    onBack: () -> Unit,
    onOpenOrders: () -> Unit,
    onEditProfile: () -> Unit,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(FastDashCanvas, Color(0xFFFFF7F0), FastDashCanvas)
                )
            )
    ) {
        if (shouldShowGuestState) {
            GuestAccountState(onLogin = onLogin)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AccountHeroCard(
                    name = displayName,
                    email = displayEmail,
                    phone = displayPhone,
                    role = displayRole,
                    onEditProfile = onEditProfile
                )
                Spacer(modifier = Modifier.height(16.dp))
                AccountSection(title = "Đơn hàng của tôi") {
                    AccountMenuItem(Icons.AutoMirrored.Outlined.Assignment, "Lịch sử đơn hàng", onOpenOrders)
                    AccountMenuItem(Icons.Outlined.Home, "Theo dõi đơn hàng", onOpenOrders, showDivider = false)
                }
                AccountSection(title = "Cài đặt") {
                    AccountMenuItem(Icons.Outlined.LocationOn, "Địa chỉ đã lưu", {}, showDivider = false)
                }
                AccountSection(title = "Hỗ trợ") {
                    AccountMenuItem(Icons.Outlined.SupportAgent, "Trung tâm trợ giúp", {})
                    AccountMenuItem(Icons.Outlined.Info, "Điều khoản và chính sách", {}, showDivider = false)
                }
                AccountSection(title = "Thông tin tài khoản") {
                    AccountMenuItem(Icons.Outlined.Phone, "Số điện thoại", {}, displayPhone, enabled = false)
                    AccountMenuItem(Icons.Outlined.Badge, "Vai trò tài khoản", {}, displayRole, enabled = false, showDivider = false)
                }
                LogoutButton(onClick = { showLogoutDialog = true })
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FastDashRed,
                            contentColor = FastDashWhite
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Đăng xuất", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showLogoutDialog = false },
                        border = BorderStroke(1.dp, FastDashLine),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Ở lại", color = FastDashInk)
                    }
                },
                title = {
                    Text(
                        text = "Đăng xuất khỏi tài khoản?",
                        color = FastDashInk,
                        fontWeight = FontWeight.Black
                    )
                },
                text = {
                    Text(
                        text = "Bạn sẽ cần đăng nhập lại để xem đơn hàng và thông tin tài khoản.",
                        color = FastDashMuted
                    )
                },
                containerColor = FastDashSurface,
                shape = RoundedCornerShape(28.dp)
            )
        }
    }
}

@Composable
private fun AccountHeroCard(
    name: String,
    email: String,
    phone: String,
    role: String,
    onEditProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FastDashRedDark, FastDashRed, FastDashOrange)
                )
            )
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = FastDashSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(82.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(FastDashSurfaceAlt, Color(0xFFFFD6A8))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = FastDashRed,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif,
                            color = FastDashInk,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = email,
                            fontSize = 14.sp,
                            color = FastDashMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = phone,
                            fontSize = 13.sp,
                            color = FastDashMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileBadge(
                        icon = Icons.Outlined.Badge,
                        text = role,
                        accent = FastDashRed
                    )
                }

                OutlinedButton(
                    onClick = onEditProfile,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, FastDashLine)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = FastDashInk,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chỉnh sửa hồ sơ", color = FastDashInk, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProfileBadge(
    icon: ImageVector,
    text: String,
    accent: Color
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            Text(text = text, color = FastDashInk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AccountSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = FastDashInk
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = FastDashSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun AccountMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    enabled: Boolean = true,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(16.dp),
                color = FastDashSurfaceAlt
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) FastDashRed else FastDashMuted
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FastDashInk
                )
                subtitle?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = FastDashMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (enabled) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = FastDashMuted
                )
            }
        }
        if (showDivider) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(FastDashLine)
            )
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(56.dp),
        border = BorderStroke(1.dp, FastDashRed.copy(alpha = 0.28f)),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = FastDashWhite)
    ) {
        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = FastDashRed)
        Spacer(modifier = Modifier.width(10.dp))
        Text("Đăng xuất", color = FastDashRed, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GuestAccountState(onLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(FastDashOrange.copy(alpha = 0.85f), FastDashRed.copy(alpha = 0.92f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = FastDashWhite,
                modifier = Modifier.size(46.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Bạn chưa đăng nhập",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            color = FastDashInk
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Đăng nhập để theo dõi đơn hàng và quản lý thông tin tài khoản.",
            color = FastDashMuted,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onLogin,
            colors = ButtonDefaults.buttonColors(
                containerColor = FastDashRed,
                contentColor = FastDashWhite
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Đăng nhập", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp))
        }
    }
}
