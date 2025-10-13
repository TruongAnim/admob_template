package com.truonganim.admob.ui.photoviewer

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter

/**
 * Photo Viewer Screen - Full screen photo viewer with swipe
 */
@Composable
fun PhotoViewerScreen(
    characterId: Int,
    initialPhotoIndex: Int,
    onBackClick: () -> Unit,
    viewModel: PhotoViewerViewModel = viewModel(
        factory = PhotoViewerViewModelFactory(characterId, initialPhotoIndex)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Permission launcher for storage (Android 9 and below)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onSaveClick(context) {}
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Photo Pager
        if (uiState.photos.isNotEmpty()) {
            PhotoPager(
                photos = uiState.photos,
                initialPage = uiState.currentPhotoIndex,
                onPageChanged = viewModel::onPhotoIndexChanged
            )
        }
        
        // Top Bar with Back Button
        PhotoViewerTopBar(
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart)
        )
        
        // Bottom Action Buttons
        PhotoViewerBottomBar(
            onSaveClick = {
                viewModel.onSaveClick(context) {
                    // Request permission if needed
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            },
            onMiddleButtonClick = {
                // TODO: Implement middle button action
            },
            onShareClick = {
                viewModel.onShareClick(context)
            },
            isSaving = uiState.isSaving,
            isSharing = uiState.isSharing,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoPager(
    photos: List<String>,
    initialPage: Int,
    onPageChanged: (Int) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { photos.size }
    )
    
    // Track page changes
    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }
    
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(photos[page]),
                contentDescription = "Photo ${page + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun PhotoViewerTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun PhotoViewerBottomBar(
    onSaveClick: () -> Unit,
    onMiddleButtonClick: () -> Unit,
    onShareClick: () -> Unit,
    isSaving: Boolean,
    isSharing: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Save Button
        ActionButton(
            onClick = onSaveClick,
            icon = Icons.Default.Download,
            contentDescription = "Save",
            isLoading = isSaving
        )
        
        Spacer(modifier = Modifier.width(24.dp))
        
        // Middle Button (TODO)
        ActionButton(
            onClick = onMiddleButtonClick,
            icon = Icons.Default.Download, // Placeholder icon
            contentDescription = "Action",
            isLoading = false
        )
        
        Spacer(modifier = Modifier.width(24.dp))
        
        // Share Button
        ActionButton(
            onClick = onShareClick,
            icon = Icons.Default.Share,
            contentDescription = "Share",
            isLoading = isSharing
        )
    }
}

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isLoading: Boolean
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFFFF9800), // Orange color
        contentColor = Color.White,
        modifier = Modifier.size(56.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

