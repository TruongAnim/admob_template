package com.truonganim.admob.ui.albumdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.truonganim.admob.data.AppCharacter

/**
 * Album Detail Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: String,
    onBackClick: () -> Unit,
    onCharacterClick: (Int) -> Unit = {},
    viewModel: AlbumDetailViewModel = viewModel(
        factory = AlbumDetailViewModelFactory(albumId, LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            AlbumDetailTopBar(
                albumName = uiState.albumName,
                onBackClick = onBackClick,
                onUnlockAllClick = viewModel::onUnlockAllClick,
                albumId = uiState.albumId
            )
        }
    ) { paddingValues ->
        AlbumDetailContent(
            appCharacters = uiState.appCharacters,
            isLoading = uiState.isLoading,
            onCharacterClick = { character ->
                onCharacterClick(character.id)
            },
            onFavoriteClick = viewModel::onFavoriteClick,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumDetailTopBar(
    albumName: String,
    onBackClick: () -> Unit,
    onUnlockAllClick: () -> Unit,
    albumId: String = ""
) {
    TopAppBar(
        title = {
            Text(
                text = albumName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
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
            // Only show "UNLOCK ALL" button if not FAVOURITE category
            if (albumId != "favourite") {
                Button(
                    onClick = onUnlockAllClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800) // Orange
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "UNLOCK ALL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    )
}

@Composable
private fun AlbumDetailContent(
    appCharacters: List<AppCharacter>,
    isLoading: Boolean,
    onCharacterClick: (AppCharacter) -> Unit,
    onFavoriteClick: (AppCharacter) -> Unit,
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(appCharacters) { character ->
                    CharacterGridItem(
                        appCharacter = character,
                        onClick = { onCharacterClick(character) },
                        onFavoriteClick = { onFavoriteClick(character) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterGridItem(
    appCharacter: AppCharacter,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
            
            // Lock Icon (Top Left) - if not unlocked
            if (!appCharacter.isUnlocked) {
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                        .align(Alignment.TopStart),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
            
            // Favorite Icon (Top Right)
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = if (appCharacter.isFavorite) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },
                    contentDescription = "Favorite",
                    tint = if (appCharacter.isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Bottom Section
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = 0.5f)
                    )
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play Icon
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFF9800) // Orange
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "▶",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Progress Text
                    Text(
                        text = appCharacter.progressText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

