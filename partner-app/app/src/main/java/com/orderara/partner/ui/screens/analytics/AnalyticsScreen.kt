package com.orderara.partner.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.ShoppingBag
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
import com.orderara.partner.ui.components.MetricStatCard
import com.orderara.partner.ui.theme.*

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val analytics by viewModel.analytics.collectAsState()

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
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Business Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextLight
                    )
                    Text(
                        text = "Real-time daily earnings & order volume",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLightSecondary
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "TODAY'S REVENUE",
                        value = "₹${analytics.todayRevenue.toInt()}",
                        subtitle = "100% Payout (No cut)",
                        icon = Icons.Filled.CurrencyRupee,
                        accentColor = SuccessEmerald,
                        modifier = Modifier.weight(1f)
                    )

                    MetricStatCard(
                        title = "SUB-ORDERS",
                        value = "${analytics.todayOrdersCount}",
                        subtitle = "~${analytics.avgPrepTimeMinutes} mins avg prep",
                        icon = Icons.Filled.ShoppingBag,
                        accentColor = PartnerPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Weekly Revenue Bar Chart Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Weekly Earnings Trend (₹)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )

                        val maxEarnings = (analytics.weeklyRevenueTrend.maxOfOrNull { it.second } ?: 1.0).let { if (it <= 0.0) 1.0 else it }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            analytics.weeklyRevenueTrend.forEach { (day, amount) ->
                                val barHeightFraction = if (amount <= 0.0) 0.05f else ((amount / maxEarnings).toFloat()).coerceIn(0.05f, 1f)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .fillMaxHeight(barHeightFraction)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(if (day == "Sat" || day == "Sun") PartnerPrimary else SuccessEmerald)
                                    )
                                    Text(
                                        text = day,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextLightSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Top Selling Dishes Ranking
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Best Selling Dishes This Week",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )

                        analytics.topSellingItems.forEachIndexed { index, (dish, count) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SurfaceDarkLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            color = TextLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = dish,
                                        color = TextLight,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Text(
                                    text = "$count sold",
                                    color = SuccessEmerald,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
