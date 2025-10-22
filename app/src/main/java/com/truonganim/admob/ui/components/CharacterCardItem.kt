package com.truonganim.admob.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.truonganim.admob.R
import com.truonganim.admob.config.AppConfig
import com.truonganim.admob.data.AppCharacter

/**
 * Reusable Character Card Item
 * Used in Games tab, Favorites tab, and other places
 */
@Composable
fun CharacterCardItem(
    character: AppCharacter,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLockOverlay: Boolean = true,
    showName: Boolean = true
) {
    Card(
        modifier = modifier
            .aspectRatio(0.7f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Thumbnail Image
            Image(
                painter = rememberAsyncImagePainter(character.thumbnail),
                contentDescription = character.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay (only if showing name)
            if (showName) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 200f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            }

            // Lock overlay if character is locked
            if (showLockOverlay && !character.isUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Show different icon based on lock type
                        when {
                            character.isLockedByGame -> {
                                Icon(
                                    painter = painterResource(R.drawable.ic_game),
                                    contentDescription = "Locked by Game",
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            character.isLockedByAd -> {
                                Icon(
                                    painter = painterResource(R.drawable.ic_play_ads),
                                    contentDescription = "Locked by Ad",
                                    tint = Color.Red,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = character.progressText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            FavoriteOverlayButton(
                isFavorite = character.isFavorite,
                onClick = onFavoriteClick,
                modifier = Modifier.align(Alignment.TopStart)
            )

            // Character Name (Bottom) - only if showName is true
            if (showName && AppConfig.UI.SHOW_CHARACTER_NAME) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = Color.Transparent
                ) {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        ),
                        maxLines = 1,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

