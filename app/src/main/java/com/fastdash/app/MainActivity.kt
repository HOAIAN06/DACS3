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
import com.fastdash.app.ui.home.HomeScreen
import com.fastdash.app.ui.order.OrderDetailScreen
import com.fastdash.app.ui.order.OrderHistoryScreen
import com.fastdash.app.ui.order.OrderHistoryUiModel
import com.fastdash.app.ui.payment.PaymentScreen
import com.fastdash.app.ui.product.ProductDetailScreen
import com.fastdash.app.ui.profile.ProfileScreen
import com.fastdash.app.ui.theme.FastDash_androidTheme
import com.fastdash.app.utils.TokenManager
import com.fastdash.app.viewmodel.*
import kotlinx.coroutines.launch

private enum class AppRoute {
    LOGIN, REGISTER, ADMIN_HOME, ADMIN_PRODUCT, ADMIN_ORDERS, ADMIN_CATEGORIES,
    ADMIN_SIZES, ADMIN_TOPPINGS, ADMIN_USERS, ADMIN_BRANCHES, ADMIN_PAYMENTS,
    ADMIN_PLACEHOLDER, HOME, PRODUCT_DETAIL, ORDER_HISTORY, ORDER_DETAIL,
    PROFILE, CART, CHECKOUT, PAYMENT
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FastDash_androidTheme {
                val context = LocalContext.current
                val tokenManager = remember { TokenManager(applicationContext) }
                
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
                
                val cartState by cartViewModel.cart.collectAsState()
                val cartMessage by cartViewModel.message.collectAsState()
                val profileState by profileViewModel.user.collectAsState()
                val orderListState by orderViewModel.orders.collectAsState()
                val orderMessage by orderViewModel.message.collectAsState()
                val selectedOrderState by orderViewModel.selectedOrder.collectAsState()

                fun currentRole(): String = tokenManager.getRole().orEmpty().trim().uppercase()
                fun isAdmin(): Boolean = currentRole() == "ADMIN"
                fun rootRoute(): AppRoute = if (isAdmin()) AppRoute.ADMIN_HOME else AppRoute.HOME
                fun safeOrderCode(raw: String?): String = raw?.takeIf { it.isNotBlank() } ?: "Không có mã đơn"
                fun safeOrderCreatedAt(raw: String?): String = raw?.takeIf { it.isNotBlank() } ?: "Chưa có thời gian"
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
                                selectedProduct = product
                                productDetailViewModel.loadDetails(product.id)
                                route = AppRoute.PRODUCT_DETAIL
                            },
                            orders = orderListState.map {
                                OrderHistoryUiModel(
                                    it.id,
                                    safeOrderCode(it.orderCode),
                                    safeOrderCreatedAt(it.createdAt),
                                    it.items?.size ?: 0,
                                    it.totalAmount,
                                    safeOrderStatus(it.status)
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
                                OrderHistoryUiModel(
                                    it.id,
                                    safeOrderCode(it.code),
                                    safeOrderCreatedAt(it.createdAt),
                                    it.items?.size ?: 0,
                                    it.totalAmount,
                                    safeOrderStatus(it.status)
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
                                OrderDetailScreen(
                                    order = com.fastdash.app.ui.order.OrderDetailUiModel(
                                        id = order.id,
                                        orderCode = safeOrderCode(order.orderCode),
                                        status = safeOrderStatus(order.status),
                                        createdAt = safeOrderCreatedAt(order.createdAt),
                                        deliveryAddress = order.deliveryAddress.orEmpty(),
                                        shippingFee = order.shippingFee,
                                        items = order.items.orEmpty().map { item ->
                                            com.fastdash.app.ui.order.OrderItemUiModel(
                                                id = item.id,
                                                name = item.productName,
                                                quantity = item.quantity,
                                                unitPrice = item.unitPrice
                                            )
                                        }
                                    ),
                                    onBack = { route = AppRoute.ORDER_HISTORY },
                                    onReorder = { route = AppRoute.HOME }
                                )
                            } ?: run {
                                selectedOrderId?.let { orderViewModel.loadOrderDetail(it) }
                            }
                        }
                        AppRoute.CART -> CartScreen(
                            cartItems = cartState?.items ?: emptyList(),
                            subtotal = cartState?.subtotal ?: 0.0,
                            shippingFee = cartState?.resolvedShippingFee ?: 0.0,
                            onBack = {
                                directCheckoutItem = null
                                directCheckoutTotal = 0.0
                                route = rootRoute()
                            },
                            onRemoveItem = { cartViewModel.removeFromCart(it) },
                            onCheckout = {
                                directCheckoutItem = null
                                directCheckoutTotal = 0.0
                                route = AppRoute.CHECKOUT
                            }
                        )
                        AppRoute.CHECKOUT -> CheckoutScreen(
                            subtotal = directCheckoutItem?.let { directCheckoutTotal } ?: (cartState?.subtotal ?: 0.0),
                            initialFullName = profileState?.fullName ?: "",
                            initialPhone = profileState?.phone ?: "",
                            initialAddress = profileState?.address ?: "",
                            onBack = { route = if (directCheckoutItem != null) AppRoute.PRODUCT_DETAIL else AppRoute.CART },
                            onConfirm = { request ->
                                scope.launch {
                                    val success = directCheckoutItem?.let { item ->
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

                                    if (success) {
                                        if (directCheckoutItem == null) {
                                            cartViewModel.loadCart()
                                        }
                                        orderViewModel.loadOrders()
                                        directCheckoutItem = null
                                        directCheckoutTotal = 0.0
                                        route = AppRoute.ORDER_HISTORY
                                    }
                                }
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
