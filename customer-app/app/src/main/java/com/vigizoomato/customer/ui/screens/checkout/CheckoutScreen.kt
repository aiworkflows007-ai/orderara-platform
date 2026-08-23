package com.vigizoomato.customer.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vigizoomato.customer.ui.components.AppHeader
import com.vigizoomato.customer.ui.theme.*

data class PaymentOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isRecommended: Boolean = false
)

@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    viewModel: CheckoutViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.cartSummary

    val paymentOptions = listOf(
        PaymentOption("UPI (Google Pay)", "Google Pay UPI", "Instant payment via UPI PIN", Icons.Filled.AccountBalanceWallet, isRecommended = true),
        PaymentOption("UPI (PhonePe)", "PhonePe UPI", "Fast checkout via PhonePe", Icons.Filled.AccountBalanceWallet),
        PaymentOption("UPI (Paytm)", "Paytm UPI", "Pay via Paytm app", Icons.Filled.AccountBalanceWallet),
        PaymentOption("Cards", "Credit / Debit Card", "Visa, Mastercard, RuPay", Icons.Filled.CreditCard),
        PaymentOption("NetBanking", "Net Banking", "All major Indian banks", Icons.Filled.AccountBalance),
        PaymentOption("Wallets", "Wallets (Paytm / Amazon Pay)", "Direct balance deduction", Icons.Outlined.AccountBalanceWallet)
    )

    Scaffold(
        topBar = {
            AppHeader(
                title = "Payment Options",
                subtitle = "Grand Total: ₹${summary.grandTotal.toInt()}",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceWhite,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.executePaymentAndPlaceOrder(
                                deliveryInstructions = "",
                                onOrderPlaced = onOrderPlaced
                            )
                        },
                        enabled = !uiState.isProcessingPayment && summary.grandTotal > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        if (uiState.isProcessingPayment) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(color = SurfaceWhite, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Text("Processing Secure Payment...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "Pay ₹${summary.grandTotal.toInt()} via ${uiState.selectedPaymentMethod}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
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
            // Online Payment Policy Notice
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryGreenLight)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = SecondaryGreen)
                        Column {
                            Text("100% Safe & Secure Online Payment", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SecondaryGreen)
                            Text("Split payouts are automatically dispatched to each restaurant partner.", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }

            // Payment Methods List
            item {
                Text(
                    text = "Select Payment Method",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(paymentOptions) { option ->
                val isSelected = uiState.selectedPaymentMethod == option.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectPaymentMethod(option.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PrimaryOrange) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) PrimaryOrangeLight else BackgroundLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = option.title,
                                    tint = if (isSelected) PrimaryOrange else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = option.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (option.isRecommended) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(SecondaryGreenLight)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("RECOMMENDED", color = SecondaryGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(
                                    text = option.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.selectPaymentMethod(option.id) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryOrange)
                        )
                    }
                }
            }

            // Summary Breakdown Pill
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Sub-Orders Count: ${summary.groups.size}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("Delivering To: ${uiState.deliveryAddress?.street ?: "Selected Address"}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Divider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Payable:", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("₹${summary.grandTotal.toInt()}", fontWeight = FontWeight.ExtraBold, color = PrimaryOrange, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
