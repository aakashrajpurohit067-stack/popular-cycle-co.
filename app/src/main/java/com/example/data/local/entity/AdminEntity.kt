package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admins")
data class AdminEntity(
    @PrimaryKey
    val phone: String,
    val name: String,
    val roleTitle: String = "Authorized Administrator",
    val isActive: Boolean = true
)
