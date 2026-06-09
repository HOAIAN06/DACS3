package com.fastdash.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fastdash.app.data.model.request.CreateOrderRequest
import com.fastdash.app.data.model.request.OrderItemRequest
import com.fastdash.app.data.model.response.OrderResponse
import com.fastdash.app.data.model.response.ProductResponse
import com.fastdash.app.data.repository.AdminBranchRepository
import com.fastdash.app.data.repository.AdminCategoryRepository
import com.fastdash.app.data.repository.AdminCustomerRepository
import com.fastdash.app.data.repository.AdminDashboardRepository
import com.fastdash.app.data.repository.AdminOrderRepository
import com.fastdash.app.data.repository.AdminProductRepository
import com.fastdash.app.data.repository.AdminSizeRepository
import com.fastdash.app.data.repository.AdminToppingRepository
import com.fastdash.app.data.repository.CartRepository
import com.fastdash.app.data.repository.OrderRepository
import com.fastdash.app.data.repository.ProductRepository
import com.fastdash.app.data.repository.UserRepository
import com.fastdash.app.ui.admin.AdminBranchesScreen
import com.fastdash.app.ui.admin.AdminCategoriesScreen
import com.fastdash.app.ui.admin.AdminComingSoonScreen
import com.fastdash.app.ui.admin.AdminCustomersScreen
import com.fastdash.app.ui.admin.AdminDashboardScreen
import com.fastdash.app.ui.admin.AdminOrderDetailScreen
import com.fastdash.app.ui.admin.AdminOrdersScreen
import com.fastdash.app.ui.admin.AdminProductScreen
import com.fastdash.app.ui.admin.AdminRevenueScreen
import com.fastdash.app.ui.admin.AdminSizesScreen
import com.fastdash.app.ui.admin.AdminToppingsScreen
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
import com.fastdash.app.ui.payment.PaymentScreenState
import com.fastdash.app.ui.product.ProductDetailScreen
import com.fastdash.app.ui.profile.EditProfileScreen
import com.fastdash.app.ui.profile.ProfileScreen
import com.fastdash.app.ui.theme.FastDash_androidTheme
import com.fastdash.app.utils.PendingPaymentStore
import com.fastdash.app.utils.SavedLocationStore
import com.fastdash.app.utils.TokenManager
import com.fastdash.app.viewmodel.AdminBranchViewModel
import com.fastdash.app.viewmodel.AdminBranchViewModelFactory
import com.fastdash.app.viewmodel.AdminCategoryViewModel
import com.fastdash.app.viewmodel.AdminCategoryViewModelFactory
import com.fastdash.app.viewmodel.AdminCustomerViewModel
import com.fastdash.app.viewmodel.AdminCustomerViewModelFactory
import com.fastdash.app.viewmodel.AdminDashboardViewModel
import com.fastdash.app.viewmodel.AdminDashboardViewModelFactory
import com.fastdash.app.viewmodel.AdminOrderDetailViewModel
import com.fastdash.app.viewmodel.AdminOrderDetailViewModelFactory
import com.fastdash.app.viewmodel.AdminOrdersViewModel
import com.fastdash.app.viewmodel.AdminOrdersViewModelFactory
import com.fastdash.app.viewmodel.AdminProductViewModel
import com.fastdash.app.viewmodel.AdminProductViewModelFactory
import com.fastdash.app.viewmodel.AdminSizeViewModel
import com.fastdash.app.viewmodel.AdminSizeViewModelFactory
import com.fastdash.app.viewmodel.AdminToppingViewModel
import com.fastdash.app.viewmodel.AdminToppingViewModelFactory
import com.fastdash.app.viewmodel.AppViewModelFactory
import com.fastdash.app.viewmodel.CartViewModel
import com.fastdash.app.viewmodel.OrderViewModel
import com.fastdash.app.viewmodel.ProductDetailViewModel
import com.fastdash.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private enum class AppRoute {
    LOGIN, REGISTER, ADMIN_HOME, ADMIN_PRODUCT, ADMIN_ORDERS, ADMIN_CATEGORIES,
    ADMIN_ORDER_DETAIL, ADMIN_SIZES, ADMIN_TOPPINGS, ADMIN_CUSTOMERS, ADMIN_BRANCHES, ADMIN_REVENUE,
    ADMIN_PLACEHOLDER, HOME, PRODUCT_DETAIL, ORDER_HISTORY, ORDER_DETAIL,
    PROFILE, EDIT_PROFILE, CART, CHECKOUT, PICK_LOCATION, PAYMENT
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FastDash_androidTheme {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val tokenManager = remember { TokenManager(applicationContext) }
                val savedLocationStore = remember { SavedLocationStore(applicationContext) }
                val pendingPaymentStore = remember { PendingPaymentStore(applicationContext) }

                val cartRepository = remember { CartRepository(applicationContext) }
                val productRepository = remember { ProductRepository(applicationContext) }
                val orderRepository = remember { OrderRepository(applicationContext) }
                val userRepository = remember { UserRepository(applicationContext) }
                val adminProductRepository = remember { AdminProductRepository(applicationContext) }
                val adminBranchRepository = remember { AdminBranchRepository(applicationContext) }
                val adminCategoryRepository = remember { AdminCategoryRepository(applicationContext) }
                val adminDashboardRepository = remember { AdminDashboardRepository(applicationContext) }
                val adminOrderRepository = remember { AdminOrderRepository(applicationContext) }
                val adminSizeRepository = remember { AdminSizeRepository(applicationContext) }
                val adminToppingRepository = remember { AdminToppingRepository(applicationContext) }
                val adminCustomerRepository = remember { AdminCustomerRepository(applicationContext) }

                val cartViewModel: CartViewModel = viewModel(factory = AppViewModelFactory { CartViewModel(cartRepository) })
                val orderViewModel: OrderViewModel = viewModel(factory = AppViewModelFactory { OrderViewModel(orderRepository) })
                val productDetailViewModel: ProductDetailViewModel = viewModel(factory = AppViewModelFactory { ProductDetailViewModel(productRepository) })
                val profileViewModel: ProfileViewModel = viewModel(factory = AppViewModelFactory { ProfileViewModel(userRepository) })

                val scope = rememberCoroutineScope()
                var isLoggedIn by remember { mutableStateOf(!tokenManager.getToken().isNullOrEmpty()) }
                var selectedProduct by remember { mutableStateOf<ProductResponse?>(null) }
                var selectedOrderId by remember { mutableStateOf<Long?>(null) }
                var adminSelectedOrderId by remember { mutableStateOf<Long?>(null) }
                var adminOrdersInitialStatus by remember { mutableStateOf<String?>(null) }
                var directCheckoutItem by remember { mutableStateOf<OrderItemRequest?>(null) }
                var directCheckoutTotal by remember { mutableStateOf(0.0) }
                var checkoutPickedLocation by remember { mutableStateOf<PickedLocation?>(null) }
                var checkoutCurrentLocation by remember { mutableStateOf<PickedLocation?>(null) }
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

                var paymentOrderId by remember { mutableStateOf(pendingPaymentStore.getOrderId()) }
                var paymentUrl by remember { mutableStateOf(pendingPaymentStore.getPaymentUrl()) }
                var paymentOrderCode by remember { mutableStateOf<String?>(null) }
                var paymentAmount by remember { mutableStateOf(0.0) }
                var paymentScreenState by remember {
                    mutableStateOf(
                        if (paymentOrderId != null) PaymentScreenState.CHECKING else PaymentScreenState.OPENING
                    )
                }
                var paymentStatusMessage by remember { mutableStateOf<String?>(null) }
                var paymentCheckLoading by remember { mutableStateOf(false) }
                var paymentOpenLoading by remember { mutableStateOf(false) }
                var paymentReturnArmed by remember { mutableStateOf(false) }
                var paymentShouldCheckOnEnter by remember { mutableStateOf(paymentOrderId != null) }

                val cartState by cartViewModel.cart.collectAsState()
                val cartMessage by cartViewModel.message.collectAsState()
                val cartLoading by cartViewModel.loading.collectAsState()
                val profileState by profileViewModel.user.collectAsState()
                val profileLoading by profileViewModel.isLoading.collectAsState()
                val profileSaving by profileViewModel.isSaving.collectAsState()
                val profileErrorMessage by profileViewModel.errorMessage.collectAsState()
                val profileSuccessMessage by profileViewModel.successMessage.collectAsState()
                val productDetailLoading by productDetailViewModel.loading.collectAsState()
                val orderListState by orderViewModel.orders.collectAsState()
                val orderMessage by orderViewModel.message.collectAsState()
                val selectedOrderState by orderViewModel.selectedOrder.collectAsState()

                fun currentRole(): String = tokenManager.getRole().orEmpty().trim().uppercase()
                fun isAdmin(): Boolean = currentRole() == "ADMIN"
                fun rootRoute(): AppRoute = if (isAdmin()) AppRoute.ADMIN_HOME else AppRoute.HOME
                fun safeOrderCode(raw: String?): String = raw.normalizeVietnameseText().takeIf { it.isNotBlank() } ?: "Khong co ma don"
                fun safeOrderCreatedAt(raw: String?): String = formatOrderDate(raw)
                fun safeOrderStatus(raw: String?): String = raw?.takeIf { it.isNotBlank() } ?: "PENDING"
                fun calculateOrderTotal(order: OrderResponse): Double {
                    return order.totalAmount.takeIf { it > 0.0 }
                        ?: (order.subtotal + order.shippingFee - order.discountAmount)
                }

                fun clearCheckoutDrafts() {
                    directCheckoutItem = null
                    directCheckoutTotal = 0.0
                    checkoutPickedLocation = null
                    checkoutCurrentLocation = null
                    checkoutDraftName = ""
                    checkoutDraftPhone = ""
                    checkoutDraftAddressDetail = ""
                    checkoutDraftNote = ""
                }

                fun cacheOrderMetadata(
                    order: OrderResponse,
                    createdAtFallback: String,
                    branchNameFallback: String?,
                    branchAddressFallback: String?,
                    noteFallback: String?
                ) {
                    lastCreatedOrderId = order.id.takeIf { it > 0L }
                    lastCreatedOrderCreatedAt = order.createdAt ?: createdAtFallback
                    lastCreatedBranchName = order.branchName ?: branchNameFallback
                    lastCreatedBranchAddress = order.branchAddress ?: branchAddressFallback
                    lastCreatedOrderNote = order.note ?: noteFallback
                    selectedOrderId = order.id.takeIf { it > 0L }
                    paymentOrderCode = order.orderCode ?: paymentOrderCode
                    paymentAmount = calculateOrderTotal(order)
                }

                fun clearPersistedPendingPayment() {
                    pendingPaymentStore.clear()
                }

                fun openPaymentInBrowser(url: String): Boolean {
                    val parsedUrl = runCatching { Uri.parse(url) }.getOrNull() ?: return false
                    return runCatching {
                        CustomTabsIntent.Builder().build().launchUrl(context, parsedUrl)
                        true
                    }.recoverCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, parsedUrl))
                        true
                    }.getOrDefault(false)
                }

                suspend fun refreshPaymentStatus(showCheckingState: Boolean = true) {
                    val orderId = paymentOrderId ?: return
                    if (showCheckingState) {
                        paymentScreenState = PaymentScreenState.CHECKING
                    }
                    paymentCheckLoading = true
                    try {
                        val order = orderViewModel.fetchOrderDetail(orderId)
                        if (order == null) {
                            paymentStatusMessage = "Khong the kiem tra trang thai thanh toan."
                            if (paymentScreenState == PaymentScreenState.CHECKING) {
                                paymentScreenState = PaymentScreenState.PENDING
                            }
                            return
                        }

                        cacheOrderMetadata(
                            order = order,
                            createdAtFallback = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            branchNameFallback = lastCreatedBranchName,
                            branchAddressFallback = lastCreatedBranchAddress,
                            noteFallback = lastCreatedOrderNote
                        )
                        selectedOrderId = order.id.takeIf { it > 0L }
                        paymentUrl = order.paymentUrl ?: paymentUrl
                        paymentOrderCode = order.orderCode ?: paymentOrderCode
                        paymentAmount = calculateOrderTotal(order)

                        val normalizedPaymentStatus = order.paymentStatus.orEmpty().trim().uppercase()
                        val normalizedOrderStatus = (order.orderStatus ?: order.status).orEmpty().trim().uppercase()

                        when {
                            normalizedPaymentStatus == "PAID" && normalizedOrderStatus == "PENDING_CONFIRMATION" -> {
                                paymentScreenState = PaymentScreenState.PAID
                                paymentStatusMessage = "Don hang dang cho cua hang xac nhan."
                                paymentReturnArmed = false
                                paymentShouldCheckOnEnter = false
                                clearPersistedPendingPayment()
                                orderViewModel.loadOrders()
                            }
                            normalizedPaymentStatus == "FAILED" || normalizedOrderStatus == "PAYMENT_FAILED" -> {
                                paymentScreenState = PaymentScreenState.FAILED
                                paymentStatusMessage = "Ban co the thu thanh toan lai cho don hang nay."
                                paymentReturnArmed = false
                                paymentShouldCheckOnEnter = false
                                clearPersistedPendingPayment()
                            }
                            normalizedPaymentStatus == "PENDING" && normalizedOrderStatus == "PENDING_PAYMENT" -> {
                                paymentScreenState = PaymentScreenState.PENDING
                                paymentStatusMessage = "Don hang van dang cho thanh toan."
                            }
                            else -> {
                                paymentScreenState = PaymentScreenState.PENDING
                                paymentStatusMessage = "Trang thai thanh toan chua hoan tat. Vui long kiem tra lai."
                            }
                        }
                    } finally {
                        paymentCheckLoading = false
                    }
                }

                suspend fun openPendingPayment(forceRefreshUrl: Boolean = false) {
                    val orderId = paymentOrderId ?: return
                    paymentOpenLoading = true
                    paymentScreenState = PaymentScreenState.OPENING
                    try {
                        var targetUrl = paymentUrl
                        if (forceRefreshUrl || targetUrl.isNullOrBlank()) {
                            val response = orderViewModel.createVnpayPayment(orderId)
                            targetUrl = response?.paymentUrl
                            if (!targetUrl.isNullOrBlank()) {
                                paymentUrl = targetUrl
                                pendingPaymentStore.save(orderId, targetUrl)
                            }
                        }

                        if (targetUrl.isNullOrBlank()) {
                            paymentScreenState = PaymentScreenState.PENDING
                            paymentStatusMessage = "Khong lay duoc link thanh toan VNPAY. Ban co the thu lai sau."
                            return
                        }

                        if (openPaymentInBrowser(targetUrl)) {
                            paymentReturnArmed = true
                            paymentScreenState = PaymentScreenState.CHECKING
                            paymentStatusMessage = "Thanh toan xong, vui long quay lai ung dung FastDash de tiep tuc."
                        } else {
                            paymentScreenState = PaymentScreenState.PENDING
                            paymentStatusMessage = "Khong the mo cong thanh toan VNPAY tren thiet bi nay."
                        }
                    } finally {
                        paymentOpenLoading = false
                    }
                }

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
                val adminBranchViewModel: AdminBranchViewModel? = if (isAdmin()) {
                    viewModel(factory = AdminBranchViewModelFactory(adminBranchRepository))
                } else null
                val adminDashboardViewModel: AdminDashboardViewModel? = if (isAdmin()) {
                    viewModel(factory = AdminDashboardViewModelFactory(adminDashboardRepository))
                } else null
                val adminOrdersViewModel: AdminOrdersViewModel? = if (isAdmin()) {
                    viewModel(factory = AdminOrdersViewModelFactory(adminOrderRepository, adminDashboardRepository))
                } else null
                val adminOrderDetailViewModel: AdminOrderDetailViewModel? = if (isAdmin()) {
                    viewModel(factory = AdminOrderDetailViewModelFactory(adminOrderRepository))
                } else null
                val adminSizeViewModel: AdminSizeViewModel? = if (isAdmin()) {
                    viewModel(factory = AdminSizeViewModelFactory(adminSizeRepository))
                } else null
                val adminToppingViewModel: AdminToppingViewModel? = if (isAdmin()) {
                    viewModel(factory = AdminToppingViewModelFactory(adminToppingRepository))
                } else null
                val adminCustomerViewModel: AdminCustomerViewModel? = if (isAdmin()) {
                    viewModel(factory = AdminCustomerViewModelFactory(adminCustomerRepository))
                } else null

                var route by remember {
                    mutableStateOf(
                        when {
                            !isLoggedIn -> AppRoute.LOGIN
                            isAdmin() -> AppRoute.ADMIN_HOME
                            paymentOrderId != null -> AppRoute.PAYMENT
                            else -> AppRoute.HOME
                        }
                    )
                }

                fun logout() {
                    tokenManager.clear()
                    isLoggedIn = false
                    adminSelectedOrderId = null
                    clearCheckoutDrafts()
                    paymentOrderId = null
                    paymentUrl = null
                    paymentOrderCode = null
                    paymentAmount = 0.0
                    paymentScreenState = PaymentScreenState.OPENING
                    paymentStatusMessage = null
                    paymentReturnArmed = false
                    paymentShouldCheckOnEnter = false
                    clearPersistedPendingPayment()
                    route = AppRoute.LOGIN
                }

                fun openAdminOrders(initialStatus: String?) {
                    adminOrdersInitialStatus = initialStatus?.trim()?.uppercase()?.ifBlank { null }
                    Log.d("AdminOrders", "openAdminOrders initialStatus=$initialStatus normalized=${adminOrdersInitialStatus}")
                    route = AppRoute.ADMIN_ORDERS
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

                LaunchedEffect(profileSuccessMessage) {
                    profileSuccessMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        if (route == AppRoute.EDIT_PROFILE) {
                            route = AppRoute.PROFILE
                        }
                        profileViewModel.clearSuccessMessage()
                    }
                }

                LaunchedEffect(isLoggedIn, currentRole()) {
                    if (!isLoggedIn) return@LaunchedEffect

                    if (isAdmin()) {
                        adminDashboardViewModel?.refresh()
                        adminOrdersViewModel?.refresh()
                        return@LaunchedEffect
                    }

                    cartViewModel.loadCart()
                    profileViewModel.loadProfile()
                    orderViewModel.loadOrders()
                    if (paymentOrderId != null) {
                        paymentScreenState = PaymentScreenState.CHECKING
                        paymentShouldCheckOnEnter = true
                        route = AppRoute.PAYMENT
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
                                route = if (paymentOrderId != null && !isAdmin()) AppRoute.PAYMENT else rootRoute()
                            },
                            onOpenRegister = { route = AppRoute.REGISTER }
                        )
                    }
                } else {
                    when (route) {
                        AppRoute.ADMIN_HOME -> AdminDashboardScreen(
                            viewModel = adminDashboardViewModel!!,
                            onOpenProducts = { route = AppRoute.ADMIN_PRODUCT },
                            onOpenOrders = { filter -> openAdminOrders(filter) },
                            onOpenRevenue = { route = AppRoute.ADMIN_REVENUE },
                            onOpenCategories = { route = AppRoute.ADMIN_CATEGORIES },
                            onOpenToppings = { route = AppRoute.ADMIN_TOPPINGS },
                            onOpenCustomers = { route = AppRoute.ADMIN_CUSTOMERS },
                            onOpenBranches = { route = AppRoute.ADMIN_BRANCHES },
                            onLogout = { logout() }
                        )
                        AppRoute.ADMIN_PRODUCT -> AdminProductScreen(adminProductViewModel!!, { route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_CATEGORIES -> AdminCategoriesScreen(adminCategoryViewModel!!, { route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_ORDERS -> AdminOrdersScreen(
                            viewModel = adminOrdersViewModel!!,
                            initialStatus = adminOrdersInitialStatus,
                            onBack = { route = AppRoute.ADMIN_HOME },
                            onOpenOrder = { orderId ->
                                adminSelectedOrderId = orderId
                                route = AppRoute.ADMIN_ORDER_DETAIL
                            },
                            onUnauthorized = { logout() }
                        )
                        AppRoute.ADMIN_ORDER_DETAIL -> {
                            adminSelectedOrderId?.let { orderId ->
                                AdminOrderDetailScreen(
                                    orderId = orderId,
                                    viewModel = adminOrderDetailViewModel!!,
                                    onBack = { route = AppRoute.ADMIN_ORDERS },
                                    onOrderUpdated = {
                                        adminOrdersViewModel?.refresh()
                                        adminDashboardViewModel?.refresh()
                                    },
                                    onUnauthorized = { logout() }
                                )
                            } ?: run {
                                route = AppRoute.ADMIN_ORDERS
                            }
                        }
                        AppRoute.ADMIN_SIZES -> AdminSizesScreen(adminSizeViewModel!!, { route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_TOPPINGS -> AdminToppingsScreen(adminToppingViewModel!!, { route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_CUSTOMERS -> AdminCustomersScreen(
                            viewModel = adminCustomerViewModel!!,
                            onBack = { route = AppRoute.ADMIN_HOME },
                            onLogout = { logout() },
                            onOpenOrderDetail = { orderId ->
                                adminSelectedOrderId = orderId
                                route = AppRoute.ADMIN_ORDER_DETAIL
                            }
                        )
                        AppRoute.ADMIN_BRANCHES -> AdminBranchesScreen(
                            viewModel = adminBranchViewModel!!,
                            onBack = { route = AppRoute.ADMIN_HOME }
                        )
                        AppRoute.ADMIN_REVENUE -> AdminRevenueScreen(
                            viewModel = adminDashboardViewModel!!,
                            onBack = { route = AppRoute.ADMIN_HOME },
                            onOpenCompletedOrders = { openAdminOrders("COMPLETED") }
                        )
                        AppRoute.ADMIN_PLACEHOLDER -> AdminComingSoonScreen("Coming Soon", "Module is under development", { route = AppRoute.ADMIN_HOME }, { logout() })

                        AppRoute.HOME -> HomeScreen(
                            onOpenProduct = { product ->
                                selectedProduct = product
                                productDetailViewModel.loadDetails(product.id)
                                route = AppRoute.PRODUCT_DETAIL
                            },
                            onCheckout = { route = AppRoute.CART },
                            onOpenEditProfile = {
                                profileViewModel.loadProfile(force = true)
                                route = AppRoute.EDIT_PROFILE
                            },
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
                                val totalAmount = calculateOrderTotal(it)
                                val normalizedItems = it.items.orEmpty()
                                val firstItemName = normalizedItems.firstOrNull()?.productName.normalizeVietnameseText().orEmpty()
                                val firstItemQuantity = normalizedItems.firstOrNull()?.quantity ?: 0
                                val createdAtValue = if (it.id == lastCreatedOrderId && it.createdAt.isNullOrBlank()) lastCreatedOrderCreatedAt else it.createdAt
                                val itemPreview = when {
                                    firstItemName.isNotBlank() -> {
                                        val firstItemLabel = "$firstItemName x${firstItemQuantity.coerceAtLeast(1)}"
                                        if (normalizedItems.size > 1) "$firstItemLabel va ${normalizedItems.size - 1} mon khac" else firstItemLabel
                                    }
                                    normalizedItems.sumOf { item -> item.quantity } > 0 -> "${normalizedItems.sumOf { item -> item.quantity }} mon"
                                    else -> "Don giao hang"
                                }
                                OrderHistoryUiModel(
                                    it.id,
                                    safeOrderCode(it.orderCode),
                                    safeOrderCreatedAt(createdAtValue),
                                    it.items?.size ?: 0,
                                    totalAmount,
                                    safeOrderStatus(it.orderStatus ?: it.status),
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
                            isLoggedIn = isLoggedIn,
                            onOrdersTabSelected = { orderViewModel.loadOrders() },
                            onAccountTabSelected = { profileViewModel.loadProfile(force = true) },
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
                                        clearCheckoutDrafts()
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
                                        if (normalizedItems.size > 1) "$firstItemLabel va ${normalizedItems.size - 1} mon khac" else firstItemLabel
                                    }
                                    totalQuantity > 0 -> "$totalQuantity mon"
                                    else -> "Don giao hang"
                                }
                                OrderHistoryUiModel(
                                    it.id,
                                    safeOrderCode(it.code),
                                    safeOrderCreatedAt(createdAtValue),
                                    totalQuantity,
                                    calculateOrderTotal(it),
                                    safeOrderStatus(it.orderStatus ?: it.status),
                                    itemPreview
                                )
                            }
                            OrderHistoryScreen(
                                orders = uiOrders,
                                onBack = { route = rootRoute() },
                                onOpenOrder = {
                                    selectedOrderId = it.id
                                    orderViewModel.loadOrderDetail(it.id)
                                    route = AppRoute.ORDER_DETAIL
                                }
                            )
                        }
                        AppRoute.ORDER_DETAIL -> {
                            selectedOrderState?.let { order ->
                                val subtotal = order.subtotal.takeIf { it > 0.0 }
                                    ?: order.items.orEmpty().sumOf { item ->
                                        item.totalPrice.takeIf { total -> total > 0.0 } ?: (item.unitPrice * item.quantity)
                                    }
                                val totalAmount = calculateOrderTotal(order)
                                OrderDetailScreen(
                                    order = OrderDetailUiModel(
                                        id = order.id,
                                        orderCode = safeOrderCode(order.orderCode),
                                        status = safeOrderStatus(order.orderStatus ?: order.status),
                                        createdAt = safeOrderCreatedAt(
                                            if (order.id == lastCreatedOrderId && order.createdAt.isNullOrBlank()) lastCreatedOrderCreatedAt else order.createdAt
                                        ),
                                        receiverName = order.receiverName.normalizeVietnameseText().ifBlank { "Chua co thong tin" },
                                        receiverPhone = order.receiverPhone.normalizeVietnameseText().ifBlank { "Chua co thong tin" },
                                        deliveryAddress = order.deliveryAddress.normalizeVietnameseText().ifBlank { "Chua co dia chi giao hang" },
                                        branchName = order.branchName.normalizeVietnameseText().ifBlank {
                                            if (order.id == lastCreatedOrderId) lastCreatedBranchName.normalizeVietnameseText().ifBlank { "Chua co thong tin" } else "Chua co thong tin"
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
                                                name = item.productName.normalizeVietnameseText().ifBlank { "Mon da dat" },
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
                                clearCheckoutDrafts()
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
                                        cacheOrderMetadata(
                                            order = createdOrder,
                                            createdAtFallback = createdAtFallback,
                                            branchNameFallback = request.branchName,
                                            branchAddressFallback = request.branchAddress,
                                            noteFallback = request.note
                                        )
                                        clearCheckoutDrafts()

                                        if (createdOrder.paymentMethod.orEmpty().uppercase() == "VNPAY" || request.paymentMethod.uppercase() == "VNPAY") {
                                            paymentOrderId = createdOrder.id.takeIf { it > 0L }
                                            paymentUrl = createdOrder.paymentUrl
                                            paymentOrderCode = createdOrder.orderCode
                                            paymentAmount = calculateOrderTotal(createdOrder)
                                            paymentScreenState = PaymentScreenState.OPENING
                                            paymentStatusMessage = null
                                            paymentReturnArmed = false
                                            paymentShouldCheckOnEnter = false
                                            paymentOrderId?.let { orderId ->
                                                pendingPaymentStore.save(orderId, paymentUrl)
                                                route = AppRoute.PAYMENT
                                                openPendingPayment(forceRefreshUrl = paymentUrl.isNullOrBlank())
                                            }
                                        } else {
                                            Toast.makeText(context, "Dat hang thanh cong", Toast.LENGTH_SHORT).show()
                                            route = if (selectedOrderId != null) AppRoute.ORDER_DETAIL else AppRoute.ORDER_HISTORY
                                        }
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
                        AppRoute.PAYMENT -> {
                            if (paymentShouldCheckOnEnter && paymentOrderId != null) {
                                LaunchedEffect(paymentOrderId, paymentShouldCheckOnEnter) {
                                    paymentShouldCheckOnEnter = false
                                    refreshPaymentStatus(showCheckingState = true)
                                }
                            }

                            DisposableEffect(lifecycleOwner, route, paymentReturnArmed, paymentOrderId) {
                                val observer = LifecycleEventObserver { _, event ->
                                    if (route == AppRoute.PAYMENT && event == Lifecycle.Event.ON_RESUME && paymentReturnArmed && paymentOrderId != null) {
                                        paymentReturnArmed = false
                                        scope.launch { refreshPaymentStatus(showCheckingState = true) }
                                    }
                                }
                                lifecycleOwner.lifecycle.addObserver(observer)
                                onDispose {
                                    lifecycleOwner.lifecycle.removeObserver(observer)
                                }
                            }

                            PaymentScreen(
                                amount = paymentAmount,
                                orderCode = paymentOrderCode,
                                state = paymentScreenState,
                                message = paymentStatusMessage,
                                isPrimaryLoading = paymentCheckLoading,
                                isSecondaryLoading = paymentOpenLoading,
                                onBack = {
                                    route = if (selectedOrderId != null) AppRoute.ORDER_DETAIL else AppRoute.ORDER_HISTORY
                                },
                                onOpenPayment = {
                                    scope.launch {
                                        openPendingPayment(
                                            forceRefreshUrl = paymentScreenState == PaymentScreenState.FAILED || paymentUrl.isNullOrBlank()
                                        )
                                    }
                                },
                                onCheckAgain = {
                                    scope.launch { refreshPaymentStatus(showCheckingState = true) }
                                },
                                onOpenOrderDetail = {
                                    selectedOrderId = paymentOrderId ?: selectedOrderId
                                    selectedOrderId?.let { orderViewModel.loadOrderDetail(it) }
                                    route = AppRoute.ORDER_DETAIL
                                }
                            )
                        }
                        AppRoute.PROFILE -> ProfileScreen(
                            fullName = profileState?.fullName ?: "User",
                            email = profileState?.email ?: "",
                            phone = profileState?.phone ?: "",
                            role = profileState?.role?.ifBlank { "USER" } ?: "USER",
                            onBack = { route = rootRoute() },
                            onOpenOrders = { route = AppRoute.ORDER_HISTORY },
                            onEditProfile = {
                                profileViewModel.loadProfile(force = true)
                                route = AppRoute.EDIT_PROFILE
                            },
                            onLogout = { logout() }
                        )
                        AppRoute.EDIT_PROFILE -> EditProfileScreen(
                            initialFullName = profileState?.fullName?.ifBlank { "Khách hàng FastDash" } ?: "Khách hàng FastDash",
                            initialEmail = profileState?.email.orEmpty(),
                            initialPhone = profileState?.phone.orEmpty(),
                            emailEditable = true,
                            isLoading = profileLoading,
                            isSaving = profileSaving,
                            errorMessage = profileErrorMessage,
                            onConsumeError = { profileViewModel.clearErrorMessage() },
                            onBack = { route = AppRoute.PROFILE },
                            onSave = { fullName, email, phone ->
                                profileViewModel.updateProfile(fullName = fullName, email = email, phone = phone)
                            }
                        )
                        else -> route = rootRoute()
                    }
                }
            }
        }
    }
}
