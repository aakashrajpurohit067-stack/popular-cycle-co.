package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val totalAmount: Double,
    val paymentMethod: String,
    val orderStatus: String = "Placed", // Placed, Confirmed, Shipped, Out for Delivery, Delivered, Cancelled
    val orderDateMillis: Long = System.currentTimeMillis(),
    val itemsSummary: String
)
