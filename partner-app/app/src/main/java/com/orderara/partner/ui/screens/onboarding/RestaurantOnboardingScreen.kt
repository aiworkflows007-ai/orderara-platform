package com.orderara.partner.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.orderara.partner.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestaurantOnboardingScreen(
    onRegistrationComplete: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: RestaurantOnboardingViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val availableCuisines = listOf(
        "Biryani", "North Indian", "South Indian", "Pizza", "Burgers", 
        "Chinese", "Mughlai", "Kebabs", "Fast Food", "Desserts", "Bakery", "Cafe"
    )

    val popularUpiHandles = listOf(
        "@okhdfcbank", "@okaxis", "@okicici", "@oksbi", "@ybl", "@paytm", "@ibl"
    )

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PartnerPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Restaurant Partner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Register Your Restaurant",
                                style = MaterialTheme.typography.labelSmall,
                                color = PartnerPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    TextButton(onClick = onLoginClick) {
                        Text(
                            text = "Log In",
                            color = PartnerPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Step Indicators (1..4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("1. Details", "2. Location", "3. UPI Payout", "4. Launch").forEachIndexed { idx, label ->
                        val stepNum = idx + 1
                        val isActive = state.currentStep >= stepNum
                        val isCurrent = state.currentStep == stepNum
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isActive) PartnerPrimary else CardBorderDark
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$stepNum",
                                    color = if (isActive) Color.White else TextLightSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) Color.White else TextLightSecondary
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x33EF4444)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.errorMessage ?: "",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // STEP 1: Basic Restaurant Information
            if (state.currentStep == 1) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Restaurant Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = state.restaurantName,
                            onValueChange = { viewModel.updateRestaurantName(it) },
                            label = { Text("Restaurant Brand Name *") },
                            placeholder = { Text("e.g. Royal Biryani House") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PartnerPrimary,
                                unfocusedBorderColor = CardBorderDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.ownerName,
                            onValueChange = { viewModel.updateOwnerName(it) },
                            label = { Text("Owner / Manager Full Name *") },
                            placeholder = { Text("e.g. Farhan Khan") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PartnerPrimary,
                                unfocusedBorderColor = CardBorderDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = state.phone,
                                onValueChange = { viewModel.updatePhone(it) },
                                label = { Text("Phone Number *") },
                                placeholder = { Text("+91 98450 11223") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = PartnerPrimary,
                                    unfocusedBorderColor = CardBorderDark
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = state.email,
                                onValueChange = { viewModel.updateEmail(it) },
                                label = { Text("Business Email") },
                                placeholder = { Text("owner@restaurant.com") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = PartnerPrimary,
                                    unfocusedBorderColor = CardBorderDark
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text(
                            text = "Select Cuisines Offered",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLightSecondary
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            availableCuisines.forEach { cuisine ->
                                val isSelected = state.selectedCuisines.contains(cuisine)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PartnerPrimary else CardBorderDark)
                                        .clickable { viewModel.toggleCuisine(cuisine) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cuisine,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextLight
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Pure Vegetarian Restaurant",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Mark if your kitchen serves veg items exclusively",
                                    fontSize = 11.sp,
                                    color = TextLightSecondary
                                )
                            }
                            Switch(
                                checked = state.isVegOnly,
                                onCheckedChange = { viewModel.updateVegOnly(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = SuccessEmerald, checkedTrackColor = Color(0x3310B981))
                            )
                        }
                    }
                }
            }

            // STEP 2: Location & Delivery Radius
            if (state.currentStep == 2) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Store Location & Service Radius",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = state.address,
                            onValueChange = { viewModel.updateAddress(it) },
                            label = { Text("Kitchen Street Address / Landmark *") },
                            placeholder = { Text("e.g. 100ft Road, HAL 2nd Stage, Indiranagar") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PartnerPrimary,
                                unfocusedBorderColor = CardBorderDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.city,
                            onValueChange = { viewModel.updateCity(it) },
                            label = { Text("City *") },
                            placeholder = { Text("Bangalore") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PartnerPrimary,
                                unfocusedBorderColor = CardBorderDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Delivery Radius Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Delivery Radius",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${state.deliveryRadiusKm.toInt()} km",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PartnerPrimary
                                )
                            }
                            Text(
                                text = "Only customers within this radius can order from your kitchen",
                                fontSize = 11.sp,
                                color = TextLightSecondary
                            )
                            Slider(
                                value = state.deliveryRadiusKm.toFloat(),
                                onValueChange = { viewModel.updateRadius(it.toDouble()) },
                                valueRange = 1f..15f,
                                steps = 13,
                                colors = SliderDefaults.colors(
                                    thumbColor = PartnerPrimary,
                                    activeTrackColor = PartnerPrimary,
                                    inactiveTrackColor = CardBorderDark
                                )
                            )
                        }

                        // Minimum Order Value Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Minimum Order Threshold",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "₹${state.minOrderValue.toInt()}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PartnerPrimary
                                )
                            }
                            Text(
                                text = "Customer sub-orders must meet this amount to checkout",
                                fontSize = 11.sp,
                                color = TextLightSecondary
                            )
                            Slider(
                                value = state.minOrderValue.toFloat(),
                                onValueChange = { viewModel.updateMinOrder(it.toDouble()) },
                                valueRange = 99f..499f,
                                steps = 7,
                                colors = SliderDefaults.colors(
                                    thumbColor = PartnerPrimary,
                                    activeTrackColor = PartnerPrimary,
                                    inactiveTrackColor = CardBorderDark
                                )
                            )
                        }
                    }
                }
            }

            // STEP 3: Pure UPI Payout Details
            if (state.currentStep == 3) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = SuccessEmerald,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Instant Daily UPI Payouts",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "All customer payments settle directly to your UPI ID",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextLightSecondary
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x2210B981)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "⚡ 100% Direct UPI Settlement • 0% Commission",
                                    color = SuccessEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedTextField(
                            value = state.upiId,
                            onValueChange = { viewModel.updateUpiId(it) },
                            label = { Text("Enter UPI ID (VPA) *") },
                            placeholder = { Text("e.g. royalbiryani@okhdfcbank") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PartnerPrimary,
                                unfocusedBorderColor = CardBorderDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick UPI Handle Suffix Chips
                        Text(
                            text = "Quick Handles:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLightSecondary
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            popularUpiHandles.forEach { handle ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CardBorderDark)
                                        .clickable { viewModel.appendUpiHandle(handle) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = handle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextLight
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = state.upiHolderName,
                            onValueChange = { viewModel.updateUpiHolderName(it) },
                            label = { Text("Account Holder / UPI Name") },
                            placeholder = { Text("e.g. Royal Biryani Hospitality LLP") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PartnerPrimary,
                                unfocusedBorderColor = CardBorderDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkNavy),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "ℹ️ Payouts are transferred automatically to this UPI ID every night without any transaction fees or commission cuts.",
                                color = TextLightSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // STEP 4: Review & Free Trial Activation
            if (state.currentStep == 4) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Ready to Launch!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Restaurant Unlimited Partner Subscription",
                                    fontSize = 11.sp,
                                    color = TextLightSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x33F59E0B))
                                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "14-DAY FREE TRIAL",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        HorizontalDivider(color = CardBorderDark)

                        // Registration Summary Pills
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Restaurant:", color = TextLightSecondary, fontSize = 12.sp)
                                Text(state.restaurantName.ifEmpty { "My Restaurant" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Owner / Contact:", color = TextLightSecondary, fontSize = 12.sp)
                                Text("${state.ownerName} (${state.phone})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Daily Payout UPI:", color = TextLightSecondary, fontSize = 12.sp)
                                Text(state.upiId.ifEmpty { "Registered UPI" }, color = SuccessEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Service Radius:", color = TextLightSecondary, fontSize = 12.sp)
                                Text("${state.deliveryRadiusKm.toInt()} km", color = PartnerPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Monthly Price:", color = TextLightSecondary, fontSize = 12.sp)
                                Text("Flat ₹999/mo (Free for first 14 days)", color = SuccessEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkNavy),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    "✓ 0% Commission on unlimited customer orders",
                                    "✓ Direct daily instant UPI payouts to ${state.upiId.ifEmpty { "your UPI" }}",
                                    "✓ Live Kitchen Display System (KDS) & KOT tickets",
                                    "✓ Instant out-of-stock menu toggles",
                                    "✓ Direct restaurant-to-customer chat & dispatch"
                                ).forEach { benefit ->
                                    Text(
                                        text = benefit,
                                        color = TextLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Action Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.currentStep > 1) {
                    OutlinedButton(
                        onClick = { viewModel.previousStep() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (state.currentStep < 4) {
                            viewModel.nextStep()
                        } else {
                            viewModel.submitRegistration(onRegistrationComplete)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PartnerPrimary),
                    modifier = Modifier.weight(2f)
                ) {
                    Text(
                        text = if (state.currentStep < 4) "Continue →" else "🚀 Activate Trial & Launch Store",
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}
