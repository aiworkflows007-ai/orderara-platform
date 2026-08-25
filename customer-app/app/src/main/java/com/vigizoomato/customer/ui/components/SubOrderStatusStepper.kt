package com.vigizoomato.customer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vigizoomato.customer.data.models.OrderStatus
import com.vigizoomato.customer.data.models.SubOrder
import com.vigizoomato.customer.ui.theme.*

@Composable
fun SubOrderStatusCard(
    subOrder: SubOrder,
    onChatClick: (String, String) -> Unit,
    onCallClick: (String) -> Unit,
    onRateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        OrderStatus.PLACED,
        OrderStatus.ACCEPTED,
        OrderStatus.PREPARING,
        OrderStatus.OUT_FOR_DELIVERY,
        OrderStatus.DELIVERED
    )
    val currentStepIndex = subOrder.status.stepIndex

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Restaurant Header & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subOrder.restaurantName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Sub-Order #${subOrder.subOrderId} • ${subOrder.items.sumOf { it.quantity }} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                val badgeColor = when (subOrder.status) {
                    OrderStatus.DELIVERED -> SecondaryGreen
                    OrderStatus.OUT_FOR_DELIVERY -> PrimaryOrange
                    OrderStatus.CANCELLED -> NonVegRed
                    else -> OfferBlue
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = subOrder.status.label,
                        color = badgeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Items Summary Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundLight)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                subOrder.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            VegNonVegBadge(isVeg = item.menuItem.isVeg, size = 12.dp)
                            Text(
                                text = "${item.quantity}x ${item.menuItem.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "₹${item.totalPrice.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Horizontal Stepper Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, step ->
                    val isCompleted = index <= currentStepIndex
                    val isCurrent = index == currentStepIndex

                    // Step Node Indicator
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> PrimaryOrange
                                    else -> BorderLight
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < currentStepIndex || subOrder.status == OrderStatus.DELIVERED) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = SurfaceWhite,
                                modifier = Modifier.size(15.dp)
                            )
                        } else {
                            Text(
                                text = (index + 1).toString(),
                                color = if (isCurrent) SurfaceWhite else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Timeline line
                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.5.dp)
                                .background(if (index < currentStepIndex) PrimaryOrange else BorderLight)
                        )
                    }
                }
            }

            // Estimated Delivery Card
            if (subOrder.status != OrderStatus.DELIVERED && subOrder.status != OrderStatus.CANCELLED) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryOrangeLight)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Estimated Delivery",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryOrangeDark
                        )
                        Text(
                            text = "~${subOrder.estimatedDeliveryMinutes} mins • Direct Staff Delivery",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryOrangeDark
                        )
                    }

                    if (subOrder.status == OrderStatus.OUT_FOR_DELIVERY) {
                        Text(
                            text = subOrder.driverName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryOrangeDark
                        )
                    }
                }
            }

            // Action Buttons: Chat with Restaurant / Call Staff / Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (subOrder.status == OrderStatus.DELIVERED) {
                    if (!subOrder.isRated) {
                        Button(
                            onClick = onRateClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.StarBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rate This Restaurant & Food", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Rated: ${subOrder.ratingScore}", color = SecondaryGreen, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(Icons.Filled.Star, contentDescription = null, tint = SecondaryGreen, modifier = Modifier.size(13.dp))
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { onChatClick(subOrder.subOrderId, subOrder.restaurantName) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryOrange)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = "Chat", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chat Staff", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onCallClick(subOrder.restaurantPhone) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Outlined.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Kitchen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

        }
    }
}
