package com.truonganim.admob.ui.characterdetail

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.truonganim.admob.ads.RewardAdHelper
import com.truonganim.admob.ads.RewardAdPlace
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.ui.components.GradientButton
import com.truonganim.admob.ui.components.GradientPresets

/**
 * Character Detail Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    characterId: Int,
    onBackClick: () -> Unit,
    onPhotoClick: (Int) -> Unit = {},
    viewModel: CharacterDetailViewModel = viewModel(
        factory = CharacterDetailViewModelFactory(characterId, LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            CharacterDetailTopBar(
                characterId = characterId,
                characterName = uiState.appCharacter?.name ?: "",
                isFavorite = uiState.appCharacter?.isFavorite ?: false,
                onBackClick = onBackClick,
                onFavoriteClick = viewModel::onCharacterFavoriteClick
            )
        }
    ) { paddingValues ->
        CharacterDetailContent(
            photos = uiState.appCharacter?.photos ?: emptyList(),
            favouritePhotoUrls = uiState.favouritePhotoUrls,
            isLoading = uiState.isLoading,
            isSettingWallpaper = uiState.isSettingWallpaper,
            wallpaperProgress = uiState.wallpaperProgress,
            onPhotoClick = onPhotoClick,
            onPhotoFavoriteClick = viewModel::onPhotoFavoriteClick,
            onSetRandomWallpaperClick = viewModel::onSetRandomWallpaperClick,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharacterDetailTopBar(
    characterId: Int,
    characterName: String,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = characterName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            // Only show favourite icon if not FAVOURITE_PHOTOS special character
            if (characterId != AppCharacter.FAVOURITE_PHOTOS_ID) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}

@Composable
private fun CharacterDetailContent(
    photos: List<String>,
    favouritePhotoUrls: Set<String>,
    isLoading: Boolean,
    isSettingWallpaper: Boolean,
    wallpaperProgress: Pair<Int, Int>?,
    onPhotoClick: (Int) -> Unit,
    onPhotoFavoriteClick: (String) -> Unit,
    onSetRandomWallpaperClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Random Wallpaper Button
                if (photos.isNotEmpty()) {
                    GradientButton(
                        text = if (isSettingWallpaper) {
                            wallpaperProgress?.let { (current, total) ->
                                "DOWNLOADING $current/$total"
                            } ?: "SETTING UP..."
                        } else {
                            "SET RANDOM WALLPAPER"
                        },
                        onClick = onSetRandomWallpaperClick,
                        icon = Icons.Default.Wallpaper,
                        isLoading = isSettingWallpaper,
                        enabled = !isSettingWallpaper,
                        gradientColors = GradientPresets.Aurora,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        cornerRadius = 28.dp
                    )
                }

                // Photos Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(photos) { index, photoUrl ->
                        PhotoGridItem(
                            photoUrl = photoUrl,
                            isFavorite = favouritePhotoUrls.contains(photoUrl),
                            onClick = { onPhotoClick(index) },
                            onFavoriteClick = { onPhotoFavoriteClick(photoUrl) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoGridItem(
    photoUrl: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.6f) // Portrait aspect ratio
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
                    imageVector = if (isFavorite) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

