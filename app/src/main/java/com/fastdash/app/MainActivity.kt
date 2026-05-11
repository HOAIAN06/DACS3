package com.fastdash.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
                
                // Admin ViewModels
                val adminProductViewModel: AdminProductViewModel = viewModel(
                    factory = AdminProductViewModelFactory(
                        adminProductRepository,
                        adminCategoryRepository,
                        adminToppingRepository,
                        adminSizeRepository
                    )
                )
                val adminCategoryViewModel: AdminCategoryViewModel = viewModel(factory = AdminCategoryViewModelFactory(adminCategoryRepository))
                val adminSizeViewModel: AdminSizeViewModel = viewModel(factory = AdminSizeViewModelFactory(adminSizeRepository))
                val adminToppingViewModel: AdminToppingViewModel = viewModel(factory = AdminToppingViewModelFactory(adminToppingRepository))

                val scope = rememberCoroutineScope()
                var isLoggedIn by remember { mutableStateOf(!tokenManager.getToken().isNullOrEmpty()) }
                var selectedProduct by remember { mutableStateOf<ProductResponse?>(null) }
                var selectedOrderId by remember { mutableStateOf<Long?>(null) }
                
                val cartState by cartViewModel.cart.collectAsState()
                val cartMessage by cartViewModel.message.collectAsState()
                val profileState by profileViewModel.user.collectAsState()
                val orderListState by orderViewModel.orders.collectAsState()

                fun currentRole(): String = tokenManager.getRole().orEmpty().trim().uppercase()
                fun isAdmin(): Boolean = currentRole() == "ADMIN"
                fun rootRoute(): AppRoute = if (isAdmin()) AppRoute.ADMIN_HOME else AppRoute.HOME

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
                    route = AppRoute.LOGIN
                }

                LaunchedEffect(cartMessage) {
                    cartMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        cartViewModel.clearMessage()
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
                            onOpenPlaceholder = { title, _ ->
                                when (title) {
                                    "ADMIN_USERS" -> route = AppRoute.ADMIN_USERS
                                    "ADMIN_BRANCHES" -> route = AppRoute.ADMIN_BRANCHES
                                    "ADMIN_PAYMENTS" -> route = AppRoute.ADMIN_PAYMENTS
                                    else -> route = AppRoute.ADMIN_PLACEHOLDER
                                }
                            },
                            onLogout = { logout() }
                        )
                        AppRoute.ADMIN_PRODUCT -> AdminProductScreen(adminProductViewModel, { route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_CATEGORIES -> AdminCategoriesScreen(adminCategoryViewModel, { route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_SIZES -> AdminSizesScreen(adminSizeViewModel, { route = AppRoute.ADMIN_HOME }, { logout() })
                        AppRoute.ADMIN_TOPPINGS -> AdminToppingsScreen(adminToppingViewModel, { route = AppRoute.ADMIN_HOME }, { logout() })
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
                            onOpenOrders = { route = AppRoute.ORDER_HISTORY },
                            onOpenProfile = { route = AppRoute.PROFILE },
                            onCheckout = { route = AppRoute.CART },
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
                                        cartViewModel.addToCart(pid, q, s, t)
                                        route = AppRoute.CART
                                    }
                                )
                            }
                        }
                        AppRoute.ORDER_HISTORY -> {
                            val uiOrders = orderListState.map { 
                                OrderHistoryUiModel(it.id, it.code, it.createdAt, it.items?.size ?: 0, it.totalAmount, it.status) 
                            }
                            OrderHistoryScreen(orders = uiOrders, onBack = { route = rootRoute() }, onOpenOrder = { 
                                selectedOrderId = it.id
                                route = AppRoute.ORDER_DETAIL
                            })
                        }
                        AppRoute.CART -> CartScreen(
                            cartItems = cartState?.items ?: emptyList(),
                            subtotal = cartState?.subtotal ?: 0.0,
                            shippingFee = cartState?.shippingFee ?: 0.0,
                            onBack = { route = rootRoute() },
                            onRemoveItem = { cartViewModel.removeFromCart(it) },
                            onCheckout = { route = AppRoute.CHECKOUT }
                        )
                        AppRoute.CHECKOUT -> CheckoutScreen(
                            total = cartState?.total ?: 0.0,
                            onBack = { route = AppRoute.CART },
                            onConfirm = { _, _, addr ->
                                scope.launch {
                                    val items = cartState?.items?.map { OrderItemRequest(it.productId, it.quantity, null) } ?: emptyList()
                                    if (orderViewModel.createOrder(addr, items)) {
                                        cartViewModel.loadCart()
                                        route = AppRoute.PAYMENT
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
                            role = profileState?.role ?: "CUSTOMER",
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
