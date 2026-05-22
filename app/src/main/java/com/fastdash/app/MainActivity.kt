package com.fastdash.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fastdash.app.data.model.request.CreateOrderRequest
import com.fastdash.app.data.model.request.OrderItemRequest
import com.fastdash.app.data.model.response.ProductResponse
import com.fastdash.app.data.repository.*
import com.fastdash.app.ui.admin.*
import com.fastdash.app.ui.auth.LoginScreen
import com.fastdash.app.ui.auth.RegisterScreen
import com.fastdash.app.ui.cart.CartScreen
import com.fastdash.app.ui.checkout.CheckoutScreen
import com.fastdash.app.ui.checkout.DeliveryLocation
import com.fastdash.app.ui.checkout.MapPickerScreen
import com.fastdash.app.ui.checkout.PickedLocation
import com.fastdash.app.ui.home.HomeScreen
import com.fastdash.app.ui.order.OrderDetailScreen
import com.fastdash.app.ui.order.OrderDetailUiModel
import com.fastdash.app.ui.order.OrderHistoryScreen
import com.fastdash.app.ui.order.OrderHistoryUiModel
import com.fastdash.app.ui.order.OrderItemUiModel
import com.fastdash.app.ui.order.formatOrderDate
import com.fastdash.app.ui.order.normalizeVietnameseText
import com.fastdash.app.ui.payment.PaymentScreen
import com.fastdash.app.ui.product.ProductDetailScreen
import com.fastdash.app.ui.profile.ProfileScreen
import com.fastdash.app.utils.SavedLocationStore
import com.fastdash.app.ui.theme.FastDash_androidTheme
import com.fastdash.app.utils.TokenManager
import com.fastdash.app.viewmodel.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private enum class AppRoute {
    LOGIN, REGISTER, ADMIN_HOME, ADMIN_PRODUCT, ADMIN_ORDERS, ADMIN_CATEGORIES,
    ADMIN_SIZES, ADMIN_TOPPINGS, ADMIN_USERS, ADMIN_BRANCHES, ADMIN_PAYMENTS,
    ADMIN_PLACEHOLDER, HOME, PRODUCT_DETAIL, ORDER_HISTORY, ORDER_DETAIL,
    PROFILE, CART, CHECKOUT, PICK_LOCATION, PAYMENT
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FastDash_androidTheme {
                val context = LocalContext.current
                val tokenManager = remember { TokenManager(applicationContext) }
                val savedLocationStore = remember { SavedLocationStore(applicationContext) }
                
                // Repositories
                val cartRepository = remember { CartRepository(applicationContext) }
                val productRepository = remember { ProductRepository(applicationContext) }
                val orderRepository = remember { OrderRepository(applicationContext) }
                val userRepository = remember { UserRepository(applicationContext) }
                val adminProductRepository = remember { AdminProductRepository(applicationContext) }
                val adminCategoryRepository = remember { AdminCategoryRepository(applicationContext) }
                val adminSizeRepository = remember { AdminSizeRepository(applicationContext) }
                val adminToppingRepository = remember { AdminToppingRepository(applicationContext) }

                // ViewModels
                val cartViewModel: CartViewModel = viewModel(factory = AppViewModelFactory { CartViewModel(cartRepository) })
                val orderViewModel: OrderViewModel = viewModel(factory = AppViewModelFactory { OrderViewModel(orderRepository) })
                val productDetailViewModel: ProductDetailViewModel = viewModel(factory = AppViewModelFactory { ProductDetailViewModel(productRepository) })
                val profileViewModel: ProfileViewModel = viewModel(factory = AppViewModelFactory { ProfileViewModel(userRepository) })
                
                val scope = rememberCoroutineScope()
                var isLoggedIn by remember { mutableStateOf(!tokenManager.getToken().isNullOrEmpty()) }
                var selectedProduct by remember { mutableStateOf<ProductResponse?>(null) }
                var selectedOrderId by remember { mutableStateOf<Long?>(null) }
                var directCheckoutItem by remember { mutableStateOf<OrderItemRequest?>(null) }
                var directCheckoutTotal by remember { mutableStateOf(0.0) }
                var checkoutPickedLocation by remember { mutableStateOf<PickedLocation?>(null) }
                var checkoutCurrentLocation by remember { mutableStateOf<PickedLocation?>(null) }
                var checkoutLoadingLocation by remember { mutableStateOf(false) }
                var checkoutDraftName by remember { mutableStateOf("") }
                var checkoutDraftPhone by remember { mutableStateOf("") }
                var checkoutDraftAddressDetail by remember { mutableStateOf("") }
                var checkoutDraftNote by remember { mutableStateOf("") }
                var savedDeliveryLocations by remember { mutableStateOf(savedLocationStore.getLocations()) }
                var lastCreatedOrderId by remember { mutableStateOf<Long?>(null) }
                var lastCreatedOrderCreatedAt by remember { mutableStateOf<String?>(null) }
                var lastCreatedBranchName by remember { mutableStateOf<String?>(null) }
                var lastCreatedBranchAddress by remember { mutableStateOf<String?>(null) }
                var lastCreatedOrderNote by remember { mutableStateOf<String?>(null) }
                
                val cartState by cartViewModel.cart.collectAsState()
                val cartMessage by cartViewModel.message.collectAsState()
                val cartLoading by cartViewModel.loading.collectAsState()
                val profileState by profileViewModel.user.collectAsState()
                val productDetailLoading by productDetailViewModel.loading.collectAsState()
                val orderListState by orderViewModel.orders.collectAsState()
                val orderMessage by orderViewModel.message.collectAsState()
                val selectedOrderState by orderViewModel.selectedOrder.collectAsState()

                fun currentRole(): String = tokenManager.getRole().orEmpty().trim().uppercase()
                fun isAdmin(): Boolean = currentRole() == "ADMIN"
                fun rootRoute(): AppRoute = if (isAdmin()) AppRoute.ADMIN_HOME else AppRoute.HOME
                fun safeOrderCode(raw: String?): String = raw.normalizeVietnameseText().takeIf { it.isNotBlank() } ?: "Không có mã đơn"
                fun safeOrderCreatedAt(raw: String?): String = formatOrderDate(raw)
                fun safeOrderStatus(raw: String?): String = raw?.takeIf { it.isNotBlank() } ?: "PENDING"

                val adminProductViewModel: AdminProductViewModel? = if (isAdmin()) {
                    viewModel(
                        factory = AdminProductViewModelFactory(
                            adminProductRepository,
                            adminCategoryRepository,
                            adminToppingRepository,
                            adminSizeRepository,
                            productRepository
                        )
                    )
                } else null
                val adminCategoryViewModel: AdminCategoryViewModel? = if (isAdmin()) {
                    viewModel(factory = AdminCategoryViewModelFactory(adminCategoryRepository))
                } else null
                val adminSizeViewModel: AdminSizeViewModel? = if (isAdmin()) {
                    viewModel(factory = AdminSizeViewModelFactory(adminSizeRepository))
                } else null
                val adminToppingViewModel: AdminToppingViewModel? = if (isAdmin()) {
                    viewModel(factory = AdminToppingViewModelFactory(adminToppingRepository))
                } else null

                var route by remember {
                    mutableStateOf(
                        when {
                            !isLoggedIn -> AppRoute.LOGIN
                            isAdmin() -> AppRoute.ADMIN_HOME
                            else -> AppRoute.HOME
                        }
                    )
                }

                fun logout() {
                    tokenManager.clear()
                    isLoggedIn = false
                    directCheckoutItem = null
                    directCheckoutTotal = 0.0
                    route = AppRoute.LOGIN
                }

                LaunchedEffect(cartMessage) {
                    cartMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        cartViewModel.clearMessage()
                    }
                }

                LaunchedEffect(orderMessage) {
                    orderMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        orderViewModel.clearMessage()
                    }
                }

                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn && !isAdmin()) {
                        cartViewModel.loadCart()
                        profileViewModel.loadProfile()
                        orderViewModel.loadOrders()
                    }
                }

                LaunchedEffect(orderListState) {
                    val merged = (savedDeliveryLocations + orderListState.mapNotNull { order ->
                        val lat = order.deliveryLatitude ?: return@mapNotNull null
                        val lng = order.deliveryLongitude ?: return@mapNotNull null
                        val address = order.deliveryAddress.normalizeVietnameseText().takeIf { it.isNotBlank() } ?: return@mapNotNull null
                        PickedLocation(
                            latitude = lat,
                            longitude = lng,
                            address = address,
                            detailAddress = address
                        )
                    }).distinctBy { "${it.latitude}|${it.longitude}|${it.address}|${it.detailAddress}" }
                    if (merged != savedDeliveryLocations) {
                        savedDeliveryLocations = merged
                        savedLocationStore.saveLocations(merged)
                    }
                }

                if (!isLoggedIn) {
                    when (route) {
                        AppRoute.REGISTER -> RegisterScreen(
                            onBackToLogin = { route = AppRoute.LOGIN },
                            onRegisterSuccess = { route = AppRoute.LOGIN }
                        )
                        else -> LoginScreen(
                            onLoginSuccess = {
                                isLoggedIn = true
                                route = rootRoute()
                            },
                            onOpenRegister = { route = AppRoute.REGISTER }
                        )
                    }
                } else {
                    when (route) {
                        AppRoute.ADMIN_HOME -> AdminDashboardScreen(
                            onOpenProducts = { route = AppRoute.ADMIN_PRODUCT },
                            onOpenOrders = { route = AppRoute.ADMIN_ORDERS },
                            onOpenCategories = { route = AppRoute.ADMIN_CATEGORIES },
                            onOpenSizes = { route = AppRoute.ADMIN_SIZES },
                            onOpenToppings = { route = AppRoute.ADMIN_TOPPINGS },
                            onOpenUsers = { route = AppRoute.ADMIN_USERS },
                            onOpenBranches = { route = AppRoute.ADMIN_BRANCHES },
                            onOpenPayments = { route = AppRoute.ADMIN_PAYMENTS },
                            onOpenPlaceholder = { _, _ -> route = AppRoute.ADMIN_PLACEHOLDER },
                            onLogout = { logout() }
                        )
                        AppRoute.ADMIN_PRODUCT -> AdminProductScreen(adminProductViewModel!!, { route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_CATEGORIES -> AdminCategoriesScreen(adminCategoryViewModel!!, { route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_SIZES -> AdminSizesScreen(adminSizeViewModel!!, { route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_TOPPINGS -> AdminToppingsScreen(adminToppingViewModel!!, { route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_USERS -> AdminUsersScreen({ route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_BRANCHES -> AdminBranchesScreen({ route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_PAYMENTS -> AdminPaymentsScreen({ route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_ORDERS -> AdminOrdersScreen({ route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_PLACEHOLDER -> AdminComingSoonScreen("Coming Soon", "Module is under development", { route = AppRoute.ADMIN_HOME }, { logout() })

                        AppRoute.HOME -> HomeScreen(
                            onOpenProduct = { product ->
                                selectedProduct = product
                                productDetailViewModel.loadDetails(product.id)
                                route = AppRoute.PRODUCT_DETAIL
                            },
                            onCheckout = { route = AppRoute.CART },
                            onAddToCart = { product ->
                                if (product.isCustomizable == 1) {
                                    selectedProduct = product
                                    productDetailViewModel.loadDetails(product.id)
                                    route = AppRoute.PRODUCT_DETAIL
                                } else {
                                    cartViewModel.addToCart(product.id, 1, null, emptyList())
                                }
                            },
                            orders = orderListState.map {
                                val totalAmount = it.totalAmount.takeIf { amount -> amount > 0.0 } ?: (it.subtotal + it.shippingFee - it.discountAmount)
                                val normalizedItems = it.items.orEmpty()
                                val firstItemName = normalizedItems.firstOrNull()?.productName.normalizeVietnameseText().orEmpty()
                                val firstItemQuantity = normalizedItems.firstOrNull()?.quantity ?: 0
                                val createdAtValue = if (it.id == lastCreatedOrderId && it.createdAt.isNullOrBlank()) lastCreatedOrderCreatedAt else it.createdAt
                                val itemPreview = when {
                                    firstItemName.isNotBlank() -> {
                                        val firstItemLabel = "$firstItemName x${firstItemQuantity.coerceAtLeast(1)}"
                                        if (normalizedItems.size > 1) "$firstItemLabel và ${normalizedItems.size - 1} món khác" else firstItemLabel
                                    }
                                    normalizedItems.sumOf { item -> item.quantity } > 0 -> "${normalizedItems.sumOf { item -> item.quantity }} món"
                                    else -> "Đơn giao hàng"
                                }
                                OrderHistoryUiModel(
                                    it.id,
                                    safeOrderCode(it.orderCode),
                                    safeOrderCreatedAt(createdAtValue),
                                    it.items?.size ?: 0,
                                    totalAmount,
                                    safeOrderStatus(it.status),
                                    itemPreview
                                )
                            },
                            onOpenOrder = {
                                selectedOrderId = it.id
                                orderViewModel.loadOrderDetail(it.id)
                                route = AppRoute.ORDER_DETAIL
                            },
                            profileFullName = profileState?.fullName ?: "User",
                            profileEmail = profileState?.email ?: "",
                            profilePhone = profileState?.phone ?: "",
                            profileRole = profileState?.role?.ifBlank { "USER" } ?: "USER",
                            deliveryAddress = profileState?.address ?: "",
                            onLogout = { logout() },
                            cartCount = cartState?.items?.size ?: 0,
                            cartTotal = cartState?.total ?: 0.0
                        )
                        AppRoute.PRODUCT_DETAIL -> {
                            selectedProduct?.let { product ->
                                val sizes by productDetailViewModel.sizes.collectAsState()
                                val toppings by productDetailViewModel.toppings.collectAsState()
                                ProductDetailScreen(
                                    product = product,
                                    sizes = sizes,
                                    toppings = toppings,
                                    isLoading = productDetailLoading,
                                    onBack = { route = rootRoute() },
                                    onAddToCart = { pid, q, s, t ->
                                        directCheckoutItem = null
                                        directCheckoutTotal = 0.0
                                        cartViewModel.addToCart(pid, q, s, t)
                                        route = AppRoute.CART
                                    },
                                    onBuyNow = { pid, q, s, t, total ->
                                        directCheckoutItem = OrderItemRequest(
                                            productId = pid,
                                            productSizeId = s,
                                            quantity = q,
                                            toppingIds = t
                                        )
                                        directCheckoutTotal = total
                                        route = AppRoute.CHECKOUT
                                    }
                                )
                            }
                        }
                        AppRoute.ORDER_HISTORY -> {
                            val uiOrders = orderListState.map { 
                                val normalizedItems = it.items.orEmpty()
                                val firstItemName = normalizedItems.firstOrNull()?.productName.normalizeVietnameseText().orEmpty()
                                val firstItemQuantity = normalizedItems.firstOrNull()?.quantity ?: 0
                                val totalQuantity = normalizedItems.sumOf { item -> item.quantity }
                                val createdAtValue = if (it.id == lastCreatedOrderId && it.createdAt.isNullOrBlank()) lastCreatedOrderCreatedAt else it.createdAt
                                val itemPreview = when {
                                    firstItemName.isNotBlank() -> {
                                        val firstItemLabel = "$firstItemName x${firstItemQuantity.coerceAtLeast(1)}"
                                        if (normalizedItems.size > 1) "$firstItemLabel và ${normalizedItems.size - 1} món khác" else firstItemLabel
                                    }
                                    totalQuantity > 0 -> "$totalQuantity món"
                                    else -> "Đơn giao hàng"
                                }
                                OrderHistoryUiModel(
                                    it.id,
                                    safeOrderCode(it.code),
                                    safeOrderCreatedAt(createdAtValue),
                                    totalQuantity,
                                    it.totalAmount.takeIf { amount -> amount > 0.0 } ?: (it.subtotal + it.shippingFee - it.discountAmount),
                                    safeOrderStatus(it.status),
                                    itemPreview
                                )
                            }
                            OrderHistoryScreen(orders = uiOrders, onBack = { route = rootRoute() }, onOpenOrder = { 
                                selectedOrderId = it.id
                                orderViewModel.loadOrderDetail(it.id)
                                route = AppRoute.ORDER_DETAIL
                            })
                        }
                        AppRoute.ORDER_DETAIL -> {
                            selectedOrderState?.let { order ->
                                val subtotal = order.subtotal.takeIf { it > 0.0 }
                                    ?: order.items.orEmpty().sumOf { item -> item.totalPrice.takeIf { total -> total > 0.0 } ?: (item.unitPrice * item.quantity) }
                                val totalAmount = order.totalAmount.takeIf { it > 0.0 }
                                    ?: (subtotal + order.shippingFee - order.discountAmount)
                                OrderDetailScreen(
                                    order = OrderDetailUiModel(
                                        id = order.id,
                                        orderCode = safeOrderCode(order.orderCode),
                                        status = safeOrderStatus(order.status),
                                        createdAt = safeOrderCreatedAt(
                                            if (order.id == lastCreatedOrderId && order.createdAt.isNullOrBlank()) lastCreatedOrderCreatedAt else order.createdAt
                                        ),
                                        receiverName = order.receiverName.normalizeVietnameseText().ifBlank { "Chưa có thông tin" },
                                        receiverPhone = order.receiverPhone.normalizeVietnameseText().ifBlank { "Chưa có thông tin" },
                                        deliveryAddress = order.deliveryAddress.normalizeVietnameseText().ifBlank { "Chưa có địa chỉ giao hàng" },
                                        branchName = order.branchName.normalizeVietnameseText().ifBlank {
                                            if (order.id == lastCreatedOrderId) lastCreatedBranchName.normalizeVietnameseText().ifBlank { "Chưa có thông tin" } else "Chưa có thông tin"
                                        },
                                        branchAddress = order.branchAddress.normalizeVietnameseText().ifBlank {
                                            if (order.id == lastCreatedOrderId) lastCreatedBranchAddress.normalizeVietnameseText() else ""
                                        },
                                        distanceKm = order.distanceKm,
                                        paymentMethod = order.paymentMethod.orEmpty(),
                                        paymentStatus = order.paymentStatus.orEmpty(),
                                        subtotal = subtotal,
                                        shippingFee = order.shippingFee,
                                        discountAmount = order.discountAmount,
                                        totalAmount = totalAmount,
                                        note = order.note.normalizeVietnameseText().ifBlank {
                                            if (order.id == lastCreatedOrderId) lastCreatedOrderNote.normalizeVietnameseText() else ""
                                        },
                                        items = order.items.orEmpty().map { item ->
                                            OrderItemUiModel(
                                                id = item.id,
                                                name = item.productName.normalizeVietnameseText().ifBlank { "Món đã đặt" },
                                                sizeName = item.sizeName.normalizeVietnameseText().orEmpty(),
                                                toppings = item.toppings.map { topping -> topping.normalizeVietnameseText() }.filter { it.isNotBlank() },
                                                quantity = item.quantity,
                                                unitPrice = item.unitPrice,
                                                note = item.note.normalizeVietnameseText()
                                            )
                                        }
                                    ),
                                    onBack = { route = AppRoute.ORDER_HISTORY },
                                    onReorder = { route = AppRoute.HOME },
                                    onCancelOrder = {
                                        scope.launch {
                                            if (orderViewModel.cancelOrder(it.id)) {
                                                selectedOrderId?.let { id -> orderViewModel.loadOrderDetail(id) }
                                            }
                                        }
                                    }
                                )
                            } ?: run {
                                selectedOrderId?.let { orderViewModel.loadOrderDetail(it) }
                            }
                        }
                        AppRoute.CART -> CartScreen(
                            cartItems = cartState?.items ?: emptyList(),
                            subtotal = cartState?.subtotal ?: 0.0,
                            isLoading = cartLoading,
                            errorMessage = cartMessage,
                            onBack = {
                                directCheckoutItem = null
                                directCheckoutTotal = 0.0
                                route = rootRoute()
                            },
                            onUpdateQuantity = { itemId, quantity ->
                                cartViewModel.updateCartItem(itemId, quantity)
                            },
                            onRemoveItem = { cartViewModel.removeFromCart(it) },
                            onCheckout = {
                                directCheckoutItem = null
                                directCheckoutTotal = 0.0
                                route = AppRoute.CHECKOUT
                            },
                            onRetry = { cartViewModel.loadCart() },
                            onBrowseMenu = { route = rootRoute() }
                        )
                        AppRoute.CHECKOUT -> CheckoutScreen(
                            subtotal = directCheckoutItem?.let { directCheckoutTotal } ?: (cartState?.subtotal ?: 0.0),
                            initialFullName = checkoutDraftName.ifBlank { profileState?.fullName ?: "" },
                            initialPhone = checkoutDraftPhone.ifBlank { profileState?.phone ?: "" },
                            initialAddress = profileState?.address ?: "",
                            initialAddressDetail = checkoutDraftAddressDetail,
                            initialNote = checkoutDraftNote,
                            pickedLocation = checkoutPickedLocation,
                            savedLocations = savedDeliveryLocations,
                            onBack = { route = if (directCheckoutItem != null) AppRoute.PRODUCT_DETAIL else AppRoute.CART },
                            onOpenMapPicker = { route = AppRoute.PICK_LOCATION },
                            onCurrentLocationChanged = { checkoutCurrentLocation = it },
                            onDeliveryLocationChanged = { checkoutPickedLocation = it },
                            onRecipientNameChanged = { checkoutDraftName = it },
                            onRecipientPhoneChanged = { checkoutDraftPhone = it },
                            onAddressDetailChanged = { checkoutDraftAddressDetail = it },
                            onNoteChanged = { checkoutDraftNote = it },
                            onConfirm = { request ->
                                scope.launch {
                                    val createdAtFallback = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                    val createdOrder = directCheckoutItem?.let { item ->
                                        orderViewModel.createOrder(
                                            CreateOrderRequest(
                                                branchId = request.branchId,
                                                deliveryType = request.deliveryType,
                                                receiverName = request.receiverName,
                                                receiverPhone = request.receiverPhone,
                                                deliveryAddress = request.deliveryAddress,
                                                deliveryLatitude = request.deliveryLatitude,
                                                deliveryLongitude = request.deliveryLongitude,
                                                note = request.note,
                                                paymentMethod = request.paymentMethod,
                                                items = listOf(item)
                                            )
                                        )
                                    } ?: orderViewModel.createOrderFromCart(request)

                                    if (createdOrder != null) {
                                        if (directCheckoutItem == null) {
                                            cartViewModel.loadCart()
                                        }
                                        orderViewModel.loadOrders()
                                        checkoutPickedLocation?.let {
                                            savedLocationStore.addLocation(it)
                                            savedDeliveryLocations = savedLocationStore.getLocations()
                                        }
                                        lastCreatedOrderId = createdOrder.id.takeIf { it > 0L }
                                        lastCreatedOrderCreatedAt = createdOrder.createdAt ?: createdAtFallback
                                        lastCreatedBranchName = createdOrder.branchName ?: request.branchName
                                        lastCreatedBranchAddress = createdOrder.branchAddress ?: request.branchAddress
                                        lastCreatedOrderNote = createdOrder.note ?: request.note
                                        selectedOrderId = createdOrder.id.takeIf { it > 0L }
                                        directCheckoutItem = null
                                        directCheckoutTotal = 0.0
                                        checkoutPickedLocation = null
                                        checkoutCurrentLocation = null
                                        checkoutDraftName = ""
                                        checkoutDraftPhone = ""
                                        checkoutDraftAddressDetail = ""
                                        checkoutDraftNote = ""
                                        route = if (selectedOrderId != null) AppRoute.ORDER_DETAIL else AppRoute.ORDER_HISTORY
                                    }
                                }
                            }
                        )
                        AppRoute.PICK_LOCATION -> MapPickerScreen(
                            initialLocation = checkoutPickedLocation?.let {
                                DeliveryLocation(
                                    address = it.address,
                                    latitude = it.latitude,
                                    longitude = it.longitude,
                                    detailAddress = it.detailAddress
                                )
                            },
                            onBack = { route = AppRoute.CHECKOUT },
                            onConfirm = { location ->
                                checkoutPickedLocation = PickedLocation(
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    address = location.address,
                                    detailAddress = location.detailAddress
                                )
                                route = AppRoute.CHECKOUT
                            }
                        )
                        AppRoute.PAYMENT -> PaymentScreen(
                            amount = cartState?.total ?: 0.0,
                            onBack = { route = rootRoute() },
                            onConfirmPayment = {
                                scope.launch {
                                    cartRepository.clearCart()
                                    cartViewModel.loadCart()
                                    route = rootRoute()
                                }
                            }
                        )
                        AppRoute.PROFILE -> ProfileScreen(
                            fullName = profileState?.fullName ?: "User",
                            email = profileState?.email ?: "",
                            phone = profileState?.phone ?: "",
                            role = profileState?.role?.ifBlank { "USER" } ?: "USER",
                            onBack = { route = rootRoute() },
                            onOpenOrders = { route = AppRoute.ORDER_HISTORY },
                            onLogout = { logout() }
                        )
                        else -> route = rootRoute()
                    }
                }
            }
        }
    }
}









