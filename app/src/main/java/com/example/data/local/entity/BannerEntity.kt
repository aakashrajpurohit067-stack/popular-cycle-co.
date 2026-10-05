package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subtitle: String,
    val discountTag: String,
    val targetCategory: String = "All Products",
    val imageResId: Int = 0,
    val isLoginPromo: Boolean = false,
    val isActive: Boolean = true
)
