package com.orderara.partner.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.orderara.partner.data.models.PartnerMenuItem
import com.orderara.partner.ui.components.MenuManagerItemCard
import com.orderara.partner.ui.theme.*

@Composable
fun MenuManagementScreen(
    viewModel: MenuViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var editingItem by remember { mutableStateOf<PartnerMenuItem?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

    if (isAddingNew || editingItem != null) {
        AddEditDishDialog(
            initialItem = editingItem,
            onDismiss = {
                isAddingNew = false
                editingItem = null
            },
            onSave = { name, category, price, desc, isVeg, imgUrl ->
                if (editingItem != null) {
                    viewModel.updateDish(
                        editingItem!!.copy(
                            name = name,
                            category = category,
                            price = price,
                            description = desc,
                            isVeg = isVeg,
                            imageUrl = if (imgUrl.isBlank()) editingItem!!.imageUrl else imgUrl
                        )
                    )
                } else {
                    viewModel.addDish(name, category, price, desc, isVeg, imgUrl)
                }
                isAddingNew = false
                editingItem = null
            }
        )
    }

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
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Menu & Stock Manager",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = TextLight
                            )
                            Text(
                                text = "Toggle item stock availability in real time",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextLightSecondary
                            )
                        }

                        Button(
                            onClick = { isAddingNew = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PartnerPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Dish", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Search Box
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search dishes by name...", color = TextLightSecondary) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextLightSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = PartnerPrimary,
                            unfocusedBorderColor = SurfaceDarkLight,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Filter Pills
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("All Dishes (${uiState.menuItems.size})", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceDark,
                                labelColor = TextLight,
                                selectedContainerColor = PartnerPrimary,
                                selectedLabelColor = SurfaceWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    items(uiState.categories) { cat ->
                        FilterChip(
                            selected = uiState.selectedCategory == cat,
                            onClick = { viewModel.selectCategory(if (uiState.selectedCategory == cat) null else cat) },
                            label = { Text(cat, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceDark,
                                labelColor = TextLight,
                                selectedContainerColor = PartnerPrimary,
                                selectedLabelColor = SurfaceWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Menu Items List
            items(uiState.menuItems, key = { it.id }) { item ->
                MenuManagerItemCard(
                    item = item,
                    onToggleStock = { viewModel.toggleStockAvailability(it) },
                    onEditClick = { editingItem = it },
                    onDeleteClick = { viewModel.deleteDish(it) }
                )
            }
        }
    }
}

@Composable
fun AddEditDishDialog(
    initialItem: PartnerMenuItem? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, price: Double, desc: String, isVeg: Boolean, imageUrl: String) -> Unit
) {
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: "Biryani Specials") }
    var priceStr by remember { mutableStateOf(initialItem?.price?.toInt()?.toString() ?: "") }
    var desc by remember { mutableStateOf(initialItem?.description ?: "") }
    var isVeg by remember { mutableStateOf(initialItem?.isVeg ?: true) }
    var imageUrl by remember { mutableStateOf(initialItem?.imageUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem != null) "Edit Dish" else "Add New Dish",
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dish Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dietary Type:", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = isVeg,
                            onClick = { isVeg = true },
                            label = { Text("Veg", fontWeight = FontWeight.Bold) }
                        )
                        FilterChip(
                            selected = !isVeg,
                            onClick = { isVeg = false },
                            label = { Text("Non-Veg", fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && price > 0) {
                        onSave(name, category, price, desc, isVeg, imageUrl)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PartnerPrimary)
            ) {
                Text("Save Dish", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
