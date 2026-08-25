package com.orderara.partner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orderara.partner.data.models.IncomingSubOrder
import com.orderara.partner.data.models.PartnerOrderStatus
import com.orderara.partner.ui.theme.*

@Composable
fun OrderCard(
    order: IncomingSubOrder,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onAdvanceStatus: (PartnerOrderStatus) -> Unit,
    onChatClick: () -> Unit,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Sub-Order ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "#${order.subOrderId}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "• ${order.orderTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Order: ${order.parentOrderId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Status Badge
                val badgeColor = when (order.status) {
                    PartnerOrderStatus.PLACED -> WarningAmber
                    PartnerOrderStatus.ACCEPTED -> InfoBlue
                    PartnerOrderStatus.PREPARING -> PartnerPrimary
                    PartnerOrderStatus.OUT_FOR_DELIVERY -> InfoBlue
                    PartnerOrderStatus.DELIVERED -> SuccessEmerald
                    PartnerOrderStatus.REJECTED -> AlertRed
                    PartnerOrderStatus.CANCELLED -> AlertRed
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = order.status.label.uppercase(),
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Customer Info & Quick Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundLight)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = order.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Place,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = order.deliveryAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // 48dp tap area (Android minimum) with a 36dp visible circle inside.
                // A kitchen is the worst possible place to need a precise tap.
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onChatClick,
                        // Label belongs on the button, not the 18dp icon inside it --
                        // IconButton does not merge child semantics, so a label on the
                        // icon leaves the actual button nameless to a screen reader.
                        modifier = Modifier
                            .size(48.dp)
                            .semantics { contentDescription = "Chat customer" }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceWhite),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = null, tint = PartnerPrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    IconButton(
                        onClick = onCallClick,
                        // Label belongs on the button, not the 18dp icon inside it --
                        // IconButton does not merge child semantics, so a label on the
                        // icon leaves the actual button nameless to a screen reader.
                        modifier = Modifier
                            .size(48.dp)
                            .semantics { contentDescription = "Call customer" }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceWhite),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Phone, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Special Instructions Alert
            if (order.specialInstructions.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(WarningAmberLight)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.EditNote,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Notes: ${order.specialInstructions}",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            HorizontalDivider(color = BorderLight)

            // Items Checklist (KOT)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                order.items.forEach { item ->
                    var isChecked by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isChecked = !isChecked },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isChecked) SuccessEmerald else BackgroundLight)
                                    .border(1.dp, if (isChecked) SuccessEmerald else BorderLight, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChecked) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = SurfaceWhite, modifier = Modifier.size(14.dp))
                                }
                            }

                            Column {
                                Text(
                                    text = "${item.quantity}x ${item.name}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChecked) TextSecondary else TextPrimary
                                )
                                if (item.specialNotes.isNotBlank()) {
                                    Text(
                                        text = item.specialNotes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PartnerPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
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

            HorizontalDivider(color = BorderLight)

            // Earnings & Payment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Net Earnings",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "₹${order.netEarnings.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = SuccessEmerald
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SuccessEmeraldLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.paymentStatus,
                        color = SuccessEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Primary Workflow Actions
            when (order.status) {
                PartnerOrderStatus.PLACED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PartnerPrimary)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Accept & Start Prep", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                PartnerOrderStatus.ACCEPTED -> {
                    Button(
                        onClick = { onAdvanceStatus(PartnerOrderStatus.PREPARING) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PartnerPrimary)
                    ) {
                        Icon(Icons.Filled.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Food Preparation", fontWeight = FontWeight.Bold)
                    }
                }

                PartnerOrderStatus.PREPARING -> {
                    Button(
                        onClick = { onAdvanceStatus(PartnerOrderStatus.OUT_FOR_DELIVERY) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
                    ) {
                        Icon(Icons.Filled.DeliveryDining, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dispatch: Out for Delivery", fontWeight = FontWeight.Bold)
                    }
                }

                PartnerOrderStatus.OUT_FOR_DELIVERY -> {
                    Button(
                        onClick = { onAdvanceStatus(PartnerOrderStatus.DELIVERED) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm Delivered to Customer", fontWeight = FontWeight.Bold)
                    }
                }

                PartnerOrderStatus.DELIVERED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SuccessEmeraldLight)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = SuccessEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Sub-Order Successfully Delivered",
                                color = SuccessEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                PartnerOrderStatus.REJECTED, PartnerOrderStatus.CANCELLED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AlertRedLight)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Cancel,
                                contentDescription = null,
                                tint = AlertRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (order.status == PartnerOrderStatus.REJECTED) "Order Rejected" else "Order Cancelled",
                                color = AlertRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
