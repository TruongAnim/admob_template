package com.truonganim.admob.ui.characterdetail

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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter

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
        factory = CharacterDetailViewModelFactory(characterId)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            CharacterDetailTopBar(
                characterName = uiState.character?.name ?: "",
                isFavorite = uiState.character?.isFavorite ?: false,
                onBackClick = onBackClick,
                onFavoriteClick = viewModel::onFavoriteClick
            )
        }
    ) { paddingValues ->
        CharacterDetailContent(
            photos = uiState.character?.photos ?: emptyList(),
            isLoading = uiState.isLoading,
            onPhotoClick = onPhotoClick,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharacterDetailTopBar(
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
    )
}

@Composable
private fun CharacterDetailContent(
    photos: List<String>,
    isLoading: Boolean,
    onPhotoClick: (Int) -> Unit,
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
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(photos) { index, photoUrl ->
                    PhotoGridItem(
                        photoUrl = photoUrl,
                        onClick = { onPhotoClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoGridItem(
    photoUrl: String,
    onClick: () -> Unit
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
        }
    }
}

