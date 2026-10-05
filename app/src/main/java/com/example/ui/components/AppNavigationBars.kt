package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SlateBorder
import com.example.ui.viewmodel.Screen

@Composable
fun AppTopBar(
    currentScreen: Screen,
    userRole: UserRole,
    searchQuery: String,
    cartItemCount: Int,
    onSearchChange: (String) -> Unit,
    onVoiceSearchClick: () -> Unit,
    onAiChatClick: () -> Unit,
    onCartClick: () -> Unit,
    onToggleRole: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    title: String? = null
) {
    Surface(
        color = NavyPrimary,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.DirectionsBike,
                            contentDescription = "Logo",
                            tint = AmberAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = title ?: "Popular Cycle Co.",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (title == null) {
                            Text(
                                text = "Bikes • E-Cycles • Tyres • Spares",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Actions: Role toggle chip, AI Chat shortcut, and Cart
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Quick Role toggle badge
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onToggleRole)
                            .testTag("role_toggle_chip"),
                        color = if (userRole == UserRole.ADMIN) AmberAccent else Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = if (userRole == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (userRole == UserRole.ADMIN) Color.Black else Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (userRole == UserRole.ADMIN) "Admin Panel" else "Shop Mode",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (userRole == UserRole.ADMIN) Color.Black else Color.White
                            )
                        }
                    }

                    // AI Assistant button
                    IconButton(
                        onClick = onAiChatClick,
                        modifier = Modifier.testTag("topbar_ai_chat")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = AmberAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Cart with Badge
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier.testTag("topbar_cart")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge(containerColor = AmberAccent, contentColor = Color.Black) {
                                        Text("$cartItemCount", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Search Bar (Shown on Home and Category screens)
            if (currentScreen == Screen.Home || currentScreen == Screen.Category) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("search_input"),
                    placeholder = {
                        Text(
                            text = "Search 21-gear cycles, e-bikes, tyres...",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onVoiceSearchClick, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Search",
                                    tint = AmberAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = SlateBorder
                    )
                )
            }
        }
    }
}

@Composable
fun AppBottomBar(
    currentScreen: Screen,
    cartItemCount: Int,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_navigation_bar")
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.Home,
            onClick = { onNavigate(Screen.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyPrimary,
                selectedTextColor = NavyPrimary,
                indicatorColor = AmberAccent.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Category,
            onClick = { onNavigate(Screen.Category) },
            icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
            label = { Text("Categories", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyPrimary,
                selectedTextColor = NavyPrimary,
                indicatorColor = AmberAccent.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == Screen.AiChat,
            onClick = { onNavigate(Screen.AiChat) },
            icon = {
                BadgedBox(
                    badge = {
                        Badge(containerColor = AmberAccent) {
                            Text("AI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant")
                }
            },
            label = { Text("AI Advisor", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyPrimary,
                selectedTextColor = NavyPrimary,
                indicatorColor = AmberAccent.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Orders,
            onClick = { onNavigate(Screen.Orders) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
            label = { Text("My Orders", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyPrimary,
                selectedTextColor = NavyPrimary,
                indicatorColor = AmberAccent.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Cart,
            onClick = { onNavigate(Screen.Cart) },
            icon = {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge(containerColor = AmberAccent, contentColor = Color.Black) {
                                Text("$cartItemCount", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                }
            },
            label = { Text("Cart", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyPrimary,
                selectedTextColor = NavyPrimary,
                indicatorColor = AmberAccent.copy(alpha = 0.2f)
            )
        )
    }
}
