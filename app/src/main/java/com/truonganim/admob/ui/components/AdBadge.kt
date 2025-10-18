package com.truonganim.admob.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdBadge(
    progressText: String,
    modifier: Modifier = Modifier,
    pillColor: Color = Color(0xFFFF6B6B),
    adBg: Color = Color.White,
    adTextColor: Color = pillColor
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Ô “AD” vuông bo góc
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = adBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Nếu có icon riêng thì dùng painterResource; không có thì để chữ “AD”
                    // Icon(painterResource(R.drawable.ic_ad), contentDescription = null, tint = adTextColor, modifier = Modifier.size(14.dp))
                    Text(
                        text = "AD",
                        color = adTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = progressText,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
