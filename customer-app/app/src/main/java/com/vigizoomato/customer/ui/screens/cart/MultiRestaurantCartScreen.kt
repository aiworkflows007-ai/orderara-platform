package com.vigizoomato.customer.ui.screens.cart

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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vigizoomato.customer.ui.components.*
import com.vigizoomato.customer.ui.theme.*

@Composable
fun MultiRestaurantCartScreen(
    onBackClick: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onExploreRestaurants: () -> Unit,
    viewModel: CartViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.cartSummary

    Scaffold(
        topBar = {
            AppHeader(
                title = "Your Cart",
                subtitle = if (summary.groups.size > 1) "${summary.groups.size} Restaurants (${summary.totalItemCount} Items)" else "${summary.totalItemCount} Items",
                onBackClick = onBackClick,
                actions = {
                    if (summary.totalItemCount > 0) {
                        IconButton(onClick = { viewModel.clearCart() }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Clear Cart", tint = TextSecondary)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (summary.totalItemCount > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceWhite,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Grand Total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "₹${summary.grandTotal.toInt()}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                            }

                            Button(
                                onClick = onNavigateToCheckout,
                                enabled = summary.allMinOrdersMet,
                                modifier = Modifier
                                    .height(52.dp)
                                    .weight(1f)
                                    .padding(start = 24.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryOrange,
                                    disabledContainerColor = BorderLight
                                )
                            ) {
                                Text(
                                    text = if (summary.allMinOrdersMet) "Proceed to Pay  →" else "Min Orders Not Met",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = BackgroundLight,
        modifier = modifier
    ) { innerPadding ->
        if (summary.totalItemCount == 0) {
            // Empty Cart View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(PrimaryOrangeLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingBag,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Text(
                        text = "Your Cart is Empty",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "Explore top restaurants nearby and add delicious dishes to your order!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Button(
                        onClick = onExploreRestaurants,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        Text("Browse Restaurants", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Multi-Restaurant Notification Banner
                if (summary.groups.size > 1) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = OfferBlueBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = OfferBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Multi-Restaurant Combined Checkout",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = OfferBlue
                                    )
                                    Text(
                                        text = "You are ordering from ${summary.groups.size} independent restaurants. One single online payment will split into ${summary.groups.size} independent deliveries.",
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Sub-Order Cards per Restaurant
                items(summary.groups) { group ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Restaurant Name & Min Order Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = group.restaurantName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Min Order: ₹${group.minOrderValue.toInt()} • Delivery: ₹${group.deliveryFee.toInt()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }

                                if (group.isMinOrderMet) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SecondaryGreenLight)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("✓ Met", color = SecondaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Min Order Warning if not met
                            if (!group.isMinOrderMet) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(WarningAmberBg)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "⚠️ Add ₹${group.amountNeededForMinOrder.toInt()} more from ${group.restaurantName} to satisfy minimum order value.",
                                        color = WarningAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            HorizontalDivider(color = BorderLight)

                            // Items in this sub-order
                            group.items.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        VegNonVegBadge(isVeg = item.menuItem.isVeg, size = 13.dp)
                                        Column {
                                            Text(
                                                text = item.menuItem.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "₹${item.menuItem.price.toInt()}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    QuantitySelector(
                                        quantity = item.quantity,
                                        onAdd = { viewModel.addToCart(item.menuItem, group.restaurantName, group.minOrderValue) },
                                        onRemove = { viewModel.removeFromCart(item.menuItem.id) }
                                    )
                                }
                            }

                            HorizontalDivider(color = BorderLight)

                            // Sub-Order Subtotal & Discounts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal (${group.restaurantName})", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text("₹${group.subTotal.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            if (group.discount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Restaurant Coupon Discount", style = MaterialTheme.typography.bodySmall, color = SecondaryGreen)
                                    Text("-₹${group.discount.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SecondaryGreen)
                                }
                            }
                        }
                    }
                }

                // Promo / Coupon Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Outlined.LocalOffer, contentDescription = null, tint = PrimaryOrange)
                                Text(
                                    text = "Offers & Coupons",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = uiState.couponInputText,
                                    onValueChange = { viewModel.onCouponInputChanged(it) },
                                    placeholder = { Text("Enter Promo Code (e.g. ARAWELCOME)", fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { viewModel.applyEnteredCoupon() },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                                ) {
                                    Text("Apply")
                                }
                            }

                            // Feedback message
                            uiState.couponMessage?.let { msg ->
                                Text(
                                    text = msg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isCouponSuccess) SecondaryGreen else NonVegRed
                                )
                            }

                            // Applied Coupons Tags
                            if (summary.appliedCoupons.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    summary.appliedCoupons.forEach { coupon ->
                                        AssistChip(
                                            onClick = { viewModel.removeCoupon(coupon.code) },
                                            label = { Text("${coupon.code} ✕") },
                                            colors = AssistChipDefaults.assistChipColors(containerColor = SecondaryGreenLight)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Delivery Address Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryOrangeLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = PrimaryOrange)
                                }

                                Column {
                                    Text(
                                        text = "Deliver to: ${uiState.deliveryAddress?.title ?: "Select Address"}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = uiState.deliveryAddress?.fullAddress ?: "No address selected",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }

                            TextButton(onClick = onNavigateToAddresses) {
                                Text("CHANGE", color = PrimaryOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Detailed Bill Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Bill Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Item Total", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text("₹${summary.totalItemsPrice.toInt()}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery Fees (${summary.groups.size} Restaurants)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text("₹${summary.totalDeliveryFee.toInt()}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Taxes & Packaging Fees", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text("₹${summary.taxesAndPackaging.toInt()}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }

                            if (summary.totalDiscount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discounts & Coupons", style = MaterialTheme.typography.bodyMedium, color = SecondaryGreen)
                                    Text("-₹${summary.totalDiscount.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SecondaryGreen)
                                }
                            }

                            HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("To Pay", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("₹${summary.grandTotal.toInt()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = PrimaryOrange)
                            }
                        }
                    }
                }
            }
        }
    }
}
