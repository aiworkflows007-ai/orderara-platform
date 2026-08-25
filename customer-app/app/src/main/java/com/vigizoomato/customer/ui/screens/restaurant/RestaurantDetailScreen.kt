package com.vigizoomato.customer.ui.screens.restaurant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.vigizoomato.customer.ui.components.*
import com.vigizoomato.customer.ui.theme.*

@Composable
fun RestaurantDetailScreen(
    restaurantId: String,
    onBackClick: () -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: RestaurantDetailViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(restaurantId) {
        viewModel.loadRestaurant(restaurantId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val restaurant = uiState.restaurant

    if (restaurant == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryOrange)
        }
        return
    }

    // Check cart progress for this restaurant
    val groupForThisRest = uiState.cartSummary.groups.find { it.restaurantId == restaurantId }
    val currentSpend = groupForThisRest?.subTotal ?: 0.0
    val minOrder = restaurant.minOrderValue
    val minOrderMet = currentSpend >= minOrder

    Scaffold(
        bottomBar = {
            FloatingCartBar(
                itemCount = uiState.cartSummary.totalItemCount,
                restaurantCount = uiState.cartSummary.groups.size,
                totalPrice = uiState.cartSummary.grandTotal,
                onViewCartClick = onNavigateToCart
            )
        },
        containerColor = BackgroundLight,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Image Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    AsyncImage(
                        model = restaurant.bannerUrl,
                        contentDescription = restaurant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                    )

                    // Navigation Bar (Back & Favorite)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SurfaceWhite)
                        }

                        IconButton(
                            onClick = { viewModel.toggleFavorite() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                        ) {
                            Icon(
                                imageVector = if (restaurant.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (restaurant.isFavorite) PrimaryOrange else SurfaceWhite
                            )
                        }
                    }

                    // Restaurant Name & Tag at bottom of Hero
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = SurfaceWhite
                        )
                        Text(
                            text = restaurant.cuisineTypes.joinToString(" • "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SurfaceWhite.copy(alpha = 0.92f)
                        )
                    }
                }
            }

            // Restaurant Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (restaurant.rating >= 4.0) SecondaryGreen else GoldStar)
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = String.format("%.1f", restaurant.rating),
                                            color = SurfaceWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = SurfaceWhite,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "• ${restaurant.deliveryTimeMinutes} mins delivery",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = "Radius: ${restaurant.deliveryRadiusKm} km",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = OfferBlue
                            )
                        }

                        restaurant.discountOffer?.let { offer ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(OfferBlueBg)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = offer,
                                    color = OfferBlue,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // Minimum Order Value Status Bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (minOrderMet) SecondaryGreenLight else WarningAmberBg)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (minOrderMet) "Minimum Order Met (₹${minOrder.toInt()})" else "Min Order Requirement: ₹${minOrder.toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (minOrderMet) SecondaryGreen else WarningAmber
                                )
                                Text(
                                    text = "Current: ₹${currentSpend.toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (minOrderMet) SecondaryGreen else WarningAmber
                                )
                            }

                            if (!minOrderMet && currentSpend > 0) {
                                val remaining = minOrder - currentSpend
                                Text(
                                    text = "Add items worth ₹${remaining.toInt()} more from ${restaurant.name} to checkout this sub-order.",
                                    fontSize = 11.sp,
                                    color = WarningAmber
                                )
                            }
                        }
                    }
                }
            }

            if (!restaurant.isOpen) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = NonVegRed.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.RemoveShoppingCart, contentDescription = null, tint = NonVegRed, modifier = Modifier.size(20.dp))
                            Column {
                                Text(
                                    text = "Currently Closed",
                                    color = NonVegRed,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "This kitchen is offline and not accepting orders right now.",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Category Filter Pills & Veg Only Toggle
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Menu Items",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )

                        FilterChip(
                            selected = uiState.isVegOnlyFilter,
                            onClick = { viewModel.toggleVegOnly() },
                            label = { Text("Veg Only", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = { VegNonVegBadge(isVeg = true, size = 13.dp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedCategory == null,
                                onClick = { viewModel.selectCategory(null) },
                                label = { Text("All (${uiState.menuItems.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        items(uiState.categories) { cat ->
                            FilterChip(
                                selected = uiState.selectedCategory == cat,
                                onClick = { viewModel.selectCategory(cat) },
                                label = { Text(cat, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // Menu Items List
            items(uiState.menuItems) { item ->
                val qty = uiState.itemQuantities[item.id] ?: 0
                val effectiveItem = if (!restaurant.isOpen) item.copy(isAvailable = false) else item
                MenuItemCard(
                    item = effectiveItem,
                    quantity = qty,
                    onAdd = { if (restaurant.isOpen) viewModel.addToCart(item) },
                    onRemove = { viewModel.removeFromCart(item.id) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
