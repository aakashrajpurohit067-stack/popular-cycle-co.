package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val brand: String = "Popular Cycle",
    val originalPrice: Double,
    val discountedPrice: Double,
    val stock: Int = 12,
    val imageResId: Int = 0,
    val imageUrl: String = "",
    val description: String = "",
    val frameMaterial: String = "Aluminium Alloy",
    val gears: String = "21 Speed Shimano",
    val brakes: String = "Dual Disc Brakes",
    val wheelSize: String = "27.5T",
    val offerTag: String = "",
    val isOnSale: Boolean = false,
    val isFeatured: Boolean = false,
    val rating: Float = 4.6f,
    val reviewCount: Int = 42
) {
    val discountPercent: Int
        get() = if (originalPrice > discountedPrice && originalPrice > 0) {
            (((originalPrice - discountedPrice) / originalPrice) * 100).toInt()
        } else 0

    val savingsAmount: Double
        get() = (originalPrice - discountedPrice).coerceAtLeast(0.0)
}
