package com.vigizoomato.customer.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.vigizoomato.customer.VigiZoomatoApp
import com.vigizoomato.customer.ui.components.AppHeader
import com.vigizoomato.customer.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateToAddresses: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authRepo = VigiZoomatoApp.container.authRepository
    val user by authRepo.currentUser.collectAsState()

    Scaffold(
        topBar = {
            AppHeader(
                title = "My Account",
                subtitle = "Manage profile & preferences"
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // User Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PrimaryOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.name?.take(1) ?: "U",
                                color = SurfaceWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user?.name ?: "Guest User",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = user?.phone ?: "+91 98765 43210",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = user?.email ?: "user@vigizoomato.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }
                    }
                }
            }

            // Options List
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column {
                        ProfileOptionItem(
                            icon = Icons.Outlined.LocationOn,
                            title = "Manage Delivery Addresses",
                            subtitle = "${user?.savedAddresses?.size ?: 0} saved addresses",
                            onClick = onNavigateToAddresses
                        )
                        Divider(color = BorderLight)
                        ProfileOptionItem(
                            icon = Icons.Outlined.FavoriteBorder,
                            title = "Favorite Restaurants",
                            subtitle = "Quick access to your loved food places",
                            onClick = onNavigateToFavorites
                        )
                        Divider(color = BorderLight)
                        ProfileOptionItem(
                            icon = Icons.Outlined.ReceiptLong,
                            title = "Past Orders & Invoices",
                            subtitle = "View order history and receipts",
                            onClick = onNavigateToOrders
                        )
                        Divider(color = BorderLight)
                        ProfileOptionItem(
                            icon = Icons.Outlined.Payment,
                            title = "Payment Methods",
                            subtitle = "Saved UPI IDs, cards and wallets",
                            onClick = { }
                        )
                    }
                }
            }

            // App info & Logout
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column {
                        ProfileOptionItem(
                            icon = Icons.Outlined.Info,
                            title = "About Restaurant",
                            subtitle = "v1.0.0 • Multi-Restaurant Platform",
                            onClick = { }
                        )
                        Divider(color = BorderLight)
                        ProfileOptionItem(
                            icon = Icons.Outlined.ExitToApp,
                            title = "Log Out",
                            subtitle = "Sign out from this device",
                            titleColor = NonVegRed,
                            onClick = {
                                authRepo.logout()
                                onLogout()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = titleColor, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = titleColor)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextTertiary)
    }
}
