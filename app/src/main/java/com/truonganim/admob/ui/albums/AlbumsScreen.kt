package com.truonganim.admob.ui.albums

import android.R
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.truonganim.admob.ads.AdGateHelper
import com.truonganim.admob.data.Album
import com.truonganim.admob.data.AlbumCategory
import com.truonganim.admob.ui.components.AdBadge

/**
 * Albums Screen
 */
@Composable
fun AlbumsScreen(
    onAlbumClick: (AlbumCategory) -> Unit = {},
    viewModel: AlbumsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Track which album is pending (waiting for ad result)
    var pendingAlbum by remember { mutableStateOf<AlbumCategory?>(null) }

    // Ad gate launcher
    val adGateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        AdGateHelper.handleAdGateResult(
            resultCode = result.resultCode,
            data = result.data,
            onAdShown = {
                // Ad shown successfully, navigate to album
                pendingAlbum?.let { onAlbumClick(it) }
                pendingAlbum = null
            },
            onAdFailed = {
                // Ad failed to load (required ad)
                Toast.makeText(
                    context,
                    "Failed to load ad. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
                pendingAlbum = null
            },
            onAdSkipped = {
                // Ad skipped (interval not reached or optional ad failed)
                pendingAlbum?.let { onAlbumClick(it) }
                pendingAlbum = null
            }
        )
    }

    AlbumsContent(
        albums = uiState.albums,
        isLoading = uiState.isLoading,
        onAlbumClick = { album ->
            // Store pending album
            pendingAlbum = album.category

            // Show ad gate (optional - respects interval)
            AdGateHelper.showOptionalAdGate(adGateLauncher, context as ComponentActivity)
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
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Image
            Image(
                painter = painterResource(id = album.imageRes),
                contentDescription = album.category.displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Overlay gradient for better text visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Category Title (Center)
                Text(
                    text = album.category.displayName,
                    modifier = Modifier.align(Alignment.BottomStart),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                )
                // Progress Indicator (Bottom Left)
                if (album.totalCount > 0) {
                    // Add AD badge at bottom right
                    AdBadge(
                        progressText = album.progressText,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlbumsContentPreview() {
    val sampleAlbums = listOf(
        Album(
            category = AlbumCategory.ROLE_PLAY,
            imageRes = R.drawable.ic_menu_gallery,
            totalCount = 100,
            id = "album_1",
            currentCount = 25
        ),
        Album(
            category = AlbumCategory.FULL,
            imageRes = R.drawable.ic_menu_camera,
            totalCount = 80,
            id = "album_2",
            currentCount = 40
        ),
        Album(
            category = AlbumCategory.NORMAL,
            imageRes = R.drawable.ic_menu_gallery,
            totalCount = 50,
            id = "album_3",
            currentCount = 10
        )
    )
    AlbumsContent(
        albums = sampleAlbums,
        isLoading = false,
        onAlbumClick = {}
    )
}

@Preview
@Composable
fun AlbumCardPreview() {
    val sampleAlbum = Album(
        category = AlbumCategory.ROLE_PLAY,
        imageRes = R.drawable.ic_menu_gallery,
        totalCount = 100,
        id = "sample_album_1",
        currentCount = 1
    )
    AlbumCard(album = sampleAlbum, onClick = {})
}