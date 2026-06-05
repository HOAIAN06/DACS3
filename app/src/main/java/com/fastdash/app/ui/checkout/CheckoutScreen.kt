package com.fastdash.app.ui.checkout

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.fastdash.app.data.model.request.CheckoutRequest
import com.fastdash.app.data.model.response.ApiErrorResponse
import com.fastdash.app.data.model.response.BranchResponse
import com.fastdash.app.data.model.response.ShippingFeeQuoteResponse
import com.fastdash.app.data.repository.BranchRepository
import com.fastdash.app.data.repository.OrderRepository
import com.fastdash.app.utils.CurrencyUtils
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private val BrandRed = Color(0xFFC8102E)
private val AppBackground = Color(0xFFF5F5F2)
private val CardBackground = Color.White
private val CardMuted = Color(0xFFF7F7F5)
private val InputBackground = Color(0xFFF3F4F6)
private val TextPrimary = Color(0xFF171717)
private val TextSecondary = Color(0xFF6B7280)
private val BorderNeutral = Color(0xFFE5E7EB)
private val SuccessTint = Color(0xFFE9F7EF)
private val SuccessText = Color(0xFF1F7A43)
private val WarningTint = Color(0xFFFFF4E5)
private val WarningText = Color(0xFFB45309)
private val ErrorTint = Color(0xFFFDECEC)
private val ErrorText = Color(0xFFB42318)

private data class CheckoutVisualState(
    val currentAddressLabel: String,
    val deliveryAddressLabel: String,
    val finalAddress: String,
    val disableReason: String?,
    val selectedBranchDistanceKm: Double?,
    val canOrder: Boolean,
    val ctaLabel: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    subtotal: Double = 0.0,
    initialFullName: String = "",
    initialPhone: String = "",
    initialAddress: String = "",
    initialAddressDetail: String = "",
    initialNote: String = "",
    pickedLocation: PickedLocation? = null,
    savedLocations: List<PickedLocation> = emptyList(),
    onBack: () -> Unit = {},
    onOpenMapPicker: () -> Unit = {},
    onCurrentLocationChanged: (PickedLocation?) -> Unit = {},
    onDeliveryLocationChanged: (PickedLocation?) -> Unit = {},
    onRecipientNameChanged: (String) -> Unit = {},
    onRecipientPhoneChanged: (String) -> Unit = {},
    onAddressDetailChanged: (String) -> Unit = {},
    onNoteChanged: (String) -> Unit = {},
    onConfirm: (CheckoutRequest) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val branchRepository = remember { BranchRepository(context.applicationContext) }
    val orderRepository = remember { OrderRepository(context.applicationContext) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val gson = remember { Gson() }

    var name by remember(initialFullName) { mutableStateOf(initialFullName) }
    var phone by remember(initialPhone) { mutableStateOf(initialPhone) }
    var currentAutoAddress by remember { mutableStateOf("") }
    var deliveryAutoAddress by remember { mutableStateOf("") }
    var addressDetail by remember(initialAddressDetail) { mutableStateOf(initialAddressDetail) }
    var branches by remember { mutableStateOf<List<BranchResponse>>(emptyList()) }
    var selectedBranchId by remember { mutableStateOf<Long?>(null) }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var locationMode by remember { mutableStateOf("CURRENT") }
    var quote by remember { mutableStateOf<ShippingFeeQuoteResponse?>(null) }
    var quoteError by remember { mutableStateOf<String?>(null) }
    var loadingLocation by remember { mutableStateOf(false) }
    var currentLatitude by remember { mutableStateOf<Double?>(null) }
    var currentLongitude by remember { mutableStateOf<Double?>(null) }
    var deliveryLatitude by remember { mutableStateOf<Double?>(null) }
    var deliveryLongitude by remember { mutableStateOf<Double?>(null) }
    var note by remember(initialNote) { mutableStateOf(initialNote) }

    val shippingFee = quote?.shippingFee ?: 0.0
    val total = subtotal + shippingFee
    val selectedBranch = branches.firstOrNull { it.id == selectedBranchId }
    val savedLocationEntries = remember(savedLocations) {
        savedLocations.distinctBy { "${it.latitude}|${it.longitude}|${it.address}|${it.detailAddress}" }
    }

    fun branchDistanceKm(branch: BranchResponse): Double? {
        val userLat = deliveryLatitude ?: return null
        val userLng = deliveryLongitude ?: return null
        val branchLat = branch.latitude ?: return null
        val branchLng = branch.longitude ?: return null
        return haversineKm(userLat, userLng, branchLat, branchLng)
    }

    val visualState = remember(name, phone, addressDetail, deliveryAutoAddress, deliveryLatitude, deliveryLongitude, paymentMethod, selectedBranch, quote, currentAutoAddress) {
        val phoneValid = phone.filter { it.isDigit() }.length >= 9
        val nameValid = name.trim().length >= 2
        val coordinateFallbackAddress = deliveryLatitude?.let { lat -> deliveryLongitude?.let { lng -> String.format(Locale.US, "%.4f, %.4f", lat, lng) } }.orEmpty()
        val finalAddress = listOf(addressDetail.trim(), deliveryAutoAddress.trim().ifBlank { coordinateFallbackAddress }).filter { it.isNotBlank() }.distinct().joinToString(", ")
        val branchDistance = selectedBranch?.let(::branchDistanceKm)
        val disableReason = when {
            name.isBlank() || !nameValid -> "Vui lòng nhập họ tên người nhận."
            phone.isBlank() || !phoneValid -> "Vui lòng nhập số điện thoại hợp lệ."
            deliveryLatitude == null || deliveryLongitude == null -> "Hãy chọn địa chỉ giao hàng trước khi đặt đơn."
            finalAddress.isBlank() -> "Địa chỉ giao hàng chưa đầy đủ."
            selectedBranch == null -> "Chưa tìm được cửa hàng có thể phục vụ khu vực này."
            branchDistance == null -> "Không tính được khoảng cách giao hàng."
            branchDistance > 10.0 -> "Địa chỉ hiện nằm ngoài phạm vi giao hàng 10 km."
            quote?.supported == false -> quote?.message ?: "Khu vực này hiện chưa hỗ trợ giao hàng."
            else -> null
        }
        CheckoutVisualState(
            currentAddressLabel = currentAutoAddress.ifBlank { "Chưa xác định vị trí hiện tại" },
            deliveryAddressLabel = deliveryAutoAddress.ifBlank { coordinateFallbackAddress },
            finalAddress = finalAddress,
            disableReason = disableReason,
            selectedBranchDistanceKm = branchDistance,
            canOrder = disableReason == null,
            ctaLabel = if (paymentMethod == "VNPAY") "Tiếp tục thanh toán" else "Xác nhận đặt hàng"
        )
    }

    fun parseApiErrorMessage(raw: String): String? = runCatching { gson.fromJson(raw, ApiErrorResponse::class.java) }.getOrNull()?.message?.takeIf { it.isNotBlank() }
    fun isPlusCode(value: String?): Boolean = !value.isNullOrBlank() && Regex("^[A-Z0-9]{4,}\\+[A-Z0-9]{2,}$").matches(value.trim())

    fun formatResolvedAddress(result: Address): String {
        val houseAndStreet = listOfNotNull(result.subThoroughfare?.takeIf { it.isNotBlank() }, result.thoroughfare?.takeIf { it.isNotBlank() }).joinToString(" ").trim()
        val line0 = result.getAddressLine(0)?.trim().orEmpty()
        val realisticParts = listOf(houseAndStreet.ifBlank { null }, result.subLocality, result.locality, result.subAdminArea, result.adminArea)
            .mapNotNull { it?.trim() }
            .filter { it.isNotBlank() && !isPlusCode(it) }
            .distinct()
        val formatted = realisticParts.joinToString(", ").trim()
        if (formatted.isNotBlank()) return formatted
        if (line0.isNotBlank() && !isPlusCode(line0)) return line0
        return listOfNotNull(result.subLocality, result.locality, result.subAdminArea, result.adminArea).distinct().joinToString(", ")
    }

    fun resetQuoteState() {
        quote = null
        quoteError = null
    }

    fun findNearestBranchId(lat: Double, lng: Double): Long? {
        return branches.mapNotNull { branch ->
            val branchLat = branch.latitude ?: return@mapNotNull null
            val branchLng = branch.longitude ?: return@mapNotNull null
            branch.id to haversineKm(lat, lng, branchLat, branchLng)
        }.minByOrNull { it.second }?.first
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
                    if (quote?.supported != true) quoteError = quote?.message ?: "Giao hàng không hỗ trợ tại vị trí này"
                } else {
                    quote = null
                    val rawError = runCatching { response.errorBody()?.string().orEmpty() }.getOrDefault("")
                    quoteError = parseApiErrorMessage(rawError) ?: "Không thể lấy phí vận chuyển"
                }
            } catch (e: Exception) {
                quote = null
                quoteError = e.message ?: "Lỗi kết nối"
            } finally {
                loadingLocation = false
            }
        }
    }

    fun selectDeliveryLocation(lat: Double, lng: Double, resolvedAddress: String) {
        deliveryLatitude = lat
        deliveryLongitude = lng
        deliveryAutoAddress = resolvedAddress
        onDeliveryLocationChanged(PickedLocation(lat, lng, resolvedAddress, addressDetail.trim()))
        if (addressDetail.isBlank() && initialAddress.isNotBlank()) addressDetail = initialAddress
        findNearestBranchId(lat, lng)?.let { selectedBranchId = it }
        quoteShipping(lat, lng)
    }

    fun applyPickedLocation(location: PickedLocation) {
        if (location.detailAddress.isNotBlank()) {
            addressDetail = location.detailAddress
            onAddressDetailChanged(addressDetail)
        }
        selectDeliveryLocation(location.latitude, location.longitude, location.address)
    }

    fun applyResolvedLocation(lat: Double, lng: Double) {
        scope.launch {
            currentLatitude = lat
            currentLongitude = lng
            currentAutoAddress = withContext(Dispatchers.IO) {
                val geocoder = Geocoder(context, Locale("vi", "VN"))
                runCatching {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lng, 1)
                }.getOrNull()?.firstOrNull()?.let(::formatResolvedAddress).orEmpty()
            }
            val resolvedAddress = currentAutoAddress.ifBlank { String.format(Locale.US, "%.4f, %.4f", lat, lng) }
            onCurrentLocationChanged(PickedLocation(lat, lng, resolvedAddress, addressDetail.trim()))
            selectDeliveryLocation(lat, lng, resolvedAddress)
            loadingLocation = false
        }
    }

    fun resolveCurrentLocation() {
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) return
        loadingLocation = true
        quoteError = null
        val request = CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).setMaxUpdateAgeMillis(0).build()
        fusedLocationClient.getCurrentLocation(request, CancellationTokenSource().token)
            .addOnSuccessListener { location ->
                if (location != null) applyResolvedLocation(location.latitude, location.longitude)
                else fusedLocationClient.lastLocation.addOnSuccessListener { last ->
                    if (last != null) applyResolvedLocation(last.latitude, last.longitude)
                    else {
                        loadingLocation = false
                        quoteError = "Không thể lấy vị trí hiện tại"
                    }
                }
            }
            .addOnFailureListener {
                loadingLocation = false
                quoteError = it.message ?: "Lỗi định vị"
            }
    }

    LaunchedEffect(initialFullName, initialPhone, initialAddress) {
        if (name.isBlank()) name = initialFullName
        if (phone.isBlank()) phone = initialPhone
        if (addressDetail.isBlank()) addressDetail = initialAddressDetail.ifBlank { initialAddress }
    }
    LaunchedEffect(pickedLocation) { pickedLocation?.let(::applyPickedLocation) }
    LaunchedEffect(Unit) {
        runCatching { branchRepository.getBranches() }.getOrNull()?.takeIf { it.isSuccessful }?.body()?.let { branches = it.filter { branch -> branch.status == 1 } }
    }
    LaunchedEffect(branches, deliveryLatitude, deliveryLongitude) {
        val lat = deliveryLatitude
        val lng = deliveryLongitude
        if (lat != null && lng != null) findNearestBranchId(lat, lng)?.let { if (selectedBranchId != it) selectedBranchId = it }
    }
    LaunchedEffect(selectedBranchId, deliveryLatitude, deliveryLongitude) {
        val lat = deliveryLatitude
        val lng = deliveryLongitude
        if (lat != null && lng != null && selectedBranchId != null) quoteShipping(lat, lng) else resetQuoteState()
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) resolveCurrentLocation() else Toast.makeText(context, "Cần quyền truy cập vị trí", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = { CheckoutTopBar(onBack = onBack) },
        bottomBar = {
            CheckoutBottomBar(
                total = total,
                ctaLabel = visualState.ctaLabel,
                paymentMethod = paymentMethod,
                disableReason = visualState.disableReason,
                enabled = visualState.canOrder,
                onConfirm = {
                    val branchId = selectedBranchId ?: return@CheckoutBottomBar
                    onConfirm(
                        CheckoutRequest(
                            branchId = branchId,
                            deliveryType = "DELIVERY",
                            receiverName = name.trim(),
                            receiverPhone = phone.trim(),
                            deliveryAddress = visualState.finalAddress,
                            deliveryLatitude = deliveryLatitude,
                            deliveryLongitude = deliveryLongitude,
                            paymentMethod = paymentMethod,
                            note = note.trim().takeIf { it.isNotBlank() },
                            branchName = selectedBranch?.name,
                            branchAddress = selectedBranch?.address
                        )
                    )
                }
            )
        },
        containerColor = AppBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ReceiverInfoCard(
                    name = name,
                    phone = phone,
                    nameError = if (name.isBlank() || name.trim().length < 2) "Nhập họ tên đầy đủ" else null,
                    phoneError = if (phone.isBlank() || phone.filter { it.isDigit() }.length < 9) "Số điện thoại chưa hợp lệ" else null,
                    onNameChange = { name = it; onRecipientNameChanged(it) },
                    onPhoneChange = { phone = it; onRecipientPhoneChanged(it) }
                )
            }
            item {
                DeliveryAddressCard(
                    locationMode = locationMode,
                    onLocationModeChanged = { locationMode = it },
                    currentAddress = visualState.currentAddressLabel,
                    selectedAddress = visualState.deliveryAddressLabel,
                    addressDetail = addressDetail,
                    savedLocations = savedLocationEntries,
                    quote = quote,
                    quoteError = quoteError,
                    isLoading = loadingLocation,
                    hasSelectedAddress = visualState.deliveryAddressLabel.isNotBlank(),
                    onUseCurrentLocation = {
                        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        if (permission == PackageManager.PERMISSION_GRANTED) resolveCurrentLocation() else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    onOpenMap = onOpenMapPicker,
                    onSavedLocationSelected = ::applyPickedLocation,
                    onAddressDetailChange = { addressDetail = it; onAddressDetailChanged(it) }
                )
            }
            item { ServiceBranchCard(branch = selectedBranch, distanceKm = visualState.selectedBranchDistanceKm, isReady = quote?.supported == true) }
            item { PaymentMethodSection(paymentMethod = paymentMethod, onPaymentSelected = { paymentMethod = it }) }
            item { OrderNoteCard(note = note, onNoteChange = { note = it; onNoteChanged(it) }) }
            item { OrderSummaryCard(subtotal = subtotal, shippingFee = shippingFee, discountAmount = 0.0, total = total) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text("Thanh toán", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBackground)
    )
}

@Composable
private fun ReceiverInfoCard(name: String, phone: String, nameError: String?, phoneError: String?, onNameChange: (String) -> Unit, onPhoneChange: (String) -> Unit) {
    CheckoutCard(title = "Thông tin người nhận", icon = Icons.Outlined.Person) {
        FieldBlock("Họ tên", nameError) {
            CheckoutInput(name, onNameChange, "Ví dụ: Nguyễn Văn A", Icons.Outlined.Person)
        }
        Spacer(Modifier.height(12.dp))
        FieldBlock("Số điện thoại", phoneError) {
            CheckoutInput(phone, onPhoneChange, "Nhập số điện thoại nhận hàng", Icons.Outlined.Phone)
        }
    }
}

@Composable
private fun DeliveryAddressCard(
    locationMode: String,
    onLocationModeChanged: (String) -> Unit,
    currentAddress: String,
    selectedAddress: String,
    addressDetail: String,
    savedLocations: List<PickedLocation>,
    quote: ShippingFeeQuoteResponse?,
    quoteError: String?,
    isLoading: Boolean,
    hasSelectedAddress: Boolean,
    onUseCurrentLocation: () -> Unit,
    onOpenMap: () -> Unit,
    onSavedLocationSelected: (PickedLocation) -> Unit,
    onAddressDetailChange: (String) -> Unit
) {
    CheckoutCard(title = "Địa chỉ giao hàng", icon = Icons.Outlined.Place) {
        SegmentedModeSelector(listOf("CURRENT" to "Vị trí hiện tại", "SAVED" to "Địa chỉ đã lưu"), locationMode, onLocationModeChanged)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryActionButton(Modifier.weight(1f), Icons.Filled.MyLocation, if (isLoading) "Đang lấy vị trí..." else "Dùng vị trí hiện tại", isLoading, onUseCurrentLocation)
            SecondaryActionButton(Modifier.weight(1f), Icons.Outlined.ChevronRight, if (hasSelectedAddress) "Thay đổi địa chỉ" else "Chọn trên bản đồ", false, onOpenMap)
        }
        Spacer(Modifier.height(14.dp))
        if (locationMode == "SAVED") {
            if (savedLocations.isEmpty()) {
                InlineBanner("Chưa có địa chỉ đã lưu", Icons.Outlined.Info, WarningTint, WarningText)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    savedLocations.take(3).forEach { SavedLocationRow(location = it, onClick = { onSavedLocationSelected(it) }) }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
        CurrentDeliveryAddressBlock(selectedAddress, addressDetail, quote, quoteError)
        Spacer(Modifier.height(14.dp))
        FieldBlock("Địa chỉ chi tiết", if (addressDetail.isNotBlank() && addressDetail.trim().length < 5) "Bổ sung số nhà hoặc ghi chú giao hàng" else null) {
            CheckoutInput(addressDetail, onAddressDetailChange, "Số nhà, tầng, tên tòa nhà...", Icons.Filled.LocationOn, singleLine = false, minLines = 2)
        }
        AnimatedVisibility(visible = locationMode == "CURRENT") {
            Text(currentAddress, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun ServiceBranchCard(branch: BranchResponse?, distanceKm: Double?, isReady: Boolean) {
    CheckoutCard(title = "Cửa hàng phục vụ", icon = Icons.Filled.Storefront) {
        if (branch == null) {
            InlineBanner("Chưa xác định chi nhánh", Icons.Outlined.WarningAmber, WarningTint, WarningText)
            return@CheckoutCard
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(42.dp).background(BrandRed.copy(alpha = 0.08f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Storefront, contentDescription = null, tint = BrandRed)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(branch.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(branch.address, fontSize = 13.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(if (isReady) "Phục vụ khu vực của bạn" else "Đang xác định vùng giao")
                    distanceKm?.let { AssistChip("${String.format(Locale.US, "%.1f", it)} km") }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodSection(paymentMethod: String, onPaymentSelected: (String) -> Unit) {
    CheckoutCard(title = "Phương thức thanh toán", icon = Icons.Outlined.Payments) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PaymentMethodOptionCard("Thanh toán khi nhận hàng", "Trả tiền khi nhận đơn", Icons.Filled.Wallet, paymentMethod == "CASH") { onPaymentSelected("CASH") }
            PaymentMethodOptionCard("VNPAY", "Thẻ, QR, ví VNPAY", Icons.Filled.CreditCard, paymentMethod == "VNPAY") { onPaymentSelected("VNPAY") }
            AnimatedVisibility(visible = paymentMethod == "VNPAY") {
                InlineBanner("Mở VNPAY sau khi xác nhận", Icons.Outlined.Info, WarningTint, WarningText)
            }
        }
    }
}

@Composable
private fun PaymentMethodOptionCard(title: String, subtitle: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), color = if (selected) BrandRed.copy(alpha = 0.05f) else CardBackground, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, if (selected) BrandRed.copy(alpha = 0.28f) else BorderNeutral)) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).background(if (selected) BrandRed.copy(alpha = 0.1f) else CardMuted, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = if (selected) BrandRed else TextPrimary)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            }
            RadioButton(selected = selected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = BrandRed))
        }
    }
}

@Composable
private fun OrderNoteCard(note: String, onNoteChange: (String) -> Unit) {
    val suggestions = listOf("Gọi trước khi giao", "Ít đá", "Không cay", "Không lấy ống hút")
    CheckoutCard("Ghi chú đơn hàng", Icons.Outlined.EditNote) {
        CheckoutInput(note, onNoteChange, "Ví dụ: Gọi trước khi giao, không lấy tương cà...", Icons.Outlined.EditNote, singleLine = false, minLines = 3)
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { label ->
                        FilterChip(
                            selected = note.contains(label, ignoreCase = true),
                            onClick = {
                                onNoteChange(
                                    when {
                                        note.isBlank() -> label
                                        note.contains(label, ignoreCase = true) -> note
                                        else -> "$note, $label"
                                    }
                                )
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrandRed.copy(alpha = 0.08f), selectedLabelColor = BrandRed)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(subtotal: Double, shippingFee: Double, discountAmount: Double, total: Double) {
    CheckoutCard("Chi tiết đơn hàng", Icons.Outlined.LocalShipping) {
        SummaryRow("Tạm tính", CurrencyUtils.formatVnd(subtotal))
        SummaryRow("Phí giao hàng", CurrencyUtils.formatVnd(shippingFee))
        SummaryRow("Giảm giá", CurrencyUtils.formatVnd(discountAmount))
        Surface(color = CardMuted, shape = RoundedCornerShape(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Tổng cộng", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(CurrencyUtils.formatVnd(total), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrandRed)
            }
        }
    }
}

@Composable
private fun CheckoutBottomBar(total: Double, ctaLabel: String, paymentMethod: String, disableReason: String?, enabled: Boolean, onConfirm: () -> Unit) {
    Surface(shadowElevation = 14.dp, color = CardBackground, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
            AnimatedVisibility(visible = disableReason != null) {
                InlineBanner(disableReason.orEmpty(), Icons.Outlined.WarningAmber, WarningTint, WarningText, Modifier.padding(bottom = 10.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tổng thanh toán", fontSize = 12.sp, color = TextSecondary)
                    Text(CurrencyUtils.formatVnd(total), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BrandRed)
                }
                Button(
                    onClick = onConfirm,
                    enabled = enabled,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed, disabledContainerColor = Color(0xFFE5E7EB), disabledContentColor = Color(0xFF9CA3AF)),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
                ) { Text(ctaLabel, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
            }
            AnimatedVisibility(visible = paymentMethod == "VNPAY") {
                Text("Mở VNPAY sau khi xác nhận", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun CheckoutCard(title: String, icon: ImageVector, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = CardBackground, shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
        Column(modifier = Modifier.fillMaxWidth().animateContentSize().padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(BrandRed.copy(alpha = 0.08f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = BrandRed, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    subtitle?.let { Text(it, fontSize = 12.sp, color = TextSecondary) }
                }
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun FieldBlock(label: String, error: String?, field: @Composable () -> Unit) {
    Column {
        Text(label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        field()
        Spacer(Modifier.height(4.dp))
        Text(error.orEmpty(), color = if (error != null) ErrorText else Color.Transparent, fontSize = 11.sp, minLines = 1)
    }
}

@Composable
private fun CheckoutInput(value: String, onValueChange: (String) -> Unit, placeholder: String, leadingIcon: ImageVector, keyboardOptions: KeyboardOptions = KeyboardOptions.Default, singleLine: Boolean = true, minLines: Int = 1) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = TextSecondary, fontSize = 13.sp) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = TextSecondary) },
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = InputBackground, unfocusedContainerColor = InputBackground, disabledContainerColor = InputBackground, focusedBorderColor = BrandRed.copy(alpha = 0.3f), unfocusedBorderColor = Color.Transparent)
    )
}

@Composable
private fun SegmentedModeSelector(options: List<Pair<String, String>>, selected: String, onSelected: (String) -> Unit) {
    Surface(color = CardMuted, shape = RoundedCornerShape(14.dp)) {
        Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEach { (key, title) ->
                val active = selected == key
                Surface(modifier = Modifier.weight(1f).clickable { onSelected(key) }, color = if (active) CardBackground else Color.Transparent, shape = RoundedCornerShape(12.dp)) {
                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(title, fontSize = 13.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium, color = if (active) BrandRed else TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SecondaryActionButton(modifier: Modifier = Modifier, icon: ImageVector, text: String, loading: Boolean = false, onClick: () -> Unit) {
    Surface(modifier = modifier.clickable(onClick = onClick), color = CardMuted, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, BorderNeutral)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = BrandRed)
            } else {
                Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
            }
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun CurrentDeliveryAddressBlock(selectedAddress: String, addressDetail: String, quote: ShippingFeeQuoteResponse?, quoteError: String?) {
    if (selectedAddress.isBlank()) {
        InlineBanner("Chưa chọn địa chỉ", Icons.Outlined.WarningAmber, WarningTint, WarningText)
        return
    }
    Surface(color = CardMuted, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = BrandRed, modifier = Modifier.padding(top = 2.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Địa chỉ đang giao", fontSize = 12.sp, color = TextSecondary)
                    Text(selectedAddress, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    if (addressDetail.isNotBlank()) Text(addressDetail, fontSize = 13.sp, color = TextSecondary)
                }
            }
            when {
                quote?.supported == true -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip("Đã xác định khu vực giao hàng", SuccessTint, SuccessText, Icons.Outlined.CheckCircle)
                        AssistChip(CurrencyUtils.formatVnd(quote.shippingFee))
                    }
                    Text("${String.format(Locale.US, "%.2f", quote.distanceKm)} km • ${quote.branchName}", fontSize = 12.sp, color = TextSecondary)
                }
                quoteError != null -> InlineBanner(quoteError, Icons.Outlined.WarningAmber, ErrorTint, ErrorText)
            }
        }
    }
}

@Composable
private fun SavedLocationRow(location: PickedLocation, onClick: () -> Unit) {
    val title = location.detailAddress.ifBlank { "Địa chỉ đã lưu" }
    val subtitle = location.address.ifBlank { "Chưa có mô tả vị trí" }
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), color = CardBackground, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, BorderNeutral)) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(CardMuted, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Place, contentDescription = null, tint = TextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun InlineBanner(text: String, icon: ImageVector, background: Color, contentColor: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), color = background, shape = RoundedCornerShape(14.dp)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Text(text, fontSize = 12.sp, color = contentColor, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun AssistChip(text: String, background: Color = BrandRed.copy(alpha = 0.08f), color: Color = BrandRed, icon: ImageVector? = null) {
    Surface(color = background, shape = RoundedCornerShape(999.dp)) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            icon?.let { Icon(it, contentDescription = null, tint = color, modifier = Modifier.size(14.dp)) }
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
    Spacer(Modifier.height(8.dp))
}

private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

