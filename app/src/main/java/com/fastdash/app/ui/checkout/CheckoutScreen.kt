package com.fastdash.app.ui.checkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastdash.app.data.model.request.CheckoutRequest
import com.fastdash.app.data.model.response.BranchResponse
import com.fastdash.app.data.repository.BranchRepository
import com.fastdash.app.utils.CurrencyUtils

private val PizzaHutRed = Color(0xFFC8102E)
private val LightGrey = Color(0xFFF4F4F4)
private val SurfaceWhite = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    total: Double = 0.0,
    onBack: () -> Unit = {},
    onConfirm: (CheckoutRequest) -> Unit = {}
) {
    val context = LocalContext.current
    val branchRepository = remember { BranchRepository(context.applicationContext) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var branches by remember { mutableStateOf<List<BranchResponse>>(emptyList()) }
    var selectedBranchId by remember { mutableStateOf<Long?>(null) }
    var deliveryType by remember { mutableStateOf("DELIVERY") }
    var paymentMethod by remember { mutableStateOf("COD") }

    LaunchedEffect(Unit) {
        runCatching { branchRepository.getBranches() }
            .getOrNull()
            ?.takeIf { it.isSuccessful }
            ?.body()
            ?.let { items ->
                branches = items.filter { it.status == 1 }
                selectedBranchId = branches.firstOrNull()?.id
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toan", fontWeight = FontWeight.ExtraBold) },
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
                    onClick = {
                        val branchId = selectedBranchId ?: return@Button
                        onConfirm(
                            CheckoutRequest(
                                branchId = branchId,
                                deliveryType = deliveryType,
                                receiverName = name,
                                receiverPhone = phone,
                                deliveryAddress = address,
                                paymentMethod = paymentMethod
                            )
                        )
                    },
                    enabled = name.isNotBlank() && phone.isNotBlank() && address.isNotBlank() && selectedBranchId != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PizzaHutRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("XAC NHAN DON HANG", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tong cong", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        CurrencyUtils.formatVnd(total),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = PizzaHutRed
                    )
                }
            }

            Text("CHI NHANH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            BranchSelector(branches, selectedBranchId, onBranchSelected = { selectedBranchId = it })

            Text("TUY CHON DON HANG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            SelectorRow(
                options = listOf("DELIVERY", "PICKUP"),
                selected = deliveryType,
                onSelected = { deliveryType = it }
            )
            SelectorRow(
                options = listOf("COD", "ONLINE"),
                selected = paymentMethod,
                onSelected = { paymentMethod = it }
            )

            Text("THONG TIN GIAO HANG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            CheckoutField(value = name, onValueChange = { name = it }, label = "Ho va ten", icon = Icons.Outlined.Person)
            CheckoutField(value = phone, onValueChange = { phone = it }, label = "So dien thoai", icon = Icons.Outlined.Phone)
            CheckoutField(value = address, onValueChange = { address = it }, label = "Dia chi nhan hang", icon = Icons.Outlined.Home, singleLine = false)

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BranchSelector(
    branches: List<BranchResponse>,
    selectedBranchId: Long?,
    onBranchSelected: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        branches.forEach { branch ->
            val selected = selectedBranchId == branch.id
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBranchSelected(branch.id) },
                shape = RoundedCornerShape(12.dp),
                color = if (selected) PizzaHutRed.copy(alpha = 0.08f) else SurfaceWhite
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(branch.name, fontWeight = FontWeight.Bold)
                        Text(branch.address, color = Color.Gray, fontSize = 12.sp)
                    }
                    RadioButton(selected = selected, onClick = { onBranchSelected(branch.id) })
                }
            }
        }
    }
}

@Composable
private fun SelectorRow(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelected(option) },
                label = { Text(option) }
            )
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
