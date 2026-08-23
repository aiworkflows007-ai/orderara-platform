package com.orderara.partner.ui.screens.orders

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.orderara.partner.data.models.PartnerOrderStatus
import com.orderara.partner.data.models.StaffRole
import com.orderara.partner.ui.components.OrderCard
import com.orderara.partner.ui.components.PartnerTopBar
import com.orderara.partner.ui.theme.*

@Composable
fun PartnerOrdersScreen(
    onNavigateToChat: (String, String) -> Unit,
    viewModel: PartnerOrdersViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showRoleDialog by remember { mutableStateOf(false) }

    if (showRoleDialog) {
        RoleSwitchDialog(
            currentRole = uiState.currentRole,
            onSelectRole = {
                viewModel.switchRole(it)
                showRoleDialog = false
            },
            onDismiss = { showRoleDialog = false }
        )
    }

    Scaffold(
        topBar = {
            PartnerTopBar(
                profile = uiState.profile,
                currentRole = uiState.currentRole,
                onToggleStoreOpen = { viewModel.toggleStoreOpen() },
                onSwitchRoleClick = { showRoleDialog = true }
            )
        },
        containerColor = DarkNavy,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Filter Pills Bar (All, New, Preparing, Out for Delivery, Delivered)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedStatusFilter == null,
                            onClick = { viewModel.selectFilter(null) },
                            label = { Text("All Orders", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceDark,
                                labelColor = TextLight,
                                selectedContainerColor = PartnerPrimary,
                                selectedLabelColor = SurfaceWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    items(PartnerOrderStatus.values()) { status ->
                        FilterChip(
                            selected = uiState.selectedStatusFilter == status,
                            onClick = { viewModel.selectFilter(if (uiState.selectedStatusFilter == status) null else status) },
                            label = { Text(status.label, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceDark,
                                labelColor = TextLight,
                                selectedContainerColor = PartnerPrimary,
                                selectedLabelColor = SurfaceWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Quick Stats Banner for Kitchen
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE LIVE ORDERS: ${uiState.orders.count { it.status != PartnerOrderStatus.DELIVERED && it.status != PartnerOrderStatus.REJECTED }}",
                        color = TextLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = "⚡ Real-time Dispatch",
                        color = PartnerPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Orders list or Empty state
            if (uiState.orders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = TextLightSecondary, modifier = Modifier.size(48.dp))
                            Text("No orders in this status", color = TextLightSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                items(uiState.orders, key = { it.subOrderId }) { order ->
                    OrderCard(
                        order = order,
                        onAccept = { viewModel.acceptOrder(order.subOrderId) },
                        onReject = { viewModel.rejectOrder(order.subOrderId) },
                        onAdvanceStatus = { nextStatus -> viewModel.updateStatus(order.subOrderId, nextStatus) },
                        onChatClick = { onNavigateToChat(order.subOrderId, order.customerName) },
                        onCallClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RoleSwitchDialog(
    currentRole: StaffRole,
    onSelectRole: (StaffRole) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Switch Staff Role", fontWeight = FontWeight.Black)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Test different view modes and access levels in OrderAra Partner App:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                StaffRole.values().forEach { role ->
                    val isSelected = currentRole == role
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRole(role) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PartnerPrimaryLight else BackgroundLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PartnerPrimary else BorderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = role.title,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PartnerPrimaryDark else TextPrimary
                                )
                                Text(
                                    text = when (role) {
                                        StaffRole.OWNER -> "Full access to Analytics, Billing & Menu"
                                        StaffRole.MANAGER -> "Orders, Menu, Inventory & Chat"
                                        StaffRole.KITCHEN_STAFF -> "Dedicated Kitchen Display Screen (KDS)"
                                    },
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = PartnerPrimary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PartnerPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}
