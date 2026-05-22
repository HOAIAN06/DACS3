package com.fastdash.app.ui.checkout

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import kotlin.math.*

private val PizzaHutRed = Color(0xFFC8102E)
private val BackgroundGrey = Color(0xFFF8F9FA)
private val SurfaceWhite = Color.White
private val TextSecondary = Color(0xFF6C757D)
private val DividerColor = Color(0xFFE9ECEF)

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
    var paymentMethod by remember { mutableStateOf("COD") }
    var locationMode by remember { mutableStateOf("CURRENT") }
    var quote by remember { mutableStateOf<ShippingFeeQuoteResponse?>(null) }
    var quoteError by remember { mutableStateOf<String?>(null) }
    var loadingLocation by remember { mutableStateOf(false) }
    var currentLatitude by remember { mutableStateOf<Double?>(null) }
    var currentLongitude by remember { mutableStateOf<Double?>(null) }
    var deliveryLatitude by remember { mutableStateOf<Double?>(null) }
    var deliveryLongitude by remember { mutableStateOf<Double?>(null) }
    var note by remember(initialNote) { mutableStateOf(initialNote) }

    fun branchDistanceKm(branch: BranchResponse): Double? {
        val userLat = deliveryLatitude ?: return null
        val userLng = deliveryLongitude ?: return null
        val branchLat = branch.latitude ?: return null
        val branchLng = branch.longitude ?: return null
        return haversineKm(userLat, userLng, branchLat, branchLng)
    }

    val shippingFee = quote?.shippingFee ?: 0.0
    val discountAmount = 0.0
    val total = subtotal + shippingFee
    val selectedBranch = branches.firstOrNull { it.id == selectedBranchId }
    
    // Validation
    val phoneValid = phone.filter { it.isDigit() }.length >= 9
    val nameValid = name.trim().length >= 2
    val addressDetailValid = addressDetail.trim().length >= 5
    val savedLocationEntries = remember(savedLocations) {
        savedLocations.distinctBy { "${it.latitude}|${it.longitude}|${it.address}|${it.detailAddress}" }
    }
    val selectedBranchDistanceKm = selectedBranch?.let { branch -> branchDistanceKm(branch) }
    val coordinateFallbackAddress = deliveryLatitude?.let { lat ->
        deliveryLongitude?.let { lng ->
            String.format(Locale.US, "%.4f, %.4f", lat, lng)
        }
    }.orEmpty()
    val hasUsableAddressText = addressDetailValid || deliveryAutoAddress.isNotBlank() || coordinateFallbackAddress.isNotBlank()
    val finalAddress = listOf(
        addressDetail.trim(),
        deliveryAutoAddress.trim().ifBlank { coordinateFallbackAddress }
    ).filter { it.isNotBlank() }.distinct().joinToString(", ")
    val disableReason = when {
        name.isBlank() -> "Vui lòng nhập họ tên"
        phone.isBlank() -> "Vui lòng nhập số điện thoại"
        !nameValid -> "Vui lòng nhập họ tên"
        !phoneValid -> "Vui lòng nhập số điện thoại"
        deliveryLatitude == null || deliveryLongitude == null -> "Vui lòng chọn địa chỉ giao hàng"
        !hasUsableAddressText || finalAddress.isBlank() -> "Vui lòng chọn địa chỉ giao hàng"
        selectedBranch == null -> "Không tìm thấy cửa hàng phù hợp"
        selectedBranchDistanceKm == null -> "Không tính được khoảng cách giao hàng"
        selectedBranchDistanceKm > 10.0 -> "Địa chỉ nằm ngoài phạm vi giao hàng 10 km"
        quote?.supported != true -> "Địa chỉ nằm ngoài phạm vi giao hàng 10 km"
        paymentMethod.isBlank() -> "Vui lòng chọn phương thức thanh toán"
        else -> null
    }
    val canOrder = disableReason == null

    fun parseApiErrorMessage(raw: String): String? {
        return runCatching { gson.fromJson(raw, ApiErrorResponse::class.java) }
            .getOrNull()
            ?.message
            ?.takeIf { it.isNotBlank() }
    }

    fun isPlusCode(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        return Regex("^[A-Z0-9]{4,}\\+[A-Z0-9]{2,}$").matches(value.trim())
    }

    fun formatResolvedAddress(result: Address): String {
        val houseAndStreet = listOfNotNull(
            result.subThoroughfare?.takeIf { it.isNotBlank() },
            result.thoroughfare?.takeIf { it.isNotBlank() }
        ).joinToString(" ").trim()
        val line0 = result.getAddressLine(0)?.trim().orEmpty()
        val realisticParts = listOf(
            houseAndStreet.ifBlank { null },
            result.subLocality,
            result.locality,
            result.subAdminArea,
            result.adminArea
        ).mapNotNull { it?.trim() }
            .filter { it.isNotBlank() && !isPlusCode(it) }
            .distinct()
        val formatted = realisticParts.joinToString(", ").trim()

        if (formatted.isNotBlank()) return formatted
        if (line0.isNotBlank() && !isPlusCode(line0)) return line0

        val fallbackParts = listOfNotNull(
            result.subLocality?.takeIf { it.isNotBlank() },
            result.locality?.takeIf { it.isNotBlank() },
            result.subAdminArea?.takeIf { it.isNotBlank() },
            result.adminArea?.takeIf { it.isNotBlank() }
        )
        return fallbackParts.distinct().joinToString(", ")
    }

    fun resetQuoteState() {
        quote = null
        quoteError = null
    }

    fun findNearestBranchId(lat: Double, lng: Double): Long? {
        return branches
            .mapNotNull { branch ->
                val branchLat = branch.latitude ?: return@mapNotNull null
                val branchLng = branch.longitude ?: return@mapNotNull null
                val distance = haversineKm(lat, lng, branchLat, branchLng)
                branch.id to distance
            }
            .minByOrNull { it.second }
            ?.first
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
                    if (quote?.supported != true) {
                        quoteError = quote?.message ?: "Giao hàng không hỗ trợ tại vị trí này"
                    }
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
        onDeliveryLocationChanged(
            PickedLocation(
                latitude = lat,
                longitude = lng,
                address = resolvedAddress,
                detailAddress = addressDetail.trim()
            )
        )
        if (addressDetail.isBlank() && initialAddress.isNotBlank()) {
            addressDetail = initialAddress
        }
        val nearestBranchId = findNearestBranchId(lat, lng)
        if (nearestBranchId != null) {
            selectedBranchId = nearestBranchId
        }
        quoteShipping(lat, lng)
    }

    fun applyPickedLocation(location: PickedLocation) {
        if (location.detailAddress.isNotBlank()) {
            addressDetail = location.detailAddress
            onAddressDetailChanged(addressDetail)
        }
        selectDeliveryLocation(
            lat = location.latitude,
            lng = location.longitude,
            resolvedAddress = location.address
        )
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

            val resolvedAddress = currentAutoAddress.ifBlank {
                String.format(Locale.US, "%.4f, %.4f", lat, lng)
            }
            onCurrentLocationChanged(
                PickedLocation(
                    latitude = lat,
                    longitude = lng,
                    address = resolvedAddress,
                    detailAddress = addressDetail.trim()
                )
            )
            selectDeliveryLocation(lat, lng, resolvedAddress)
            loadingLocation = false
        }
    }

    fun resolveCurrentLocation() {
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) return

        loadingLocation = true
        quoteError = null
        val cancellationTokenSource = CancellationTokenSource()
        val currentLocationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .build()
        fusedLocationClient.getCurrentLocation(currentLocationRequest, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    applyResolvedLocation(location.latitude, location.longitude)
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { last ->
                        if (last != null) applyResolvedLocation(last.latitude, last.longitude)
                        else {
                            loadingLocation = false
                            quoteError = "Không thể lấy vị trí"
                        }
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

    LaunchedEffect(pickedLocation) {
        pickedLocation?.let { applyPickedLocation(it) }
    }

    LaunchedEffect(Unit) {
        runCatching { branchRepository.getBranches() }
            .getOrNull()
            ?.takeIf { it.isSuccessful }
            ?.body()
            ?.let { items ->
                branches = items.filter { it.status == 1 }
            }
    }

    LaunchedEffect(branches, deliveryLatitude, deliveryLongitude) {
        val lat = deliveryLatitude
        val lng = deliveryLongitude
        if (lat != null && lng != null) {
            val nearestBranchId = findNearestBranchId(lat, lng)
            if (nearestBranchId != null && selectedBranchId != nearestBranchId) {
                selectedBranchId = nearestBranchId
            }
        }
    }

    LaunchedEffect(selectedBranchId, deliveryLatitude, deliveryLongitude) {
        val lat = deliveryLatitude
        val lng = deliveryLongitude
        if (lat != null && lng != null && selectedBranchId != null) {
            quoteShipping(lat, lng)
        } else {
            resetQuoteState()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) resolveCurrentLocation()
        else Toast.makeText(context, "Cần quyền truy cập vị trí", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        CurrencyUtils.formatVnd(total),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = PizzaHutRed
                    )
                    if (disableReason != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(disableReason, color = PizzaHutRed, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val branchId = selectedBranchId ?: return@Button
                            onConfirm(
                                CheckoutRequest(
                                    branchId = branchId,
                                    deliveryType = "DELIVERY",
                                    receiverName = name.trim(),
                                    receiverPhone = phone.trim(),
                                    deliveryAddress = finalAddress,
                                    deliveryLatitude = deliveryLatitude,
                                    deliveryLongitude = deliveryLongitude,
                                    paymentMethod = paymentMethod,
                                    note = note.trim().takeIf { it.isNotBlank() },
                                    branchName = selectedBranch?.name,
                                    branchAddress = selectedBranch?.address
                                )
                            )
                        },
                        enabled = canOrder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PizzaHutRed,
                            disabledContainerColor = Color.LightGray.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("XÁC NHẬN ĐẶT HÀNG", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        },
        containerColor = BackgroundGrey
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Recipient Information
            SectionCard(title = "Thông tin người nhận", icon = Icons.Outlined.Person) {
                CheckoutField(
                    value = name,
                    onValueChange = {
                        name = it
                        onRecipientNameChanged(it)
                    },
                    label = "Họ và tên",
                    placeholder = "Nhập tên người nhận",
                    icon = Icons.Outlined.Person,
                    isError = !nameValid && name.isNotBlank()
                )
                CheckoutField(
                    value = phone,
                    onValueChange = {
                        phone = it.filter { ch -> ch.isDigit() }
                        onRecipientPhoneChanged(phone)
                    },
                    label = "Số điện thoại",
                    placeholder = "Nhập số điện thoại",
                    icon = Icons.Outlined.Phone,
                    isError = !phoneValid && phone.isNotBlank()
                )
            }

            // 2. Delivery Address
            SectionCard(title = "Địa chỉ giao hàng", icon = Icons.Outlined.LocationOn) {
                SelectorRow(listOf("CURRENT", "SAVED"), locationMode) { selectedMode ->
                    locationMode = selectedMode
                    if (selectedMode == "SAVED" && savedLocationEntries.isNotEmpty()) {
                        applyPickedLocation(savedLocationEntries.first())
                    }
                }

                OutlinedButton(
                    onClick = onOpenMapPicker,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (deliveryLatitude == null || deliveryLongitude == null) "Chọn địa chỉ trên bản đồ" else "Thay đổi địa chỉ")
                }
                
                AnimatedVisibility(visible = locationMode == "CURRENT") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                if (permission == PackageManager.PERMISSION_GRANTED) resolveCurrentLocation()
                                else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PizzaHutRed.copy(alpha = 0.05f), contentColor = PizzaHutRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            if (loadingLocation) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PizzaHutRed, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Sử dụng vị trí hiện tại", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        if (currentAutoAddress.isNotBlank() || currentLatitude != null || currentLongitude != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF8F9FA),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, DividerColor)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        currentAutoAddress.ifBlank { "Chưa có vị trí" },
                                        color = if (currentAutoAddress.isBlank()) TextSecondary else Color.Black,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                if (locationMode == "SAVED") {
                    if (savedLocationEntries.isEmpty()) {
                        Text("Chưa có vị trí đã lưu", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            savedLocationEntries.forEach { savedLocation ->
                                val savedLocationLabel = listOf(savedLocation.detailAddress.trim(), savedLocation.address.trim())
                                    .filter { value -> value.isNotBlank() }
                                    .distinct()
                                    .joinToString(", ")
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { applyPickedLocation(savedLocation) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, DividerColor),
                                    color = SurfaceWhite
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("Vị trí đã lưu", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text(
                                            savedLocationLabel.ifBlank { "Chưa có mô tả vị trí" },
                                            fontSize = 13.sp,
                                            color = if (savedLocationLabel.isBlank()) TextSecondary else Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (deliveryAutoAddress.isNotBlank() || coordinateFallbackAddress.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Địa chỉ đã chọn", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                        Text(
                            deliveryAutoAddress.ifBlank { coordinateFallbackAddress },
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                        if (addressDetail.trim().isNotBlank()) {
                            Text(
                                addressDetail.trim(),
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    StatusLine(
                        icon = Icons.Outlined.Info,
                        text = "Hãy dùng vị trí hiện tại hoặc chọn trên bản đồ để xác định điểm giao hàng chính xác.",
                        color = TextSecondary
                    )
                }

                CheckoutField(
                    value = addressDetail,
                    onValueChange = {
                        addressDetail = it
                        onAddressDetailChanged(it)
                    },
                    label = "Địa chỉ chi tiết",
                    placeholder = "Số nhà, tên đường, ghi chú giao hàng...",
                    icon = Icons.Outlined.Home,
                    singleLine = false,
                    isError = !addressDetailValid && addressDetail.isNotBlank()
                )

                // Quote status & Shipping Card
                Box(modifier = Modifier.animateContentSize()) {
                    Column {
                        when {
                            quoteError != null -> StatusLine(icon = Icons.Outlined.ErrorOutline, text = quoteError!!, color = PizzaHutRed)
                            quote != null && quote!!.supported -> StatusLine(icon = Icons.Outlined.CheckCircle, text = "Đã xác định khu vực giao hàng", color = Color(0xFF2E7D32))
                        }
                        
                        if (quote != null && quote!!.supported) {
                            Spacer(Modifier.height(8.dp))
                            ShippingFeeCard(quote!!)
                        }
                    }
                }
            }

            // 3. Service Branch
            SectionCard(title = "Cửa hàng phục vụ", icon = Icons.Outlined.Storefront) {
                SelectedBranchCard(
                    branch = selectedBranch,
                    distanceKm = selectedBranchDistanceKm
                )
            }

            // 4. Payment Method
            SectionCard(title = "Phương thức thanh toán", icon = Icons.Outlined.Payments) {
                SelectorRow(listOf("COD", "ONLINE"), paymentMethod) { paymentMethod = it }
                Text(
                    if (paymentMethod == "COD") "Thanh toán khi nhận hàng"
                    else "Sau khi đặt hàng, bạn xác nhận đã chuyển khoản để admin kiểm tra.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            SectionCard(title = "Ghi chú đơn hàng", icon = Icons.Outlined.EditNote) {
                CheckoutField(
                    value = note,
                    onValueChange = {
                        note = it
                        onNoteChanged(it)
                    },
                    label = "Ghi chú cho cửa hàng / shipper",
                    placeholder = "Ví dụ: Gọi trước khi giao",
                    icon = Icons.Outlined.EditNote,
                    singleLine = false
                )
            }

            // 5. Order Summary
            SectionCard(title = "Chi tiết đơn hàng", icon = Icons.Outlined.Description) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryRow("Tạm tính", CurrencyUtils.formatVnd(subtotal))
                    SummaryRow("Phí giao hàng", CurrencyUtils.formatVnd(shippingFee))
                    SummaryRow("Giảm giá", CurrencyUtils.formatVnd(discountAmount))
                    HorizontalDivider(color = DividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tổng cộng", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(CurrencyUtils.formatVnd(total), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PizzaHutRed)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SelectedBranchCard(
    branch: BranchResponse?,
    distanceKm: Double?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8F9FA),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(branch?.name ?: "Chưa xác định cửa hàng", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                branch?.address ?: "Ứng dụng sẽ tự chọn cửa hàng gần nhất sau khi có vị trí giao hàng.",
                color = TextSecondary,
                fontSize = 12.sp
            )
            if (distanceKm != null) {
                Text(
                    "Cách bạn ${String.format(Locale.US, "%.1f", distanceKm)} km",
                    color = PizzaHutRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = PizzaHutRed, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            content()
        }
    }
}

@Composable
private fun SelectorRow(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F3F5))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) SurfaceWhite else Color.Transparent)
                    .clickable { onSelected(option) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (option) {
                        "CURRENT" -> "Vị trí hiện tại"
                        "SAVED" -> "Đã lưu"
                        "NEAREST" -> "Gần nhất"
                        "ALL" -> "Tất cả"
                        "COD" -> "Tiền mặt"
                        "ONLINE" -> "Chuyển khoản"
                        else -> option
                    },
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) PizzaHutRed else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun BranchSelector(
    branches: List<BranchResponse>,
    selectedBranchId: Long?,
    distanceProvider: (BranchResponse) -> Double?,
    onBranchSelected: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DividerColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        branches.forEachIndexed { index, branch ->
            val selected = branch.id == selectedBranchId
            val distanceKm = distanceProvider(branch)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBranchSelected(branch.id) }
                    .background(if (selected) PizzaHutRed.copy(alpha = 0.03f) else Color.Transparent)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected,
                    onClick = { onBranchSelected(branch.id) },
                    colors = RadioButtonDefaults.colors(selectedColor = PizzaHutRed),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(branch.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (selected) PizzaHutRed else Color.Black)
                    Text(branch.address, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (distanceKm != null) {
                        Text(
                            "Cách bạn: ${String.format(Locale.US, "%.1f", distanceKm)} km",
                            color = PizzaHutRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            if (index < branches.size - 1) {
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun CheckoutField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    icon: ImageVector,
    singleLine: Boolean = true,
    isError: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 13.sp, color = Color.LightGray) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = if (isError) PizzaHutRed else Color.Gray, modifier = Modifier.size(18.dp)) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PizzaHutRed,
                unfocusedBorderColor = DividerColor,
                errorBorderColor = PizzaHutRed,
                focusedContainerColor = Color(0xFFFBFBFB),
                unfocusedContainerColor = Color(0xFFFBFBFB)
            ),
            isError = isError,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
        )
    }
}

@Composable
private fun ShippingFeeCard(quote: ShippingFeeQuoteResponse) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8F9FA),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SummaryRow("Giao từ", quote.branchName, fontSize = 12.sp)
            SummaryRow("Khoảng cách", String.format(Locale.US, "%.2f km", quote.distanceKm), fontSize = 12.sp)
            SummaryRow("Phí vận chuyển", CurrencyUtils.formatVnd(quote.shippingFee), valueColor = PizzaHutRed, bold = true)
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    bold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 13.sp
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = fontSize)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium, fontSize = fontSize, color = valueColor)
    }
}

@Composable
private fun StatusLine(icon: ImageVector, text: String, color: Color) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}
