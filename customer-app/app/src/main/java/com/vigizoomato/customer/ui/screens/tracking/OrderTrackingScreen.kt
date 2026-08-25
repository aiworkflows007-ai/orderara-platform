package com.vigizoomato.customer.ui.screens.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vigizoomato.customer.data.models.OrderStatus
import com.vigizoomato.customer.ui.components.AppHeader
import com.vigizoomato.customer.ui.components.SubOrderStatusCard
import com.vigizoomato.customer.ui.theme.*

@Composable
fun OrderTrackingScreen(
    orderId: String,
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToChat: (String, String) -> Unit, // subOrderId, restaurantName
    onNavigateToRate: (String, String) -> Unit, // orderId, subOrderId
    viewModel: OrderTrackingViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.order

    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryOrange)
        }
        return
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Order #${order.id}",
                subtitle = "Placed ${order.createdAt} • ${order.subOrders.size} Sub-Orders",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(Icons.Filled.Home, contentDescription = "Home", tint = TextPrimary)
                    }
                }
            )
        },
        containerColor = BackgroundLight,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live status header banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryGreenLight)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SecondaryGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SurfaceWhite)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Order Transmitted Successfully",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryGreen
                            )
                            Text(
                                text = "Each restaurant is preparing their portion with direct restaurant delivery staff.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Sub-Order Tracking Cards for each restaurant
            item {
                Text(
                    text = "Live Restaurant Sub-Orders (${order.subOrders.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(order.subOrders) { subOrder ->
                SubOrderStatusCard(
                    subOrder = subOrder,
                    onChatClick = { sId, rName -> onNavigateToChat(sId, rName) },
                    onCallClick = { /* launch dialer */ },
                    onRateClick = { onNavigateToRate(order.id, subOrder.subOrderId) }
                )
            }

            // Delivery Details & Receipt Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = PrimaryOrange)
                            Text(
                                text = "Delivery Destination",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "${order.deliveryAddress.title}: ${order.deliveryAddress.fullAddress}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        if (order.deliveryInstructions.isNotBlank()) {
                            Text(
                                text = "Note: ${order.deliveryInstructions}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryOrangeDark
                            )
                        }

                        Divider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Outlined.Receipt, contentDescription = null, tint = SecondaryGreen)
                            Text(
                                text = "Payment Summary (${order.paymentMethod})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Transaction Ref:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(order.transactionId, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("₹${order.grandTotal.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = PrimaryOrange)
                        }
                    }
                }
            }
        }
    }
}
