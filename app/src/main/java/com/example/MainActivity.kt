package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.model.UserRole
import com.example.data.repository.CycleRepository
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CategoryProductsScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.VoiceSearchDialog
import com.example.ui.theme.PopularCycleTheme
import com.example.ui.viewmodel.CycleViewModel
import com.example.ui.viewmodel.CycleViewModelFactory
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = CycleRepository(
            productDao = database.productDao(),
            bannerDao = database.bannerDao(),
            orderDao = database.orderDao(),
            cartDao = database.cartDao()
        )
        val factory = CycleViewModelFactory(repository)

        setContent {
            PopularCycleTheme {
                val viewModel: CycleViewModel = viewModel(factory = factory)
                PopularCycleApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PopularCycleApp(viewModel: CycleViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userSession by viewModel.userSession.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isVoiceDialogOpen by viewModel.isVoiceSearchDialogOpen.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()

    val totalCartCount = remember(cartItems) {
        cartItems.sumOf { it.cartItem.quantity }
    }

    // Handle Android system back button on sub-screens
    BackHandler(enabled = currentScreen != Screen.Home && currentScreen != Screen.Login) {
        when (currentScreen) {
            Screen.ProductDetail -> viewModel.navigateTo(Screen.Home)
            Screen.Checkout -> viewModel.navigateTo(Screen.Cart)
            Screen.AdminDashboard -> {
                if (userSession.role == UserRole.ADMIN) {
                    viewModel.toggleRole()
                } else {
                    viewModel.navigateTo(Screen.Home)
                }
            }
            else -> viewModel.navigateTo(Screen.Home)
        }
    }

    // Top Bar title determination
    val topBarTitle = when (currentScreen) {
        Screen.Category -> selectedCategory
        Screen.ProductDetail -> selectedProduct?.brand ?: "Cycle Details"
        Screen.Cart -> "My Shopping Cart"
        Screen.Checkout -> "Checkout"
        Screen.Orders -> "My Orders"
        Screen.AiChat -> "Popular Cycle AI Advisor"
        Screen.AdminDashboard -> "Admin Control Panel"
        else -> null
    }

    val showTopBar = currentScreen != Screen.Login
    val showBottomBar = currentScreen != Screen.Login && currentScreen != Screen.Checkout && currentScreen != Screen.AdminDashboard

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            if (showTopBar) {
                AppTopBar(
                    currentScreen = currentScreen,
                    userRole = userSession.role,
                    searchQuery = searchQuery,
                    cartItemCount = totalCartCount,
                    onSearchChange = { viewModel.onSearchQueryChanged(it) },
                    onVoiceSearchClick = { viewModel.isVoiceSearchDialogOpen.value = true },
                    onAiChatClick = { viewModel.navigateTo(Screen.AiChat) },
                    onCartClick = { viewModel.navigateTo(Screen.Cart) },
                    onToggleRole = { viewModel.toggleRole() },
                    onBackClick = if (currentScreen != Screen.Home && currentScreen != Screen.Category) {
                        {
                            when (currentScreen) {
                                Screen.ProductDetail -> viewModel.navigateTo(Screen.Home)
                                Screen.Checkout -> viewModel.navigateTo(Screen.Cart)
                                else -> viewModel.navigateTo(Screen.Home)
                            }
                        }
                    } else null,
                    title = topBarTitle
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentScreen = currentScreen,
                    cartItemCount = totalCartCount,
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Login -> LoginScreen(viewModel = viewModel)
                Screen.Home -> HomeScreen(viewModel = viewModel)
                Screen.Category -> CategoryProductsScreen(viewModel = viewModel)
                Screen.ProductDetail -> ProductDetailScreen(viewModel = viewModel)
                Screen.Cart -> CartScreen(viewModel = viewModel)
                Screen.Checkout -> CheckoutScreen(viewModel = viewModel)
                Screen.Orders -> OrdersScreen(viewModel = viewModel)
                Screen.AiChat -> AiChatScreen(viewModel = viewModel)
                Screen.AdminDashboard -> AdminDashboardScreen(viewModel = viewModel)
            }
        }

        // Voice Search Dialog
        if (isVoiceDialogOpen) {
            VoiceSearchDialog(
                onDismiss = { viewModel.isVoiceSearchDialogOpen.value = false },
                onVoiceResult = { text -> viewModel.submitVoiceQuery(text) }
            )
        }
    }
}
