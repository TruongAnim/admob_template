package com.truonganim.admob.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truonganim.admob.R

@Composable
fun AdBadge(
    progressText: String,
    modifier: Modifier = Modifier,
    pillColor: Color = Color(0xFFFF6B6B),
    adTextColor: Color = Color.White
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = pillColor,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_ads),
                contentDescription = null,
                tint = adTextColor,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = progressText,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



@Preview
@Composable
fun AdBadgePreview() {
    AdBadge(progressText = "3/10")
}