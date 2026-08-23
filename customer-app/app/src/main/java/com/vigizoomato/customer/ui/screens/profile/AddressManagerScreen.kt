package com.vigizoomato.customer.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vigizoomato.customer.VigiZoomatoApp
import com.vigizoomato.customer.ui.components.AppHeader
import com.vigizoomato.customer.ui.theme.*

@Composable
fun AddressManagerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authRepo = VigiZoomatoApp.container.authRepository
    val user by authRepo.currentUser.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("Home") }
    var newStreet by remember { mutableStateOf("") }
    var newLandmark by remember { mutableStateOf("") }
    var newCity by remember { mutableStateOf("Bangalore") }
    var newPincode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Delivery Addresses",
                subtitle = "Choose or add new address",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryOrange,
                contentColor = SurfaceWhite
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Address")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(user?.savedAddresses ?: emptyList()) { address ->
                val isSelected = address.id == user?.selectedAddressId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { authRepo.selectAddress(address.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PrimaryOrange) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) PrimaryOrangeLight else BackgroundLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (address.title.lowercase()) {
                                        "home" -> Icons.Outlined.Home
                                        "work" -> Icons.Outlined.Work
                                        else -> Icons.Outlined.LocationOn
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) PrimaryOrange else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = address.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(PrimaryOrangeLight)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("SELECTED", color = PrimaryOrangeDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Text(
                                    text = address.fullAddress,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { authRepo.selectAddress(address.id) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryOrange)
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Delivery Address", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Label (Home, Work, Other)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newStreet,
                        onValueChange = { newStreet = it },
                        label = { Text("House / Flat / Street Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newLandmark,
                        onValueChange = { newLandmark = it },
                        label = { Text("Landmark (Optional)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPincode,
                        onValueChange = { newPincode = it },
                        label = { Text("Pincode") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newStreet.isNotBlank() && newPincode.isNotBlank()) {
                            authRepo.addAddress(newTitle, newStreet, newLandmark, newCity, newPincode)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Text("Save Address")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
