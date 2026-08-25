package com.orderara.partner.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orderara.partner.OrderAraPartnerApp
import com.orderara.partner.ui.theme.*

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToOnboarding: () -> Unit = {}
) {
    val authRepo = OrderAraPartnerApp.instance.authRepository
    val profile by authRepo.currentProfile.collectAsState()

    var radius by remember(profile.deliveryRadiusKm) { mutableStateOf(profile.deliveryRadiusKm.toFloat()) }
    var minOrder by remember(profile.minOrderValue) { mutableStateOf(profile.minOrderValue.toFloat()) }
    var showSavedToast by remember { mutableStateOf(false) }

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
                        text = "Store Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextLight
                    )
                    Text(
                        text = "Configure delivery radius & minimum order value",
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
            // Delivery Radius Slider Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Delivery Radius",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                            Text(
                                text = "${radius.toInt()} km",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = PartnerPrimary
                            )
                        }

                        Text(
                            text = "Only customers within this distance can order from your restaurant.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLightSecondary
                        )

                        Slider(
                            value = radius,
                            onValueChange = { radius = it },
                            valueRange = 1f..15f,
                            steps = 13,
                            colors = SliderDefaults.colors(
                                thumbColor = PartnerPrimary,
                                activeTrackColor = PartnerPrimary,
                                inactiveTrackColor = SurfaceDarkLight
                            )
                        )
                    }
                }
            }

            // Minimum Order Value Slider Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Minimum Order Threshold",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                            Text(
                                text = "₹${minOrder.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = PartnerPrimary
                            )
                        }

                        Text(
                            text = "Customer sub-orders must meet this amount to checkout.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLightSecondary
                        )

                        Slider(
                            value = minOrder,
                            onValueChange = { minOrder = it },
                            valueRange = 99f..499f,
                            steps = 7,
                            colors = SliderDefaults.colors(
                                thumbColor = PartnerPrimary,
                                activeTrackColor = PartnerPrimary,
                                inactiveTrackColor = SurfaceDarkLight
                            )
                        )
                    }
                }
            }

            // Save Settings CTA Button
            item {
                Button(
                    onClick = {
                        authRepo.updateStoreSettings(radius.toDouble(), minOrder.toDouble())
                        showSavedToast = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PartnerPrimary)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Settings", fontWeight = FontWeight.Bold)
                }

                if (showSavedToast) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SuccessEmeraldLight)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = SuccessEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Store settings updated successfully!", color = SuccessEmerald, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Store Info & Payout Details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Restaurant Payout Info",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                        Text(
                            text = "Linked UPI ID: ${profile.upiId}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SuccessEmerald
                        )
                        Text(
                            text = "Registered Email: ${profile.email}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextLightSecondary
                        )
                        Text(
                            text = "Restaurant Platform: v1.0.0 Partner",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLightSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedButton(
                            onClick = onNavigateToOnboarding,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PartnerPrimary)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Register Another Restaurant", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
