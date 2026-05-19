package com.fastdash.app.ui.checkout

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.fastdash.app.data.model.request.CheckoutRequest
import com.fastdash.app.data.model.response.BranchResponse
import com.fastdash.app.data.model.response.ShippingFeeQuoteResponse
import com.fastdash.app.data.repository.BranchRepository
import com.fastdash.app.data.repository.OrderRepository
import com.fastdash.app.utils.CurrencyUtils
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.Locale

private val PizzaHutRed = Color(0xFFC8102E)
private val LightGrey = Color(0xFFF4F4F4)
private val SurfaceWhite = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    subtotal: Double = 0.0,
    initialFullName: String = "",
    initialPhone: String = "",
    initialAddress: String = "",
    onBack: () -> Unit = {},
    onConfirm: (CheckoutRequest) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val branchRepository = remember { BranchRepository(context.applicationContext) }
    val orderRepository = remember { OrderRepository(context.applicationContext) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var name by remember { mutableStateOf(initialFullName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var address by remember { mutableStateOf(initialAddress) }
    var branches by remember { mutableStateOf<List<BranchResponse>>(emptyList()) }
    var selectedBranchId by remember { mutableStateOf<Long?>(null) }
    var deliveryType by remember { mutableStateOf("DELIVERY") }
    var paymentMethod by remember { mutableStateOf("COD") }
    var quote by remember { mutableStateOf<ShippingFeeQuoteResponse?>(null) }
    var quoteError by remember { mutableStateOf<String?>(null) }
    var loadingLocation by remember { mutableStateOf(false) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var currentDistanceKm by remember { mutableDoubleStateOf(0.0) }

    val selectedBranch = branches.firstOrNull { it.id == selectedBranchId }
    val isDelivery = deliveryType == "DELIVERY"
    val shippingFee = if (isDelivery) quote?.shippingFee ?: 0.0 else 0.0
    val total = subtotal + shippingFee
    val phoneValid = phone.filter { it.isDigit() }.length >= 9
    val nameValid = name.trim().length >= 2
    val addressValid = if (isDelivery) address.trim().length >= 10 else true
    val shippingSupported = !isDelivery || (quote?.supported == true)

    LaunchedEffect(initialFullName, initialPhone, initialAddress) {
        if (name.isBlank()) name = initialFullName
        if (phone.isBlank()) phone = initialPhone
        if (address.isBlank()) address = initialAddress
    }

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

    LaunchedEffect(deliveryType, selectedBranchId) {
        quote = null
        quoteError = null
        currentDistanceKm = 0.0
        if (!isDelivery) {
            latitude = null
            longitude = null
            address = selectedBranch?.address.orEmpty()
        } else if (address == selectedBranch?.address.orEmpty()) {
            address = initialAddress
        }
    }

    fun quoteShipping(lat: Double, lng: Double) {
        val branchId = selectedBranchId ?: return
        scope.launch {
            loadingLocation = true
            quoteError = null
            try {
                val response = orderRepository.getShippingFeeQuote(branchId, lat, lng)
                if (response.isSuccessful) {
                    quote = response.body()
                    currentDistanceKm = response.body()?.distanceKm ?: 0.0
                    if (quote?.supported != true) {
                        quoteError = quote?.message ?: "Không hỗ trợ giao hàng tới vị trí này"
                    }
                } else {
                    quote = null
                    quoteError = "Không lấy được phí ship: ${response.code()}"
                }
            } catch (e: Exception) {
                quote = null
                quoteError = e.message ?: "Không lấy được phí ship"
            } finally {
                loadingLocation = false
            }
        }
    }

    fun resolveCurrentLocation() {
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) return

        loadingLocation = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location == null) {
                    loadingLocation = false
                    quoteError = "Không lấy được vị trí hiện tại"
                    return@addOnSuccessListener
                }
                latitude = location.latitude
                longitude = location.longitude
                val geocoder = Geocoder(context, Locale("vi", "VN"))
                runCatching {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                }.getOrNull()?.firstOrNull()?.let { result ->
                    address = listOfNotNull(result.featureName, result.thoroughfare, result.subLocality, result.locality)
                        .joinToString(", ")
                        .ifBlank { address }
                }
                quoteShipping(location.latitude, location.longitude)
            }
            .addOnFailureListener {
                loadingLocation = false
                quoteError = it.message ?: "Không lấy được vị trí hiện tại"
            }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            resolveCurrentLocation()
        } else {
            Toast.makeText(context, "Cần quyền vị trí để tính phí ship", Toast.LENGTH_SHORT).show()
        }
    }

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
                    onClick = {
                        val branchId = selectedBranchId ?: return@Button
                        onConfirm(
                            CheckoutRequest(
                                branchId = branchId,
                                deliveryType = deliveryType,
                                receiverName = name.trim(),
                                receiverPhone = phone.trim(),
                                deliveryAddress = if (isDelivery) address.trim() else selectedBranch?.address.orEmpty(),
                                deliveryLatitude = if (isDelivery) latitude else null,
                                deliveryLongitude = if (isDelivery) longitude else null,
                                paymentMethod = paymentMethod
                            )
                        )
                    },
                    enabled = nameValid &&
                        phoneValid &&
                        selectedBranchId != null &&
                        addressValid &&
                        shippingSupported &&
                        (!isDelivery || (latitude != null && longitude != null)),
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryRow("Tạm tính", CurrencyUtils.formatVnd(subtotal))
                    SummaryRow("Phí giao hàng", CurrencyUtils.formatVnd(shippingFee))
                    SummaryRow("Tổng cộng", CurrencyUtils.formatVnd(total), valueColor = PizzaHutRed, bold = true)
                }
            }

            Text("CHI NHÁNH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            BranchSelector(branches, selectedBranchId, onBranchSelected = { selectedBranchId = it })

            Text("TÙY CHỌN ĐƠN HÀNG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            SelectorRow(listOf("DELIVERY", "PICKUP"), deliveryType) { deliveryType = it }
            SelectorRow(listOf("COD", "ONLINE"), paymentMethod) { paymentMethod = it }

            if (isDelivery) {
                Button(
                    onClick = {
                        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        if (permission == PackageManager.PERMISSION_GRANTED) {
                            resolveCurrentLocation()
                        } else {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PizzaHutRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (loadingLocation) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PizzaHutRed, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Dùng vị trí hiện tại")
                }

                quote?.let { ShippingFeeCard(it, currentDistanceKm) }
                quoteError?.let { Text(it, color = Color(0xFFD32F2F), fontSize = 12.sp) }
            }

            Text(
                if (isDelivery) "THÔNG TIN GIAO HÀNG" else "THÔNG TIN NGƯỜI NHẬN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )

            CheckoutField(
                value = name,
                onValueChange = { name = it },
                label = "Họ và tên",
                icon = Icons.Outlined.Person,
                supportingText = if (!nameValid && name.isNotBlank()) "Tên phải từ 2 ký tự" else null
            )
            CheckoutField(
                value = phone,
                onValueChange = { phone = it.filter { ch -> ch.isDigit() } },
                label = "Số điện thoại",
                icon = Icons.Outlined.Phone,
                supportingText = if (!phoneValid && phone.isNotBlank()) "Số điện thoại không hợp lệ" else null
            )
            CheckoutField(
                value = address,
                onValueChange = { address = it },
                label = if (isDelivery) "Địa chỉ nhận hàng" else "Địa chỉ chi nhánh nhận hàng",
                icon = Icons.Outlined.Home,
                singleLine = false,
                enabled = isDelivery,
                supportingText = if (isDelivery && !addressValid && address.isNotBlank()) "Địa chỉ quá ngắn" else null
            )

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
    singleLine: Boolean = true,
    enabled: Boolean = true,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = PizzaHutRed) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PizzaHutRed,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = PizzaHutRed
        ),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        supportingText = supportingText?.let { { Text(it, color = Color(0xFFD32F2F)) } }
    )
}

@Composable
private fun ShippingFeeCard(quote: ShippingFeeQuoteResponse, distanceKm: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryRow("Khoảng cách", String.format(Locale.US, "%.2f km", distanceKm))
            SummaryRow("Phí giao hàng", CurrencyUtils.formatVnd(quote.shippingFee), valueColor = PizzaHutRed)
            Text(
                quote.message,
                color = if (quote.supported) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(
            value,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.Medium,
            fontSize = 14.sp,
            color = valueColor
        )
    }
}
