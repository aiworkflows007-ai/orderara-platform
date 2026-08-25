package com.orderara.partner.ui.screens.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CardMembership
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
import com.orderara.partner.ui.theme.*

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val subInfo by viewModel.subscription.collectAsState()

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkNavy,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Partner Subscription",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextLight
                    )
                    Text(
                        text = "Flat monthly model • 0% commission on orders",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLightSecondary
                    )
                }
            }
        },
        containerColor = DarkNavy,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Plan Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = subInfo.planName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = TextLight
                                )
                                Text(
                                    text = "Flat ₹${subInfo.priceMonthly.toInt()}/month",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PartnerPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (subInfo.isTrialActive) WarningAmber.copy(alpha = 0.2f) else SuccessEmerald.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (subInfo.isTrialActive) "FREE TRIAL" else "ACTIVE",
                                    color = if (subInfo.isTrialActive) WarningAmber else SuccessEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Trial progress bar
                        if (subInfo.isTrialActive) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${subInfo.trialDaysRemaining} days remaining in trial",
                                        color = TextLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Next Billing: ${subInfo.nextBillingDate}",
                                        color = TextLightSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                val totalDays = if (subInfo.trialTotalDays <= 0) 14 else subInfo.trialTotalDays
                                val fraction = (subInfo.trialDaysRemaining.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { fraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = WarningAmber,
                                    trackColor = SurfaceDarkLight
                                )
                            }
                        }

                        HorizontalDivider(color = CardBorderDark)

                        // Feature checkmarks
                        listOf(
                            "0% Commission on all customer orders",
                            "Unlimited order receiving & live KDS dispatch",
                            "Direct text chat with customers",
                            "Full menu management & instant stock toggle",
                            "Daily automated bank payouts"
                        ).forEach { feature ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(16.dp))
                                Text(feature, color = TextLight, fontSize = 13.sp)
                            }
                        }

                        if (subInfo.isTrialActive) {
                            Button(
                                onClick = { viewModel.activatePaidPlan() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PartnerPrimary)
                            ) {
                                Text("Renew & Lock In Monthly Plan (₹999)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Invoices History
            item {
                Text(
                    text = "Billing & Invoice History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                )
            }

            items(subInfo.invoices) { inv ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(inv.title, fontWeight = FontWeight.Bold, color = TextLight)
                            Text("${inv.id} • ${inv.date}", fontSize = 11.sp, color = TextLightSecondary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(inv.amount, fontWeight = FontWeight.Bold, color = SuccessEmerald)
                            Text(inv.status, fontSize = 10.sp, color = TextLightSecondary)
                        }
                    }
                }
            }
        }
    }
}
