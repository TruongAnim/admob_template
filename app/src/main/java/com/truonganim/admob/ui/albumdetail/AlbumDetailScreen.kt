package com.truonganim.admob.ui.albumdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.truonganim.admob.R
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.ui.theme.LocalAppColors
import com.truonganim.admob.ui.utils.rememberGameLauncher

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

    // Game launcher with result handling
    val gameLauncher = rememberGameLauncher(
        onGameWin = { gameId ->
            // Unlock the pending character (saved when launching game)
            viewModel.unlockCharacterByGameWin()
        },
        onGameLose = { gameId ->
            // Clear pending unlock
            viewModel.clearPendingGameUnlock()
        },
        onGameCancelled = { gameId ->
            // Clear pending unlock
            viewModel.clearPendingGameUnlock()
        }
    )

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
                val result = viewModel.onCharacterClick(character)
                when (result) {
                    null -> {
                        // Character is unlocked, navigate to detail
                        onCharacterClick(character.id)
                    }
                    is com.truonganim.admob.data.Game -> {
                        // Character locked by game, launch game
                        gameLauncher.launch(result)
                    }
                    "ad" -> {
                        // Character locked by ad, show reward ad
                        viewModel.showRewardAdToUnlock(character)
                    }
                }
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
    val appColors = LocalAppColors.current

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = albumName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
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
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(
                        horizontal = 14.dp
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "UNLOCK ALL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = appColors.imageScreenBackground
        )
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
    val appColors = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(appColors.imageScreenBackground)
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
            .aspectRatio(0.5f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 500f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Lock Icon - if not unlocked
            if (!appCharacter.isUnlocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Show different icon based on lock type
                        when {
                            appCharacter.isLockedByGame -> {
                                Icon(
                                    painter = painterResource(R.drawable.ic_game),
                                    contentDescription = "Locked by Game",
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            appCharacter.isLockedByAd -> {
                                Icon(
                                    painter = painterResource(R.drawable.ic_play_ads),
                                    contentDescription = "Locked by Ad",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )

                                Text(
                                    text = appCharacter.progressText,
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

            Box(
                modifier = Modifier
                    .padding(top = 8.dp, start = 4.dp)
                    .size(28.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .clickable { onFavoriteClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (appCharacter.isFavorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = "Favorite",
                    tint = if (appCharacter.isFavorite) Color.Red else Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_album),
                    contentDescription = null,
                )
            }
        }
    }
}

@Preview
@Composable
fun AlbumDetailContentPreview() {
    val sampleCharacters = listOf(
        AppCharacter(
            id = 1,
            name = "Character 1",
            album = "Album 1",
            order = 1,
            adCount = 0,
            thumbnail = "",
            photos = listOf(""),
            isFavorite = true,
            isUnlocked = true,
            currentPhotoIndex = 1
        ),
        AppCharacter(
            id = 2,
            name = "Character 2",
            album = "Album 1",
            order = 2,
            adCount = 0,
            thumbnail = "",
            photos = listOf("", ""),
            isFavorite = false,
            isUnlocked = false,
            currentPhotoIndex = 0
        )
    )

    AlbumDetailContent(
        appCharacters = sampleCharacters,
        isLoading = false,
        onCharacterClick = {},
        onFavoriteClick = {}
    )
}