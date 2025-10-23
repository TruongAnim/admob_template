package com.truonganim.admob.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.truonganim.admob.R
import com.truonganim.admob.ads.LoadingStatus
import com.truonganim.admob.ads.RewardAdLoadingState
import com.truonganim.admob.utils.findActivity

/**
 * Reward Ad Loading Overlay
 * Full-screen overlay that shows loading state for reward ads
 */
@Composable
fun RewardAdLoadingOverlay(
    loadingState: RewardAdLoadingState?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = loadingState != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        loadingState?.let { state ->
            Dialog(
                onDismissRequest = {
                    // Only allow dismiss if not showing ad
                    if (state.status != LoadingStatus.SHOWING) {
                        onDismiss()
                    }
                },
                properties = DialogProperties(
                    dismissOnBackPress = state.status != LoadingStatus.SHOWING,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .wrapContentHeight(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.place.placeName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                // Close button (only if not showing)
                                if (state.status != LoadingStatus.SHOWING) {
                                    IconButton(
                                        onClick = onDismiss,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.cd_close),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            
                            Divider()
                            
                            // Content based on status
                            when (state.status) {
                                LoadingStatus.LOADING -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                LoadingStatus.READY -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                LoadingStatus.SHOWING -> {
                                    Text(
                                        text = stringResource(R.string.showing_ad),
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Text(
                                        text = stringResource(R.string.watch_ad_to_earn),
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Auto show ad when showing state
                                    val context = LocalContext.current
                                    LaunchedEffect(Unit) {
                                        state.onShow?.invoke(context.findActivity())
                                    }
                                }

                                LoadingStatus.ERROR -> {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(48.dp)
                                    )

                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.error
                                    )

                                    Button(
                                        onClick = onDismiss,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(stringResource(R.string.close))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

