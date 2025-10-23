package com.truonganim.admob.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.truonganim.admob.billing.PremiumHelper
import com.truonganim.admob.billing.PremiumPreferencesManager
import com.truonganim.admob.firebase.RemoteConfigHelper

/**
 * Settings Screen
 */
@Composable
fun SettingsScreen(
    onLanguageClick: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Observe premium status
    val premiumPrefs = remember { PremiumPreferencesManager.getInstance(context) }
    val isPremium by premiumPrefs.isPremium.collectAsState()

    val remoteConfig = RemoteConfigHelper.getInstance()
    val privacyPolicyUrl = remoteConfig.getString(com.truonganim.admob.firebase.RemoteConfigKeys.PRIVACY_POLICY_URL)
    val feedbackEmail = remoteConfig.getString(com.truonganim.admob.firebase.RemoteConfigKeys.FEEDBACK_EMAIL)
    val feedbackFormUrl = remoteConfig.getString(com.truonganim.admob.firebase.RemoteConfigKeys.FEEDBACK_FORM_URL)
    val storeUrl = remoteConfig.getString(com.truonganim.admob.firebase.RemoteConfigKeys.STORE_URL)

    SettingsContent(
        uiState = uiState,
        isPremium = isPremium,
        onLanguageClick = onLanguageClick,
        onPremiumClick = {
            PremiumHelper.showPremiumScreen(context)
        },
        onFeedbackClick = {
            // Try to open email app
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(feedbackEmail))
                putExtra(Intent.EXTRA_SUBJECT, "Feedback for App")
            }

            // Check if email app is available
            if (emailIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(emailIntent)
            } else {
                // Fallback to Google Form in browser
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(feedbackFormUrl))
                context.startActivity(browserIntent)
            }
        },
        onShareAppClick = {
            // Use share intent instead of opening browser
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Check out this app!")
                putExtra(Intent.EXTRA_TEXT, "Download this amazing app: $storeUrl")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share app via"))
        },
        onPrivacyPolicyClick = {
            // Open privacy policy URL
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
            context.startActivity(intent)
        }
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    isPremium: Boolean,
    onLanguageClick: () -> Unit,
    onPremiumClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onShareAppClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Premium Banner / VIP Badge
        if (isPremium) {
            VipBadge()
        } else {
            PremiumBanner(onClick = onPremiumClick)
        }

        HorizontalDivider()

        // Language
        SettingClickableItem(
            icon = Icons.Default.Language,
            title = "Language",
            onClick = onLanguageClick
        )

        HorizontalDivider()

        // Feedback
        SettingClickableItem(
            icon = Icons.Default.Feedback,
            title = "Feedback",
            onClick = onFeedbackClick
        )

        HorizontalDivider()

        // Share app
        SettingClickableItem(
            icon = Icons.Default.Share,
            title = "Share app",
            onClick = onShareAppClick
        )

        HorizontalDivider()

        // Privacy policy
        SettingClickableItem(
            icon = Icons.Default.PrivacyTip,
            title = "Privacy policy",
            onClick = onPrivacyPolicyClick
        )

        Spacer(modifier = Modifier.weight(1f))

        // Version Info
        Text(
            text = "Version ${uiState.version}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun SettingClickableItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Premium Banner for non-premium users
 * Matches the design mockup with gradient background
 */
@Composable
private fun PremiumBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFC1CC), // Light pink
                        Color(0xFFFFB3C1)  // Slightly darker pink
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Crown icon
        Text(
            text = "👑",
            fontSize = 32.sp,
            modifier = Modifier.padding(end = 12.dp)
        )

        // Text content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Premium Plans",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B4513) // Brown color
            )
            Text(
                text = "Unlock all exclusive premium features!",
                fontSize = 12.sp,
                color = Color(0xFF8B4513).copy(alpha = 0.8f)
            )
        }

        // Upgrade button
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF1744)
            ),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(
                text = "Upgrade",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * VIP Badge for premium users
 * Shows premium status with elegant design
 */
@Composable
private fun VipBadge() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFD700), // Gold
                        Color(0xFFFFA500)  // Orange-gold
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Crown icon
        Text(
            text = "👑",
            fontSize = 32.sp,
            modifier = Modifier.padding(end = 12.dp)
        )

        // Text content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Premium Member",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Enjoying all premium features",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        // VIP badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = "VIP",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

