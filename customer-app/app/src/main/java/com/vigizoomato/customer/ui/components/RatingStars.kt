package com.vigizoomato.customer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vigizoomato.customer.ui.theme.GoldStar
import com.vigizoomato.customer.ui.theme.SecondaryGreen
import com.vigizoomato.customer.ui.theme.SurfaceWhite

@Composable
fun RatingBadge(
    rating: Double,
    ratingCount: Int? = null,
    modifier: Modifier = Modifier
) {
    val bgColor = if (rating >= 4.0) SecondaryGreen else if (rating >= 3.0) GoldStar else Color(0xFFE53935)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = String.format("%.1f", rating),
            color = SurfaceWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Rating Star",
            tint = SurfaceWhite,
            modifier = Modifier.size(12.dp)
        )
        if (ratingCount != null) {
            Text(
                text = "($ratingCount)",
                color = SurfaceWhite.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun StarRatingInput(
    rating: Double,
    onRatingChanged: (Double) -> Unit,
    maxStars: Int = 5,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isFilled = i <= rating
            Icon(
                imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "$i Stars",
                tint = if (isFilled) GoldStar else Color.LightGray,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onRatingChanged(i.toDouble()) }
            )
        }
    }
}
