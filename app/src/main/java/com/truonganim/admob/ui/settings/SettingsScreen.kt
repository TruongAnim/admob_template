package com.truonganim.admob.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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

    val remoteConfig = RemoteConfigHelper.getInstance()
    val privacyPolicyUrl = remoteConfig.getString(com.truonganim.admob.firebase.RemoteConfigKeys.PRIVACY_POLICY_URL)
    val feedbackEmail = remoteConfig.getString(com.truonganim.admob.firebase.RemoteConfigKeys.FEEDBACK_EMAIL)
    val feedbackFormUrl = remoteConfig.getString(com.truonganim.admob.firebase.RemoteConfigKeys.FEEDBACK_FORM_URL)
    val storeUrl = remoteConfig.getString(com.truonganim.admob.firebase.RemoteConfigKeys.STORE_URL)

    SettingsContent(
        uiState = uiState,
        onLanguageClick = onLanguageClick,
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
    onLanguageClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onShareAppClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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

