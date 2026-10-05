package com.example.data.model

enum class ProductCategory(val displayName: String) {
    ALL("All Products"),
    MENS("Men’s Cycle"),
    WOMENS("Women’s Cycle"),
    KIDS("Kids Cycle"),
    TRICYCLE("Tricycle"),
    ECYCLE("E-Cycle"),
    SPARE_PARTS("Cycle Spare Parts"),
    CYCLE_TYRE("Cycle Tyre"),
    AUTO_TYRE("Auto Tyre");

    companion object {
        fun fromDisplayName(name: String): ProductCategory {
            return entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: ALL
        }
    }
}
