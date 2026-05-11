package com.fastdash.app.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastdash.app.utils.CurrencyUtils

private val PizzaHutRed = Color(0xFFC8102E)
private val LightGrey = Color(0xFFF4F4F4)
private val SurfaceWhite = Color.White
private val PrimaryBlack = Color(0xFF1C1C1C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    total: Double = 0.0,
    onBack: () -> Unit = {},
    onConfirm: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = SurfaceWhite
            ) {
                Button(
                    onClick = { onConfirm(name, phone, address) },
                    enabled = name.isNotBlank() && phone.isNotBlank() && address.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PizzaHutRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("XÁC NHẬN ĐƠN HÀNG", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        },
        containerColor = LightGrey
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tổng cộng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        CurrencyUtils.formatVnd(total),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = PizzaHutRed
                    )
                }
            }

            Text("THÔNG TIN GIAO HÀNG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            CheckoutField(
                value = name,
                onValueChange = { name = it },
                label = "Họ và tên",
                icon = Icons.Outlined.Person
            )

            CheckoutField(
                value = phone,
                onValueChange = { phone = it },
                label = "Số điện thoại",
                icon = Icons.Outlined.Phone
            )

            CheckoutField(
                value = address,
                onValueChange = { address = it },
                label = "Địa chỉ nhận hàng",
                icon = Icons.Outlined.Home,
                singleLine = false
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CheckoutField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = PizzaHutRed) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PizzaHutRed,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = PizzaHutRed
        ),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3
    )
}
