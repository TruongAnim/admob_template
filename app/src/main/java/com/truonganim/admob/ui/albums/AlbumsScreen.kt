package com.truonganim.admob.ui.albums

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.truonganim.admob.ads.AdGateHelper
import com.truonganim.admob.data.Album
import com.truonganim.admob.datastore.PreferencesManager
import com.truonganim.admob.ui.components.NotificationBanner
import com.truonganim.admob.ui.components.NotificationPermissionBottomSheet
import com.truonganim.admob.utils.NotificationPermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val preferencesManager = remember { PreferencesManager.getInstance(context) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
        CoroutineScope(Dispatchers.IO).launch {
            preferencesManager.setNotificationPermissionRequested(true)
        }
    }

    // Check permission on first launch
    LaunchedEffect(Unit) {
        val permissionRequested = preferencesManager.isNotificationPermissionRequested()
        if (!permissionRequested) {
            // First time, request permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Check current permission state
            viewModel.checkNotificationPermission()
        }
    }

    // Check permission on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkNotificationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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

    // Show notification permission dialog
    if (uiState.showNotificationDialog) {
        NotificationPermissionBottomSheet(
            onDismiss = {
                viewModel.dismissNotificationDialog()
            },
            onAllowClick = {
                viewModel.dismissNotificationDialog()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }else{
                    NotificationPermissionHelper.openNotificationSettings(context)
                }
            }
        )
    }

    AlbumsContent(
        albums = uiState.albums,
        isLoading = uiState.isLoading,
        showNotificationBanner = uiState.showNotificationBanner,
        onNotificationBannerClick = {
            NotificationPermissionHelper.openNotificationSettings(context)
        },
        onAlbumClick = { album ->
            // Store pending album
            pendingAlbumId = album.albumId

            // Show ad gate (optional - respects interval)
            AdGateHelper.showOptionalAdGate(adGateLauncher, context as ComponentActivity)
        }
    )
}

@Composable
private fun AlbumsContent(
    albums: List<Album>,
    isLoading: Boolean,
    showNotificationBanner: Boolean,
    onNotificationBannerClick: () -> Unit,
    onAlbumClick: (Album) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Notification banner
                if (showNotificationBanner) {
                    item {
                        NotificationBanner(
                            onEnableClick = onNotificationBannerClick
                        )
                    }
                }

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
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val cardHeight = screenHeight * 0.2f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(0.dp),
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
                    .height(60.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.0f),
                                Color.Black.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Bóng (shadow giả) – cùng font size, dịch nhẹ
                OutlinedText(
                    text = album.name.uppercase(),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 24.dp)
                )

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

@Composable
fun OutlinedText(
    text: String,
    modifier: Modifier = Modifier,
    fillColor: Color = Color.White,
    outlineColor: Color = Color.Black,
    outlineWidth: Dp = 2.dp,
    style: TextStyle = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    )
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val r = with(density) { outlineWidth.toPx() }

    Canvas(modifier) {
        val layout = measurer.measure(AnnotatedString(text), style = style)
        val offsets = listOf(
            Offset(-r, 0f), Offset(r, 0f),
            Offset(0f, -r), Offset(0f, r),
            Offset(-r, -r), Offset(-r, r),
            Offset(r, -r), Offset(r, r)
        )
        // viền
        offsets.forEach { o ->
            drawText(textLayoutResult = layout, color = outlineColor, topLeft = o)
        }
        // chữ chính
        drawText(textLayoutResult = layout, color = fillColor)
    }
}

@Composable
fun AdBadge(
    progressText: String,
    modifier: Modifier = Modifier,
    pillColor: Color = Color(0xFFFF6B6B),
    adTextColor: Color = Color.White
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = pillColor,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(com.truonganim.admob.R.drawable.ic_ads),
                contentDescription = null,
                tint = adTextColor,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = progressText,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



@Preview
@Composable
fun AdBadgePreview() {
    AdBadge(progressText = "3/10")
}