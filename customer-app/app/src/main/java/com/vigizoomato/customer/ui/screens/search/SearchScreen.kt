package com.vigizoomato.customer.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import com.vigizoomato.customer.ui.components.*
import com.vigizoomato.customer.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: SearchViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceWhite,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search Input
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = { Text("Search restaurants or dishes...", color = TextSecondary) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = PrimaryOrange
                            )
                        },
                        trailingIcon = {
                            if (uiState.query.isNotBlank()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = BorderLight,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = uiState.vegOnly,
                            onClick = { viewModel.toggleVegOnly() },
                            label = { Text("Pure Veg", fontSize = 12.sp) },
                            leadingIcon = { VegNonVegBadge(isVeg = true, size = 12.dp) }
                        )

                        FilterChip(
                            selected = uiState.minRating == 4.0,
                            onClick = { viewModel.setMinRating(4.0) },
                            label = { Text("4.0+", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = GoldStar,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )

                        FilterChip(
                            selected = uiState.minRating == 4.5,
                            onClick = { viewModel.setMinRating(4.5) },
                            label = { Text("4.5+", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = GoldStar,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                }
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Popular searches suggestions if query is empty
            if (uiState.query.isBlank()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Popular Searches",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.popularSearches) { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(SurfaceWhite)
                                        .clickable { viewModel.onQueryChange(tag) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "🔍 $tag",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Results count
            item {
                Text(
                    text = "${uiState.searchResults.size} Results Found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Restaurant Cards
            items(uiState.searchResults) { rest ->
                RestaurantCard(
                    restaurant = rest,
                    onRestaurantClick = onNavigateToRestaurant,
                    onFavoriteToggle = { viewModel.toggleFavorite(it) }
                )
            }
        }
    }
}
