package com.example.data.model

import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.ProductEntity

data class CartItemWithProduct(
    val cartItem: CartItemEntity,
    val product: ProductEntity
) {
    val itemTotal: Double get() = product.discountedPrice * cartItem.quantity
    val originalTotal: Double get() = product.originalPrice * cartItem.quantity
    val savings: Double get() = (originalTotal - itemTotal).coerceAtLeast(0.0)
}
