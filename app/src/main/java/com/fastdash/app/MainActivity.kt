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
import com.fastdash.app.data.model.response.CartItemResponse
import com.fastdash.app.data.model.response.OrderResponse
import com.fastdash.app.data.model.response.ProductResponse
import com.fastdash.app.data.repository.AiRepository
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
import com.fastdash.app.ui.ai.AiAssistantScreen
import com.fastdash.app.ui.auth.ForgotPasswordEmailScreen
import com.fastdash.app.ui.auth.LoginScreen
import com.fastdash.app.ui.auth.ResetPasswordScreen
import com.fastdash.app.ui.auth.RegisterScreen
import com.fastdash.app.ui.auth.VerifyOtpScreen
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
import com.fastdash.app.viewmodel.AiAssistantViewModel
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
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

private enum class AppRoute {
    LOGIN, REGISTER, ADMIN_HOME, ADMIN_PRODUCT, ADMIN_ORDERS, ADMIN_CATEGORIES,
    ADMIN_ORDER_DETAIL, ADMIN_SIZES, ADMIN_TOPPINGS, ADMIN_CUSTOMERS, ADMIN_BRANCHES, ADMIN_REVENUE,
    ADMIN_PLACEHOLDER, FORGOT_PASSWORD, VERIFY_RESET_OTP, RESET_PASSWORD, HOME, PRODUCT_DETAIL, ORDER_HISTORY, ORDER_DETAIL,
    PROFILE, EDIT_PROFILE, CART, CHECKOUT, PICK_LOCATION, PAYMENT, AI_ASSISTANT
}

private data class CartItemEditState(
    val itemId: Long,
    val productId: Long,
    val quantity: Int,
    val productSizeId: Long?,
    val toppingIds: List<Long>,
    val toppingNames: List<String>,
    val note: String?
)

private data class ReorderCheckoutDraft(
    val items: List<OrderItemRequest>,
    val subtotal: Double,
    val receiverName: String,
    val receiverPhone: String,
    val deliveryAddress: String,
    val note: String,
    val pickedLocation: PickedLocation?
)

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
                val aiRepository = remember { AiRepository(applicationContext) }
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
                val aiAssistantViewModel: AiAssistantViewModel = viewModel(factory = AppViewModelFactory { AiAssistantViewModel(aiRepository) })

                val scope = rememberCoroutineScope()
                var isLoggedIn by remember { mutableStateOf(!tokenManager.getToken().isNullOrEmpty()) }
                var selectedProduct by remember { mutableStateOf<ProductResponse?>(null) }
                var editingCartItem by remember { mutableStateOf<CartItemEditState?>(null) }
                var productDetailFromAi by remember { mutableStateOf(false) }
                var selectedOrderId by remember { mutableStateOf<Long?>(null) }
                var adminSelectedOrderId by remember { mutableStateOf<Long?>(null) }
                var adminOrdersInitialStatus by remember { mutableStateOf<String?>(null) }
                var directCheckoutItem by remember { mutableStateOf<OrderItemRequest?>(null) }
                var directCheckoutTotal by remember { mutableStateOf(0.0) }
                var reorderCheckoutDraft by remember { mutableStateOf<ReorderCheckoutDraft?>(null) }
                var checkoutPickedLocation by remember { mutableStateOf<PickedLocation?>(null) }
                var checkoutCurrentLocation by remember { mutableStateOf<PickedLocation?>(null) }
                var checkoutDraftName by remember { mutableStateOf("") }
                var checkoutDraftPhone by remember { mutableStateOf("") }
                var checkoutDraftAddressDetail by remember { mutableStateOf("") }
                var checkoutDraftNote by remember { mutableStateOf("") }
                var forgotPasswordEmail by remember { mutableStateOf("") }
                var forgotPasswordCode by remember { mutableStateOf("") }
                var forgotPasswordResendAvailableAt by remember { mutableStateOf(0L) }
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
                val aiMessages by aiAssistantViewModel.messages.collectAsState()
                val aiInputText by aiAssistantViewModel.inputText.collectAsState()
                val aiLoading by aiAssistantViewModel.isLoading.collectAsState()
                val aiErrorMessage by aiAssistantViewModel.errorMessage.collectAsState()

                fun currentRole(): String = tokenManager.getRole().orEmpty().trim().uppercase()
                fun isAdmin(): Boolean = currentRole() == "ADMIN"
                fun rootRoute(): AppRoute = if (isAdmin()) AppRoute.ADMIN_HOME else AppRoute.HOME
                fun safeOrderCode(raw: String?): String = raw.normalizeVietnameseText().takeIf { it.isNotBlank() } ?: "Không có mã đơn"
                fun safeOrderCreatedAt(raw: String?): String = formatOrderDate(raw)
                fun safeOrderStatus(raw: String?): String = raw?.takeIf { it.isNotBlank() } ?: "PENDING"
                fun calculateOrderTotal(order: OrderResponse): Double {
                    return order.totalAmount.takeIf { it > 0.0 }
                        ?: (order.subtotal + order.shippingFee - order.discountAmount)
                }

                fun clearCheckoutDrafts() {
                    directCheckoutItem = null
                    directCheckoutTotal = 0.0
                    reorderCheckoutDraft = null
                    checkoutPickedLocation = null
                    checkoutCurrentLocation = null
                    checkoutDraftName = ""
                    checkoutDraftPhone = ""
                    checkoutDraftAddressDetail = ""
                    checkoutDraftNote = ""
                }

                fun openCartItemEditor(item: CartItemResponse) {
                    val productId = item.productId ?: return
                    editingCartItem = CartItemEditState(
                        itemId = item.id,
                        productId = productId,
                        quantity = item.quantity,
                        productSizeId = item.productSizeId,
                        toppingIds = item.toppings.map { it.id },
                        toppingNames = item.toppings.mapNotNull { it.name?.takeIf { name -> name.isNotBlank() } },
                        note = item.note?.takeIf { note -> note.isNotBlank() }
                    )
                    productDetailFromAi = false
                    clearCheckoutDrafts()
                    selectedProduct = ProductResponse(
                        id = productId,
                        name = item.resolvedProductName,
                        description = null,
                        basePrice = item.unitPrice,
                        imageUrl = item.productImageUrl,
                        isCustomizable = 1,
                        categoryId = 0L,
                        categoryName = ""
                    )
                    productDetailViewModel.loadDetails(productId)
                }

                fun checkoutBackRoute(): AppRoute = when {
                    reorderCheckoutDraft != null -> AppRoute.ORDER_DETAIL
                    directCheckoutItem != null -> AppRoute.PRODUCT_DETAIL
                    else -> AppRoute.CART
                }

                suspend fun buildReorderCheckoutDraft(order: OrderResponse): ReorderCheckoutDraft {
                    val products = productRepository.getProducts().body().orEmpty()
                    val resolvedItems = order.items.orEmpty().map { orderItem ->
                        val product = products.firstOrNull {
                            it.name.trim().equals(orderItem.productName.trim(), ignoreCase = true)
                        } ?: error("Khong tim thay san pham ${orderItem.productName}")

                        val sizeId = orderItem.sizeName?.takeIf { it.isNotBlank() }?.let { sizeName ->
                            val sizes = productRepository.getProductSizes(product.id).body().orEmpty()
                            sizes.firstOrNull {
                                it.sizeName.orEmpty().trim().equals(sizeName.trim(), ignoreCase = true)
                            }?.id ?: error("Khong tim thay size $sizeName cho ${orderItem.productName}")
                        }

                        val toppingIds = if (orderItem.toppings.isEmpty()) {
                            emptyList()
                        } else {
                            val toppings = productRepository.getProductToppings(product.id).body().orEmpty()
                            orderItem.toppings.map { toppingName ->
                                toppings.firstOrNull {
                                    it.name.orEmpty().trim().equals(toppingName.trim(), ignoreCase = true)
                                }?.id ?: error("Khong tim thay topping $toppingName cho ${orderItem.productName}")
                            }
                        }

                        OrderItemRequest(
                            productId = product.id,
                            productSizeId = sizeId,
                            quantity = orderItem.quantity,
                            note = orderItem.note,
                            toppingIds = toppingIds
                        )
                    }

                    val pickedLocation = if (order.deliveryLatitude != null && order.deliveryLongitude != null) {
                        PickedLocation(
                            latitude = order.deliveryLatitude,
                            longitude = order.deliveryLongitude,
                            address = order.deliveryAddress.orEmpty(),
                            detailAddress = ""
                        )
                    } else {
                        null
                    }

                    return ReorderCheckoutDraft(
                        items = resolvedItems,
                        subtotal = order.subtotal.takeIf { it > 0.0 } ?: order.items.orEmpty().sumOf { it.totalPrice },
                        receiverName = order.receiverName.orEmpty(),
                        receiverPhone = order.receiverPhone.orEmpty(),
                        deliveryAddress = order.deliveryAddress.orEmpty(),
                        note = order.note.orEmpty(),
                        pickedLocation = pickedLocation
                    )
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
                            paymentStatusMessage = "Không thể kiểm tra trạng thái thanh toán."
                            if (paymentScreenState == PaymentScreenState.CHECKING) {
                                paymentScreenState = PaymentScreenState.PENDING
                            }
                            return
                        }

                        cacheOrderMetadata(
                            order = order,
                            createdAtFallback = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Calendar.getInstance().time),
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
                                paymentStatusMessage = "Đơn hàng đang chờ cửa hàng xác nhận."
                                paymentReturnArmed = false
                                paymentShouldCheckOnEnter = false
                                clearPersistedPendingPayment()
                                orderViewModel.loadOrders()
                            }
                            normalizedPaymentStatus == "FAILED" || normalizedOrderStatus == "PAYMENT_FAILED" -> {
                                paymentScreenState = PaymentScreenState.FAILED
                                paymentStatusMessage = "Bạn có thể thử thanh toán lại cho đơn hàng này."
                                paymentReturnArmed = false
                                paymentShouldCheckOnEnter = false
                                clearPersistedPendingPayment()
                            }
                            normalizedPaymentStatus == "PENDING" && normalizedOrderStatus == "PENDING_PAYMENT" -> {
                                paymentScreenState = PaymentScreenState.PENDING
                                paymentStatusMessage = "Đơn hàng vẫn đang chờ thanh toán."
                            }
                            else -> {
                                paymentScreenState = PaymentScreenState.PENDING
                                paymentStatusMessage = "Trạng thái thanh toán chưa hoàn tất. Vui lòng kiểm tra lại."
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
                            paymentStatusMessage = "Không lấy được liên kết thanh toán VNPAY. Bạn có thể thử lại sau."
                            return
                        }

                        if (openPaymentInBrowser(targetUrl)) {
                            paymentReturnArmed = true
                            paymentScreenState = PaymentScreenState.CHECKING
                            paymentStatusMessage = "Thanh toán xong, vui lòng quay lại ứng dụng FastDash để tiếp tục."
                        } else {
                            paymentScreenState = PaymentScreenState.PENDING
                            paymentStatusMessage = "Không thể mở cổng thanh toán VNPAY trên thiết bị này."
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
                    forgotPasswordEmail = ""
                    forgotPasswordCode = ""
                    forgotPasswordResendAvailableAt = 0L
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
                        AppRoute.FORGOT_PASSWORD -> ForgotPasswordEmailScreen(
                            initialEmail = forgotPasswordEmail,
                            onBack = { route = AppRoute.LOGIN },
                            onOtpSent = { email: String, resendAvailableAt: Long ->
                                forgotPasswordEmail = email
                                forgotPasswordCode = ""
                                forgotPasswordResendAvailableAt = resendAvailableAt
                                route = AppRoute.VERIFY_RESET_OTP
                            }
                        )
                        AppRoute.VERIFY_RESET_OTP -> VerifyOtpScreen(
                            initialEmail = forgotPasswordEmail,
                            initialCode = forgotPasswordCode,
                            resendAvailableAtMillis = forgotPasswordResendAvailableAt,
                            onBack = { route = AppRoute.FORGOT_PASSWORD },
                            onChangeEmail = { route = AppRoute.FORGOT_PASSWORD },
                            onVerified = { email: String, code: String ->
                                forgotPasswordEmail = email
                                forgotPasswordCode = code
                                route = AppRoute.RESET_PASSWORD
                            },
                            onCooldownChanged = { cooldown: Long -> forgotPasswordResendAvailableAt = cooldown }
                        )
                        AppRoute.RESET_PASSWORD -> ResetPasswordScreen(
                            email = forgotPasswordEmail,
                            code = forgotPasswordCode,
                            onBack = { route = AppRoute.VERIFY_RESET_OTP },
                            onResetSuccess = {
                                forgotPasswordEmail = ""
                                forgotPasswordCode = ""
                                forgotPasswordResendAvailableAt = 0L
                                route = AppRoute.LOGIN
                            }
                        )
                        else -> LoginScreen(
                            onLoginSuccess = {
                                isLoggedIn = true
                                route = if (paymentOrderId != null && !isAdmin()) AppRoute.PAYMENT else rootRoute()
                            },
                            onOpenRegister = { route = AppRoute.REGISTER },
                            onOpenForgotPassword = { route = AppRoute.FORGOT_PASSWORD }
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
                                editingCartItem = null
                                productDetailFromAi = false
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
                                    editingCartItem = null
                                    productDetailFromAi = false
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
                            onAiClick = { route = AppRoute.AI_ASSISTANT },
                            cartCount = cartState?.items?.size ?: 0,
                            cartTotal = cartState?.total ?: 0.0
                        )
                        AppRoute.AI_ASSISTANT -> AiAssistantScreen(
                            messages = aiMessages,
                            inputText = aiInputText,
                            isLoading = aiLoading,
                            errorMessage = aiErrorMessage,
                            onInputChange = aiAssistantViewModel::onInputChange,
                            onSendMessage = aiAssistantViewModel::sendMessage,
                            onQuickPromptClick = aiAssistantViewModel::sendQuickPrompt,
                            onOpenProduct = { product ->
                                editingCartItem = null
                                productDetailFromAi = true
                                selectedProduct = ProductResponse(
                                    id = product.id,
                                    name = product.name,
                                    description = product.description,
                                    basePrice = product.basePrice,
                                    imageUrl = product.imageUrl,
                                    isCustomizable = 1,
                                    categoryId = 0L,
                                    categoryName = product.categoryName.orEmpty()
                                )
                                productDetailViewModel.loadDetails(product.id)
                                route = AppRoute.PRODUCT_DETAIL
                            },
                            onBack = { route = AppRoute.HOME },
                            onClearError = { aiAssistantViewModel.clearError() }
                        )
                        AppRoute.PRODUCT_DETAIL -> {
                            selectedProduct?.let { product ->
                                val sizes by productDetailViewModel.sizes.collectAsState()
                                val toppings by productDetailViewModel.toppings.collectAsState()
                                ProductDetailScreen(
                                    product = product,
                                    sizes = sizes,
                                    toppings = toppings,
                                    initialQuantity = editingCartItem?.quantity ?: 1,
                                    initialSizeId = editingCartItem?.productSizeId,
                                    initialToppingIds = editingCartItem?.toppingIds ?: emptyList(),
                                    initialSelectedToppingNames = editingCartItem?.toppingNames ?: emptyList(),
                                    initialNote = editingCartItem?.note.orEmpty(),
                                    isLoading = productDetailLoading,
                                    onBack = {
                                        if (editingCartItem != null) {
                                            editingCartItem = null
                                            route = AppRoute.CART
                                        } else {
                                            route = if (productDetailFromAi) AppRoute.AI_ASSISTANT else rootRoute()
                                        }
                                    },
                                    onAddToCart = { pid, q, s, t, note ->
                                        clearCheckoutDrafts()
                                        editingCartItem?.let { cartItem ->
                                            cartViewModel.replaceCartItem(
                                                oldItemId = cartItem.itemId,
                                                productId = pid,
                                                quantity = q,
                                                productSizeId = s,
                                                toppingIds = t,
                                                note = note
                                            )
                                        } ?: cartViewModel.addToCart(pid, q, s, t, note)
                                        editingCartItem = null
                                        route = AppRoute.CART
                                    },
                                    onBuyNow = { pid, q, s, t, note, total ->
                                        directCheckoutItem = OrderItemRequest(
                                            productId = pid,
                                            productSizeId = s,
                                            quantity = q,
                                            note = note,
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
                                        paymentUrl = order.paymentUrl.orEmpty(),
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
                                    onReorder = {
                                        scope.launch {
                                            runCatching { buildReorderCheckoutDraft(order) }
                                                .onSuccess { draft ->
                                                    clearCheckoutDrafts()
                                                    reorderCheckoutDraft = draft
                                                    checkoutPickedLocation = draft.pickedLocation
                                                    checkoutCurrentLocation = draft.pickedLocation
                                                    checkoutDraftName = draft.receiverName
                                                    checkoutDraftPhone = draft.receiverPhone
                                                    checkoutDraftAddressDetail = ""
                                                    checkoutDraftNote = draft.note
                                                    route = AppRoute.CHECKOUT
                                                }
                                                .onFailure { error ->
                                                    Toast.makeText(
                                                        context,
                                                        error.message ?: "Khong the dat lai don nay",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        }
                                    },
                                    onCancelOrder = {
                                        scope.launch {
                                            if (orderViewModel.cancelOrder(it.id)) {
                                                selectedOrderId?.let { id -> orderViewModel.loadOrderDetail(id) }
                                            }
                                        }
                                    },
                                    onRetryPayment = {
                                        paymentOrderId = it.id.takeIf { id -> id > 0L }
                                        paymentUrl = it.paymentUrl.ifBlank { null }
                                        paymentOrderCode = it.orderCode
                                        paymentAmount = it.totalAmount
                                        paymentScreenState = if (it.paymentStatus.trim().uppercase() == "FAILED" || it.status.trim().uppercase() == "PAYMENT_FAILED") {
                                            PaymentScreenState.FAILED
                                        } else {
                                            PaymentScreenState.PENDING
                                        }
                                        paymentStatusMessage = null
                                        paymentReturnArmed = false
                                        paymentShouldCheckOnEnter = false
                                        paymentOrderId?.let { orderId ->
                                            pendingPaymentStore.save(orderId, paymentUrl)
                                            route = AppRoute.PAYMENT
                                            scope.launch {
                                                openPendingPayment(
                                                    forceRefreshUrl = paymentUrl.isNullOrBlank() ||
                                                        it.paymentStatus.trim().uppercase() == "FAILED" ||
                                                        it.status.trim().uppercase() == "PAYMENT_FAILED"
                                                )
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
                                editingCartItem = null
                                clearCheckoutDrafts()
                                route = rootRoute()
                            },
                            onEditItem = {
                                openCartItemEditor(it)
                                route = AppRoute.PRODUCT_DETAIL
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
                            onBrowseMenu = {
                                editingCartItem = null
                                route = rootRoute()
                            }
                        )
                        AppRoute.CHECKOUT -> CheckoutScreen(
                            subtotal = reorderCheckoutDraft?.subtotal ?: directCheckoutItem?.let { directCheckoutTotal } ?: (cartState?.subtotal ?: 0.0),
                            initialFullName = checkoutDraftName.ifBlank { profileState?.fullName ?: "" },
                            initialPhone = checkoutDraftPhone.ifBlank { profileState?.phone ?: "" },
                            initialAddress = reorderCheckoutDraft?.deliveryAddress?.ifBlank { profileState?.address ?: "" } ?: (profileState?.address ?: ""),
                            initialAddressDetail = checkoutDraftAddressDetail,
                            initialNote = checkoutDraftNote,
                            pickedLocation = checkoutPickedLocation,
                            savedLocations = savedDeliveryLocations,
                            onBack = { route = checkoutBackRoute() },
                            onOpenMapPicker = { route = AppRoute.PICK_LOCATION },
                            onCurrentLocationChanged = { checkoutCurrentLocation = it },
                            onDeliveryLocationChanged = { checkoutPickedLocation = it },
                            onRecipientNameChanged = { checkoutDraftName = it },
                            onRecipientPhoneChanged = { checkoutDraftPhone = it },
                            onAddressDetailChanged = { checkoutDraftAddressDetail = it },
                            onNoteChanged = { checkoutDraftNote = it },
                            onConfirm = { request ->
                                scope.launch {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                                    val createdAtFallback = sdf.format(Calendar.getInstance().time)
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
                                    } ?: reorderCheckoutDraft?.let { draft ->
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
                                                items = draft.items
                                            )
                                        )
                                    } ?: orderViewModel.createOrderFromCart(request)

                                    if (createdOrder != null) {
                                        if (directCheckoutItem == null && reorderCheckoutDraft == null) {
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

