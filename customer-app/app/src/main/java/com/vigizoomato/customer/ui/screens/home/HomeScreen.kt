package com.vigizoomato.customer.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vigizoomato.customer.ui.components.*
import com.vigizoomato.customer.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(SurfaceWhite)) {
                HomeLocationHeader(
                    currentAddress = uiState.currentAddress,
                    onAddressClick = onNavigateToAddresses,
                    onNotificationsClick = { /* open notifications */ }
                )
                SearchBarPreview(
                    onClick = onNavigateToSearch,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        },
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Promo Banners Spotlight
            item {
                Spacer(modifier = Modifier.height(6.dp))
                PromoBannersCarousel(
                    coupons = uiState.coupons,
                    onCouponClick = { coupon ->
                        viewModel.applyCouponToCart(coupon)
                    }
                )
            }

            // Cuisine Category Pills
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "What's on your mind?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "See All",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(viewModel.cuisines) { item ->
                            val cuisine = item.name
                            val isSelected = uiState.selectedCuisine == cuisine || (cuisine == "All" && uiState.selectedCuisine == null)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    // mergeDescendants keeps the photo and label as one
                                    // node instead of leaking a bare 13dp text node, and
                                    // contentDescription names the tile. (Modifier order
                                    // relative to selectable makes no observable difference
                                    // here -- both were measured on device.)
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = cuisine
                                    }
                                    .selectable(
                                        selected = isSelected,
                                        onClick = { viewModel.selectCuisine(if (cuisine == "All") null else cuisine) },
                                        role = Role.RadioButton
                                    )
                                    .padding(2.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // The ring lives on an outer box so that selecting a
                                // tile never changes its size -- a border drawn on the
                                // photo itself would nudge every neighbouring tile.
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .shadow(if (isSelected) 6.dp else 2.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(if (isSelected) PrimaryOrangeLight else SurfaceWhite)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) PrimaryOrange else BorderLight,
                                            shape = CircleShape
                                        )
                                        .padding(if (isSelected) 2.dp else 1.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.photoUrl != null) {
                                        AsyncImage(
                                            model = item.photoUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                // Neutral fill holds the circle while the
                                                // photo downloads, so the rail never pops.
                                                .background(BackgroundLight)
                                        )
                                    } else if (item.icon != null) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) PrimaryOrange else TextSecondary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = cuisine,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                    color = if (isSelected) PrimaryOrange else TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Quick Filter Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = uiState.isVegOnly,
                        onClick = { viewModel.toggleVegOnly() },
                        label = { Text("Pure Veg", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = { VegNonVegBadge(isVeg = true, size = 13.dp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryGreenLight,
                            selectedLabelColor = VegGreen
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    FilterChip(
                        selected = uiState.sortBy == "Rating",
                        onClick = { viewModel.setSortBy(if (uiState.sortBy == "Rating") "Popular" else "Rating") },
                        label = { Text("4.0+", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = GoldStar,
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    )

                    FilterChip(
                        selected = uiState.sortBy == "DeliveryTime",
                        onClick = { viewModel.setSortBy(if (uiState.sortBy == "DeliveryTime") "Popular" else "DeliveryTime") },
                        label = { Text("Fastest", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ALL RESTAURANTS (${uiState.filteredRestaurants.size})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Restaurant Cards
            items(uiState.filteredRestaurants) { rest ->
                RestaurantCard(
                    restaurant = rest,
                    onRestaurantClick = onNavigateToRestaurant,
                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
