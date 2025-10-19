package com.truonganim.admob.ui.albums

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.truonganim.admob.ads.AdGateHelper
import com.truonganim.admob.data.Album

/**
 * Albums Screen
 */
@Composable
fun AlbumsScreen(
    onAlbumClick: (String) -> Unit = {}, // Now takes albumId instead of AlbumCategory
    viewModel: AlbumsViewModel = viewModel(
        factory = AlbumsViewModelFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Track which album is pending (waiting for ad result)
    var pendingAlbumId by remember { mutableStateOf<String?>(null) }

    // Ad gate launcher
    val adGateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        AdGateHelper.handleAdGateResult(
            resultCode = result.resultCode,
            data = result.data,
            onAdShown = {
                // Ad shown successfully, navigate to album
                pendingAlbumId?.let { onAlbumClick(it) }
                pendingAlbumId = null
            },
            onAdFailed = {
                // Ad failed to load (required ad)
                Toast.makeText(
                    context,
                    "Failed to load ad. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
                pendingAlbumId = null
            },
            onAdSkipped = {
                // Ad skipped (interval not reached or optional ad failed)
                pendingAlbumId?.let { onAlbumClick(it) }
                pendingAlbumId = null
            }
        )
    }

    AlbumsContent(
        albums = uiState.albums,
        isLoading = uiState.isLoading,
        onAlbumClick = { album ->
            // Store pending album
            pendingAlbumId = album.albumId

            // Show ad gate (optional - respects interval)
            AdGateHelper.showOptionalAdGate(adGateLauncher, context as androidx.activity.ComponentActivity)
        }
    )
}

@Composable
private fun AlbumsContent(
    albums: List<Album>,
    isLoading: Boolean,
    onAlbumClick: (Album) -> Unit
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(albums) { album ->
                    AlbumCard(
                        album = album,
                        onClick = { onAlbumClick(album) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumCard(
    album: Album,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Image
            Image(
                painter = rememberAsyncImagePainter(album.thumbnail),
                contentDescription = album.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dark overlay for better text visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Name Label (Top Left)
                Surface(
                    modifier = Modifier.align(Alignment.TopStart),
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF2196F3) // Blue
                ) {
                    Text(
                        text = album.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    )
                }

                // Ad Count Badge (Top Right) - Only show if not unlocked
                if (!album.isUnlocked && album.requiredAdCount > 0) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE91E63) // Pink
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Ad",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "0/${album.remainingAds}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

