package com.fastdash.app.ui.checkout

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

private val FastDashRed = Color(0xFFC8102E)
private val SurfaceWhite = Color.White
private val BackgroundGrey = Color(0xFFF8F9FA)
private val TextSecondary = Color(0xFF6C757D)
private val DividerColor = Color(0xFFE9ECEF)
private val DefaultMapPoint = LatLng(16.0471, 108.2068)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    initialLocation: DeliveryLocation?,
    onBack: () -> Unit,
    onConfirm: (DeliveryLocation) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val startPoint = remember(initialLocation) {
        initialLocation?.let { LatLng(it.latitude, it.longitude) } ?: DefaultMapPoint
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPoint, 15f)
    }

    var selectedPoint by remember { mutableStateOf(startPoint) }
    var detailAddress by rememberSaveable { mutableStateOf(initialLocation?.detailAddress.orEmpty()) }
    var resolvedAddress by rememberSaveable { mutableStateOf(initialLocation?.address.orEmpty()) }
    var resolvingAddress by remember { mutableStateOf(false) }
    var loadingCurrentLocation by remember { mutableStateOf(false) }

    suspend fun updateSelectedPoint(point: LatLng) {
        selectedPoint = point
        resolvingAddress = true
        resolvedAddress = reverseGeocode(context, point.latitude, point.longitude)
        resolvingAddress = false
    }

    fun loadCurrentLocation() {
        scope.launch {
            loadingCurrentLocation = true
            val location = getCurrentLatLng(fusedLocationClient)
            loadingCurrentLocation = false
            if (location == null) {
                Toast.makeText(context, "Không lấy được vị trí hiện tại", Toast.LENGTH_SHORT).show()
                return@launch
            }
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(location, 16f))
            updateSelectedPoint(location)
        }
    }

    LaunchedEffect(startPoint.latitude, startPoint.longitude) {
        if (resolvedAddress.isBlank()) {
            resolvingAddress = true
            resolvedAddress = reverseGeocode(context, startPoint.latitude, startPoint.longitude)
            resolvingAddress = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            loadCurrentLocation()
        } else {
            Toast.makeText(context, "Cần cấp quyền vị trí để dùng vị trí hiện tại", Toast.LENGTH_SHORT).show()
        }
    }

    val disableReason = when {
        resolvingAddress -> "Đang lấy địa chỉ gần đúng"
        resolvedAddress.isBlank() -> "Không lấy được địa chỉ gần đúng"
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn vị trí giao hàng", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        bottomBar = {
            Surface(color = SurfaceWhite, shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Địa chỉ gần đúng", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                    Text(
                        if (resolvedAddress.isBlank()) "Chưa có địa chỉ" else resolvedAddress,
                        color = if (resolvedAddress.isBlank()) TextSecondary else Color.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        "Tọa độ: ${String.format(Locale.US, "%.4f", selectedPoint.latitude)}, ${String.format(Locale.US, "%.4f", selectedPoint.longitude)}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = detailAddress,
                        onValueChange = { detailAddress = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Địa chỉ chi tiết") },
                        placeholder = { Text("Số nhà, tên đường, ghi chú giao hàng...") },
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FastDashRed,
                            unfocusedBorderColor = DividerColor
                        )
                    )
                    if (disableReason != null) {
                        Text(disableReason, color = FastDashRed, fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            onConfirm(
                                DeliveryLocation(
                                    address = resolvedAddress,
                                    latitude = selectedPoint.latitude,
                                    longitude = selectedPoint.longitude,
                                    detailAddress = detailAddress.trim()
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = disableReason == null,
                        colors = ButtonDefaults.buttonColors(containerColor = FastDashRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Xác nhận vị trí giao hàng", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = BackgroundGrey
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = true
                ),
                onMapClick = { point ->
                    scope.launch {
                        updateSelectedPoint(point)
                    }
                }
            ) {
                Marker(
                    state = MarkerState(position = selectedPoint),
                    title = "Vị trí giao hàng"
                )
            }
            if (resolvingAddress) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = SurfaceWhite.copy(alpha = 0.95f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Text("Đang lấy địa chỉ...", fontSize = 12.sp)
                    }
                }
            }
            Button(
                onClick = {
                    val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    if (permission == PackageManager.PERMISSION_GRANTED) {
                        loadCurrentLocation()
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceWhite, contentColor = FastDashRed),
                shape = RoundedCornerShape(999.dp)
            ) {
                if (loadingCurrentLocation) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = FastDashRed)
                } else {
                    Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.size(8.dp))
                Text("Vị trí hiện tại")
            }
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = FastDashRed,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 24.dp)
                    .size(40.dp)
            )
        }
    }
}

private suspend fun getCurrentLatLng(
    fusedLocationClient: FusedLocationProviderClient
): LatLng? = suspendCancellableCoroutine { continuation ->
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            continuation.resume(location?.let { LatLng(it.latitude, it.longitude) })
        }
        .addOnFailureListener {
            continuation.resume(null)
        }
}

private suspend fun reverseGeocode(
    context: Context,
    latitude: Double,
    longitude: Double
): String = withContext(Dispatchers.IO) {
    try {
        val geocoder = Geocoder(context, Locale("vi", "VN"))
        val address = runCatching {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latitude, longitude, 1)
        }.getOrNull()?.firstOrNull()
        address?.toReadableAddress().orEmpty().ifBlank {
            String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
        }
    } catch (_: Exception) {
        String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
    }
}

private fun Address.toReadableAddress(): String {
    fun isPlusCode(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        return Regex("^[A-Z0-9]{4,}\\+[A-Z0-9]{2,}$").matches(value.trim())
    }

    val houseAndStreet = listOfNotNull(
        subThoroughfare?.takeIf { it.isNotBlank() },
        thoroughfare?.takeIf { it.isNotBlank() }
    ).joinToString(" ").trim()

    val parts = listOf(
        houseAndStreet.ifBlank { null },
        subLocality,
        locality,
        adminArea
    ).mapNotNull { it?.trim() }
        .filter { it.isNotBlank() && !isPlusCode(it) }
        .distinct()

    if (parts.isNotEmpty()) return parts.joinToString(", ")

    val line0 = getAddressLine(0).orEmpty().trim()
    if (line0.isNotBlank() && !isPlusCode(line0)) return line0

    return listOfNotNull(subLocality, locality, adminArea)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(", ")
}
