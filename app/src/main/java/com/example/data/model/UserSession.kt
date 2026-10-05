package com.example.data.model

enum class UserRole {
    CUSTOMER,
    ADMIN
}

data class UserSession(
    val isLoggedIn: Boolean = true,
    val phone: String = "9876543210",
    val name: String = "Cycle Enthusiast",
    val address: String = "24, MG Road, Popular Cycle Hub, Bengaluru, 560001",
    val role: UserRole = UserRole.CUSTOMER
)
