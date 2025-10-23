package com.truonganim.admob.ui.premium

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.truonganim.admob.R
import com.truonganim.admob.billing.SubscriptionPlan

/**
 * Premium Screen
 * Displays subscription plans and features
 */
@Composable
fun PremiumScreen(
    viewModel: PremiumViewModel,
    onClose: () -> Unit,
    onPurchaseSuccess: () -> Unit,
    onContinueClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPlan by viewModel.selectedPlan.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // Background image with gradient overlay
        BackgroundWithGradient()

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Close button
            CloseButton(onClick = onClose)

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Crown icon
                CrownIcon()

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = stringResource(R.string.premium_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = stringResource(R.string.premium_subtitle),
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Features list
                when (uiState) {
                    is PremiumUiState.Success -> {
                        FeaturesList()

                        Spacer(modifier = Modifier.height(32.dp))

                        // Subscription plans
                        val plans = (uiState as PremiumUiState.Success).plans
                        SubscriptionPlans(
                            plans = plans,
                            selectedPlan = selectedPlan,
                            onPlanSelected = { viewModel.selectPlan(it) }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Continue button
                        ContinueButton(
                            onClick = onContinueClick,
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Footer links
                        FooterLinks()
                    }
                    is PremiumUiState.Loading -> {
                        CircularProgressIndicator(color = Color.White)
                    }
                    is PremiumUiState.Error -> {
                        Text(
                            text = (uiState as PremiumUiState.Error).message,
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Loading overlay
        LoadingOverlay(
            isVisible = isLoading,
            message = "Processing purchase..."
        )
    }
}

@Composable
private fun BackgroundWithGradient() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image (placeholder)
        AsyncImage(
            model = "https://images.unsplash.com/photo-1524502397800-2eeaad7c3fe5?w=800",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun CrownIcon() {
    Text(
        text = stringResource(R.string.crown_emoji),
        fontSize = 64.sp
    )
}

@Composable
private fun FeaturesList() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FeatureItem("Unlock All Categories & Albums")
        FeatureItem("Unlock Meet & Profiles")
        FeatureItem("Unlock AI Girl Call & Chat")
        FeatureItem("No Ads Experience")
    }
}

@Composable
private fun FeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFFFF1744),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SubscriptionPlans(
    plans: List<SubscriptionPlan>,
    selectedPlan: SubscriptionPlan?,
    onPlanSelected: (SubscriptionPlan) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        plans.forEach { plan ->
            PlanCard(
                plan = plan,
                isSelected = plan.id == selectedPlan?.id,
                onClick = { onPlanSelected(plan) }
            )
        }
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFFFF1744) else Color.White.copy(alpha = 0.3f)
    val backgroundColor = if (isSelected) Color(0xFFFF1744).copy(alpha = 0.1f) else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = plan.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                if (plan.discount != null) {
                    Text(
                        text = plan.discount,
                        fontSize = 12.sp,
                        color = Color(0xFFFF1744),
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.premium_price_format, plan.price, plan.period),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ContinueButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFF1744),
            disabledContainerColor = Color.Gray
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(
            text = stringResource(R.string.continue_button),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun FooterLinks() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.cancel_anytime),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.terms_of_use),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.clickable { /* Handle click */ }
        )
        Text(
            text = stringResource(R.string.separator),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = stringResource(R.string.privacy_policy),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.clickable { /* Handle click */ }
        )
    }
}

