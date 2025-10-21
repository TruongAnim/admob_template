package com.truonganim.admob.ui.favorites

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.truonganim.admob.R
import com.truonganim.admob.data.AppCharacter

/**
 * Favorites Screen
 */
@Composable
fun FavoritesScreen(
    onCharacterClick: (Int) -> Unit = {},
    onPhotoClick: (String, List<String>) -> Unit = { _, _ -> },
    onViewAllCharactersClick: () -> Unit = {},
    onViewAllPhotosClick: () -> Unit = {},
    viewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    FavoritesContent(
        favoriteAppCharacters = uiState.favoriteAppCharacters,
        favoritePhotos = uiState.favoritePhotos,
        isLoading = uiState.isLoading,
        onExploreClick = {},
        onCharacterClick = { character ->
            onCharacterClick(character.id)
        },
        onPhotoClick = onPhotoClick,
        onCharacterFavoriteClick = viewModel::onCharacterFavoriteClick,
        onPhotoFavoriteClick = viewModel::onPhotoFavoriteClick,
        onViewAllCharactersClick = onViewAllCharactersClick,
        onViewAllPhotosClick = onViewAllPhotosClick
    )
}

@Composable
private fun FavoritesContent(
    favoriteAppCharacters: List<AppCharacter>,
    favoritePhotos: List<String>,
    isLoading: Boolean,
    onExploreClick: () -> Unit,
    onCharacterClick: (AppCharacter) -> Unit,
    onPhotoClick: (String, List<String>) -> Unit,
    onCharacterFavoriteClick: (AppCharacter) -> Unit,
    onPhotoFavoriteClick: (String) -> Unit,
    onViewAllCharactersClick: () -> Unit,
    onViewAllPhotosClick: () -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (favoriteAppCharacters.isEmpty() && favoritePhotos.isEmpty()) {
        // Empty state
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_no_favorites),
                    contentDescription = "No Favorites",
                )

                Text(
                    text = "No Favorites Yet",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    fontSize = 16.sp
                )

                Text(
                    text = "Your favorite wallpapers will appear here. Start exploring and add some to your collection!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 72.dp),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )

                Button(
                    onClick = onExploreClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(
                        horizontal = 14.dp
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "Explore now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

            }
        }
    } else {
        // Content with favorites
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Favourites",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Favourite Characters Section
            if (favoriteAppCharacters.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    FavouriteCharactersSection(
                        appCharacters = favoriteAppCharacters,
                        onCharacterClick = onCharacterClick,
                        onFavoriteClick = onCharacterFavoriteClick,
                        onViewAllClick = onViewAllCharactersClick
                    )
                }
            }

            // Favourite Photos Section
            if (favoritePhotos.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    FavouritePhotosSectionHeader(
                        photoCount = favoritePhotos.size,
                        onViewAllClick = onViewAllPhotosClick
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Photos grid items
                items(favoritePhotos.chunked(3)) { rowPhotos ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPhotos.forEach { photoUrl ->
                            Box(modifier = Modifier.weight(1f)) {
                                FavouritePhotoItem(
                                    photoUrl = photoUrl,
                                    onClick = { onPhotoClick(photoUrl, favoritePhotos) },
                                    onFavoriteClick = { onPhotoFavoriteClick(photoUrl) }
                                )
                            }
                        }
                        // Fill empty spaces in the last row
                        repeat(3 - rowPhotos.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FavouriteCharactersSection(
    appCharacters: List<AppCharacter>,
    onCharacterClick: (AppCharacter) -> Unit,
    onFavoriteClick: (AppCharacter) -> Unit,
    onViewAllClick: () -> Unit
) {
    Column {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Favourite Characters",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Text(
                text = "View All (${appCharacters.size})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onViewAllClick)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal scrolling list
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(appCharacters) { character ->
                FavouriteCharacterItem(
                    appCharacter = character,
                    onClick = { onCharacterClick(character) },
                    onFavoriteClick = { onFavoriteClick(character) }
                )
            }
        }
    }
}

@Composable
private fun FavouriteCharacterItem(
    appCharacter: AppCharacter,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
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
                painter = rememberAsyncImagePainter(appCharacter.thumbnail),
                contentDescription = appCharacter.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Favorite Icon (Top Right)
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Character Name (Bottom)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = appCharacter.name,
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

@Composable
private fun FavouritePhotosSectionHeader(
    photoCount: Int,
    onViewAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Favourite Photos",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
        )

        Text(
            text = "View All ($photoCount)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onViewAllClick)
        )
    }
}

@Composable
private fun FavouritePhotoItem(
    photoUrl: String,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.6f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Photo Image
            Image(
                painter = rememberAsyncImagePainter(photoUrl),
                contentDescription = "Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Favorite Icon (Top Right)
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesContentEmptyPreview() {
    FavoritesContent(
        favoriteAppCharacters = emptyList(),
        favoritePhotos = emptyList(),
        isLoading = false,
        onCharacterClick = {},
        onPhotoClick = { _, _ -> },
        onCharacterFavoriteClick = {},
        onPhotoFavoriteClick = {},
        onViewAllCharactersClick = {},
        onViewAllPhotosClick = {},
        onExploreClick = {}
    )
}
