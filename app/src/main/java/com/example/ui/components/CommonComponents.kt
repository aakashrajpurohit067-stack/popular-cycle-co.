package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.entity.ProductEntity
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldDiscount
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.OfferBadgeBackground
import com.example.ui.theme.OfferBadgeBorder
import com.example.ui.theme.OfferBadgeText
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StrikePrice

@Composable
fun StrikeThroughPriceTag(
    originalPrice: Double,
    discountedPrice: Double,
    modifier: Modifier = Modifier,
    largeFont: Boolean = false
) {
    val discountPercent = if (originalPrice > discountedPrice && originalPrice > 0) {
        (((originalPrice - discountedPrice) / originalPrice) * 100).toInt()
    } else 0

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "₹${discountedPrice.toInt()}",
                fontSize = if (largeFont) 22.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (originalPrice > discountedPrice) {
                Text(
                    text = "₹${originalPrice.toInt()}",
                    fontSize = if (largeFont) 15.sp else 12.sp,
                    color = StrikePrice,
                    textDecoration = TextDecoration.LineThrough
                )
            }
        }
        if (discountPercent > 0) {
            Text(
                text = "$discountPercent% OFF",
                fontSize = if (largeFont) 13.sp else 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldDiscount
            )
        }
    }
}

@Composable
fun OfferTagBadge(
    tag: String,
    modifier: Modifier = Modifier
) {
    if (tag.isBlank()) return
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = OfferBadgeBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, OfferBadgeBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalOffer,
                contentDescription = null,
                tint = OfferBadgeText,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = tag.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = OfferBadgeText
            )
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFF1F5F9))
            ) {
                val photoList = product.getPhotoList()
                val imageModel: Any = when {
                    photoList.isNotEmpty() -> java.io.File(photoList.first())
                    product.imageUrl.isNotBlank() -> product.imageUrl
                    product.imageResId != 0 -> product.imageResId
                    else -> R.drawable.cycle_mtb_1791185202930
                }
                AsyncImage(
                    model = imageModel,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )

                // Top Offer Badge
                if (product.offerTag.isNotBlank() || product.isOnSale) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        OfferTagBadge(tag = if (product.offerTag.isNotBlank()) product.offerTag else "SALE")
                    }
                }

                // Rating pill
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "${product.rating}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = product.brand.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateTextSecondary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                StrikeThroughPriceTag(
                    originalPrice = product.originalPrice,
                    discountedPrice = product.discountedPrice
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (product.stock > 0) "In Stock (${product.stock})" else "Out of Stock",
                        fontSize = 10.sp,
                        color = if (product.stock > 0) EmeraldDiscount else Color.Red,
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("add_to_cart_${product.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = "Add to Cart",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
