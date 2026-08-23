package com.orderara.partner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orderara.partner.data.models.StaffRole
import com.orderara.partner.ui.theme.*

enum class PartnerTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val requiredRole: StaffRole = StaffRole.KITCHEN_STAFF
) {
    ORDERS("orders", "Orders (KDS)", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong, StaffRole.KITCHEN_STAFF),
    MENU("menu", "Menu & Stock", Icons.Filled.RestaurantMenu, Icons.Outlined.RestaurantMenu, StaffRole.MANAGER),
    ANALYTICS("analytics", "Analytics", Icons.Filled.Insights, Icons.Outlined.Insights, StaffRole.OWNER),
    BILLING("billing", "Subscription", Icons.Filled.CardMembership, Icons.Outlined.CardMembership, StaffRole.OWNER),
    SETTINGS("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings, StaffRole.MANAGER)
}

@Composable
fun PartnerBottomBar(
    currentRoute: String,
    currentRole: StaffRole,
    onTabSelected: (PartnerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DarkNavy,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PartnerTab.values().forEach { tab ->
                val isAccessible = when (currentRole) {
                    StaffRole.OWNER -> true
                    StaffRole.MANAGER -> tab.requiredRole != StaffRole.OWNER
                    StaffRole.KITCHEN_STAFF -> tab.requiredRole == StaffRole.KITCHEN_STAFF
                }

                if (isAccessible) {
                    val isSelected = currentRoute == tab.route
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onTabSelected(tab) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.title,
                            tint = if (isSelected) PartnerPrimary else TextLightSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = tab.title,
                            color = if (isSelected) PartnerPrimary else TextLightSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
