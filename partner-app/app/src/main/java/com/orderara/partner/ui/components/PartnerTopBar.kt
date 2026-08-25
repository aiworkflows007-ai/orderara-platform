package com.orderara.partner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.orderara.partner.data.models.RestaurantProfile
import com.orderara.partner.data.models.StaffRole
import com.orderara.partner.ui.theme.*

@Composable
fun PartnerTopBar(
    profile: RestaurantProfile,
    currentRole: StaffRole,
    onToggleStoreOpen: () -> Unit,
    onSwitchRoleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DarkNavy,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Restaurant Name & Rating
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextLight
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SuccessEmerald.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = SuccessEmerald,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text("${profile.rating}", color = SuccessEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        text = profile.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLightSecondary,
                        maxLines = 1
                    )
                }

                // Store Open / Closed Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (profile.isOpen) SuccessEmerald.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (profile.isOpen) "ONLINE" else "OFFLINE",
                            color = if (profile.isOpen) SuccessEmerald else AlertRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Switch(
                        checked = profile.isOpen,
                        onCheckedChange = { onToggleStoreOpen() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceWhite,
                            checkedTrackColor = SuccessEmerald,
                            uncheckedThumbColor = SurfaceWhite,
                            uncheckedTrackColor = SurfaceDarkLight
                        )
                    )
                }
            }

            // Role Switcher Pill Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark)
                    .clickable { onSwitchRoleClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(currentRole.badgeColorHex))
                    )
                    Text(
                        text = "Active Role: ${currentRole.title}",
                        color = TextLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Switch Role",
                        color = PartnerPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = "Switch",
                        tint = PartnerPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
