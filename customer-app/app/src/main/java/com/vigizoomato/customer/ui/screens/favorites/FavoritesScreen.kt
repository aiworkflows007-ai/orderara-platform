package com.vigizoomato.customer.ui.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vigizoomato.customer.VigiZoomatoApp
import com.vigizoomato.customer.ui.components.AppHeader
import com.vigizoomato.customer.ui.components.RestaurantCard
import com.vigizoomato.customer.ui.theme.*

@Composable
fun FavoritesScreen(
    onNavigateToRestaurant: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val repo = VigiZoomatoApp.container.restaurantRepository
    val restaurants by repo.restaurants.collectAsState()
    val favorites = restaurants.filter { it.isFavorite }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Favorite Restaurants",
                subtitle = "${favorites.size} Saved Places"
            )
        },
        containerColor = BackgroundLight,
        modifier = modifier
    ) { innerPadding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(50.dp))
                    Text("No favorite restaurants yet", fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text("Tap the heart icon on any restaurant to save it here.", color = TextTertiary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(favorites) { rest ->
                    RestaurantCard(
                        restaurant = rest,
                        onRestaurantClick = onNavigateToRestaurant,
                        onFavoriteToggle = { repo.toggleFavorite(it) }
                    )
                }
            }
        }
    }
}
