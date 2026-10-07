package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.BannerEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.model.ProductCategory
import com.example.ui.components.OfferTagBadge
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldDiscount
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StrikePrice
import com.example.ui.viewmodel.CycleViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: CycleViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val allBanners by viewModel.allBanners.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Products", "Orders & Sales", "Promotions", "Store Stats")

    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var showProductDialog by remember { mutableStateOf(false) }

    var bannerToEdit by remember { mutableStateOf<BannerEntity?>(null) }
    var showBannerDialog by remember { mutableStateOf(false) }

    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Admin Header
            Surface(color = NavyDark, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Admin Control Center",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Popular Cycle Co. Management",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Return to Shop Button
                    Button(
                        onClick = { viewModel.toggleRole() },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(34.dp).testTag("switch_to_shop_mode")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBike,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Shop Mode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            // Tab Bar
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = NavyPrimary,
                edgePadding = 12.dp
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Products Management Tab
                    AdminProductsTab(
                        products = allProducts,
                        onAddProduct = {
                            productToEdit = null
                            showProductDialog = true
                        },
                        onEditProduct = { p ->
                            productToEdit = p
                            showProductDialog = true
                        },
                        onDeleteProduct = { p ->
                            productToDelete = p
                        }
                    )
                }
                1 -> {
                    // Orders & Sales Tab
                    AdminOrdersTab(
                        orders = allOrders,
                        onUpdateStatus = { id, status ->
                            viewModel.updateOrderStatus(id, status)
                        }
                    )
                }
                2 -> {
                    // Promotional Banners Tab
                    AdminBannersTab(
                        banners = allBanners,
                        onAddBanner = {
                            bannerToEdit = null
                            showBannerDialog = true
                        },
                        onEditBanner = { b ->
                            bannerToEdit = b
                            showBannerDialog = true
                        },
                        onDeleteBanner = { id ->
                            viewModel.deleteBanner(id)
                        }
                    )
                }
                3 -> {
                    // Store Stats & Analytics
                    AdminStatsTab(products = allProducts, orders = allOrders)
                }
            }
        }

        // FAB to add item if in Products tab
        if (selectedTab == 0) {
            FloatingActionButton(
                onClick = {
                    productToEdit = null
                    showProductDialog = true
                },
                containerColor = AmberAccent,
                contentColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("admin_add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    }

    // Add / Edit Product Dialog
    if (showProductDialog) {
        ProductEditDialog(
            initialProduct = productToEdit,
            viewModel = viewModel,
            onDismiss = { showProductDialog = false },
            onSave = { product ->
                viewModel.saveProduct(product)
                showProductDialog = false
            }
        )
    }

    // Delete Product Confirmation
    if (productToDelete != null) {
        val p = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to remove \"${p.name}\" from the store catalog?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(p.id)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add / Edit Banner Dialog
    if (showBannerDialog) {
        BannerEditDialog(
            initialBanner = bannerToEdit,
            onDismiss = { showBannerDialog = false },
            onSave = { banner ->
                viewModel.saveBanner(banner)
                showBannerDialog = false
            }
        )
    }
}

@Composable
private fun AdminProductsTab(
    products: List<ProductEntity>,
    onAddProduct: () -> Unit,
    onEditProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catalog Inventory (${products.size} Products)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
                Button(
                    onClick = onAddProduct,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(34.dp).testTag("add_product_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Product", fontSize = 11.sp)
                }
            }
        }

        items(products, key = { it.id }) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = product.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberAccent
                            )
                            if (product.offerTag.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                OfferTagBadge(tag = product.offerTag)
                            }
                        }
                        Text(
                            text = product.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyDark
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "₹${product.discountedPrice.toInt()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                            Text(
                                text = "MRP ₹${product.originalPrice.toInt()}",
                                fontSize = 11.sp,
                                color = StrikePrice,
                                textDecoration = TextDecoration.LineThrough
                            )
                            Text(
                                text = "• Stock: ${product.stock}",
                                fontSize = 11.sp,
                                color = if (product.stock > 0) EmeraldDiscount else Color.Red
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { onEditProduct(product) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyPrimary)
                        }
                        IconButton(onClick = { onDeleteProduct(product) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminOrdersTab(
    orders: List<OrderEntity>,
    onUpdateStatus: (Long, String) -> Unit
) {
    val totalRevenue = orders.sumOf { it.totalAmount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Revenue (Sales)", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        Text("₹${totalRevenue.toInt()}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Orders", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        Text("${orders.size}", color = AmberAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Customer Orders (${orders.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )
        }

        items(orders, key = { it.id }) { order ->
            val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(Date(order.orderDateMillis))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = order.orderNumber, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            Text(text = dateStr, fontSize = 10.sp, color = SlateTextSecondary)
                        }
                        Text(
                            text = "₹${order.totalAmount.toInt()}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyDark
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Customer: ${order.customerName} (+91 ${order.customerPhone})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyDark
                    )
                    Text(
                        text = "Address: ${order.deliveryAddress}",
                        fontSize = 11.sp,
                        color = SlateTextSecondary
                    )
                    Text(
                        text = "Payment Mode: ${order.paymentMethod}",
                        fontSize = 11.sp,
                        color = NavyDark
                    )
                    Text(
                        text = "Items: ${order.itemsSummary}",
                        fontSize = 11.sp,
                        color = SlateTextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Updater Dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status: ${order.orderStatus}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (order.orderStatus) {
                                "Delivered" -> EmeraldDiscount
                                "Cancelled" -> Color.Red
                                else -> AmberAccent
                            }
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Confirmed", "Dispatched", "Delivered").forEach { nextStatus ->
                                if (order.orderStatus != nextStatus) {
                                    OutlinedButton(
                                        onClick = { onUpdateStatus(order.id, nextStatus) },
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Text(nextStatus, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminBannersTab(
    banners: List<BannerEntity>,
    onAddBanner: () -> Unit,
    onEditBanner: (BannerEntity) -> Unit,
    onDeleteBanner: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Promotional Banners (${banners.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
                Button(
                    onClick = onAddBanner,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(34.dp).testTag("add_banner_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Banner", fontSize = 11.sp)
                }
            }
        }

        items(banners, key = { it.id }) { banner ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AmberAccent
                            ) {
                                Text(
                                    text = banner.discountTag,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (banner.isLoginPromo) "LOGIN SCREEN" else "HOME SCREEN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = banner.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                        Text(text = banner.subtitle, fontSize = 11.sp, color = SlateTextSecondary)
                    }

                    Row {
                        IconButton(onClick = { onEditBanner(banner) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyPrimary)
                        }
                        IconButton(onClick = { onDeleteBanner(banner.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatsTab(
    products: List<ProductEntity>,
    orders: List<OrderEntity>
) {
    val totalRevenue = orders.sumOf { it.totalAmount }
    val outOfStock = products.count { it.stock <= 0 }
    val onSaleCount = products.count { it.isOnSale || it.offerTag.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Store Performance & Analytics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyDark)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(
                title = "Total Gross Revenue",
                value = "₹${totalRevenue.toInt()}",
                color = NavyPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Total Orders",
                value = "${orders.size}",
                color = AmberAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(
                title = "Catalog Products",
                value = "${products.size}",
                color = NavyDark,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Active Offers / Sale",
                value = "$onSaleCount",
                color = EmeraldDiscount,
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Inventory Health", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Out of Stock Items", fontSize = 12.sp, color = SlateTextSecondary)
                    Text("$outOfStock", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (outOfStock > 0) Color.Red else EmeraldDiscount)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Popular Cycle Hub Status", fontSize = 12.sp, color = SlateTextSecondary)
                    Text("Active & Ready for Dispatch", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldDiscount)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, color = SlateTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductEditDialog(
    initialProduct: ProductEntity?,
    viewModel: CycleViewModel,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var brand by remember { mutableStateOf(initialProduct?.brand ?: "Popular Cycle") }
    var model by remember { mutableStateOf(initialProduct?.model ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: ProductCategory.MENS.displayName) }
    var originalPriceStr by remember { mutableStateOf(initialProduct?.originalPrice?.toInt()?.toString() ?: "15000") }
    var discountedPriceStr by remember { mutableStateOf(initialProduct?.discountedPrice?.toInt()?.toString() ?: "10999") }
    var stockStr by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "10") }
    var gears by remember { mutableStateOf(initialProduct?.gears ?: "21 Speed Shimano") }
    var frame by remember { mutableStateOf(initialProduct?.frameMaterial ?: "Lightweight Alloy") }
    var brakes by remember { mutableStateOf(initialProduct?.brakes ?: "Dual Disc Brakes") }
    var wheelSize by remember { mutableStateOf(initialProduct?.wheelSize ?: "27.5T") }
    var offerTag by remember { mutableStateOf(initialProduct?.offerTag ?: "SPECIAL OFFER") }
    var isOnSale by remember { mutableStateOf(initialProduct?.isOnSale ?: true) }
    var description by remember { mutableStateOf(initialProduct?.description ?: "Engineered with precision for peak cycling performance.") }

    var existingPhotoPaths by remember { mutableStateOf(initialProduct?.getPhotoList()?.toMutableList() ?: mutableListOf<String>()) }
    var newlyPickedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isSaving by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            newlyPickedUris = newlyPickedUris + uris
        }
    }

    var expandedCatDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProduct == null) "Add New Cycle / Product" else "Edit Product",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Photo Upload Section
                Text(
                    text = "Bicycle Photos (${existingPhotoPaths.size + newlyPickedUris.size} selected):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("admin_upload_photos_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = NavyPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Photos from Gallery", fontSize = 11.sp, color = NavyPrimary)
                    }

                    if (existingPhotoPaths.isNotEmpty() || newlyPickedUris.isNotEmpty()) {
                        Text(
                            text = "Clear All",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            modifier = Modifier
                                .clickable {
                                    existingPhotoPaths = mutableListOf()
                                    newlyPickedUris = emptyList()
                                }
                                .padding(4.dp)
                        )
                    }
                }

                // Photo Preview Carousel with replace/remove/cover controls
                if (existingPhotoPaths.isNotEmpty() || newlyPickedUris.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Existing saved photos
                        items(existingPhotoPaths.size) { idx ->
                            val path = existingPhotoPaths[idx]
                            Box(modifier = Modifier.size(80.dp)) {
                                AsyncImage(
                                    model = java.io.File(path),
                                    contentDescription = "Existing Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                // Cover Photo indicator
                                if (idx == 0) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = AmberAccent,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(3.dp)
                                    ) {
                                        Text(
                                            text = "COVER",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                // Remove button
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Red,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopEnd)
                                        .clickable {
                                            val updated = existingPhotoPaths.toMutableList()
                                            updated.removeAt(idx)
                                            existingPhotoPaths = updated
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Newly picked URIs
                        items(newlyPickedUris.size) { idx ->
                            val uri = newlyPickedUris[idx]
                            val isCover = existingPhotoPaths.isEmpty() && idx == 0
                            Box(modifier = Modifier.size(80.dp)) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "New Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                if (isCover) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = AmberAccent,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(3.dp)
                                    ) {
                                        Text(
                                            text = "COVER",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                // Remove button
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Red,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopEnd)
                                        .clickable {
                                            val updated = newlyPickedUris.toMutableList()
                                            updated.removeAt(idx)
                                            newlyPickedUris = updated
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name (e.g. Popular Dominator Pro)") },
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model Code") },
                        modifier = Modifier.weight(1f).testTag("product_model_input"),
                        singleLine = true
                    )
                }

                // Category selector
                ExposedDropdownMenuBox(
                    expanded = expandedCatDropdown,
                    onExpandedChange = { expandedCatDropdown = !expandedCatDropdown }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCatDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCatDropdown,
                        onDismissRequest = { expandedCatDropdown = false }
                    ) {
                        ProductCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    category = cat.displayName
                                    expandedCatDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = originalPriceStr,
                        onValueChange = { originalPriceStr = it },
                        label = { Text("MRP (Cut Price)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("mrp_price_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = discountedPriceStr,
                        onValueChange = { discountedPriceStr = it },
                        label = { Text("Offer Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("offer_price_input"),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Stock Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = offerTag,
                        onValueChange = { offerTag = it },
                        label = { Text("Offer Tag (e.g. MEGA DEAL)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isOnSale, onCheckedChange = { isOnSale = it })
                    Text("Highlight as 'On-Sale' on Home Screen", fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = gears,
                    onValueChange = { gears = it },
                    label = { Text("Gears") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = wheelSize,
                    onValueChange = { wheelSize = it },
                    label = { Text("Wheel Size (e.g. 29T, 27.5T)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        coroutineScope.launch {
                            val savedPaths = viewModel.saveUploadedImages(context, newlyPickedUris)
                            val allPhotos = existingPhotoPaths + savedPaths
                            val orig = originalPriceStr.toDoubleOrNull() ?: 10000.0
                            val disc = discountedPriceStr.toDoubleOrNull() ?: 8000.0
                            val stk = stockStr.toIntOrNull() ?: 10

                            val product = ProductEntity(
                                id = initialProduct?.id ?: 0L,
                                name = name.ifBlank { "Popular Cycle Bike" },
                                category = category,
                                brand = brand.ifBlank { "Popular Cycle" },
                                model = model,
                                originalPrice = orig,
                                discountedPrice = disc,
                                stock = stk,
                                imageResId = initialProduct?.imageResId ?: R.drawable.cycle_mtb_1791185202930,
                                imageUrisJson = allPhotos.joinToString("||"),
                                description = description,
                                frameMaterial = frame,
                                gears = gears,
                                brakes = brakes,
                                wheelSize = wheelSize,
                                offerTag = offerTag,
                                isOnSale = isOnSale,
                                isFeatured = initialProduct?.isFeatured ?: true
                            )
                            onSave(product)
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.testTag("save_product_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                } else {
                    Text("Save Product")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BannerEditDialog(
    initialBanner: BannerEntity?,
    onDismiss: () -> Unit,
    onSave: (BannerEntity) -> Unit
) {
    var title by remember { mutableStateOf(initialBanner?.title ?: "MEGA CYCLE FEST 2026") }
    var subtitle by remember { mutableStateOf(initialBanner?.subtitle ?: "Up to 45% OFF + Free Assembly") }
    var discountTag by remember { mutableStateOf(initialBanner?.discountTag ?: "FLAT 45% OFF") }
    var isLoginPromo by remember { mutableStateOf(initialBanner?.isLoginPromo ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialBanner == null) "Add Promotional Banner" else "Edit Banner",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Banner Headline") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Promotional Subtitle") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = discountTag,
                    onValueChange = { discountTag = it },
                    label = { Text("Discount Pill Tag (e.g. 45% OFF)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isLoginPromo, onCheckedChange = { isLoginPromo = it })
                    Text("Display on Login Screen", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val banner = BannerEntity(
                        id = initialBanner?.id ?: 0L,
                        title = title,
                        subtitle = subtitle,
                        discountTag = discountTag,
                        targetCategory = "All Products",
                        imageResId = initialBanner?.imageResId ?: R.drawable.home_cycle_banner_1791185136122,
                        isLoginPromo = isLoginPromo,
                        isActive = true
                    )
                    onSave(banner)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Save Banner")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
