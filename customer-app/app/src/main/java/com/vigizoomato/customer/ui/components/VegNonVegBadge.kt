package com.vigizoomato.customer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vigizoomato.customer.ui.theme.NonVegRed
import com.vigizoomato.customer.ui.theme.VegGreen

@Composable
fun VegNonVegBadge(
    isVeg: Boolean,
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isVeg) VegGreen else NonVegRed
    val shape = RoundedCornerShape(3.dp)

    Box(
        modifier = modifier
            .size(size)
            .border(width = 1.5.dp, color = borderColor, shape = shape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.5f)) {
            if (isVeg) {
                drawCircle(
                    color = VegGreen,
                    radius = size.toPx() * 0.25f,
                    center = Offset(size.toPx() * 0.25f, size.toPx() * 0.25f)
                )
            } else {
                val path = Path().apply {
                    moveTo(size.toPx() * 0.25f, 0f)
                    lineTo(size.toPx() * 0.5f, size.toPx() * 0.5f)
                    lineTo(0f, size.toPx() * 0.5f)
                    close()
                }
                drawPath(path = path, color = NonVegRed)
            }
        }
    }
}
