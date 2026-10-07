package com.example.data.ai

import com.example.BuildConfig
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey: String = try {
        BuildConfig.GEMINI_API_KEY
    } catch (_: Exception) {
        ""
    }

    suspend fun chatWithAssistant(
        userMessage: String,
        availableProducts: List<ProductEntity>,
        chatHistory: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateSmartFallbackResponse(userMessage, availableProducts)
        }

        try {
            val catalogSummary = availableProducts.take(12).joinToString("\n") {
                "- ${it.name} (${it.category}, Brand: ${it.brand}): Offer Price ₹${it.discountedPrice.toInt()} (MRP ₹${it.originalPrice.toInt()}, ${it.discountPercent}% OFF), Stock: ${it.stock}, Gears: ${it.gears}, Wheel: ${it.wheelSize}, Offer: ${it.offerTag}"
            }

            val systemPrompt = """
                You are the official Senior Cycle Specialist and Customer Advisor at "Popular Cycle Company" (India's premier cycle, e-cycle, tyre & spare parts retailer).
                Be warm, enthusiastic, concise, and helpful. Use Indian Rupee (₹) symbol.
                Our Store Catalog highlights:
                $catalogSummary
                
                You help customers with:
                1. Cycle frame & wheel sizing according to rider height & age.
                2. Comparing geared MTBs, hybrid bikes, step-through city cycles, and E-cycles.
                3. Choosing genuine tyres (bicycle, scooter, motorbike) & spare parts.
                4. Assembly instructions, warranty info (1 year frame warranty, 7-day exchange), and doorstep delivery.
                Provide bullet points where applicable. Recommend specific models from our catalog whenever relevant.
            """.trimIndent()

            val contentsArray = JSONArray()

            // Add previous history
            for ((role, text) in chatHistory.takeLast(4)) {
                val turnObj = JSONObject()
                turnObj.put("role", if (role == "user") "user" else "model")
                val parts = JSONArray().put(JSONObject().put("text", text))
                turnObj.put("parts", parts)
                contentsArray.put(turnObj)
            }

            // Current message
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            currentTurn.put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
            contentsArray.put(currentTurn)

            val rootJson = JSONObject()
            rootJson.put("contents", contentsArray)

            val sysInstructionObj = JSONObject()
            sysInstructionObj.put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            rootJson.put("systemInstruction", sysInstructionObj)

            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("topP", 0.9)
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val respBody = response.body?.string() ?: ""
                val respJson = JSONObject(respBody)
                val text = respJson
                    .optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            }
        } catch (_: Exception) {
            // Fall through to smart fallback
        }

        generateSmartFallbackResponse(userMessage, availableProducts)
    }

    suspend fun analyzeSearchQuery(
        query: String,
        allProducts: List<ProductEntity>
    ): List<ProductEntity> = withContext(Dispatchers.Default) {
        val lower = query.lowercase().trim()
        if (lower.isEmpty()) return@withContext allProducts

        // Smart heuristics + AI intent matching
        val matches = allProducts.filter { product ->
            val inName = product.name.lowercase().contains(lower)
            val inCategory = product.category.lowercase().contains(lower)
            val inDesc = product.description.lowercase().contains(lower)
            val inBrand = product.brand.lowercase().contains(lower)
            val inGears = product.gears.lowercase().contains(lower)
            val inWheel = product.wheelSize.lowercase().contains(lower)
            val inOffer = product.offerTag.lowercase().contains(lower)

            // Budget filter intent (e.g., "under 15000", "below 10000")
            val underBudgetMatch = if (lower.contains("under") || lower.contains("below") || lower.contains("<")) {
                val budgetDigits = lower.replace(Regex("[^0-9]"), "").toDoubleOrNull()
                budgetDigits != null && product.discountedPrice <= budgetDigits
            } else false

            // Type intent
            val electricIntent = (lower.contains("electric") || lower.contains("e-cycle") || lower.contains("battery") || lower.contains("ebike")) && product.category.contains("E-Cycle")
            val gearIntent = (lower.contains("gear") || lower.contains("speed") || lower.contains("shimano")) && !product.gears.contains("Single Speed")
            val tyreIntent = (lower.contains("tyre") || lower.contains("tire") || lower.contains("tube") || lower.contains("wheel")) && (product.category.contains("Tyre") || product.name.contains("Tyre"))
            val kidIntent = (lower.contains("kid") || lower.contains("child") || lower.contains("junior") || lower.contains("toddler") || lower.contains("trike") || lower.contains("tricycle")) && (product.category.contains("Kids") || product.category.contains("Tricycle"))
            val womenIntent = (lower.contains("women") || lower.contains("lady") || lower.contains("girl") || lower.contains("grace")) && product.category.contains("Women")

            inName || inCategory || inDesc || inBrand || inGears || inWheel || inOffer || underBudgetMatch || electricIntent || gearIntent || tyreIntent || kidIntent || womenIntent
        }

        if (matches.isNotEmpty()) matches else allProducts.take(4)
    }

    private fun generateSmartFallbackResponse(userQuery: String, products: List<ProductEntity>): String {
        val q = userQuery.lowercase()
        return when {
            q.contains("size") || q.contains("height") -> {
                """
                🚲 **Popular Cycle Height & Frame Sizing Guide**:
                
                • **4'10" to 5'3" (147-160 cm)**: 26T Wheel size / 15"-16" Frame (e.g., *Popular Grace Bella 26T*)
                • **5'3" to 5'8" (160-173 cm)**: 27.5T Wheel size / 17"-18" Frame (e.g., *Popular Volt E-Trail 27.5T*)
                • **5'8" to 6'2"+ (173-188 cm)**: 29T Wheel size / 19"-20" Frame (e.g., *Popular Dominator Pro 29T*)
                • **Kids (Ages 5-9)**: 20T Wheel size (*Popular Blaze 20T*)
                • **Toddlers (1.5-4 yrs)**: 3-Wheel Tricycle (*Popular Little Champ*)
                
                Would you like help picking a model for daily commuting or off-road trails?
                """.trimIndent()
            }
            q.contains("electric") || q.contains("e-cycle") || q.contains("battery") -> {
                val ecycle = products.firstOrNull { it.category.contains("E-Cycle") }
                val priceText = ecycle?.let { "₹${it.discountedPrice.toInt()} (Save ₹${it.savingsAmount.toInt()})" } ?: "₹27,499"
                """
                ⚡ **Popular Volt E-Trail 27.5T Highlights**:
                
                • **Battery**: 36V 10.4Ah Li-Ion detachable with lock & key
                • **Range**: 45 to 55 km per single charge (takes only 3.5 hrs)
                • **Riding Modes**: 5-Level Pedal Assist + Full Twist Throttle
                • **Top Speed**: 25 km/h (Govt RTO exempt, No license required)
                • **Price**: $priceText with Free Doorstep Delivery & 2-Year Motor Warranty!
                """.trimIndent()
            }
            q.contains("tyre") || q.contains("puncture") -> {
                """
                🛞 **Tyre & Tube Recommendations**:
                
                • **MTB Cycle Tyre**: *Ralco All-Terrain 27.5x2.10* (Cut-resistant, ₹999)
                • **Scooter Tubeless Tyre**: *CEAT Gripp Pro 90/90-12* (Fits Activa/Jupiter, ₹1,649)
                • **Motorcycle Rear Tyre**: *MRF Mogrip Moto-D 100/90-17* (₹2,499)
                
                All tyres in our shop include genuine manufacturer warranty and fresh factory batch stamps!
                """.trimIndent()
            }
            q.contains("offer") || q.contains("discount") || q.contains("sale") -> {
                """
                🎉 **Current Ongoing Mega Offers at Popular Cycle**:
                
                • **Mega Cycle Fest**: Up to 45% Strike-through discount on select 29T and 27.5T bikes.
                • **Free Cycling Helmet & Lock** with all gear cycles above ₹9,999.
                • **Extra ₹500 Instant Discount** on UPI payment mode.
                • **No-Cost EMI** available starting at ₹999/month on major credit cards.
                """.trimIndent()
            }
            q.contains("order") || q.contains("delivery") || q.contains("assemble") -> {
                """
                🚚 **Delivery & Doorstep Assembly**:
                
                • **100% Assembled Delivery**: Our technicians deliver ready-to-ride cycles in major cities.
                • **Delivery Timeline**: 2 to 4 business days.
                • **Warranty**: 1-Year comprehensive frame & fork warranty.
                • You can track your real-time order status anytime in the **My Orders** tab!
                """.trimIndent()
            }
            else -> {
                """
                Hello! Welcome to **Popular Cycle Company**. 🚴‍♂️
                
                I am your AI cycling assistant. I can help you with:
                1. Finding the right cycle for your height, age, and budget.
                2. Sizing advice (26T, 27.5T, 29T wheels).
                3. Choosing between E-Cycles, Geared Mountain Bikes, and City Bikes.
                4. Checking compatibility for tyres and spare parts.
                5. Applying the best discount offers on your cart.
                
                Tell me what you are looking for today!
                """.trimIndent()
            }
        }
    }
}
