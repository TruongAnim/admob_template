package com.truonganim.admob.ui.games

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.truonganim.admob.R
import com.truonganim.admob.config.AppConfig
import com.truonganim.admob.data.AppCharacter
import com.truonganim.admob.data.Game
import com.truonganim.admob.ui.components.CharacterCardItem
import com.truonganim.admob.ui.utils.rememberGameLauncher

/**
 * Games Screen
 */
@Composable
fun GamesScreen(
    onGameClick: (Game) -> Unit = {},
    onCharacterClick: (Int) -> Unit = {},
    onViewAllCharactersClick: () -> Unit = {},
    viewModel: GamesViewModel = viewModel(
        factory = GamesViewModelFactory(LocalContext.current)
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

    GamesContent(
        games = uiState.games,
        gameAlbumCharacters = uiState.gameAlbumCharacters,
        isLoading = uiState.isLoading,
        onGameClick = { game ->
            // Launch game directly when clicking game card
            gameLauncher.launch(game)
        },
        onCharacterClick = { character ->
            val result = viewModel.onCharacterClick(character)
            when (result) {
                null -> {
                    // Character is unlocked, navigate to detail
                    onCharacterClick(character.id)
                }
                is Game -> {
                    // Character locked by game, launch game
                    gameLauncher.launch(result)
                }
                "ad" -> {
                    // Character locked by ad, show reward ad
                    viewModel.showRewardAdToUnlock(character)
                }
            }
        },
        onCharacterFavoriteClick = viewModel::onCharacterFavoriteClick,
        onViewAllCharactersClick = onViewAllCharactersClick
    )
}

@Composable
private fun GamesContent(
    games: List<Game>,
    gameAlbumCharacters: List<AppCharacter>,
    isLoading: Boolean,
    onGameClick: (Game) -> Unit,
    onCharacterClick: (AppCharacter) -> Unit,
    onCharacterFavoriteClick: (AppCharacter) -> Unit,
    onViewAllCharactersClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Game Album Characters Section (if available)
                if (gameAlbumCharacters.isNotEmpty()) {
                    item {
                        GameAlbumCharactersSection(
                            characters = gameAlbumCharacters,
                            onCharacterClick = onCharacterClick,
                            onCharacterFavoriteClick = onCharacterFavoriteClick,
                            onViewAllClick = onViewAllCharactersClick
                        )
                    }
                }

                // Games list
                items(games) { game ->
                    GameCard(
                        game = game,
                        onClick = { onGameClick(game) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameCard(
    game: Game,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Thumbnail Image
            Image(
                painter = rememberAsyncImagePainter(game.thumbnailUrl),
                contentDescription = game.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Overlay gradient
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.4f)
            ) {}
            
            // Game info
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Lock icon if not unlocked
                if (!game.isUnlocked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${game.adsRequired} ads",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Game name and description
                Column {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = game.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

/**
 * Game Album Characters Section
 */
@Composable
private fun GameAlbumCharactersSection(
    characters: List<AppCharacter>,
    onCharacterClick: (AppCharacter) -> Unit,
    onCharacterFavoriteClick: (AppCharacter) -> Unit,
    onViewAllClick: () -> Unit
) {
    Column {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Game Album",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Text(
                text = "View All (${characters.size})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onViewAllClick)
            )
        }

        // Horizontal scrolling list
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(characters) { character ->
                CharacterCardItem(
                    character = character,
                    onClick = { onCharacterClick(character) },
                    onFavoriteClick = { onCharacterFavoriteClick(character) },
                    modifier = Modifier.width(120.dp),
                    showLockOverlay = true,
                    showName = true
                )
            }
        }
    }
}

