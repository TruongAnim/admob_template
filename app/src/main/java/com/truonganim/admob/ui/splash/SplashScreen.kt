package com.truonganim.admob.ui.splash

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.truonganim.admob.R
import com.truonganim.admob.ui.theme.AdMobBaseTheme

/**
 * Splash Screen Composable
 * Displays background image, dot loading animation, and ad notice
 */
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = viewModel(),
    onAdReadyToShow: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val loadingProgress by viewModel.loadingProgress.collectAsState()
    val shouldShowAd by viewModel.shouldShowAd.collectAsState()

    // Start loading when screen is first composed
    LaunchedEffect(Unit) {
        activity?.let {
            viewModel.startLoading(it)
        }
    }

    // Trigger ad show when loading is complete
    LaunchedEffect(shouldShowAd) {
        if (shouldShowAd) {
            onAdReadyToShow()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.splash_bg2),
            contentDescription = stringResource(R.string.cd_splash_background),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Dot loading animation
            DotLoadingAnimation(
                dotSize = 8.dp,
                dotColor = Color.White,
                dotSpacing = 6.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ad notice text
            Text(
                text = stringResource(R.string.splash_ad_notice),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    AdMobBaseTheme {
        SplashScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SplashScreenDarkPreview() {
    AdMobBaseTheme {
        SplashScreen()
    }
}

