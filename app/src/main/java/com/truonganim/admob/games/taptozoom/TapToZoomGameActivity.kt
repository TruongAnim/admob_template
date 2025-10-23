package com.truonganim.admob.games.taptozoom

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.truonganim.admob.R
import com.truonganim.admob.data.Game
import com.truonganim.admob.data.GameResult
import com.truonganim.admob.ui.base.BaseActivity
import com.truonganim.admob.ui.theme.AdMobBaseTheme

/**
 * Tap to Zoom Game Activity
 *
 * Game rules:
 * - User sees an image
 * - Tap the image to zoom in
 * - Successfully zooming in = WIN
 */
class TapToZoomGameActivity : BaseActivity() {

    private var gameId: String = ""
    private var inputImages: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get game data from intent
        gameId = intent.getStringExtra(Game.EXTRA_GAME_ID) ?: ""
        inputImages = intent.getStringArrayListExtra(Game.EXTRA_INPUT_IMAGES) ?: emptyList()

        setContent {
            AdMobBaseTheme {
                TapToZoomGameScreen(
                    imageUrl = inputImages.firstOrNull() ?: "",
                    onWin = { finishWithResult(GameResult.WIN) },
                    onClose = { finishWithResult(GameResult.CANCELLED) }
                )
            }
        }
    }

    private fun finishWithResult(result: GameResult) {
        val resultIntent = Intent().apply {
            putExtra(Game.EXTRA_GAME_RESULT, result)
            putExtra(Game.EXTRA_GAME_ID, gameId)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TapToZoomGameScreen(
    imageUrl: String,
    onWin: () -> Unit,
    onClose: () -> Unit
) {
    var isZoomed by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(false) }

    // Animate scale
    val scale by animateFloatAsState(
        targetValue = if (isZoomed) 2f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "scale"
    )

    // Show win dialog after zoom animation
    LaunchedEffect(isZoomed) {
        if (isZoomed) {
            kotlinx.coroutines.delay(600) // Wait for animation to complete
            showWinDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tap_to_zoom),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Instructions
                if (!isZoomed) {
                    Text(
                        text = stringResource(R.string.tap_instruction),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }

                // Image
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .scale(scale)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isZoomed) {
                                isZoomed = true
                            }
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Game Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                if (!isZoomed) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(R.string.tap_here),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Win Dialog
    if (showWinDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = stringResource(R.string.you_win),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.congratulations),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = onWin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue")
                }
            }
        )
    }
}

