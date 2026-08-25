package com.vigizoomato.customer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vigizoomato.customer.data.models.MenuItem
import com.vigizoomato.customer.ui.theme.*

@Composable
fun MenuItemCard(
    item: MenuItem,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left: Dish Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 14.dp)
                    .alpha(if (item.isAvailable) 1f else 0.55f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VegNonVegBadge(isVeg = item.isVeg, size = 15.dp)

                    if (item.isBestSeller) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoldStar.copy(alpha = 0.18f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = DarkCharcoal,
                                    modifier = Modifier.size(9.dp)
                                )
                                Text(
                                    text = "BESTSELLER",
                                    color = DarkCharcoal,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    if (item.spicyLevel > 1) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                            modifier = Modifier.semantics {
                                contentDescription = "Spice level ${item.spicyLevel} of 3"
                            }
                        ) {
                            repeat(item.spicyLevel) {
                                Icon(
                                    Icons.Filled.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = NonVegRed,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Text(
                    text = "₹${item.price.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = DarkCharcoal
                )

                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                Text(
                    text = "Customisable",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            // Right: Food Photo with Floating Add / Quantity Button
            Box(
                modifier = Modifier
                    .size(width = 115.dp, height = 115.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                )

                if (!item.isAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkCharcoal.copy(alpha = 0.35f))
                    )
                }

                // Quantity selector anchored at bottom center of the image
                Box(
                    modifier = Modifier
                        .offset(y = 12.dp)
                        .wrapContentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isAvailable) {
                        QuantitySelector(
                            quantity = quantity,
                            onAdd = onAdd,
                            onRemove = onRemove
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BorderLight,
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                text = "OUT OF STOCK",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
