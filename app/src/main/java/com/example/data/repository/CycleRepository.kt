package com.example.data.repository

import com.example.data.local.dao.BannerDao
import com.example.data.local.dao.CartDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.BannerEntity
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.model.CartItemWithProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CycleRepository(
    private val productDao: ProductDao,
    private val bannerDao: BannerDao,
    private val orderDao: OrderDao,
    private val cartDao: CartDao
) {
    // Product queries
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val saleAndOfferProducts: Flow<List<ProductEntity>> = productDao.getSaleAndOfferProducts()
    val featuredProducts: Flow<List<ProductEntity>> = productDao.getFeaturedProducts()

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> {
        return if (category == "All Products") {
            productDao.getAllProducts()
        } else {
            productDao.getProductsByCategory(category)
        }
    }

    fun getProductById(id: Long): Flow<ProductEntity?> = productDao.getProductById(id)
    suspend fun getProductByIdDirect(id: Long): ProductEntity? = productDao.getProductByIdDirect(id)
    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    suspend fun saveProduct(product: ProductEntity): Long {
        return if (product.id == 0L) {
            productDao.insertProduct(product)
        } else {
            productDao.updateProduct(product)
            product.id
        }
    }

    suspend fun deleteProduct(id: Long) = productDao.deleteProductById(id)

    // Banners
    val homeBanners: Flow<List<BannerEntity>> = bannerDao.getHomeBanners()
    val loginBanners: Flow<List<BannerEntity>> = bannerDao.getLoginBanners()
    val allBanners: Flow<List<BannerEntity>> = bannerDao.getAllBanners()

    suspend fun saveBanner(banner: BannerEntity): Long {
        return if (banner.id == 0L) {
            bannerDao.insertBanner(banner)
        } else {
            bannerDao.updateBanner(banner)
            banner.id
        }
    }

    suspend fun deleteBanner(id: Long) = bannerDao.deleteBannerById(id)

    // Orders
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()

    fun getOrdersByCustomer(phone: String): Flow<List<OrderEntity>> = orderDao.getOrdersByPhone(phone)

    suspend fun placeOrder(order: OrderEntity): Long = orderDao.insertOrder(order)

    suspend fun updateOrderStatus(orderId: Long, status: String) {
        orderDao.updateOrderStatus(orderId, status)
    }

    // Cart
    val cartItemsWithProducts: Flow<List<CartItemWithProduct>> =
        cartDao.getAllCartItems().combine(productDao.getAllProducts()) { cartItems, products ->
            val productMap = products.associateBy { it.id }
            cartItems.mapNotNull { item ->
                productMap[item.productId]?.let { prod ->
                    CartItemWithProduct(cartItem = item, product = prod)
                }
            }
        }

    suspend fun addToCart(productId: Long, quantity: Int = 1) {
        val existing = cartDao.getCartItem(productId)
        if (existing != null) {
            cartDao.updateQuantity(productId, existing.quantity + quantity)
        } else {
            cartDao.insertOrUpdate(CartItemEntity(productId = productId, quantity = quantity))
        }
    }

    suspend fun updateCartQuantity(productId: Long, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(productId)
        } else {
            cartDao.updateQuantity(productId, quantity)
        }
    }

    suspend fun removeFromCart(productId: Long) = cartDao.deleteCartItem(productId)

    suspend fun clearCart() = cartDao.clearCart()
}
