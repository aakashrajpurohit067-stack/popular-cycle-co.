package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.auth.AuthService
import com.example.data.auth.SessionManager
import com.example.data.local.entity.BannerEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.model.CartItemWithProduct
import com.example.data.model.ChatMessage
import com.example.data.model.ProductCategory
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.data.repository.CycleRepository
import com.example.util.ImageStorageHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object CustomerLogin : Screen("customer_login")
    object AdminLogin : Screen("admin_login")
    object Home : Screen("home")
    object Category : Screen("category")
    object ProductDetail : Screen("product_detail")
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object Orders : Screen("orders")
    object AiChat : Screen("ai_chat")
    object AdminDashboard : Screen("admin_dashboard")
}

class CycleViewModel(
    private val repository: CycleRepository,
    private val authService: AuthService,
    private val sessionManager: SessionManager,
    private val aiService: GeminiAiService = GeminiAiService()
) : ViewModel() {

    // Start on Splash Screen as requested
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // User session - loaded from persistent storage
    private val _userSession = MutableStateFlow<UserSession>(sessionManager.getSession())
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    // Resend OTP Cooldown from AuthService
    val resendCooldown: StateFlow<Int> = authService.resendCooldown

    // --- Customer Authentication State ---
    val customerPhone = MutableStateFlow("9876543210")
    val customerName = MutableStateFlow("Rahul Sharma")
    val customerOtp = MutableStateFlow("")
    val isCustomerOtpSent = MutableStateFlow(false)
    val isCustomerLoading = MutableStateFlow(false)
    val customerAuthError = MutableStateFlow<String?>(null)
    val lastCustomerGeneratedOtp = MutableStateFlow("")

    suspend fun sendCustomerOtpDirect(): String {
        val phone = customerPhone.value.trim()
        if (phone.length < 10) {
            customerAuthError.value = "Please enter a valid 10-digit mobile number"
            return ""
        }
        isCustomerLoading.value = true
        customerAuthError.value = null

        try {
            when (val result = authService.sendOtpSuspend(phone)) {
                is com.example.data.auth.OtpResult.Success -> {
                    lastCustomerGeneratedOtp.value = result.code
                    isCustomerOtpSent.value = true
                    customerAuthError.value = null
                    return result.code
                }
                is com.example.data.auth.OtpResult.Error -> {
                    customerAuthError.value = result.message
                    return ""
                }
            }
        } catch (e: Exception) {
            customerAuthError.value = "Unable to dispatch OTP: ${e.localizedMessage ?: "Network timeout"}"
            return ""
        } finally {
            isCustomerLoading.value = false
        }
    }

    fun sendCustomerOtp() {
        viewModelScope.launch {
            sendCustomerOtpDirect()
        }
    }

    fun quickFillCustomerOtp() {
        customerOtp.value = lastCustomerGeneratedOtp.value.ifBlank { "123456" }
    }

    suspend fun verifyCustomerOtpDirect(): Boolean {
        isCustomerLoading.value = true
        customerAuthError.value = null

        try {
            val verified = authService.verifyOtp(customerPhone.value, customerOtp.value)
            if (verified) {
                val newSession = UserSession(
                    isLoggedIn = true,
                    phone = customerPhone.value.trim(),
                    name = customerName.value.ifBlank { "Customer" },
                    address = "Doorstep Delivery Address",
                    role = UserRole.CUSTOMER
                )
                _userSession.value = newSession
                sessionManager.saveSession(newSession)
                isCustomerOtpSent.value = false
                customerOtp.value = ""
                _currentScreen.value = Screen.Home
                return true
            } else {
                customerAuthError.value = "Incorrect OTP code. Enter the 6-digit code or try 123456."
                return false
            }
        } catch (e: Exception) {
            customerAuthError.value = "Verification error: ${e.localizedMessage}"
            return false
        } finally {
            isCustomerLoading.value = false
        }
    }

    fun verifyCustomerOtp() {
        viewModelScope.launch {
            verifyCustomerOtpDirect()
        }
    }

    // --- Admin Authentication State ---
    val adminPhone = MutableStateFlow("9876543210")
    val adminOtp = MutableStateFlow("")
    val isAdminOtpSent = MutableStateFlow(false)
    val isAdminLoading = MutableStateFlow(false)
    val adminAuthError = MutableStateFlow<String?>(null)
    val lastAdminGeneratedOtp = MutableStateFlow("")

    suspend fun sendAdminOtpDirect(): String {
        val phone = adminPhone.value.trim()
        if (phone.length < 10) {
            adminAuthError.value = "Please enter a valid 10-digit mobile number"
            return ""
        }
        isAdminLoading.value = true
        adminAuthError.value = null

        try {
            when (val result = authService.sendOtpSuspend(phone)) {
                is com.example.data.auth.OtpResult.Success -> {
                    lastAdminGeneratedOtp.value = result.code
                    isAdminOtpSent.value = true
                    adminAuthError.value = null
                    return result.code
                }
                is com.example.data.auth.OtpResult.Error -> {
                    adminAuthError.value = result.message
                    return ""
                }
            }
        } catch (e: Exception) {
            adminAuthError.value = "Unable to dispatch Admin OTP: ${e.localizedMessage ?: "Network timeout"}"
            return ""
        } finally {
            isAdminLoading.value = false
        }
    }

    fun sendAdminOtp() {
        viewModelScope.launch {
            sendAdminOtpDirect()
        }
    }

    fun quickFillAdminOtp() {
        adminOtp.value = lastAdminGeneratedOtp.value.ifBlank { "123456" }
    }

    suspend fun verifyAdminOtpDirect(): Boolean {
        isAdminLoading.value = true
        adminAuthError.value = null

        try {
            // First verify OTP
            val isOtpValid = authService.verifyOtp(adminPhone.value, adminOtp.value)
            if (!isOtpValid) {
                adminAuthError.value = "Incorrect OTP code. Please enter the valid code."
                return false
            }

            // Secure Admin Authorization Check in Database
            val adminRecord = repository.verifyAdminAuthorization(adminPhone.value)

            if (adminRecord != null && adminRecord.isActive) {
                val newSession = UserSession(
                    isLoggedIn = true,
                    phone = adminPhone.value.trim(),
                    name = adminRecord.name,
                    address = "Popular Cycle Headquarters",
                    role = UserRole.ADMIN
                )
                _userSession.value = newSession
                sessionManager.saveSession(newSession)
                isAdminOtpSent.value = false
                adminOtp.value = ""
                _currentScreen.value = Screen.AdminDashboard
                return true
            } else {
                adminAuthError.value = "Access Denied: +91 ${adminPhone.value} is not an authorized administrator. Use Customer Login or contact store management."
                return false
            }
        } catch (e: Exception) {
            adminAuthError.value = "Authorization failure: ${e.localizedMessage}"
            return false
        } finally {
            isAdminLoading.value = false
        }
    }

    fun verifyAdminOtp() {
        viewModelScope.launch {
            verifyAdminOtpDirect()
        }
    }

    fun toggleRole() {
        val currentRole = _userSession.value.role
        if (currentRole == UserRole.ADMIN) {
            // Switch to shopping mode
            val updated = _userSession.value.copy(role = UserRole.CUSTOMER)
            _userSession.value = updated
            sessionManager.saveSession(updated)
            _currentScreen.value = Screen.Home
        } else {
            // Switch back to admin panel if previously authenticated as admin
            viewModelScope.launch {
                val adminRecord = repository.verifyAdminAuthorization(_userSession.value.phone)
                if (adminRecord != null) {
                    val updated = _userSession.value.copy(role = UserRole.ADMIN)
                    _userSession.value = updated
                    sessionManager.saveSession(updated)
                    _currentScreen.value = Screen.AdminDashboard
                } else {
                    _currentScreen.value = Screen.AdminLogin
                }
            }
        }
    }

    fun logout() {
        val clearSession = UserSession(
            isLoggedIn = false,
            phone = "",
            name = "",
            address = "",
            role = UserRole.CUSTOMER
        )
        _userSession.value = clearSession
        sessionManager.clearSession()
        _currentScreen.value = Screen.Welcome
    }

    // --- Products & Banners Flows ---
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val saleAndOfferProducts: StateFlow<List<ProductEntity>> = repository.saleAndOfferProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredProducts: StateFlow<List<ProductEntity>> = repository.featuredProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val homeBanners: StateFlow<List<BannerEntity>> = repository.homeBanners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loginBanners: StateFlow<List<BannerEntity>> = repository.loginBanners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBanners: StateFlow<List<BannerEntity>> = repository.allBanners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category Screen
    val selectedCategory = MutableStateFlow(ProductCategory.ALL.displayName)

    fun selectCategory(category: String) {
        selectedCategory.value = category
        _currentScreen.value = Screen.Category
    }

    // Product Detail
    val selectedProduct = MutableStateFlow<ProductEntity?>(null)

    fun openProductDetail(product: ProductEntity) {
        selectedProduct.value = product
        _currentScreen.value = Screen.ProductDetail
    }

    // Search and Voice Search
    val searchQuery = MutableStateFlow("")
    val isVoiceSearchDialogOpen = MutableStateFlow(false)
    val aiSearchReasoning = MutableStateFlow<String?>(null)

    private val _aiSearchResults = MutableStateFlow<List<ProductEntity>>(emptyList())
    val aiSearchResults: StateFlow<List<ProductEntity>> = _aiSearchResults.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        if (query.isBlank()) {
            _aiSearchResults.value = emptyList()
            aiSearchReasoning.value = null
            return
        }
        viewModelScope.launch {
            val results = aiService.analyzeSearchQuery(query, allProducts.value)
            _aiSearchResults.value = results
            aiSearchReasoning.value = "AI matched ${results.size} items matching \"$query\""
        }
    }

    fun submitVoiceQuery(voiceText: String) {
        isVoiceSearchDialogOpen.value = false
        searchQuery.value = voiceText
        onSearchQueryChanged(voiceText)
    }

    // Cart Management
    val cartItems: StateFlow<List<CartItemWithProduct>> = repository.cartItemsWithProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val couponCode = MutableStateFlow("")
    val isCouponApplied = MutableStateFlow(false)
    val couponDiscountAmount = MutableStateFlow(0.0)
    val couponMessage = MutableStateFlow<String?>(null)

    fun applyCoupon(code: String) {
        couponCode.value = code
        if (code.trim().equals("CYCLEFEST", ignoreCase = true)) {
            isCouponApplied.value = true
            couponDiscountAmount.value = 500.0
            couponMessage.value = "Coupon applied! ₹500 Instant Discount"
        } else if (code.trim().equals("POPULAR10", ignoreCase = true)) {
            isCouponApplied.value = true
            val total = cartItems.value.sumOf { it.itemTotal }
            couponDiscountAmount.value = (total * 0.10).coerceAtMost(1000.0)
            couponMessage.value = "Coupon applied! 10% Extra Savings"
        } else {
            isCouponApplied.value = false
            couponDiscountAmount.value = 0.0
            couponMessage.value = "Invalid coupon code. Try CYCLEFEST or POPULAR10"
        }
    }

    fun addToCart(product: ProductEntity, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(product.id, quantity)
        }
    }

    fun updateCartQuantity(productId: Long, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, quantity)
        }
    }

    fun removeFromCart(productId: Long) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Checkout & Order Placement
    val checkoutCustomerName = MutableStateFlow("")
    val checkoutPhone = MutableStateFlow("")
    val checkoutAddress = MutableStateFlow("")
    val checkoutPaymentMethod = MutableStateFlow("Cash on Delivery")
    val lastPlacedOrder = MutableStateFlow<OrderEntity?>(null)

    fun startCheckout() {
        val user = _userSession.value
        checkoutCustomerName.value = user.name
        checkoutPhone.value = user.phone
        checkoutAddress.value = user.address.ifBlank { "Flat 402, Green Avenue, Delhi - 110085" }
        _currentScreen.value = Screen.Checkout
    }

    fun placeOrder(onSuccess: (OrderEntity) -> Unit) {
        val items = cartItems.value
        if (items.isEmpty()) return

        val itemsSummary = items.joinToString(", ") {
            "${it.cartItem.quantity}x ${it.product.name} (₹${it.product.discountedPrice.toInt()})"
        }
        val subtotal = items.sumOf { it.itemTotal }
        val finalAmount = (subtotal - couponDiscountAmount.value).coerceAtLeast(0.0)
        val orderNo = "ORD-${System.currentTimeMillis().toString().takeLast(6)}"

        val newOrder = OrderEntity(
            orderNumber = orderNo,
            customerName = checkoutCustomerName.value.ifBlank { "Customer" },
            customerPhone = checkoutPhone.value.ifBlank { "9876543210" },
            deliveryAddress = checkoutAddress.value.ifBlank { "Doorstep Delivery" },
            totalAmount = finalAmount,
            paymentMethod = checkoutPaymentMethod.value,
            orderStatus = "Placed",
            itemsSummary = itemsSummary
        )

        viewModelScope.launch {
            val id = repository.placeOrder(newOrder)
            val created = newOrder.copy(id = id)
            lastPlacedOrder.value = created
            repository.clearCart()
            isCouponApplied.value = false
            couponDiscountAmount.value = 0.0
            onSuccess(created)
        }
    }

    // Orders
    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerOrders: StateFlow<List<OrderEntity>> = combine(
        repository.allOrders,
        _userSession
    ) { orders, user ->
        orders.filter { it.customerPhone == user.phone || user.role == UserRole.ADMIN }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateOrderStatus(orderId: Long, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    // AI Customer Assistant Chatbot
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "assistant",
                message = "Welcome to Popular Cycle Company! 🚲\n\nI am your AI Cycling Advisor. How can I help you today? Ask about cycle sizing, geared vs single-speed bikes, e-cycles, tyres, or delivery & offers."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    val chatInput = MutableStateFlow("")
    val isAiTyping = MutableStateFlow(false)

    fun sendChatMessage(text: String = chatInput.value) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || isAiTyping.value) return

        val userMsg = ChatMessage(sender = "user", message = trimmed)
        _chatMessages.value = _chatMessages.value + userMsg
        chatInput.value = ""
        isAiTyping.value = true

        viewModelScope.launch {
            val history = _chatMessages.value.map { it.sender to it.message }
            val reply = aiService.chatWithAssistant(
                userMessage = trimmed,
                availableProducts = allProducts.value,
                chatHistory = history
            )
            val assistantMsg = ChatMessage(sender = "assistant", message = reply)
            _chatMessages.value = _chatMessages.value + assistantMsg
            isAiTyping.value = false
        }
    }

    // Admin Operations
    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.saveProduct(product)
            // If the currently viewed product was edited, update its selection
            if (selectedProduct.value?.id == product.id) {
                selectedProduct.value = product
            }
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
            if (selectedProduct.value?.id == id) {
                selectedProduct.value = null
            }
        }
    }

    fun saveBanner(banner: BannerEntity) {
        viewModelScope.launch {
            repository.saveBanner(banner)
        }
    }

    fun deleteBanner(id: Long) {
        viewModelScope.launch {
            repository.deleteBanner(id)
        }
    }

    suspend fun saveUploadedImages(context: Context, uris: List<Uri>): List<String> {
        val savedPaths = mutableListOf<String>()
        for (uri in uris) {
            val savedPath = ImageStorageHelper.saveImageToInternalStorage(context, uri)
            if (savedPath != null) {
                savedPaths.add(savedPath)
            }
        }
        return savedPaths
    }
}
