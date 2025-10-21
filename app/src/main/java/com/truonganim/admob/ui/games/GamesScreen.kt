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

    GamesContent(
        games = uiState.games,
        gameAlbumCharacters = uiState.gameAlbumCharacters,
        isLoading = uiState.isLoading,
        onGameClick = onGameClick,
        onCharacterClick = onCharacterClick,
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
    onCharacterClick: (Int) -> Unit,
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
    onCharacterClick: (Int) -> Unit,
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
                GameAlbumCharacterItem(
                    character = character,
                    onClick = { onCharacterClick(character.id) },
                    onFavoriteClick = { onCharacterFavoriteClick(character) }
                )
            }
        }
    }
}

/**
 * Game Album Character Item
 */
@Composable
private fun GameAlbumCharacterItem(
    character: AppCharacter,
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
                painter = rememberAsyncImagePainter(character.thumbnail),
                contentDescription = character.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay
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

            // Lock overlay if character is locked
            if (!character.isUnlocked) {
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

            // Favorite Icon (Top Left)
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, start = 8.dp)
                    .size(28.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .clickable { onFavoriteClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (character.isFavorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = "Favorite",
                    tint = if (character.isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Character Name (Bottom)
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

