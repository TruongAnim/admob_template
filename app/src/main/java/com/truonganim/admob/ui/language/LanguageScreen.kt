package com.truonganim.admob.ui.language

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.truonganim.admob.R
import com.truonganim.admob.ads.native_ads.NativeAdPosition
import com.truonganim.admob.ads.native_ads.NativeAdView
import com.truonganim.admob.ui.theme.AdMobBaseTheme

/**
 * Language Selection Screen
 * Displays a grid of supported languages for user to select
 */
@Composable
fun LanguageScreen(
    viewModel: LanguageViewModel = viewModel(),
    showNativeAd: Boolean = true,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onLanguageConfirmed: () -> Unit = {}
) {
    val languages by viewModel.languages.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isFirstSelection by viewModel.isFirstSelection.collectAsState()

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
        // Top Bar with Title and Apply Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button (only visible when showBackButton is true)
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Title
            Text(
                text = stringResource(R.string.select_language),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            ConfirmLanguageButton(
                enabled = selectedLanguage != null,
                onClick = {
                    if (selectedLanguage != null) {
                        viewModel.confirmLanguage()
                        onLanguageConfirmed()
                    }
                },
                modifier = Modifier.padding(end = 8.dp) // canh lề phải đẹp hơn
            )
        }

        // Language List
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(languages) { language ->
                LanguageItem(
                    language = language,
                    isSelected = selectedLanguage?.code == language.code,
                    onClick = {
                        viewModel.selectLanguage(language)
                    }
                )
            }
        }

            // Native Ad at the bottom (only show if showNativeAd is true)
            if (showNativeAd) {
                NativeAdView(
                    position = if (isFirstSelection) {
                        NativeAdPosition.LANGUAGE_SCREEN
                    } else {
                        NativeAdPosition.LANGUAGE_SCREEN_2
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Loading dialog
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                .clickable(                               // ⬅️ chặn tap rơi xuống dưới
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* no-op */ },
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 64.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmLanguageButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (enabled) MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    else MaterialTheme.colorScheme.outlineVariant
    val iconTint = if (enabled) MaterialTheme.colorScheme.error  // đỏ như ảnh
    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = bg,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (enabled) 2.dp else 0.dp
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(32.dp),
            content = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.select_language),
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun LanguageScreenPreview() {
    AdMobBaseTheme {
        LanguageScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LanguageScreenDarkPreview() {
    AdMobBaseTheme {
        LanguageScreen()
    }
}

