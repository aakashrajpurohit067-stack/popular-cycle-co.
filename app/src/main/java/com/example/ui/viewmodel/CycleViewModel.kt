package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.local.entity.BannerEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.model.CartItemWithProduct
import com.example.data.model.ChatMessage
import com.example.data.model.ProductCategory
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.data.repository.CycleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class Screen(val route: String) {
    object Login : Screen("login")
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
    private val aiService: GeminiAiService = GeminiAiService()
) : ViewModel() {

    // User session
    private val _userSession = MutableStateFlow(
        UserSession(
            isLoggedIn = true,
            phone = "9876543210",
            name = "Rahul Sharma",
            address = "Flat 402, Green Avenue, Rohini Sector 14, Delhi - 110085",
            role = UserRole.CUSTOMER
        )
    )
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    // Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Auth OTP State
    val loginPhone = MutableStateFlow("9876543210")
    val loginName = MutableStateFlow("Rahul Sharma")
    val loginAddress = MutableStateFlow("Flat 402, Green Avenue, Rohini Sector 14, Delhi - 110085")
    val loginOtp = MutableStateFlow("")
    val generatedOtp = MutableStateFlow("")
    val isOtpSent = MutableStateFlow(false)
    val authError = MutableStateFlow<String?>(null)
    val otpRole = MutableStateFlow(UserRole.CUSTOMER)

    fun sendOtp() {
        if (loginPhone.value.trim().length < 10) {
            authError.value = "Please enter a valid 10-digit mobile number"
            return
        }
        val code = String.format("%06d", Random.nextInt(100000, 999999))
        generatedOtp.value = code
        isOtpSent.value = true
        authError.value = null
    }

    fun quickFillTestOtp() {
        loginOtp.value = generatedOtp.value
    }

    fun verifyOtp() {
        if (loginOtp.value != generatedOtp.value && loginOtp.value != "123456") {
            authError.value = "Invalid OTP. Use the simulated code above or 123456"
            return
        }
        _userSession.value = UserSession(
            isLoggedIn = true,
            phone = loginPhone.value.trim(),
            name = loginName.value.ifBlank { "Cycle Customer" },
            address = loginAddress.value.ifBlank { "Local Delivery Address" },
            role = otpRole.value
        )
        isOtpSent.value = false
        loginOtp.value = ""
        authError.value = null
        if (otpRole.value == UserRole.ADMIN) {
            _currentScreen.value = Screen.AdminDashboard
        } else {
            _currentScreen.value = Screen.Home
        }
    }

    fun toggleRole() {
        val newRole = if (_userSession.value.role == UserRole.CUSTOMER) UserRole.ADMIN else UserRole.CUSTOMER
        _userSession.value = _userSession.value.copy(role = newRole)
        if (newRole == UserRole.ADMIN) {
            _currentScreen.value = Screen.AdminDashboard
        } else {
            _currentScreen.value = Screen.Home
        }
    }

    fun logout() {
        _userSession.value = _userSession.value.copy(isLoggedIn = false)
        _currentScreen.value = Screen.Login
    }

    // Products & Banners
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
    val isAiSearchMode = MutableStateFlow(false)
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
        checkoutAddress.value = user.address
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
            deliveryAddress = checkoutAddress.value.ifBlank { "Hub Delivery" },
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
                message = "Welcome to Popular Cycle Company! 🚲\n\nI am your AI Cycling Advisor. How can I help you today? You can ask me about cycle sizing, geared vs single-speed bikes, e-cycles, tyres, or delivery & offers."
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
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
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
}
