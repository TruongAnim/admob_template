package com.truonganim.admob.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteOverlayButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,                 // ví dụ: .align(Alignment.TopStart)
    size: Dp = 28.dp,                              // kích thước vòng tròn
    iconSize: Dp = 16.dp,                          // kích thước icon
    containerColor: Color = Color.Black.copy(alpha = 0.5f),
    favoriteTint: Color = Color.Red,
    defaultTint: Color = Color.White,
    shape: RoundedCornerShape = CircleShape,
    enabled: Boolean = true,
    outerPadding: PaddingValues = PaddingValues(8.dp) // padding so với cạnh card
) {
    Surface(
        modifier = modifier
            .padding(outerPadding)
            .size(size),
        shape = shape,
        color = containerColor,
        tonalElevation = 0.dp // tăng nếu muốn đổ bóng nhẹ
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) favoriteTint else defaultTint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}