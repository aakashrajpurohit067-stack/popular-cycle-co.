package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.R
import com.example.data.local.dao.BannerDao
import com.example.data.local.dao.CartDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.BannerEntity
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.model.ProductCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        BannerEntity::class,
        OrderEntity::class,
        CartItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun bannerDao(): BannerDao
    abstract fun orderDao(): OrderDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "popular_cycle_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val productDao = database.productDao()
            val bannerDao = database.bannerDao()
            val orderDao = database.orderDao()

            if (productDao.getProductCount() > 0) return

            val initialProducts = listOf(
                ProductEntity(
                    id = 1,
                    name = "Popular Dominator Pro 29T MTB",
                    category = ProductCategory.MENS.displayName,
                    brand = "Popular Cycle",
                    originalPrice = 18999.0,
                    discountedPrice = 12999.0,
                    stock = 14,
                    imageResId = R.drawable.cycle_mtb_1791185202930,
                    description = "Aggressive 29-inch hardtail alloy MTB built for rough trails and city sprinting. Features 21-speed Shimano Tourney gears, responsive dual mechanical disc brakes, and 100mm lockable front suspension.",
                    frameMaterial = "6061 Lightweight Alloy",
                    gears = "21 Speed Shimano EF500",
                    brakes = "Dual 160mm Rotor Disc",
                    wheelSize = "29 x 2.35 Inch",
                    offerTag = "MEGA DEAL",
                    isOnSale = true,
                    isFeatured = true,
                    rating = 4.8f,
                    reviewCount = 128
                ),
                ProductEntity(
                    id = 2,
                    name = "Popular Volt E-Trail 27.5T Electric Cycle",
                    category = ProductCategory.ECYCLE.displayName,
                    brand = "Popular Volt",
                    originalPrice = 36999.0,
                    discountedPrice = 27499.0,
                    stock = 8,
                    imageResId = R.drawable.cycle_electric_1791185218657,
                    description = "Next-gen intelligent e-cycle with 36V 10.4Ah detachable lithium-ion battery. Delivers up to 55km range per charge with 5-level pedal assist and throttle mode. Top speed 25 km/h with smart LED display.",
                    frameMaterial = "Aircraft Grade Alloy",
                    gears = "7 Speed Shimano Revoshift",
                    brakes = "Dual Electric Cut-off Disc",
                    wheelSize = "27.5 Inch All-Terrain",
                    offerTag = "₹9,500 OFF",
                    isOnSale = true,
                    isFeatured = true,
                    rating = 4.9f,
                    reviewCount = 95
                ),
                ProductEntity(
                    id = 3,
                    name = "Popular Grace Bella 26T City Bicycle",
                    category = ProductCategory.WOMENS.displayName,
                    brand = "Popular Cycle",
                    originalPrice = 11499.0,
                    discountedPrice = 8299.0,
                    stock = 20,
                    imageResId = R.drawable.cycle_mtb_1791185202930,
                    description = "Charming step-through city bicycle designed for effortless commuting and fitness. Comes with front utility wicker-style basket, cushioned sprung saddle, full mudguards, and rear carrier rack.",
                    frameMaterial = "Low-Step Steel Alloy",
                    gears = "Single Speed Smooth Drive",
                    brakes = "Power V-Brakes",
                    wheelSize = "26 x 1.75 Inch",
                    offerTag = "HOT OFFER",
                    isOnSale = true,
                    isFeatured = true,
                    rating = 4.7f,
                    reviewCount = 64
                ),
                ProductEntity(
                    id = 4,
                    name = "Popular Blaze 20T Kids Sportster",
                    category = ProductCategory.KIDS.displayName,
                    brand = "Popular Junior",
                    originalPrice = 7999.0,
                    discountedPrice = 5499.0,
                    stock = 15,
                    imageResId = R.drawable.cycle_mtb_1791185202930,
                    description = "Vibrant, sturdy bike crafted for kids aged 5 to 9 years. Built with extra-wide training wheels, full chain cover for safety, dual calipers, and non-toxic high-impact finish.",
                    frameMaterial = "Hi-Ten Tubular Steel",
                    gears = "Single Speed Easy Pedal",
                    brakes = "Front & Rear Caliper",
                    wheelSize = "20 Inch",
                    offerTag = "SPECIAL OFFER",
                    isOnSale = true,
                    isFeatured = false,
                    rating = 4.6f,
                    reviewCount = 48
                ),
                ProductEntity(
                    id = 5,
                    name = "Popular Little Champ Toddler Tricycle",
                    category = ProductCategory.TRICYCLE.displayName,
                    brand = "Popular Tiny",
                    originalPrice = 4499.0,
                    discountedPrice = 2899.0,
                    stock = 25,
                    imageResId = R.drawable.cycle_mtb_1791185202930,
                    description = "Ergonomic 3-wheel tricycle for toddlers aged 1.5 to 4 years. Features directional parent push handle, adjustable canopy, front & back storage baskets, and bell.",
                    frameMaterial = "Reinforced Carbon Steel",
                    gears = "Direct Drive Front Wheel",
                    brakes = "Non-slip Safety Pedals",
                    wheelSize = "10 Inch EVA Foam",
                    offerTag = "FLAT 35% OFF",
                    isOnSale = true,
                    isFeatured = false,
                    rating = 4.5f,
                    reviewCount = 37
                ),
                ProductEntity(
                    id = 6,
                    name = "Ralco All-Terrain 27.5x2.10 Mountain Cycle Tyre",
                    category = ProductCategory.CYCLE_TYRE.displayName,
                    brand = "Ralco Tyres",
                    originalPrice = 1499.0,
                    discountedPrice = 999.0,
                    stock = 50,
                    imageResId = R.drawable.cycle_parts_tyre_1791185235544,
                    description = "Heavy-duty wire bead cycle tyre with aggressive chevron knobby tread pattern for superior trail grip, wet pavement traction, and anti-puncture shielding.",
                    frameMaterial = "N/A",
                    gears = "N/A",
                    brakes = "N/A",
                    wheelSize = "27.5 x 2.10",
                    offerTag = "BEST SELLER",
                    isOnSale = false,
                    isFeatured = false,
                    rating = 4.7f,
                    reviewCount = 112
                ),
                ProductEntity(
                    id = 7,
                    name = "CEAT Gripp Pro 90/90-12 Tubeless Scooter Tyre",
                    category = ProductCategory.AUTO_TYRE.displayName,
                    brand = "CEAT",
                    originalPrice = 2199.0,
                    discountedPrice = 1649.0,
                    stock = 35,
                    imageResId = R.drawable.cycle_parts_tyre_1791185235544,
                    description = "Premium tubeless scooter tyre providing maximum wet cornering grip, long tread life, and reinforced sidewalls for Honda Activa, Jupiter, and Access 125.",
                    frameMaterial = "Multi-Ply High Silica Rubber",
                    gears = "N/A",
                    brakes = "N/A",
                    wheelSize = "12 Inch Rim",
                    offerTag = "DEAL OF THE DAY",
                    isOnSale = true,
                    isFeatured = true,
                    rating = 4.8f,
                    reviewCount = 89
                ),
                ProductEntity(
                    id = 8,
                    name = "Shimano Altus 21-Speed Gear Shifter & Derailleur Kit",
                    category = ProductCategory.SPARE_PARTS.displayName,
                    brand = "Shimano Genuine",
                    originalPrice = 3899.0,
                    discountedPrice = 2699.0,
                    stock = 22,
                    imageResId = R.drawable.cycle_parts_tyre_1791185235544,
                    description = "Complete genuine gear transmission upgrade set including Shimano Tourney TY300 rear derailleur, front derailleur, 3x7 speed optical gear shifters, and slick cables.",
                    frameMaterial = "Forged Alloy & Steel",
                    gears = "3x7 Speed (21 Speed)",
                    brakes = "Integrated Brake Levers",
                    wheelSize = "Universal Compatibility",
                    offerTag = "POPULAR CHOICE",
                    isOnSale = true,
                    isFeatured = false,
                    rating = 4.9f,
                    reviewCount = 53
                ),
                ProductEntity(
                    id = 9,
                    name = "Popular Aerospeed 700C Gravel Hybrid Cycle",
                    category = ProductCategory.MENS.displayName,
                    brand = "Popular Cycle",
                    originalPrice = 24999.0,
                    discountedPrice = 17999.0,
                    stock = 11,
                    imageResId = R.drawable.cycle_mtb_1791185202930,
                    description = "High-speed gravel and urban commuter featuring hydroformed double-butted alloy frame, rigid aero fork, 700x38C fast-rolling tyres, and 24-speed microSHIFT gears.",
                    frameMaterial = "Hydroformed 6061 Alloy",
                    gears = "24 Speed microSHIFT",
                    brakes = "Dual Flat-Mount Disc",
                    wheelSize = "700 x 38C",
                    offerTag = "CLEARANCE SALE",
                    isOnSale = true,
                    isFeatured = true,
                    rating = 4.8f,
                    reviewCount = 76
                ),
                ProductEntity(
                    id = 10,
                    name = "MRF Mogrip Moto-D 100/90-17 Motorcycle Tyre",
                    category = ProductCategory.AUTO_TYRE.displayName,
                    brand = "MRF",
                    originalPrice = 3299.0,
                    discountedPrice = 2499.0,
                    stock = 18,
                    imageResId = R.drawable.cycle_parts_tyre_1791185235544,
                    description = "Directional block pattern dual-sport motorcycle rear tyre for Pulsar, Apache, and FZ series. High endurance tread compound and superior dry/wet grip.",
                    frameMaterial = "Heavy Nylon Ply",
                    gears = "N/A",
                    brakes = "N/A",
                    wheelSize = "17 Inch Rim",
                    offerTag = "HOT OFFER",
                    isOnSale = true,
                    isFeatured = false,
                    rating = 4.7f,
                    reviewCount = 42
                )
            )
            productDao.insertProducts(initialProducts)

            val initialBanners = listOf(
                BannerEntity(
                    title = "MEGA CYCLE FEST 2026",
                    subtitle = "Up to 45% OFF + Free Assembly & Helmet",
                    discountTag = "FLAT 45% OFF",
                    targetCategory = "All Products",
                    imageResId = R.drawable.home_cycle_banner_1791185136122,
                    isLoginPromo = false,
                    isActive = true
                ),
                BannerEntity(
                    title = "E-CYCLE REVOLUTION",
                    subtitle = "Say Goodbye to Fuel Costs • Flat ₹9,500 Discount",
                    discountTag = "ELECTRIC POWER",
                    targetCategory = ProductCategory.ECYCLE.displayName,
                    imageResId = R.drawable.login_promo_banner_1791185121376,
                    isLoginPromo = false,
                    isActive = true
                ),
                BannerEntity(
                    title = "POPULAR CYCLE EXCLUSIVE",
                    subtitle = "Premium Bikes, Tyres & Spares at Factory Prices",
                    discountTag = "EXTRA ₹1,000 OFF",
                    targetCategory = "All Products",
                    imageResId = R.drawable.login_promo_banner_1791185121376,
                    isLoginPromo = true,
                    isActive = true
                )
            )
            bannerDao.insertBanners(initialBanners)

            val initialOrders = listOf(
                OrderEntity(
                    orderNumber = "ORD-2026-9812",
                    customerName = "Rahul Sharma",
                    customerPhone = "9876543210",
                    deliveryAddress = "Flat 402, Green Avenue, Rohini Sector 14, Delhi - 110085",
                    totalAmount = 12999.0,
                    paymentMethod = "UPI (Google Pay)",
                    orderStatus = "Out for Delivery",
                    orderDateMillis = System.currentTimeMillis() - 86400000L * 2,
                    itemsSummary = "1x Popular Dominator Pro 29T MTB (₹12,999)"
                ),
                OrderEntity(
                    orderNumber = "ORD-2026-9745",
                    customerName = "Pooja Verma",
                    customerPhone = "9812345678",
                    deliveryAddress = "B-12, Gomti Nagar, Lucknow, UP - 226010",
                    totalAmount = 9298.0,
                    paymentMethod = "Cash on Delivery",
                    orderStatus = "Confirmed",
                    orderDateMillis = System.currentTimeMillis() - 86400000L * 4,
                    itemsSummary = "1x Popular Grace Bella 26T City Bicycle (₹8,299), 1x Ralco MTB Tyre (₹999)"
                )
            )
            initialOrders.forEach { orderDao.insertOrder(it) }
        }
    }
}
